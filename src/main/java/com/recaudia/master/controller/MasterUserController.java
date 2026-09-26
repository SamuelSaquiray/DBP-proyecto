package com.recaudia.master.controller;

import com.recaudia.master.dto.MasterUserRequest;
import com.recaudia.master.dto.MasterUserResponse;
import com.recaudia.master.service.MasterAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/master/usuarios", "/api/v1/master/usuarios"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('MASTER')")
public class MasterUserController {

    private final MasterAuthService service;

    @PostMapping
    public ResponseEntity<MasterUserResponse> create(@Valid @RequestBody MasterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(request.getNombre(), request.getEmail(), request.getPassword()));
    }
}
