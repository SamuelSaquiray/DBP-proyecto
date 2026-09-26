package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatbotResponse {
    private String respuesta;
    private Long cuentaId;
}
