package com.recaudia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data public class WhatsAppRequest { @NotBlank private String telefono; @NotBlank private String mensaje; }
