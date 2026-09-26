package com.recaudia.dto;

import com.recaudia.domain.EstadoAlerta;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertaUpdateRequest {

    @NotNull
    private EstadoAlerta estado;
}
