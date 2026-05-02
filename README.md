# Projeto Final Back-end - Raizes do Nordeste

API REST para a rede de lanchonetes Raizes do Nordeste.

## Etapa atual

Etapa 1 concluida: estrutura inicial do projeto.

## Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation
- JWT (dependencias preparadas)
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

## Como executar localmente

1. Copie .env.example para .env e ajuste valores se necessario.
2. Suba o banco local (opcional nesta etapa):

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

## Observacao importante da Etapa 1

As configuracoes de DataSource/JPA/Flyway estao temporariamente desativadas em `application.yml` para permitir a inicializacao sem banco enquanto o dominio e as migrations ainda nao foram implementados. Isso sera habilitado na Etapa 2.