package com.recaudia.service;

import com.recaudia.domain.Empresa;
import com.recaudia.domain.Usuario;
import com.recaudia.dto.UsuarioRequest;
import com.recaudia.dto.UsuarioResponse;
import com.recaudia.repository.EmpresaRepository;
import com.recaudia.repository.UsuarioRepository;
import com.recaudia.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        Long empresaId = TenantContext.getTenantId();
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .activo(true)
                .empresa(empresa)
                .build();

        usuario = usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        Long empresaId = TenantContext.getTenantId();
        return usuarioRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void desactivar(Long usuarioId) {
        Long empresaId = TenantContext.getTenantId();
        Usuario u = usuarioRepository.findById(usuarioId).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (!u.getEmpresa().getId().equals(empresaId)) throw new EntityNotFoundException("Usuario no pertenece a la empresa");
        u.setActivo(false); usuarioRepository.save(u);
    }

    @Transactional
    public UsuarioResponse actualizar(Long usuarioId, UsuarioRequest request) {
        Long empresaId=TenantContext.getTenantId();
        Usuario u=usuarioRepository.findById(usuarioId).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if(!u.getEmpresa().getId().equals(empresaId)) throw new EntityNotFoundException("Usuario no pertenece a la empresa");
        if(!u.getEmail().equals(request.getEmail()) && usuarioRepository.existsByEmail(request.getEmail())) throw new IllegalArgumentException("El email ya está registrado");
        u.setNombre(request.getNombre()); u.setEmail(request.getEmail()); u.setRol(request.getRol());
        if(request.getPassword()!=null && !request.getPassword().isBlank()) u.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return toResponse(usuarioRepository.save(u));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .activo(u.getActivo())
                .empresaId(u.getEmpresa().getId())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
