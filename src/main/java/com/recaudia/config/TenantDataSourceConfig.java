package com.recaudia.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.recaudia.integration.AwsSecretsManagerService;
import com.recaudia.security.TenantContext;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(TenantDataSourceProperties.class)
public class TenantDataSourceConfig {

    /**
     * DataSource principal de la aplicación.
     *
     * Si no existe un TenantContext, se utiliza MASTER.
     *
     * Si existe un TenantContext, se utiliza la base de datos
     * correspondiente al tenant.
     */
    @Bean
    public TenantRoutingDataSource tenantRoutingDataSource(
            TenantDataSourceProperties properties,
            AwsSecretsManagerService secretsManager) {

        if (properties.getMasterSecretName() == null
                || properties.getMasterSecretName().isBlank()) {

            throw new IllegalStateException(
                    "No está configurado app.master-secret-name");
        }

        /*
         * ---------------------------------------------------------
         * 1. CREAR DATASOURCE MASTER
         * ---------------------------------------------------------
         */

        JsonNode masterSecret =
                secretsManager.getSecret(properties.getMasterSecretName());

        String masterHost = required(masterSecret, "host");
        String masterUsername = required(masterSecret, "username");
        String masterPassword = required(masterSecret, "password");

        int masterPort = masterSecret.has("port")
                ? masterSecret.get("port").asInt()
                : 5432;

        String masterDatabase;

        if (masterSecret.has("dbname")
                && !masterSecret.get("dbname").asText().isBlank()) {

            masterDatabase = masterSecret.get("dbname").asText();

        } else if (masterSecret.has("database")
                && !masterSecret.get("database").asText().isBlank()) {

            masterDatabase = masterSecret.get("database").asText();

        } else {

            throw new IllegalStateException(
                    "El secret MASTER no contiene 'dbname' ni 'database'");
        }

        HikariDataSource masterDataSource = new HikariDataSource();

        masterDataSource.setJdbcUrl(
                "jdbc:postgresql://"
                        + masterHost
                        + ":"
                        + masterPort
                        + "/"
                        + masterDatabase
        );

        masterDataSource.setUsername(masterUsername);
        masterDataSource.setPassword(masterPassword);
        masterDataSource.setDriverClassName("org.postgresql.Driver");

        masterDataSource.setMaximumPoolSize(10);
        masterDataSource.setMinimumIdle(2);
        masterDataSource.setPoolName("recaudia-master");

        /*
         * ---------------------------------------------------------
         * 2. CREAR DATASOURCES DE TENANTS
         * ---------------------------------------------------------
         *
         * Puede no existir ninguno.
         *
         * Esto es importante porque al inicio todavía podemos
         * tener 0 empresas registradas.
         */

        Map<Object, Object> dataSources = new LinkedHashMap<>();

        if (properties.getTenants() != null) {

            for (var entry : properties.getTenants().entrySet()) {

                Long tenantId = entry.getKey();
                var tenant = entry.getValue();

                if (tenant.getDatabaseName() == null
                        || tenant.getDatabaseName().isBlank()) {

                    throw new IllegalStateException(
                            "El tenant "
                                    + tenantId
                                    + " no tiene database-name configurado");
                }

                if (tenant.getSecretName() == null
                        || tenant.getSecretName().isBlank()) {

                    throw new IllegalStateException(
                            "El tenant "
                                    + tenantId
                                    + " no tiene secret-name configurado");
                }

                JsonNode secret =
                        secretsManager.getSecret(tenant.getSecretName());

                String host = required(secret, "host");
                String username = required(secret, "username");
                String password = required(secret, "password");

                int port = secret.has("port")
                        ? secret.get("port").asInt()
                        : 5432;

                HikariDataSource tenantDataSource =
                        new HikariDataSource();

                tenantDataSource.setJdbcUrl(
                        "jdbc:postgresql://"
                                + host
                                + ":"
                                + port
                                + "/"
                                + tenant.getDatabaseName()
                );

                tenantDataSource.setUsername(username);
                tenantDataSource.setPassword(password);
                tenantDataSource.setDriverClassName(
                        "org.postgresql.Driver"
                );

                tenantDataSource.setMaximumPoolSize(
                        tenant.getMaximumPoolSize()
                );

                tenantDataSource.setMinimumIdle(
                        tenant.getMinimumIdle()
                );

                tenantDataSource.setPoolName(
                        "recaudia-tenant-" + tenantId
                );

                dataSources.put(tenantId, tenantDataSource);
            }
        }

        /*
         * ---------------------------------------------------------
         * 3. CREAR ROUTING DATASOURCE
         * ---------------------------------------------------------
         *
         * Si no hay TenantContext:
         *
         *      -> MASTER
         *
         * Si hay TenantContext:
         *
         *      -> TENANT correspondiente
         */

        TenantRoutingDataSource routing =
                new TenantRoutingDataSource(masterDataSource);

        routing.setTargetDataSources(dataSources);

        routing.setDefaultTargetDataSource(masterDataSource);

        routing.afterPropertiesSet();

        return routing;
    }

    /**
     * DataSource utilizado por Spring/JPA.
     */
    @Bean
    @Primary
    public DataSource dataSource(
            TenantRoutingDataSource routingDataSource) {

        return routingDataSource;
    }

    /**
     * Obtiene un campo obligatorio del secret.
     */
    private String required(
            JsonNode secret,
            String field) {

        JsonNode value = secret.get(field);

        if (value == null
                || value.isNull()
                || value.asText().isBlank()) {

            throw new IllegalStateException(
                    "El secret no contiene el campo requerido: "
                            + field
            );
        }

        return value.asText();
    }

    /**
     * DataSource que decide dinámicamente si debe utilizar
     * MASTER o un TENANT.
     */
    public static class TenantRoutingDataSource
            extends AbstractRoutingDataSource {

        /**
         * MASTER siempre está disponible.
         */
        private final DataSource masterDataSource;

        /**
         * DataSources de tenants registrados.
         */
        private final Map<Object, Object> targets =
                new LinkedHashMap<>();

        public TenantRoutingDataSource(
                DataSource masterDataSource) {

            this.masterDataSource = masterDataSource;
        }

        /**
         * Decide qué datasource utilizar.
         *
         * Sin tenant:
         *      MASTER
         *
         * Con tenant:
         *      TENANT
         */
        @Override
        protected Object determineCurrentLookupKey() {

            Long tenantId =
                    TenantContext.getTenantId();

            if (tenantId == null) {
                return null;
            }

            return tenantId;
        }

        /**
         * Agrega dinámicamente un nuevo tenant.
         *
         * Esto será utilizado cuando se registre una nueva empresa.
         */
        public synchronized void addTenant(
                Long tenantId,
                DataSource dataSource) {

            targets.put(
                    tenantId,
                    dataSource
            );

            super.setTargetDataSources(
                    new LinkedHashMap<>(targets)
            );

            /*
             * MASTER continúa siendo el datasource por defecto.
             */
            super.setDefaultTargetDataSource(
                    masterDataSource
            );

            afterPropertiesSet();
        }

        /**
         * Permite actualizar los tenants existentes.
         */
        @Override
        public void setTargetDataSources(
                Map<Object, Object> targetDataSources) {

            targets.clear();

            if (targetDataSources != null) {
                targets.putAll(targetDataSources);
            }

            super.setTargetDataSources(
                    new LinkedHashMap<>(targets)
            );
        }
    }
}