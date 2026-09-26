package com.recaudia.controller;

import com.recaudia.domain.Usuario;
import com.recaudia.dto.AuthResponse;
import com.recaudia.dto.LoginRequest;
import com.recaudia.dto.RefreshTokenRequest;
import com.recaudia.repository.UsuarioRepository;
import com.recaudia.security.JwtService;
import com.recaudia.security.TenantContext;
import com.recaudia.config.DatabaseTenantGuard;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final DatabaseTenantGuard tenantGuard;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        tenantGuard.select(request.getEmpresaId());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow();

            // Defensa adicional: el usuario autenticado debe pertenecer
            // al mismo tenant que fue seleccionado para la conexión.
            if (!request.getEmpresaId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(401).build();
            }

            String token = jwtService.generateToken(
                    usuario.getEmail(),
                    usuario.getId(),
                    request.getEmpresaId(),
                    usuario.getRol().name()
            );
            String refreshToken = jwtService.generateRefreshToken(
                    usuario.getEmail(), usuario.getId(), request.getEmpresaId(), usuario.getRol().name());

            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .usuarioId(usuario.getId())
                    .nombre(usuario.getNombre())
                    .email(usuario.getEmail())
                    .rol(usuario.getRol().name())
                    .empresaId(request.getEmpresaId())
                    .build();

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).build();
        } finally {
            TenantContext.clear();
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String email = jwtService.extractUsername(request.getRefreshToken());
            Long empresaId = jwtService.extractEmpresaId(request.getRefreshToken());
            if (!jwtService.isRefreshTokenValid(request.getRefreshToken(), email) || empresaId == null) {
                return ResponseEntity.status(401).build();
            }
            tenantGuard.select(empresaId);
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())
                    || usuario.getEmpresa() == null || !empresaId.equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(401).build();
            }
            String token = jwtService.generateToken(email, usuario.getId(), empresaId, usuario.getRol().name());
            String refreshToken = jwtService.generateRefreshToken(email, usuario.getId(), empresaId, usuario.getRol().name());
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token).refreshToken(refreshToken)
                    .usuarioId(usuario.getId()).nombre(usuario.getNombre())
                    .email(usuario.getEmail()).rol(usuario.getRol().name()).empresaId(empresaId).build());
        } finally {
            TenantContext.clear();
        }
    }

}
