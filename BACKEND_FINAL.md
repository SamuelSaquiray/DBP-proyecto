# Recaud.IA Backend - Final

## Incluido en esta versión

- Spring Boot 3.2.5 / Java 21.
- PostgreSQL + JPA/Hibernate.
- Arquitectura multi-tenant con base maestra y bases tenant.
- Provisionamiento de empresas y administrador MASTER.
- Spring Security + JWT + refresh tokens.
- Claims JWT: `userId`, `empresaId`, `rol`, `tipo`.
- BCrypt y validación de password.
- Roles ADMIN, FACTURACION, COBRANZAS, BI y MASTER.
- DTOs, validaciones y `@RestControllerAdvice`.
- Más de 7 excepciones personalizadas.
- Facturas, pagos, notas de crédito, cuentas, servicios y contratos.
- Alertas con aprobación/rechazo y revisores.
- Auditoría de operaciones.
- Dashboard con deuda por antigüedad y cuentas suspendidas.
- Eventos transaccionales para facturas, pagos y alertas.
- Async executor para procesos costosos.
- Validación masiva.
- Monte Carlo.
- Conciliación automática.
- Email HTML asíncrono.
- Chatbot real mediante API LLM compatible con Chat Completions.
- WhatsApp individual y campañas de mora mediante endpoint configurado.
- Upload de archivos a S3.
- Exportación CSV.
- CRUD/actualizaciones principales.
- Swagger/OpenAPI.
- Postman actualizado.
- Test automatizado de Monte Carlo.
- GitHub Actions CI.
- Docker Compose para PostgreSQL local.

## Antes de desplegar

Configurar en Elastic Beanstalk:

```text
AWS_REGION
RDS_MASTER_SECRET_NAME
PROVISIONING_KEY_SECRET_NAME
JWT_SECRET
JWT_EXPIRATION_MS
JWT_REFRESH_EXPIRATION_MS
MASTER_ADMIN_NAME
MASTER_ADMIN_EMAIL
MASTER_ADMIN_PASSWORD
AWS_S3_BUCKET
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_PASSWORD
MAIL_SMTP_AUTH
MAIL_SMTP_STARTTLS
LLM_API_URL
LLM_API_KEY
LLM_MODEL
WHATSAPP_API_URL
WHATSAPP_TOKEN
CORS_ALLOWED_ORIGINS
```

Las integraciones externas pueden dejarse vacías si no se van a probar. `JWT_SECRET` sí es obligatorio.

## Verificación

El entorno de trabajo utilizado para modificar el ZIP no tenía Maven instalado, por lo que el `mvn clean test` final debe ejecutarse en el equipo del proyecto o en GitHub Actions antes de considerar el artefacto compilado como verificado.
