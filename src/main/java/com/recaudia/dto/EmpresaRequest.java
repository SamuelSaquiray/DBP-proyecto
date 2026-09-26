package com.recaudia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpresaRequest {

    @NotBlank
    @Size(max = 200)
    private String razonSocial;

    @NotBlank
    @Size(max = 20)
    private String ruc;

    @Size(max = 100)
    private String dominio;
}
