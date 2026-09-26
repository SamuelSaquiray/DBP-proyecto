package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReconciliationResponse {
    private long facturasProcesadas;
    private long facturasActualizadas;
    private long inconsistencias;
}
