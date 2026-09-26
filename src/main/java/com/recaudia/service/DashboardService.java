package com.recaudia.service;

import com.recaudia.domain.EstadoAlerta;
import com.recaudia.repository.AlertaRepository;
import com.recaudia.repository.FacturaRepository;
import com.recaudia.repository.PagoRepository;
import com.recaudia.repository.CuentaRepository;
import com.recaudia.domain.EstadoCuenta;
import com.recaudia.domain.EstadoFactura;
import java.time.LocalDate;
import com.recaudia.security.TenantContext;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final AlertaRepository alertaRepository;
    private final CuentaRepository cuentaRepository;

    @Transactional(readOnly = true)
    public DashboardResponse obtenerResumen() {
        Long empresaId = TenantContext.getTenantId();

        BigDecimal totalFacturado = facturaRepository.sumMontoTotalByEmpresaId(empresaId);
        BigDecimal totalCobrado = facturaRepository.sumMontoPagadoByEmpresaId(empresaId);
        BigDecimal porcentajeCobrado = totalFacturado.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalCobrado.multiply(BigDecimal.valueOf(100))
                .divide(totalFacturado, 2, RoundingMode.HALF_UP);

        long alertasPendientes = alertaRepository.countByEmpresaIdAndEstado(empresaId, EstadoAlerta.PENDIENTE);
        long cuentasSuspendidas = cuentaRepository.findByEmpresaIdAndEstado(empresaId, EstadoCuenta.SUSPENDIDA).size();
        var hoy = LocalDate.now();
        var facturas = facturaRepository.findByEmpresaId(empresaId);
        BigDecimal deuda0a30=BigDecimal.ZERO, deuda31a60=BigDecimal.ZERO, deuda61a90=BigDecimal.ZERO, deuda90mas=BigDecimal.ZERO;
        for (var f : facturas) {
            if (f.getEstado()==EstadoFactura.PAGADA || f.getEstado()==EstadoFactura.ANULADA || f.getFechaVencimiento()==null) continue;
            BigDecimal saldo=f.getMontoTotal().subtract(f.getMontoPagado());
            if (saldo.signum()<=0 || !f.getFechaVencimiento().isBefore(hoy)) continue;
            long dias=java.time.temporal.ChronoUnit.DAYS.between(f.getFechaVencimiento(),hoy);
            if(dias<=30) deuda0a30=deuda0a30.add(saldo); else if(dias<=60) deuda31a60=deuda31a60.add(saldo); else if(dias<=90) deuda61a90=deuda61a90.add(saldo); else deuda90mas=deuda90mas.add(saldo);
        }

        return DashboardResponse.builder()
                .totalFacturado(totalFacturado)
                .totalCobrado(totalCobrado)
                .porcentajeCobrado(porcentajeCobrado)
                .alertasPendientes(alertasPendientes)
                .cuentasSuspendidas(cuentasSuspendidas)
                .deuda0a30(deuda0a30).deuda31a60(deuda31a60).deuda61a90(deuda61a90).deuda90Mas(deuda90mas)
                .build();
    }

    @Data
    @Builder
    public static class DashboardResponse {
        private BigDecimal totalFacturado;
        private BigDecimal totalCobrado;
        private BigDecimal porcentajeCobrado;
        private long alertasPendientes;
        private long cuentasSuspendidas;
        private BigDecimal deuda0a30;
        private BigDecimal deuda31a60;
        private BigDecimal deuda61a90;
        private BigDecimal deuda90Mas;
    }
}
