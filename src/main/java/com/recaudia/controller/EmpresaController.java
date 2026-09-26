package com.recaudia.controller;

import com.recaudia.dto.EmpresaResponse;
import com.recaudia.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/empresas", "/api/v1/empresas"})
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.obtenerPorId(id));
    }
}
