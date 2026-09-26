package com.recaudia.dto;

import com.recaudia.domain.Rol;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private Boolean activo;
    private Long empresaId;
    private LocalDateTime createdAt;
}
