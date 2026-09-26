package com.recaudia.controller;

import com.recaudia.dto.NotaDeCreditoRequest;
import com.recaudia.dto.NotaDeCreditoResponse;
import com.recaudia.service.NotaDeCreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/notas-credito", "/api/v1/notas-credito"})
@RequiredArgsConstructor
public class NotaDeCreditoController {
    private final NotaDeCreditoService service;

    @PostMapping
    public ResponseEntity<NotaDeCreditoResponse> crear(@Valid @RequestBody NotaDeCreditoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }
}
