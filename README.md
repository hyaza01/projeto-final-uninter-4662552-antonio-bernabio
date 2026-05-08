# Projeto Final Back-end - Raizes do Nordeste

API REST para operacao de pedidos, pagamento mock, estoque, fidelidade, promocoes e auditoria da rede Raizes do Nordeste.

## Acesso rapido

- Repositorio: https://github.com/hyaza01/projeto-final-uninter-4662552-antonio-bernabio
- Swagger local: http://localhost:8080/swagger-ui/index.html
- Colecao Postman: docs/postman/raizes-do-nordeste.postman_collection.json
- DER: docs/der.md e docs/der.puml
- LGPD: docs/lgpd.md
- Diagramas: docs/casos-de-uso.puml, docs/classes.puml, docs/sequencia-pedido-pagamento.puml

## Descricao

O projeto implementa o fluxo critico de venda:

1. Cliente autentica com JWT.
2. Cliente cria pedido (status inicial AGUARDANDO_PAGAMENTO).
3. Cliente processa pagamento mock (APROVADO ou RECUSADO).
4. Em caso de aprovacao, estoque baixa, auditoria registra e fidelidade pontua com consentimento.
5. Time operacional evolui status do pedido ate ENTREGUE.

## Tecnologias

- Java 21
- Spring Boot 3
- Spring Web
- Spring Validation
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Swagger/OpenAPI (springdoc)
- JUnit 5 + MockMvc + Embedded PostgreSQL
- Postman

## Requisitos

- JDK 21
- Docker e Docker Compose
- Maven Wrapper (ja incluido)

## Estrutura de pastas

```text
src/main/java/br/com/raizesdonordeste/backend
  api/
    controllers/
    dto/
    exception/
  application/
    services/
  domain/
    enums/
    model/
  infrastructure/
    audit/
    config/
    payment/
    persistence/
    security/

src/main/resources/
  application.yml
  db/migration/

docs/
  der.md
  der.puml
  casos-de-uso.puml
  classes.puml
  sequencia-pedido-pagamento.puml
  lgpd.md
  postman/raizes-do-nordeste.postman_collection.json
```

## Variaveis de ambiente

A aplicacao usa as variaveis abaixo (com fallback em application.yml):

- DB_HOST
- DB_PORT
- DB_NAME
- DB_USER
- DB_PASSWORD
- APP_PORT
- JWT_SECRET
- JWT_EXPIRATION_MINUTES
- SEED_TEST_USERS_ENABLED

## Subindo banco com Docker Compose

```bash
docker compose up -d
```

## Executando migrations

As migrations Flyway rodam automaticamente ao iniciar a API.

Arquivos principais:

- src/main/resources/db/migration/V1__create_core_schema.sql
- src/main/resources/db/migration/V2__seed_initial_data.sql
- src/main/resources/db/migration/V3__add_final_fields_for_promocao_auditoria_fidelidade.sql

## Iniciando a API

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Swagger

- UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Usuarios de teste

Usuarios seed (quando SEED_TEST_USERS_ENABLED=true):

- admin@raizes.local (ADMIN)
- gerente@raizes.local (GERENTE)
- cliente@raizes.local (CLIENTE)

Senha de teste:

- Os hashes seed sao placeholders e nao devem ser usados para login direto.
- Para execucao funcional, registre usuarios via endpoint /api/v1/auth/register.

## Endpoints principais

### Auth

- POST /api/v1/auth/register
- POST /api/v1/auth/login
- GET /api/v1/auth/me

### Catalogo e unidades

- GET /api/v1/catalogo/produtos
- GET /api/v1/catalogo/produtos/{produtoId}
- GET /api/v1/unidades/{unidadeId}/cardapio

### Estoque

- GET /api/v1/estoque/unidades/{unidadeId}
- GET /api/v1/estoque/unidades/{unidadeId}/produtos/{produtoId}
- POST /api/v1/estoque/movimentacoes
- GET /api/v1/estoque/{estoqueId}/movimentacoes

### Pedidos

- POST /api/v1/pedidos
- GET /api/v1/pedidos/me
- GET /api/v1/pedidos/me/{pedidoId}
- GET /api/v1/pedidos?canalPedido=&status=&unidadeId=&clienteId=&page=&limit=&sort=
- GET /api/v1/pedidos/unidade/{unidadeId}
- PATCH /api/v1/pedidos/{pedidoId}/status

### Pagamentos

- POST /api/v1/pedidos/{pedidoId}/pagamentos/mock
- GET /api/v1/pagamentos/{pagamentoId}
- POST /api/v1/pedidos/{pedidoId}/pagamento (legado)

### Fidelidade

