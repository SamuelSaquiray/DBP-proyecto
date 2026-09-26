package com.recaudia.config;

import com.recaudia.integration.AwsSecretsManagerService;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(ProvisioningProperties.class)
public class ProvisioningConfig {

    @Bean(name = "provisioningDataSource")
    public DataSource provisioningDataSource(
            ProvisioningProperties properties,
            AwsSecretsManagerService secretsManager) {

        var secret = secretsManager.getSecret(properties.getRdsSecretName());

        String host = required(secret, "host");
        String username = required(secret, "username");
        String password = required(secret, "password");
        int port = secret.has("port") ? secret.get("port").asInt() : 5432;
        String database = required(secret, "dbname");

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(3);
        ds.setMinimumIdle(1);
        ds.setPoolName("recaudia-provisioning-admin");
        return ds;
    }

    @Bean(name = "provisioningJdbcTemplate")
    public JdbcTemplate provisioningJdbcTemplate(
            @Qualifier("provisioningDataSource") DataSource provisioningDataSource) {
        return new JdbcTemplate(provisioningDataSource);
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
