package com.recaudia.master.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MasterLoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
