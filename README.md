# Projeto Final Back-end - Raizes do Nordeste

API REST para a rede de lanchonetes Raizes do Nordeste.

## Etapa atual

Etapa 4 concluida: catalogo publico de produtos e consulta de estoque por perfis internos.

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

## Catalogo e estoque (Etapa 4)

### Endpoints

- `GET /api/v1/catalogo/produtos` (publico)
- `GET /api/v1/catalogo/produtos/{produtoId}` (publico)
- `GET /api/v1/estoque/unidades/{unidadeId}` (`ADMIN`, `GERENTE`, `ATENDENTE`, `COZINHA`)
- `GET /api/v1/estoque/unidades/{unidadeId}/produtos/{produtoId}` (`ADMIN`, `GERENTE`, `ATENDENTE`, `COZINHA`)

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

## Validacoes recomendadas da Etapa 4

1. Catalogo publico sem token:

```http
GET /api/v1/catalogo/produtos
```

2. Filtro por categoria:

```http
GET /api/v1/catalogo/produtos?categoria=LANCHE
```

3. Estoque sem token deve retornar 401:

```http
GET /api/v1/estoque/unidades/1/produtos/1
```

4. Estoque com token de CLIENTE deve retornar 403.

5. Estoque com token de GERENTE deve retornar 200.

## Servico de commit agendado para 18h

Para agendar commit automatico local somente as 18h de hoje:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\agendar-commit-hoje-18h.ps1
```

Script executado no horario:

- `.\tools\commit-agendado-18h.ps1`

Observacao: o agendamento faz `git add .` e `git commit` somente se houver alteracoes pendentes.

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

## Observacao importante da Etapa 4

A aplicacao permanece exigindo PostgreSQL ativo em `localhost:5432` (ou variaveis de ambiente customizadas). O catalogo e publico, mas os endpoints de estoque exigem JWT valido no header `Authorization: Bearer <token>` com perfil interno autorizado.