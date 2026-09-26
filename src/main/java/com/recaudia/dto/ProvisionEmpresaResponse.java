package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProvisionEmpresaResponse {
    private Long empresaId;
    private String razonSocial;
    private String databaseName;
    private String adminEmail;
}
