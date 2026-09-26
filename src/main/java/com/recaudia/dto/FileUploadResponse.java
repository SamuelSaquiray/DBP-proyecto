package com.recaudia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadResponse {
    private String key;
    private String fileName;
    private long size;
    private String contentType;
}
