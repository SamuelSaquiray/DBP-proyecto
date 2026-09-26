package com.recaudia.controller;

import com.recaudia.domain.Cuenta;
import com.recaudia.domain.Factura;
import com.recaudia.dto.ChatbotRequest;
import com.recaudia.dto.ChatbotResponse;
import com.recaudia.integration.LlmService;
import com.recaudia.repository.CuentaRepository;
import com.recaudia.repository.FacturaRepository;
import com.recaudia.security.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/chatbot", "/api/v1/chatbot"})
@RequiredArgsConstructor
public class ChatbotController {
    private final LlmService llmService;
    private final CuentaRepository cuentaRepository;
    private final FacturaRepository facturaRepository;

    @PostMapping("/cobranza")
    public ResponseEntity<ChatbotResponse> preguntar(@Valid @RequestBody ChatbotRequest request) {
        Long empresaId = TenantContext.getTenantId();
        String contexto;
        if (request.getCuentaId() != null) {
            Cuenta c = cuentaRepository.findById(request.getCuentaId()).orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
            if (!c.getEmpresa().getId().equals(empresaId)) throw new IllegalArgumentException("La cuenta no pertenece a la empresa");
            var facturas = facturaRepository.findByCuentaId(c.getId());
            BigDecimal pendiente = facturas.stream().map(f -> f.getMontoTotal().subtract(f.getMontoPagado())).reduce(BigDecimal.ZERO, BigDecimal::add);
            contexto = "Cuenta: " + c.getRazonSocial() + ", código: " + c.getCodigoCliente() + ", estado: " + c.getEstado() +
                    ", saldo pendiente: " + pendiente + ", facturas: " + facturas.stream().map(this::facturaResumen).collect(Collectors.joining(" | "));
        } else {
            var facturas = facturaRepository.findByEmpresaId(empresaId);
            BigDecimal pendiente = facturas.stream().map(f -> f.getMontoTotal().subtract(f.getMontoPagado())).reduce(BigDecimal.ZERO, BigDecimal::add);
            contexto = "Empresa tenant " + empresaId + ", facturas: " + facturas.size() + ", saldo pendiente total: " + pendiente;
        }
        String respuesta = llmService.preguntarSobreCobranza(request.getPregunta(), contexto);
        return ResponseEntity.ok(ChatbotResponse.builder().respuesta(respuesta).cuentaId(request.getCuentaId()).build());
    }

    private String facturaResumen(Factura f) { return f.getNumeroFactura()+"="+f.getEstado()+" pendiente="+f.getMontoTotal().subtract(f.getMontoPagado()); }
}
