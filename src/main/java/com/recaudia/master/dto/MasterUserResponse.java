package com.recaudia.master.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MasterUserResponse {
    private Long id;
    private String nombre;
    private String email;
    private Boolean activo;
    private LocalDateTime createdAt;
}
