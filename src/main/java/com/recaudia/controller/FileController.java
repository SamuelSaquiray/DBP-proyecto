package com.recaudia.controller;

import com.recaudia.dto.FileUploadResponse;
import com.recaudia.integration.S3Service;
import com.recaudia.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/files", "/api/v1/files"})
@RequiredArgsConstructor
public class FileController {
    private final S3Service s3Service;
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        Long empresaId = TenantContext.getTenantId();
        String key = s3Service.subirArchivo(file, empresaId);
        return ResponseEntity.ok(FileUploadResponse.builder().key(key).fileName(file.getOriginalFilename())
                .size(file.getSize()).contentType(file.getContentType()).build());
    }
}
