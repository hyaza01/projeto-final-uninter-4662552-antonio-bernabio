# Projeto Final Back-end - Raizes do Nordeste

API REST para a rede de lanchonetes Raizes do Nordeste.

## Etapa atual

Etapa 3 concluida: autenticacao JWT, controle de perfis e rotas protegidas.

## Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation
- JWT
- Swagger/OpenAPI (dependencia preparada)
- JUnit

## Estrutura de pacotes

```
src/main/java/br/com/raizesdonordeste/backend/
  domain/
    model/
    enums/
    rules/
  application/
    usecases/
    services/
  infrastructure/
    persistence/
    security/
    payment/
    audit/
    config/
  api/
    controllers/
    dto/
    exception/
```

## Banco de dados e migrations

- Banco: PostgreSQL
- Engine de migration: Flyway
- Migration principal: `src/main/resources/db/migration/V1__create_core_schema.sql`
- Seed inicial (opcional): `src/main/resources/db/migration/V2__seed_initial_data.sql`

## Autenticacao e autorizacao (Etapa 3)

### Endpoints

- `POST /api/v1/auth/register` (publico)
- `POST /api/v1/auth/login` (publico)
- `GET /api/v1/auth/me` (autenticado)
- `GET /api/v1/private/ping` (autenticado)
- `GET /api/v1/admin/ping` (somente `ADMIN`)

### Usuarios seed de teste

- `admin@raizes.local` / `Admin@123` (`ADMIN`)
- `gerente@raizes.local` / `Gerente@123` (`GERENTE`)
- `atendente@raizes.local` / `Atendente@123` (`ATENDENTE`)
- `cozinha@raizes.local` / `Cozinha@123` (`COZINHA`)
- `cliente@raizes.local` / `Cliente@123` (`CLIENTE`)

## Como executar localmente

1. Copie .env.example para .env e ajuste valores se necessario.
2. Suba o banco local:

```bash
docker compose up -d
```

3. Inicie a aplicacao:

```bash
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Validacoes recomendadas da Etapa 2

1. Executar build:

```bash
./mvnw clean test
```

2. Confirmar criacao das tabelas (apos startup com banco ativo):

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

3. Confirmar chaves estrangeiras:

```sql
SELECT tc.table_name,
       kcu.column_name,
       ccu.table_name AS referenced_table,
       ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name, kcu.column_name;
```

## Validacoes recomendadas da Etapa 3

1. Cadastro de usuario:

```http
POST /api/v1/auth/register
```

2. Login e token JWT:

```http
POST /api/v1/auth/login
```

3. Rota protegida sem token deve retornar 401:

```http
GET /api/v1/private/ping
```

4. Rota de admin com usuario cliente deve retornar 403:

```http
GET /api/v1/admin/ping
```

## DER preliminar

Consulte o arquivo `docs/der-preliminar.md` para uma visao textual dos relacionamentos mapeados nesta etapa.

## Endpoint de saude

- `GET /api/v1/health`

Resposta esperada:

```json
{
  "status": "UP",
  "service": "raizes-backend",
  "timestamp": "2026-05-02T12:00:00Z"
}
```

## Observacao importante da Etapa 3

A aplicacao permanece exigindo PostgreSQL ativo em `localhost:5432` (ou variaveis de ambiente customizadas). O acesso autenticado agora requer JWT valido no header `Authorization: Bearer <token>`.