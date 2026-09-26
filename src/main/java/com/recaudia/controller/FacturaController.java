package com.recaudia.controller;

import com.recaudia.dto.FacturaRequest;
import com.recaudia.dto.FacturaResponse;
import com.recaudia.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/facturas", "/api/v1/facturas"})
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @PostMapping
    public ResponseEntity<FacturaResponse> crear(@Valid @RequestBody FacturaRequest request) {
        FacturaResponse response = facturaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FacturaResponse>> listar() {
        return ResponseEntity.ok(facturaService.listar());
    }

    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<FacturaResponse>> listarPorCuenta(@PathVariable Long cuentaId) { return ResponseEntity.ok(facturaService.listarPorCuenta(cuentaId)); }

    @PatchMapping("/{facturaId}/estado")
    public ResponseEntity<FacturaResponse> actualizarEstado(@PathVariable Long facturaId, @Valid @RequestBody com.recaudia.dto.FacturaEstadoRequest request) { return ResponseEntity.ok(facturaService.actualizarEstado(facturaId, request.getEstado())); }
}
