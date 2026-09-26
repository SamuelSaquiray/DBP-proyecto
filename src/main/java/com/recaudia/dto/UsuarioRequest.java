package com.recaudia.dto;

import com.recaudia.domain.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @jakarta.validation.constraints.Size(min = 8, max = 100)
    private String password;

    @NotNull
    private Rol rol;
}
