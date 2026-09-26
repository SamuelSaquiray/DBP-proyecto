package com.recaudia.controller;

import com.recaudia.dto.ServicioRequest;
import com.recaudia.dto.ServicioResponse;
import com.recaudia.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/servicios", "/api/v1/servicios"})
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<ServicioResponse>> listar() { return ResponseEntity.ok(servicioService.listar()); }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequest request) { return ResponseEntity.ok(servicioService.actualizar(id, request)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) { servicioService.desactivar(id); return ResponseEntity.noContent().build(); }
}
