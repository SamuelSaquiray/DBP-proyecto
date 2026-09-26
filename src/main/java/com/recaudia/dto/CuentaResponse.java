package com.recaudia.dto;

import com.recaudia.domain.EstadoCuenta;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CuentaResponse {
    private Long id;
    private String codigoCliente;
    private String razonSocial;
    private String ruc;
    private String emailContacto;
    private String telefonoWhatsApp;
    private EstadoCuenta estado;
    private Long empresaId;
    private LocalDateTime createdAt;
}
