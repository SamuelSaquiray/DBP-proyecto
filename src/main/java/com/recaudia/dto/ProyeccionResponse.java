package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProyeccionResponse {
    private BigDecimal promedio;
    private BigDecimal minimo;
    private BigDecimal maximo;
    private BigDecimal totalPendiente;
    private int escenarios;
}
