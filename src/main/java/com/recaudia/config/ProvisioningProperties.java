package com.recaudia.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.provisioning")
public class ProvisioningProperties {
    private String rdsSecretName;
    private String provisioningKeySecretName;
    private String databasePrefix = "recaudia_tenant_";
}
