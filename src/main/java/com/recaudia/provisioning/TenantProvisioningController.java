package com.recaudia.provisioning;

import com.recaudia.config.ProvisioningProperties;
import com.recaudia.dto.ProvisionEmpresaRequest;
import com.recaudia.dto.ProvisionEmpresaResponse;
import com.recaudia.integration.AwsSecretsManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/provisioning/empresas", "/api/v1/provisioning/empresas"})
@PreAuthorize("hasRole('MASTER')")
@RequiredArgsConstructor
public class TenantProvisioningController {

    private final TenantProvisioningService service;
    private final ProvisioningProperties properties;
    private final AwsSecretsManagerService secretsManager;

    @PostMapping
    public ResponseEntity<ProvisionEmpresaResponse> provisionar(
            @RequestHeader(value = "X-Provisioning-Key", required = false)
            String provisioningKey,
            @Valid @RequestBody ProvisionEmpresaRequest request) {

        String expectedKey = secretsManager.getString(
                properties.getProvisioningKeySecretName(),
                "provisioningKey");

        if (provisioningKey == null || !expectedKey.equals(provisioningKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.provision(request));
    }
}
