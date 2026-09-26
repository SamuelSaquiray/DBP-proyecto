package com.recaudia.controller;

import com.recaudia.async.MonteCarloService;
import com.recaudia.async.ValidacionMasivaService;
import com.recaudia.dto.AsyncOperationResponse;
import com.recaudia.dto.ProyeccionResponse;
import com.recaudia.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping({"/api/operaciones", "/api/v1/operaciones"})
@RequiredArgsConstructor
public class AsyncOperationsController {
    private final ValidacionMasivaService validacionMasivaService;
    private final MonteCarloService monteCarloService;

    @PostMapping("/validacion-facturas")
    public ResponseEntity<AsyncOperationResponse> validar() {
        Long empresaId=TenantContext.getTenantId(); validacionMasivaService.validarFacturasMasivas(empresaId);
        return ResponseEntity.accepted().body(new AsyncOperationResponse("Validación masiva iniciada", empresaId));
    }

    @GetMapping("/proyeccion-cobranza")
    public CompletableFuture<ResponseEntity<ProyeccionResponse>> proyeccion(@RequestParam(defaultValue="5000") int escenarios) {
        if (escenarios < 100 || escenarios > 100000) throw new IllegalArgumentException("escenarios debe estar entre 100 y 100000");
        Long empresaId=TenantContext.getTenantId();
        return monteCarloService.simularCobranza(empresaId, escenarios)
                .thenApply(r -> ResponseEntity.ok(ProyeccionResponse.builder().promedio(r.promedio()).minimo(r.minimo()).maximo(r.maximo()).totalPendiente(r.totalPendiente()).escenarios(escenarios).build()));
    }
}
