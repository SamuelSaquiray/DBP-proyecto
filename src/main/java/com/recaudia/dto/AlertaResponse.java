package com.recaudia.dto;

import com.recaudia.domain.EstadoAlerta;
import com.recaudia.domain.TipoAlerta;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AlertaResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private TipoAlerta tipo;
    private EstadoAlerta estado;
    private Long facturaId;
    private String numeroFactura;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaResolucion;
}
