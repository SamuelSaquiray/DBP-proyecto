package com.recaudia.controller;

import com.recaudia.dto.CuentaRequest;
import com.recaudia.dto.CuentaResponse;
import com.recaudia.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/cuentas", "/api/v1/cuentas"})
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        CuentaResponse response = cuentaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listar() {
        return ResponseEntity.ok(cuentaService.listar());
    }

    @GetMapping("/{cuentaId}")
    public ResponseEntity<CuentaResponse> obtenerPorId(@PathVariable Long cuentaId) { return ResponseEntity.ok(cuentaService.obtenerPorId(cuentaId)); }

    @PutMapping("/{cuentaId}")
    public ResponseEntity<CuentaResponse> actualizar(@PathVariable Long cuentaId, @Valid @RequestBody CuentaRequest request) { return ResponseEntity.ok(cuentaService.actualizar(cuentaId, request)); }

    @DeleteMapping("/{cuentaId}")
    public ResponseEntity<Void> cancelar(@PathVariable Long cuentaId) { cuentaService.cancelar(cuentaId); return ResponseEntity.noContent().build(); }
}
