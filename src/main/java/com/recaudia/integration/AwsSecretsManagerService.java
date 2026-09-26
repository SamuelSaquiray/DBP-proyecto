package com.recaudia.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;

@Service
@RequiredArgsConstructor
public class AwsSecretsManagerService {

    private final SecretsManagerClient client;
    private final ObjectMapper objectMapper;

    public JsonNode getSecret(String secretName) {
        if (secretName == null || secretName.isBlank()) {
            throw new IllegalArgumentException("El nombre del secret es obligatorio");
        }

        try {
            var response = client.getSecretValue(
                    GetSecretValueRequest.builder()
                            .secretId(secretName)
                            .build()
            );

            if (response.secretString() == null || response.secretString().isBlank()) {
                throw new IllegalStateException("El secret no contiene SecretString: " + secretName);
            }

            return objectMapper.readTree(response.secretString());
        } catch (ResourceNotFoundException e) {
            throw new IllegalStateException("No existe el secret de AWS: " + secretName, e);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo obtener el secret de AWS: " + secretName, e);
        }
    }

    public String getString(String secretName, String field) {
        JsonNode node = getSecret(secretName);
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new IllegalStateException(
                    "El secret " + secretName + " no contiene el campo '" + field + "'");
        }
        return value.asText();
    }

    public void createOrUpdateJsonSecret(String secretName, JsonNode value) {
        try {
            String json = objectMapper.writeValueAsString(value);

            try {
                client.getSecretValue(
                        GetSecretValueRequest.builder()
                                .secretId(secretName)
                                .build()
                );

                client.putSecretValue(
                        PutSecretValueRequest.builder()
                                .secretId(secretName)
                                .secretString(json)
                                .build()
                );
            } catch (ResourceNotFoundException e) {
                client.createSecret(
                        CreateSecretRequest.builder()
                                .name(secretName)
                                .secretString(json)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo crear/actualizar el secret de AWS: " + secretName, e);
        }
    }
}
