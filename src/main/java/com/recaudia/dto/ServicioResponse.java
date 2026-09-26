package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServicioResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private Long empresaId;
}
