package com.recaudia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatbotRequest {
    @NotBlank
    private String pregunta;
    private Long cuentaId;
}
