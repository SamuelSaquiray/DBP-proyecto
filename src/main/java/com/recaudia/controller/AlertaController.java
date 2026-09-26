package com.recaudia.controller;

import com.recaudia.dto.AlertaResponse;
import com.recaudia.dto.AlertaUpdateRequest;
import com.recaudia.service.AlertaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/alertas", "/api/v1/alertas"})
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    public ResponseEntity<List<AlertaResponse>> listar() {
        return ResponseEntity.ok(alertaService.listar());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<AlertaResponse>> listarPendientes() {
        return ResponseEntity.ok(alertaService.listarPendientes());
    }

    @PatchMapping("/{alertaId}")
    public ResponseEntity<AlertaResponse> actualizarEstado(
            @PathVariable Long alertaId,
            @Valid @RequestBody AlertaUpdateRequest request) {
        return ResponseEntity.ok(alertaService.actualizarEstado(alertaId, request));
    }
}
