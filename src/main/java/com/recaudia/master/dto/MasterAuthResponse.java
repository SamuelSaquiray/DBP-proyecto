package com.recaudia.master.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MasterAuthResponse {
    private String token;
    private Long usuarioId;
    private String nombre;
    private String email;
    private String tipo;
}
