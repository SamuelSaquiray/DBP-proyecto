package com.recaudia.controller;

import com.recaudia.dto.ServicioContratadoRequest;
import com.recaudia.dto.ServicioContratadoResponse;
import com.recaudia.service.ServicioContratadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/cuentas", "/api/v1/cuentas"})
@RequiredArgsConstructor
public class ServicioContratadoController {
    private final ServicioContratadoService service;

    @PostMapping("/{cuentaId}/servicios")
    public ResponseEntity<ServicioContratadoResponse> contratar(
            @PathVariable Long cuentaId,
            @Valid @RequestBody ServicioContratadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.contratar(cuentaId, request));
    }

    @GetMapping("/{cuentaId}/servicios")
    public ResponseEntity<List<ServicioContratadoResponse>> listar(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(service.listarPorCuenta(cuentaId));
    }
}
