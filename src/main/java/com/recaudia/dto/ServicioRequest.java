package com.recaudia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ServicioRequest {

    @NotBlank
    private String codigo;

    @NotBlank
    private String nombre;

    private String descripcion;
}
