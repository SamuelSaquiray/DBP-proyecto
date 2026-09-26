# Provisioning DB

The provisioning registry lives in `recaudia_master`.

Create it once on the PostgreSQL server:

```sql
CREATE DATABASE recaudia_master;
```

The application creates the `recaudia_tenants` table automatically on startup.

The PostgreSQL user configured by `PROVISIONING_ADMIN_DB_USERNAME` must have permission to:

- connect to `recaudia_master`;
- create databases (`CREATEDB`);
- connect to newly created tenant databases.

For production, use a dedicated provisioning role rather than a superuser and keep credentials in environment variables/secrets.
