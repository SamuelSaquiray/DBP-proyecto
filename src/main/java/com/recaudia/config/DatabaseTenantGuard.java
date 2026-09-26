package com.recaudia.config;

import com.recaudia.security.TenantContext;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTenantGuard {
    private final TenantDataSourceManager manager;

    public DatabaseTenantGuard(TenantDataSourceManager manager) {
        this.manager = manager;
    }

    public void requireConfigured(Long tenantId) {
        if (!manager.exists(tenantId)) {
            throw new IllegalArgumentException("La empresa no tiene una base de datos configurada");
        }
    }

    public void select(Long tenantId) {
        requireConfigured(tenantId);
        TenantContext.setTenantId(tenantId);
    }
}
