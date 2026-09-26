package com.recaudia.controller;

import com.recaudia.dto.UsuarioRequest;
import com.recaudia.dto.UsuarioResponse;
import com.recaudia.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/usuarios", "/api/v1/usuarios"})
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() { return ResponseEntity.ok(usuarioService.listar()); }

    @PutMapping("/{usuarioId}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long usuarioId, @Valid @RequestBody UsuarioRequest request) { return ResponseEntity.ok(usuarioService.actualizar(usuarioId, request)); }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> desactivar(@PathVariable Long usuarioId) { usuarioService.desactivar(usuarioId); return ResponseEntity.noContent().build(); }
}
