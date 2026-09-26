package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PagoResponse {
    private Long id;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private String medioPago;
    private String referencia;
    private Long facturaId;
    private String numeroFactura;
}
