package com.recaudia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class NotaDeCreditoRequest {

    @NotBlank
    private String numero;

    @NotNull
    private Long facturaId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal monto;

    private String motivo;

    @NotNull
    private LocalDate fecha;
}
