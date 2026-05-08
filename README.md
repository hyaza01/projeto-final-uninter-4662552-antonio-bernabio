# Projeto Final Back-end - Raizes do Nordeste

API REST para operacao de pedidos, pagamento mock, estoque, auditoria e fidelidade da rede Raizes do Nordeste.

## Tecnologias

- Java 21
- Spring Boot 3.3.5
- Spring Web, Validation, Security, Data JPA, Actuator
- PostgreSQL
- Flyway
- JWT (JJWT)
- OpenAPI/Swagger (springdoc)
- JUnit 5 + MockMvc + Embedded PostgreSQL

## Arquitetura

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

## Funcionalidades implementadas

- Cadastro, login e autenticacao JWT.
- Autorizacao por perfil (`CLIENTE`, `ATENDENTE`, `COZINHA`, `GERENTE`, `ADMIN`).
- Catalogo publico de produtos e consulta de estoque por unidade.
- Criacao de pedidos com validacoes de unidade/produto/estoque e `canalPedido`.
- Fluxo de pagamento mock (`APROVADO`/`RECUSADO`) por endpoint dedicado.
- Baixa de estoque apenas com pagamento aprovado.
- Operacao de status de pedido com regras de transicao.
- Estorno de estoque no cancelamento apenas quando ja houve baixa previa.
- Fidelidade com consentimento LGPD e historico de pontos.
- Auditoria de eventos sensiveis (pedido, status, pagamento).
- Filtros e paginacao de pedidos para cliente e operacao interna.
- Swagger/OpenAPI e payload padrao de erro.

## Fluxo principal do pedido

1. Cliente cria pedido (`AGUARDANDO_PAGAMENTO`).
2. Cliente chama endpoint de pagamento mock.
3. Se aprovado: pedido vai para `PAGAMENTO_APROVADO`, baixa estoque e gera pontos (se consentimento ativo).
4. Se recusado: pedido vai para `PAGAMENTO_RECUSADO` e estoque nao muda.
5. Equipe interna evolui status (`RECEBIDO`, `EM_PREPARO`, `PRONTO`, `ENTREGUE`) ou cancela.

## Setup local

### 1) Configurar variaveis

```bash
cp .env.example .env
```

No Windows PowerShell, crie o arquivo `.env` com base em `.env.example`.

### 2) Subir PostgreSQL

```bash
docker compose up -d
```

### 3) Subir a aplicacao

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

### 4) Executar testes

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## Swagger e observabilidade

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `GET /api/v1/health`
- Actuator health: `GET /actuator/health`

## Endpoints principais

Autenticacao:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`

Catalogo e estoque:

- `GET /api/v1/catalogo/produtos`
- `GET /api/v1/catalogo/produtos/{produtoId}`
- `GET /api/v1/estoque/unidades/{unidadeId}`
- `GET /api/v1/estoque/unidades/{unidadeId}/produtos/{produtoId}`

Pedidos e pagamento:

- `POST /api/v1/pedidos`
- `GET /api/v1/pedidos/me`
- `GET /api/v1/pedidos/me/{pedidoId}`
- `GET /api/v1/pedidos`
- `GET /api/v1/pedidos/unidade/{unidadeId}`
- `PATCH /api/v1/pedidos/{pedidoId}/status`
- `POST /api/v1/pedidos/{pedidoId}/pagamentos/mock`
- `GET /api/v1/pagamentos/{pagamentoId}`
- `POST /api/v1/pedidos/{pedidoId}/pagamento` (legado, mantido por compatibilidade)

Fidelidade:

- `GET /api/v1/fidelidade/me`
- `GET /api/v1/fidelidade/me/historico`
- `PATCH /api/v1/fidelidade/me/consentimento`

## Padrao de erro

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Falha de validacao dos campos enviados.",
  "details": [
    {
      "field": "campo",
      "issue": "descricao"
    }
  ],
  "timestamp": "2026-05-07T14:00:00Z",
  "path": "/api/v1/pedidos",
  "requestId": "uuid"
}
```

## Documentacao complementar

- DER preliminar: `docs/der-preliminar.md`
- Diagrama de casos de uso: `docs/diagrama-casos-uso.md`
- Diagrama de classes (dominio): `docs/diagrama-classes.md`
- Diagrama de sequencia (pedido/pagamento): `docs/diagrama-sequencia-pagamento.md`
- Nota tecnica LGPD: `docs/lgpd-nota-tecnica.md`
- Colecao Postman: `docs/raizes-api.postman_collection.json`