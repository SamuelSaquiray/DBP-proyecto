package com.recaudia.service;

import com.recaudia.domain.*;
import com.recaudia.dto.FacturaRequest;
import com.recaudia.dto.FacturaResponse;
import com.recaudia.repository.CuentaRepository;
import com.recaudia.event.FacturaCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.recaudia.repository.FacturaRepository;
import com.recaudia.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final CuentaRepository cuentaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    @Transactional
    public FacturaResponse crear(FacturaRequest request) {
        Long empresaId = TenantContext.getTenantId();
        if (facturaRepository.existsByEmpresaIdAndNumeroFactura(empresaId, request.getNumeroFactura())) throw new IllegalArgumentException("Ya existe una factura con ese número");
        Cuenta cuenta = cuentaRepository.findById(request.getCuentaId())
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));

        if (!cuenta.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("La cuenta no pertenece a la empresa");
        }

        Factura factura = Factura.builder()
                .numeroFactura(request.getNumeroFactura())
                .fechaEmision(request.getFechaEmision())
                .fechaVencimiento(request.getFechaVencimiento())
                .montoTotal(request.getMontoTotal())
                .montoPagado(BigDecimal.ZERO)
                .estado(EstadoFactura.EMITIDA)
                .cuenta(cuenta)
                .empresa(cuenta.getEmpresa())
                .build();

        factura = facturaRepository.save(factura);
        eventPublisher.publishEvent(new FacturaCreatedEvent(factura.getId(), empresaId, factura.getNumeroFactura(), cuenta.getEmailContacto()));
        auditService.log("CREAR", "FACTURA", factura.getId(), "Factura " + factura.getNumeroFactura() + " creada");
        return toResponse(factura);
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listar() {
        Long empresaId = TenantContext.getTenantId();
        return facturaRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FacturaResponse actualizarEstado(Long facturaId, com.recaudia.domain.EstadoFactura estado) {
        Long empresaId=TenantContext.getTenantId(); Factura f=facturaRepository.findById(facturaId).orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
        if(!f.getEmpresa().getId().equals(empresaId)) throw new EntityNotFoundException("Factura no pertenece a la empresa");
        f.setEstado(estado); f=facturaRepository.save(f); auditService.log("ACTUALIZAR_ESTADO", "FACTURA", f.getId(), "Nuevo estado: "+estado); return toResponse(f);
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listarPorCuenta(Long cuentaId) {
        Long empresaId = TenantContext.getTenantId();
        return facturaRepository.findByCuentaId(cuentaId)
                .stream()
                .filter(f -> f.getEmpresa().getId().equals(empresaId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private FacturaResponse toResponse(Factura f) {
        return FacturaResponse.builder()
                .id(f.getId())
                .numeroFactura(f.getNumeroFactura())
                .fechaEmision(f.getFechaEmision())
                .fechaVencimiento(f.getFechaVencimiento())
                .montoTotal(f.getMontoTotal())
                .montoPagado(f.getMontoPagado())
                .estado(f.getEstado())
                .cuentaId(f.getCuenta().getId())
                .cuentaRazonSocial(f.getCuenta().getRazonSocial())
                .empresaId(f.getEmpresa().getId())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
