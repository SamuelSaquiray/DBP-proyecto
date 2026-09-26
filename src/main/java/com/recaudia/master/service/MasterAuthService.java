package com.recaudia.master.service;

import com.recaudia.domain.UsuarioMaestro;
import com.recaudia.master.dto.MasterAuthResponse;
import com.recaudia.master.dto.MasterLoginRequest;
import com.recaudia.master.dto.MasterUserResponse;
import com.recaudia.master.repository.UsuarioMaestroRepository;
import com.recaudia.security.JwtService;
import com.recaudia.exception.DuplicateResourceException;
import com.recaudia.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MasterAuthService {

    private final UsuarioMaestroRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public MasterAuthResponse login(MasterLoginRequest request) {
        UsuarioMaestro usuario = repository.findByEmail(request.getEmail())
                .filter(UsuarioMaestro::getActivo)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String token = jwtService.generateMasterToken(usuario.getEmail());

        return MasterAuthResponse.builder()
                .token(token)
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .tipo("MASTER")
                .build();
    }

    public MasterUserResponse create(String nombre, String email, String password) {
        if (repository.existsByEmail(email)) {
            throw new DuplicateResourceException("El email ya está registrado como usuario maestro");
        }

        UsuarioMaestro usuario = repository.save(UsuarioMaestro.builder()
                .nombre(nombre)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .activo(true)
                .build());

        return MasterUserResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .activo(usuario.getActivo())
                .createdAt(usuario.getCreatedAt())
                .build();
    }
}
