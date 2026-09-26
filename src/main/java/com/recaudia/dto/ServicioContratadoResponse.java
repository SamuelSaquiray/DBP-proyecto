package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ServicioContratadoResponse {
    private Long id;
    private Long cuentaId;
    private Long servicioId;
    private String servicioNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal tarifaMensual;
    private Boolean activo;
}
