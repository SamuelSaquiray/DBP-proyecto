package com.recaudia.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recaudia.exception.InvalidOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LlmService {
    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;
    @Value("${llm.api-url:https://api.openai.com/v1/chat/completions}") private String apiUrl;
    @Value("${llm.api-key:}") private String apiKey;
    @Value("${llm.model:gpt-4o-mini}") private String model;

    public String preguntarSobreCobranza(String pregunta, String contextoEmpresa) {
        if (apiKey == null || apiKey.isBlank()) throw new InvalidOperationException("LLM_API_KEY no está configurada");
        String system = "Eres el asistente de cobranza de Recaud.IA. Responde únicamente usando el contexto proporcionado. " +
                "No inventes facturas, pagos ni montos. Si el contexto no contiene la respuesta, indícalo claramente.\nContexto:\n" + contextoEmpresa;
        Map<String,Object> body = Map.of(
                "model", model,
                "temperature", 0.1,
                "messages", List.of(
                        Map.of("role","system","content",system),
                        Map.of("role","user","content",pregunta)
                )
        );
        try {
            String raw = restClientBuilder.build().post().uri(apiUrl).contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey).body(body).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(raw);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) throw new InvalidOperationException("El proveedor LLM no devolvió una respuesta válida");
            return content.asText();
        } catch (InvalidOperationException e) { throw e; }
        catch (Exception e) { throw new InvalidOperationException("No se pudo consultar el servicio LLM", e); }
    }
}
