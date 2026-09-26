package com.recaudia.provisioning;

import com.fasterxml.jackson.databind.JsonNode;
import com.recaudia.config.ProvisioningProperties;
import com.recaudia.config.TenantDataSourceManager;
import com.recaudia.dto.ProvisionEmpresaRequest;
import com.recaudia.dto.ProvisionEmpresaResponse;
import com.recaudia.integration.AwsSecretsManagerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.recaudia.exception.DuplicateResourceException;
import com.recaudia.exception.ProvisioningException;

import java.time.LocalDateTime;

@Service
public class TenantProvisioningService {

    private final JdbcTemplate adminJdbc;
    private final ProvisioningProperties properties;
    private final TenantDataSourceManager dataSourceManager;
    private final TenantSchemaInitializer schemaInitializer;
    private final PasswordEncoder passwordEncoder;
    private final AwsSecretsManagerService secretsManager;

    public TenantProvisioningService(
            @Qualifier("provisioningJdbcTemplate") JdbcTemplate adminJdbc,
            ProvisioningProperties properties,
            TenantDataSourceManager dataSourceManager,
            TenantSchemaInitializer schemaInitializer,
            PasswordEncoder passwordEncoder,
            AwsSecretsManagerService secretsManager) {
        this.adminJdbc = adminJdbc;
        this.properties = properties;
        this.dataSourceManager = dataSourceManager;
        this.schemaInitializer = schemaInitializer;
        this.passwordEncoder = passwordEncoder;
        this.secretsManager = secretsManager;
    }

    public synchronized ProvisionEmpresaResponse provision(ProvisionEmpresaRequest request) {
        validateRequest(request);

        Long tenantId = adminJdbc.queryForObject(
                """
                INSERT INTO recaudia_tenants
                    (ruc, razon_social, dominio, activa, created_at)
                VALUES (?, ?, ?, TRUE, ?)
                RETURNING id
                """,
                Long.class,
                request.getRuc(),
                request.getRazonSocial(),
                request.getDominio(),
                LocalDateTime.now()
        );

        String dbName = properties.getDatabasePrefix() + tenantId;
        String jdbcUrl = databaseUrl(dbName);
        String secretName = properties.getRdsSecretName();

        try {
            createDatabase(dbName);

            dataSourceManager.register(tenantId, dbName, secretName);

            JdbcTemplate tenantJdbc =
                    new JdbcTemplate(dataSourceManager.get(tenantId));

            schemaInitializer.initialize(tenantJdbc);

            tenantJdbc.update(
                    """
                    INSERT INTO empresas
                        (id, razon_social, ruc, dominio, activa, created_at, updated_at)
                    VALUES (?, ?, ?, ?, TRUE, ?, ?)
                    """,
                    tenantId,
                    request.getRazonSocial(),
                    request.getRuc(),
                    request.getDominio(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            tenantJdbc.update(
                    """
                    INSERT INTO usuarios
                        (nombre, email, password_hash, rol, activo,
                         empresa_id, created_at, updated_at)
                    VALUES (?, ?, ?, 'ADMIN', TRUE, ?, ?, ?)
                    """,
                    request.getAdminNombre(),
                    request.getAdminEmail(),
                    passwordEncoder.encode(request.getAdminPassword()),
                    tenantId,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            adminJdbc.update(
                    """
                    UPDATE recaudia_tenants
                    SET database_name = ?,
                        database_url = ?,
                        secret_name = ?,
                        admin_email = ?,
                        updated_at = ?
                    WHERE id = ?
                    """,
                    dbName,
                    jdbcUrl,
                    secretName,
                    request.getAdminEmail(),
                    LocalDateTime.now(),
                    tenantId
            );

            return ProvisionEmpresaResponse.builder()
                    .empresaId(tenantId)
                    .razonSocial(request.getRazonSocial())
                    .databaseName(dbName)
                    .adminEmail(request.getAdminEmail())
                    .build();

        } catch (Exception ex) {
            try {
                ((com.zaxxer.hikari.HikariDataSource) dataSourceManager.get(tenantId)).close();
            } catch (Exception ignored) {
            }
            dropDatabase(dbName);
            adminJdbc.update("DELETE FROM recaudia_tenants WHERE id = ?", tenantId);
            throw new ProvisioningException(
                    "No se pudo provisionar la base de datos de la empresa", ex);
        }
    }

    private void validateRequest(ProvisionEmpresaRequest request) {
        Integer count = adminJdbc.queryForObject(
                "SELECT COUNT(*) FROM recaudia_tenants WHERE ruc = ?",
                Integer.class,
                request.getRuc());

        if (count != null && count > 0) {
            throw new DuplicateResourceException("Ya existe una empresa con ese RUC");
        }

        Integer emailCount = adminJdbc.queryForObject(
                "SELECT COUNT(*) FROM recaudia_tenants WHERE admin_email = ?",
                Integer.class,
                request.getAdminEmail());

        if (emailCount != null && emailCount > 0) {
            throw new DuplicateResourceException("El email del administrador ya está registrado");
        }
    }

    private String databaseUrl(String dbName) {
        JsonNode secret = secretsManager.getSecret(properties.getRdsSecretName());
        String host = required(secret, "host");
        int port = secret.has("port") ? secret.get("port").asInt() : 5432;
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
    }

    private void createDatabase(String dbName) {
        if (!dbName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Nombre de BD inválido");
        }
        adminJdbc.execute("CREATE DATABASE " + dbName);
    }

    private void dropDatabase(String dbName) {
        try {
            adminJdbc.execute("DROP DATABASE IF EXISTS " + dbName);
        } catch (Exception ignored) {
        }
    }

    private String required(JsonNode secret, String field) {
        JsonNode value = secret.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new IllegalStateException(
                    "El secret de RDS no contiene el campo requerido: " + field);
        }
        return value.asText();
    }
}
