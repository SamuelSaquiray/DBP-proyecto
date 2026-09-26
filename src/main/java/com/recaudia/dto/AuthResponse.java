package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Long usuarioId;
    private String nombre;
    private String email;
    private String rol;
    private Long empresaId;
}
