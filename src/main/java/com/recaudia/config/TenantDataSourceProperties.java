package com.recaudia.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class TenantDataSourceProperties {

    /**
     * Secret de AWS Secrets Manager que contiene
     * las credenciales de la base de datos MASTER.
     */
    private String masterSecretName;

    /**
     * Configuración de las bases de datos de cada tenant.
     *
     * Puede estar vacío al iniciar la aplicación.
     */
    private Map<Long, Tenant> tenants = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class Tenant {

        /**
         * Nombre de la base de datos del tenant.
         *
         * Ejemplo:
         * recaudia_tenant_1
         */
        private String databaseName;

        /**
         * Secret de AWS Secrets Manager del tenant.
         */
        private String secretName;

        private int maximumPoolSize = 10;

        private int minimumIdle = 2;
    }
}