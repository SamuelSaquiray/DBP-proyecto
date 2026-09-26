package com.recaudia.dto;

import com.recaudia.domain.EstadoCuenta;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CuentaRequest {

    @NotBlank
    private String codigoCliente;

    @NotBlank
    private String razonSocial;

    private String ruc;
    private String emailContacto;
    private String telefonoWhatsApp;
    private EstadoCuenta estado;
}
