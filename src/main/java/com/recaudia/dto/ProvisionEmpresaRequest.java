package com.recaudia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProvisionEmpresaRequest {
    @NotBlank private String razonSocial;
    @NotBlank private String ruc;
    private String dominio;

    @NotBlank private String adminNombre;
    @NotBlank @Email private String adminEmail;
    @NotBlank private String adminPassword;
}
