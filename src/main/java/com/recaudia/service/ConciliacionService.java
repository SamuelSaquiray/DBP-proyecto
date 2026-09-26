package com.recaudia.service;

import com.recaudia.domain.EstadoFactura;
import com.recaudia.domain.Factura;
import com.recaudia.repository.FacturaRepository;
import com.recaudia.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service @RequiredArgsConstructor @Slf4j
public class ConciliacionService {
    private final FacturaRepository facturaRepository;
    private final AuditService auditService;

    @Async("taskExecutor") @Transactional
    public CompletableFuture<Resultado> conciliar(Long empresaId) {
        TenantContext.setTenantId(empresaId);
        try {
            long procesadas=0, actualizadas=0, inconsistencias=0;
            for (Factura f : facturaRepository.findByEmpresaId(empresaId)) {
                procesadas++;
                BigDecimal suma = f.getPagos().stream().map(p -> p.getMonto()).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (suma.compareTo(f.getMontoPagado()) != 0) { f.setMontoPagado(suma); actualizadas++; inconsistencias++; }
                EstadoFactura nuevo = suma.compareTo(f.getMontoTotal()) >= 0 ? EstadoFactura.PAGADA :
                        (suma.signum()>0 ? EstadoFactura.PARCIAL : (f.getFechaVencimiento()!=null && f.getFechaVencimiento().isBefore(java.time.LocalDate.now()) ? EstadoFactura.VENCIDA : EstadoFactura.EMITIDA));
                if (f.getEstado()!=EstadoFactura.ANULADA && f.getEstado()!=nuevo) { f.setEstado(nuevo); actualizadas++; }
                facturaRepository.save(f);
            }
            auditService.log("CONCILIACION", "FACTURA", null, "Procesadas="+procesadas+", actualizadas="+actualizadas+", inconsistencias="+inconsistencias);
            return CompletableFuture.completedFuture(new Resultado(procesadas,actualizadas,inconsistencias));
        } finally { TenantContext.clear(); }
    }
    public record Resultado(long facturasProcesadas,long facturasActualizadas,long inconsistencias) {}
}
