package com.recaudia.service;

import com.recaudia.domain.Factura;
import com.recaudia.domain.NotaDeCredito;
import com.recaudia.dto.NotaDeCreditoRequest;
import com.recaudia.dto.NotaDeCreditoResponse;
import com.recaudia.repository.FacturaRepository;
import com.recaudia.repository.NotaDeCreditoRepository;
import com.recaudia.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotaDeCreditoService {

    private final NotaDeCreditoRepository notaDeCreditoRepository;
    private final FacturaRepository facturaRepository;

    @Transactional
    public NotaDeCreditoResponse crear(NotaDeCreditoRequest request) {
        Long empresaId = TenantContext.getTenantId();

        Factura factura = facturaRepository.findById(request.getFacturaId())
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        if (!factura.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("La factura no pertenece a la empresa");
        }

        NotaDeCredito nota = NotaDeCredito.builder()
                .numero(request.getNumero())
                .monto(request.getMonto())
                .motivo(request.getMotivo())
                .fecha(request.getFecha())
                .factura(factura)
                .empresa(factura.getEmpresa())
                .build();

        nota = notaDeCreditoRepository.save(nota);
        return toResponse(nota);
    }

    private NotaDeCreditoResponse toResponse(NotaDeCredito n) {
        return NotaDeCreditoResponse.builder()
                .id(n.getId())
                .numero(n.getNumero())
                .monto(n.getMonto())
                .motivo(n.getMotivo())
                .fecha(n.getFecha())
                .facturaId(n.getFactura().getId())
                .numeroFactura(n.getFactura().getNumeroFactura())
                .build();
    }
}
