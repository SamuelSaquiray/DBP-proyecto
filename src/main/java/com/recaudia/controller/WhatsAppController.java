package com.recaudia.controller;

import com.recaudia.dto.WhatsAppRequest;
import com.recaudia.integration.WhatsAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping({"/api/whatsapp","/api/v1/whatsapp"}) @RequiredArgsConstructor
public class WhatsAppController {
 private final WhatsAppService service;
 @PostMapping("/mensaje") public ResponseEntity<Void> enviar(@Valid @RequestBody WhatsAppRequest request){ service.enviarMensajeCobranza(request.getTelefono(),request.getMensaje()); return ResponseEntity.accepted().build(); }
}
