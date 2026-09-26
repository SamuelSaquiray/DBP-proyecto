package com.recaudia.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ServicioContratadoRequest {

    @NotNull
    private Long servicioId;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal tarifaMensual;
}
