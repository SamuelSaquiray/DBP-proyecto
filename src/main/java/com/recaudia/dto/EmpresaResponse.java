package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EmpresaResponse {
    private Long id;
    private String razonSocial;
    private String ruc;
    private String dominio;
    private Boolean activa;
    private LocalDateTime createdAt;
}
