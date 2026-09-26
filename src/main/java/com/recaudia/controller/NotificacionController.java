package com.recaudia.controller;

import com.recaudia.dto.AsyncOperationResponse;
import com.recaudia.integration.WhatsAppService;
import com.recaudia.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping({"/api/notificaciones","/api/v1/notificaciones"}) @RequiredArgsConstructor
public class NotificacionController {
    private final WhatsAppService whatsappService;
    @PostMapping("/whatsapp/mora")
    public ResponseEntity<AsyncOperationResponse> morosidad(){ Long id=TenantContext.getTenantId(); whatsappService.enviarRecordatorioMasivo(id); return ResponseEntity.accepted().body(new AsyncOperationResponse("Envío masivo de WhatsApp iniciado",id)); }
}
