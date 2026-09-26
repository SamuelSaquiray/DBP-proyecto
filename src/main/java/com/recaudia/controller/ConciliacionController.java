package com.recaudia.controller;

import com.recaudia.dto.AsyncOperationResponse;
import com.recaudia.dto.ReconciliationResponse;
import com.recaudia.service.ConciliacionService;
import com.recaudia.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController @RequestMapping({"/api/conciliacion","/api/v1/conciliacion"}) @RequiredArgsConstructor
public class ConciliacionController {
    private final ConciliacionService service;
    @PostMapping
    public CompletableFuture<ResponseEntity<ReconciliationResponse>> ejecutar(){
        Long id=TenantContext.getTenantId();
        return service.conciliar(id).thenApply(r -> ResponseEntity.ok(ReconciliationResponse.builder().facturasProcesadas(r.facturasProcesadas()).facturasActualizadas(r.facturasActualizadas()).inconsistencias(r.inconsistencias()).build()));
    }
}
