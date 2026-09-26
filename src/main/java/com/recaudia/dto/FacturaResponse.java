package com.recaudia.dto;

import com.recaudia.domain.EstadoFactura;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class FacturaResponse {
    private Long id;
    private String numeroFactura;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private BigDecimal montoTotal;
    private BigDecimal montoPagado;
    private EstadoFactura estado;
    private Long cuentaId;
    private String cuentaRazonSocial;
    private Long empresaId;
    private LocalDateTime createdAt;
}