- GET /api/v1/fidelidade/me
- GET /api/v1/fidelidade/me/historico
- PATCH /api/v1/fidelidade/me/consentimento
- GET /api/v1/clientes/{clienteId}/fidelidade/saldo
- GET /api/v1/clientes/{clienteId}/fidelidade/historico
- PATCH /api/v1/clientes/{clienteId}/fidelidade/consentimento

### Promocoes

- POST /api/v1/promocoes
- GET /api/v1/promocoes
- PATCH /api/v1/promocoes/{id}/status

### Auditoria

- GET /api/v1/auditorias?entidade=&entidadeId=&acao=&usuarioId=&page=&limit=

### Health

- GET /api/v1/health

## Fluxo principal implementado

1. Criacao de pedido exige canalPedido, unidade valida, itens validos e estoque suficiente.
2. Pedido e criado em AGUARDANDO_PAGAMENTO, sem baixa de estoque nesse momento.
3. Pagamento mock registra payload de envio/retorno, codigo mock e status.
4. Pagamento APROVADO:
   - Pedido vai para PAGAMENTO_APROVADO.
   - Estoque baixa.
   - Movimento de estoque e auditoria sao registrados.
   - Pontos de fidelidade sao gerados se houver consentimento ativo.
5. Pagamento RECUSADO:
   - Pedido vai para PAGAMENTO_RECUSADO.
   - Estoque nao e alterado.
   - Auditoria e registrada.

## Regras de negocio relevantes

### Pagamento mock

- Nao ha integracao de pagamento real.
- Duplicidade e bloqueada.
- Pedido CANCELADO, ENTREGUE ou ja APROVADO nao pode ser pago.
- Request invalido retorna 422.

### Canal de pedido

- Campo obrigatorio na criacao.
- Filtro disponivel em listagens operacionais.

### Estoque

- Saida maior que saldo retorna 409.
- Quantidade <= 0 em movimentacao manual retorna 422.
- Cancelamento de pedido aprovado estorna estoque.

### Fidelidade

- Regra simples: 1 ponto por R$ 1,00 (inteiro) em pedido aprovado.
- Sem consentimento ativo, nao gera pontos.
- Alteracao de consentimento registra auditoria e timestamp.

### Promocoes

- Percentual entre 0 e 100.
- Aplicacao automatica no calculo do pedido quando ativa e dentro da vigencia.
- Status de promocao pode ser alterado por endpoint dedicado.

### Auditoria

Acoes registradas:

- LOGIN_REALIZADO
- CRIACAO_PEDIDO
- PAGAMENTO_APROVADO
- PAGAMENTO_RECUSADO
- ALTERACAO_STATUS_PEDIDO
- CANCELAMENTO_PEDIDO
- MOVIMENTACAO_ESTOQUE
- ALTERACAO_CONSENTIMENTO_FIDELIDADE
- CRIACAO_PROMOCAO
- ALTERACAO_STATUS_PROMOCAO

## Padrão de erro da API

Formato:

```json
{
  "error": "CODIGO_DO_ERRO",
  "message": "Mensagem legivel",
  "details": [
    {
      "field": "campo",
      "issue": "problema"
    }
  ],
  "timestamp": "2026-05-08T12:00:00Z",
  "path": "/api/v1/pedidos",
  "requestId": "uuid"
}
```

Mapeamentos principais:

- 400: requisicao invalida
- 401: nao autenticado
- 403: sem permissao
- 404: recurso inexistente
- 409: conflito de regra de negocio
- 422: validacao de campo
- 500: erro inesperado

## Postman - importacao e ordem recomendada de testes

Colecao final:

- docs/postman/raizes-do-nordeste.postman_collection.json

Ordem recomendada:

1. T01 login valido
2. T02 consultar cardapio por unidade
3. T03 criar pedido com canalPedido APP
4. T04 pagamento mock aprovado
5. T05, T06, T07 transicoes operacionais
6. T08 filtro por canalPedido
7. T09 saldo de fidelidade
8. T10 auditoria por gerente/admin
9. T11 a T18 cenarios negativos

## Comandos para rodar testes

Linux/macOS:

```bash
./mvnw clean test
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean test
```

## Diagramas e evidencias

- DER: docs/der.md e docs/der.puml
- Casos de uso: docs/casos-de-uso.puml
- Classes: docs/classes.puml
- Sequencia pedido/pagamento: docs/sequencia-pedido-pagamento.puml
- LGPD: docs/lgpd.md

Observacao:

- Os arquivos PNG dos diagramas nao foram gerados automaticamente neste commit.
- Os arquivos PlantUML estao prontos para renderizacao local.

## Decisoes documentadas

- Cardapio por unidade exibe produtos com estoque zero marcados como indisponivel, mantendo visibilidade do catalogo da unidade.
- Pagamento permanece 100% mock, sem armazenamento de dados reais de cartao.
