package com.recaudia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FacturaRequest {

    @NotBlank
    private String numeroFactura;

    @NotNull
    private Long cuentaId;

    @NotNull
    private LocalDate fechaEmision;

    private LocalDate fechaVencimiento;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal montoTotal;
}
