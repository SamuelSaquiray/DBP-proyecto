package com.recaudia.service;

import com.recaudia.domain.EstadoFactura;
import com.recaudia.domain.Factura;
import com.recaudia.domain.Pago;
import com.recaudia.dto.PagoRequest;
import com.recaudia.dto.PagoResponse;
import com.recaudia.repository.FacturaRepository;
import com.recaudia.event.PagoRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.recaudia.repository.PagoRepository;
import com.recaudia.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    @Transactional
    public PagoResponse registrar(PagoRequest request) {
        Long empresaId = TenantContext.getTenantId();
        Factura factura = facturaRepository.findById(request.getFacturaId())
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        if (!factura.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("La factura no pertenece a la empresa");
        }

        Pago pago = Pago.builder()
                .monto(request.getMonto())
                .fechaPago(request.getFechaPago())
                .medioPago(request.getMedioPago())
                .referencia(request.getReferencia())
                .factura(factura)
                .empresa(factura.getEmpresa())
                .build();

        pago = pagoRepository.save(pago);

        BigDecimal nuevoMontoPagado = factura.getMontoPagado().add(request.getMonto());
        factura.setMontoPagado(nuevoMontoPagado);

        if (nuevoMontoPagado.compareTo(factura.getMontoTotal()) >= 0) {
            factura.setEstado(EstadoFactura.PAGADA);
        } else {
            factura.setEstado(EstadoFactura.PARCIAL);
        }

        facturaRepository.save(factura);
        eventPublisher.publishEvent(new PagoRegisteredEvent(pago.getId(), factura.getId(), empresaId, pago.getMonto()));
        auditService.log("CREAR", "PAGO", pago.getId(), "Pago de " + pago.getMonto() + " para factura " + factura.getNumeroFactura());

        return toResponse(pago);
    }

    private PagoResponse toResponse(Pago p) {
        return PagoResponse.builder()
                .id(p.getId())
                .monto(p.getMonto())
                .fechaPago(p.getFechaPago())
                .medioPago(p.getMedioPago())
                .referencia(p.getReferencia())
                .facturaId(p.getFactura().getId())
                .numeroFactura(p.getFactura().getNumeroFactura())
                .build();
    }
}
