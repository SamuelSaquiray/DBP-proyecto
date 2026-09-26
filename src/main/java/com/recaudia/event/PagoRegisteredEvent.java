package com.recaudia.event;
import java.math.BigDecimal;
public record PagoRegisteredEvent(Long pagoId, Long facturaId, Long empresaId, BigDecimal monto) {}
