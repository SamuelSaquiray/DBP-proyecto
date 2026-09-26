package com.recaudia.integration;

import com.recaudia.domain.Cuenta;
import com.recaudia.domain.EstadoCuenta;
import com.recaudia.repository.CuentaRepository;
import com.recaudia.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {
    private final RestClient.Builder restClientBuilder;
    private final CuentaRepository cuentaRepository;
    @Value("${whatsapp.api-url:}") private String apiUrl;
    @Value("${whatsapp.token:}") private String token;

    public void enviarMensajeCobranza(String telefono, String mensaje) {
        if (apiUrl == null || apiUrl.isBlank() || token == null || token.isBlank()) {
            throw new IllegalStateException("WhatsApp no está configurado");
        }
        Map<String,Object> body = Map.of("messaging_product","whatsapp","to",telefono,
                "type","text","text",Map.of("preview_url",false,"body",mensaje));
        restClientBuilder.build().post().uri(apiUrl).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + token).body(body).retrieve().toBodilessEntity();
    }

    @Async("taskExecutor")
    public void enviarRecordatorioMasivo(Long empresaId) {
        TenantContext.setTenantId(empresaId);
        try {
            List<Cuenta> cuentas = cuentaRepository.findByEmpresaIdAndEstado(empresaId, EstadoCuenta.MORA);
            for (Cuenta cuenta : cuentas) {
                if (cuenta.getTelefonoWhatsApp() == null || cuenta.getTelefonoWhatsApp().isBlank()) continue;
                try { enviarMensajeCobranza(cuenta.getTelefonoWhatsApp(),
                        "Hola " + cuenta.getRazonSocial() + ", tienes pagos pendientes. Por favor revisa tu estado de cuenta o comunícate con nuestro equipo de cobranzas."); }
                catch (Exception ex) { log.error("No se pudo enviar WhatsApp a cuenta {}", cuenta.getId(), ex); }
            }
        } finally { TenantContext.clear(); }
    }
}
