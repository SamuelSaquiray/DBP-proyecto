package com.recaudia.security;

/**
 * Tenant seleccionado para la petición actual.
 * El contexto se limpia al terminar cada request y también en los workers async.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("El tenantId no puede ser null");
        }
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static Long requireTenantId() {
        Long tenantId = getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("No existe un tenant seleccionado para la operación actual");
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
