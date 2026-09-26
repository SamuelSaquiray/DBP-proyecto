package com.recaudia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    /**
     * Identificador global del tenant que se utilizará para seleccionar
     * la base de datos de la empresa.
     */
    @jakarta.validation.constraints.NotNull
    private Long empresaId;
}
