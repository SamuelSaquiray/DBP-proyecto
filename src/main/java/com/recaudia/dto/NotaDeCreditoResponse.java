package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class NotaDeCreditoResponse {
    private Long id;
    private String numero;
    private BigDecimal monto;
    private String motivo;
    private LocalDate fecha;
    private Long facturaId;
    private String numeroFactura;
}
