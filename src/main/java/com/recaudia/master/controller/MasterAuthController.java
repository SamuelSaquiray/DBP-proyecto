package com.recaudia.master.controller;

import com.recaudia.master.dto.MasterAuthResponse;
import com.recaudia.master.dto.MasterLoginRequest;
import com.recaudia.master.service.MasterAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/master/auth", "/api/v1/master/auth"})
@RequiredArgsConstructor
public class MasterAuthController {

    private final MasterAuthService service;

    @PostMapping("/login")
    public ResponseEntity<MasterAuthResponse> login(@Valid @RequestBody MasterLoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }
}
