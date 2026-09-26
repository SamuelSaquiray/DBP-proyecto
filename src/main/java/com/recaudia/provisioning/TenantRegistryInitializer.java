package com.recaudia.provisioning;

import com.recaudia.config.ProvisioningProperties;
import com.recaudia.config.TenantDataSourceManager;
import com.recaudia.config.TenantDataSourceProperties;
import com.recaudia.integration.AwsSecretsManagerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TenantRegistryInitializer implements ApplicationRunner {

    private final JdbcTemplate adminJdbc;
    private final TenantDataSourceManager manager;
    private final ProvisioningProperties properties;
    private final TenantDataSourceProperties tenantProperties;
    private final TenantSchemaInitializer schemaInitializer;
    private final AwsSecretsManagerService secretsManager;

    public TenantRegistryInitializer(
            @Qualifier("provisioningJdbcTemplate") JdbcTemplate adminJdbc,
            TenantDataSourceManager manager,
            ProvisioningProperties properties,
            TenantDataSourceProperties tenantProperties,
            TenantSchemaInitializer schemaInitializer,
            AwsSecretsManagerService secretsManager) {
        this.adminJdbc = adminJdbc;
        this.manager = manager;
        this.properties = properties;
        this.tenantProperties = tenantProperties;
        this.schemaInitializer = schemaInitializer;
        this.secretsManager = secretsManager;
    }

    @Override
    public void run(ApplicationArguments args) {
        createRegistryTable();

        for (var entry : tenantProperties.getTenants().entrySet()) {
            Long id = entry.getKey();
            var cfg = entry.getValue();

            String secretName = cfg.getSecretName();
            String databaseName = cfg.getDatabaseName();
            String databaseUrl = databaseUrl(databaseName);

            Integer exists = adminJdbc.queryForObject(
                    "SELECT COUNT(*) FROM recaudia_tenants WHERE id = ?",
                    Integer.class,
                    id);

            if (exists != null && exists == 0) {
                adminJdbc.update(
                        """
                        INSERT INTO recaudia_tenants
                            (id, ruc, razon_social, database_name, database_url,
                             secret_name, activa, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP)
                        """,
                        id,
                        "PENDING-" + id,
                        "Tenant " + id,
                        databaseName,
                        databaseUrl,
                        secretName);
            } else {
                adminJdbc.update(
                        """
                        UPDATE recaudia_tenants
                        SET database_name = COALESCE(database_name, ?),
                            database_url = COALESCE(database_url, ?),
                            secret_name = COALESCE(secret_name, ?)
                        WHERE id = ?
                        """,
                        databaseName,
                        databaseUrl,
                        secretName,
                        id);
            }

            if (!manager.exists(id)) {
                manager.register(id, databaseName, secretName);
            }

            // Mantiene el esquema del tenant actualizado.
            schemaInitializer.initialize(new JdbcTemplate(manager.get(id)));
        }

        // Carga tenants creados dinámicamente desde la BD master.
        adminJdbc.query(
                """
                SELECT id, database_name, secret_name
                FROM recaudia_tenants
                WHERE activa = TRUE
                  AND database_name IS NOT NULL
                """,
                rs -> {
                    Long id = rs.getLong("id");
                    String databaseName = rs.getString("database_name");
                    String secretName = rs.getString("secret_name");

                    if (secretName == null || secretName.isBlank()) {
                        secretName = properties.getRdsSecretName();
                        adminJdbc.update(
                                "UPDATE recaudia_tenants SET secret_name = ? WHERE id = ?",
                                secretName,
                                id);
                    }

                    if (!manager.exists(id)) {
                        manager.register(id, databaseName, secretName);
                    }

                    schemaInitializer.initialize(
                            new JdbcTemplate(manager.get(id)));
                });

        adminJdbc.execute(
                """
                SELECT setval(
                    pg_get_serial_sequence('recaudia_tenants', 'id'),
                    GREATEST(
                        COALESCE((SELECT MAX(id) FROM recaudia_tenants), 1),
                        1
                    ),
                    true
                )
                """);
    }

    private void createRegistryTable() {
        adminJdbc.execute(
                """
                CREATE TABLE IF NOT EXISTS usuarios_maestros (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    nombre VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    activo BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP
                )
                """);

        adminJdbc.execute(
                """
                CREATE TABLE IF NOT EXISTS recaudia_tenants (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    ruc VARCHAR(20) NOT NULL UNIQUE,
                    razon_social VARCHAR(255) NOT NULL,
                    dominio VARCHAR(255),
                    database_name VARCHAR(100),
                    database_url VARCHAR(500),
                    secret_name VARCHAR(500),
                    admin_email VARCHAR(255),
                    activa BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP
                )
                """);

        // Permite actualizar instalaciones creadas con la versión anterior.
        adminJdbc.execute(
                "ALTER TABLE recaudia_tenants ADD COLUMN IF NOT EXISTS secret_name VARCHAR(500)");
    }

    private String databaseUrl(String databaseName) {
        var secret = secretsManager.getSecret(properties.getRdsSecretName());
        String host = required(secret, "host");
        int port = secret.has("port") ? secret.get("port").asInt() : 5432;

        return "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
    }

    private String required(com.fasterxml.jackson.databind.JsonNode secret, String field) {
        var value = secret.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new IllegalStateException(
                    "El secret de RDS no contiene el campo requerido: " + field);
        }
        return value.asText();
    }
}
