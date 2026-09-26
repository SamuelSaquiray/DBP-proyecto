package com.recaudia.master.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MasterUserRequest {
    @NotBlank private String nombre;
    @NotBlank @Email private String email;
    @NotBlank private String password;
}
