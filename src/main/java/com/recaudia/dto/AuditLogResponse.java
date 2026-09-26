package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class AuditLogResponse {
    private Long id; private String accion; private String entidad; private Long entidadId;
    private Long usuarioId; private String usuarioEmail; private String detalle; private LocalDateTime fecha;
}
