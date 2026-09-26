package com.recaudia.config;

import com.recaudia.integration.AwsSecretsManagerService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class TenantDataSourceManager {

    private final TenantDataSourceConfig.TenantRoutingDataSource routingDataSource;
    private final AwsSecretsManagerService secretsManager;
    private final Map<Long, HikariDataSource> dataSources = new ConcurrentHashMap<>();

    public synchronized void register(Long tenantId, String databaseName, String secretName) {
        if (tenantId == null || databaseName == null || databaseName.isBlank()) {
            throw new IllegalArgumentException("tenantId y databaseName son obligatorios");
        }
        if (dataSources.containsKey(tenantId)) {
            return;
        }

        var secret = secretsManager.getSecret(secretName);
        String host = required(secret, "host");
        String username = required(secret, "username");
        String password = required(secret, "password");
        int port = secret.has("port") ? secret.get("port").asInt() : 5432;

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + databaseName);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setPoolName("recaudia-tenant-" + tenantId);

        dataSources.put(tenantId, ds);
        routingDataSource.addTenant(tenantId, ds);
    }

    public boolean exists(Long tenantId) {
        return tenantId != null && dataSources.containsKey(tenantId);
    }

    public DataSource get(Long tenantId) {
        HikariDataSource ds = dataSources.get(tenantId);
        if (ds == null) {
            throw new IllegalArgumentException("Tenant no configurado: " + tenantId);
        }
        return ds;
    }

    private String required(com.fasterxml.jackson.databind.JsonNode secret, String field) {
        var value = secret.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new IllegalStateException("El secret no contiene el campo requerido: " + field);
        }
        return value.asText();
    }
}
