package com.recaudia.service;

import com.recaudia.domain.Alerta;
import com.recaudia.domain.EstadoAlerta;
import com.recaudia.dto.AlertaResponse;
import com.recaudia.dto.AlertaUpdateRequest;
import com.recaudia.repository.AlertaRepository;
import com.recaudia.event.AlertaStatusChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.recaudia.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<AlertaResponse> listar() {
        Long empresaId = TenantContext.getTenantId();
        return alertaRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listarPendientes() {
        Long empresaId = TenantContext.getTenantId();
        return alertaRepository.findByEmpresaIdAndEstado(empresaId, EstadoAlerta.PENDIENTE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlertaResponse actualizarEstado(Long alertaId, AlertaUpdateRequest request) {
        Long empresaId = TenantContext.getTenantId();
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new EntityNotFoundException("Alerta no encontrada"));

        if (!alerta.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("La alerta no pertenece a la empresa");
        }

        alerta.setEstado(request.getEstado());
        if (org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null
                && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof com.recaudia.domain.Usuario usuario) {
            alerta.getRevisores().add(usuario);
        }

        if (request.getEstado() == EstadoAlerta.RESUELTA || request.getEstado() == EstadoAlerta.RECHAZADA) {
            alerta.setFechaResolucion(LocalDateTime.now());
        }

        alerta = alertaRepository.save(alerta);
        eventPublisher.publishEvent(new AlertaStatusChangedEvent(alerta.getId(), empresaId, alerta.getEstado()));
        auditService.log("ACTUALIZAR_ESTADO", "ALERTA", alerta.getId(), "Nuevo estado: " + alerta.getEstado());
        return toResponse(alerta);
    }

    private AlertaResponse toResponse(Alerta a) {
        return AlertaResponse.builder()
                .id(a.getId())
                .titulo(a.getTitulo())
                .descripcion(a.getDescripcion())
                .tipo(a.getTipo())
                .estado(a.getEstado())
                .facturaId(a.getFactura() != null ? a.getFactura().getId() : null)
                .numeroFactura(a.getFactura() != null ? a.getFactura().getNumeroFactura() : null)
                .fechaCreacion(a.getFechaCreacion())
                .fechaResolucion(a.getFechaResolucion())
                .build();
    }
}
