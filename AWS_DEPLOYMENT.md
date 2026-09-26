# Recaud.IA - AWS deployment

## Architecture

- Elastic Beanstalk runs the Spring Boot application.
- RDS PostgreSQL hosts `recaudia_master` and tenant databases.
- AWS Secrets Manager stores database and provisioning secrets.
- The Elastic Beanstalk EC2 instance profile provides AWS SDK access.
- `TenantRoutingDataSource` selects the tenant database using `TenantContext`.
- S3 stores uploaded files under a tenant-specific prefix.

## Elastic Beanstalk environment variables

### Required

```text
AWS_REGION=us-east-1
RDS_MASTER_SECRET_NAME=recaudia/rds/master
PROVISIONING_KEY_SECRET_NAME=recaudia/provisioning
JWT_SECRET=<base64-secret-largo-y-aleatorio>
```

`JWT_SECRET` is mandatory in the final backend. Generate a strong Base64 secret, for example with:

```bash
openssl rand -base64 64
```

### JWT

```text
JWT_EXPIRATION_MS=86400000
JWT_REFRESH_EXPIRATION_MS=604800000
```

### MASTER bootstrap

```text
MASTER_ADMIN_NAME=Recaud.IA Master
MASTER_ADMIN_EMAIL=<correo-del-master>
MASTER_ADMIN_PASSWORD=<password-segura>
```

If the MASTER account already exists, bootstrap does not recreate it.

### S3

```text
AWS_S3_BUCKET=<nombre-del-bucket>
```

If omitted, the application uses `recaud-ia-bucket`.

### LLM

```text
LLM_API_URL=https://api.openai.com/v1/chat/completions
LLM_API_KEY=<api-key>
LLM_MODEL=gpt-4o-mini
```

The chatbot endpoint is unavailable as an AI feature until `LLM_API_KEY` is configured.

### WhatsApp

```text
WHATSAPP_API_URL=<endpoint-completo-de-WhatsApp-Cloud-API>
WHATSAPP_TOKEN=<token>
```

The URL must point directly to the WhatsApp Cloud API messages endpoint for the configured phone number.

### SMTP

```text
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<correo>
MAIL_PASSWORD=<app-password>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

### CORS

```text
CORS_ALLOWED_ORIGINS=https://tu-frontend.example,http://localhost:5173
```

Do not commit any of these secret values to GitHub.

## Secrets Manager

### Provisioning secret

Create `recaudia/provisioning` as a JSON secret:

```json
{
  "provisioningKey": "GENERATE_A_LONG_RANDOM_VALUE"
}
```

The value is sent to the provisioning endpoint through the `X-Provisioning-Key` header.

### RDS master secret

Use the secret automatically created or managed by RDS. The application expects at least:

```text
host
port
username
password
dbname
```

The application uses this secret to connect to `recaudia_master` and to provision tenant databases. The database URL is built from the host, port and generated tenant database name.

## IAM permissions

The Elastic Beanstalk EC2 role needs Secrets Manager access to the configured RDS and provisioning secrets, including:

```text
secretsmanager:GetSecretValue
```

For S3 uploads it needs, at minimum, the required object permissions on the Recaud.IA bucket, such as:

```text
s3:PutObject
```

Read/list permissions should only be granted if another backend feature requires them.

## RDS Security Group

Allow TCP port `5432` from the Elastic Beanstalk EC2 Security Group to the RDS Security Group. Do not expose PostgreSQL to `0.0.0.0/0`.

## Deployment

The application listens on:

```text
${PORT:5000}
```

which is compatible with the Elastic Beanstalk nginx proxy used by the current environment.

Before deploying a new build:

1. Configure `JWT_SECRET`.
2. Verify `RDS_MASTER_SECRET_NAME` and the provisioning secret.
3. Verify the RDS Security Group allows traffic from Elastic Beanstalk.
4. Configure CORS for the actual frontend domain.
5. Configure SMTP, LLM, WhatsApp and S3 only when those integrations are going to be tested.
6. Run `mvn clean test` with Java 21.
7. Run `mvn clean package -DskipTests` and deploy the generated JAR.
8. Import `postman_collection.json` and `postman_environment.json` to verify the public API.

Current environment URL used during the project:

```text
http://recaudia-api-env.eba-disas8yb.us-east-1.elasticbeanstalk.com
```

## Multi-tenant provisioning

The MASTER provisioning endpoint creates a tenant record in the master database, creates the PostgreSQL database, registers its datasource, initializes the schema and creates the tenant administrator. Tenant requests subsequently select the tenant database using the `empresaId` from the JWT.

For stronger production isolation, create a separate PostgreSQL role and Secrets Manager secret for each tenant with permissions restricted to that tenant database. The current implementation keeps compatibility with the RDS master credential used by the project environment.
