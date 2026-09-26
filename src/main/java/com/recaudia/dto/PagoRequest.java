package com.recaudia.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PagoRequest {

    @NotNull
    private Long facturaId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal monto;

    @NotNull
    private LocalDateTime fechaPago;

    private String medioPago;
    private String referencia;
}
