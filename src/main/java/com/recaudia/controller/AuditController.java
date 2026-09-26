package com.recaudia.controller;

import com.recaudia.dto.AuditLogResponse;
import com.recaudia.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/auditoria", "/api/v1/auditoria"})
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> listar() {
        return ResponseEntity.ok(auditService.listar());
    }

    @GetMapping("/{entidad}/{entidadId}")
    public ResponseEntity<List<AuditLogResponse>> historial(
            @PathVariable String entidad,
            @PathVariable Long entidadId
    ) {
        return ResponseEntity.ok(
                auditService.historial(entidad, entidadId)
        );
    }
}