package com.recaudia.service;

import com.recaudia.domain.Empresa;
import com.recaudia.dto.EmpresaRequest;
import com.recaudia.dto.EmpresaResponse;
import com.recaudia.security.TenantContext;
import com.recaudia.repository.EmpresaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Transactional
    public EmpresaResponse crear(EmpresaRequest request) {
        Long empresaId = TenantContext.requireTenantId();

        if (empresaRepository.existsByRuc(request.getRuc())) {
            throw new IllegalArgumentException("Ya existe una empresa con ese RUC");
        }

        Empresa empresa = Empresa.builder()
                .id(empresaId)
                .razonSocial(request.getRazonSocial())
                .ruc(request.getRuc())
                .dominio(request.getDominio())
                .activa(true)
                .build();

        empresa = empresaRepository.save(empresa);
        return toResponse(empresa);
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtenerPorId(Long id) {
        Long empresaId = TenantContext.requireTenantId();
        if (!empresaId.equals(id)) {
            throw new SecurityException("No puedes acceder a otra empresa");
        }

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        return toResponse(empresa);
    }

    private EmpresaResponse toResponse(Empresa e) {
        return EmpresaResponse.builder()
                .id(e.getId())
                .razonSocial(e.getRazonSocial())
                .ruc(e.getRuc())
                .dominio(e.getDominio())
                .activa(e.getActiva())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
