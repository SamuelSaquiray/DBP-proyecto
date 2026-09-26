package com.recaudia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AsyncOperationResponse {
    private String message;
    private Long empresaId;
}
