# Projeto Final Back-end - Raízes do Nordeste

API REST para operação de pedidos, pagamento mock, estoque, fidelidade, promoções e auditoria da rede **Raízes do Nordeste**.

O projeto foi desenvolvido com foco em API REST, persistência em banco relacional, autenticação JWT, autorização por perfis, documentação Swagger/OpenAPI, testes automatizados e evidências reproduzíveis.

---

## Acesso rápido

- Repositório: https://github.com/hyaza01/projeto-final-uninter-4662552-antonio-bernabio
- Health check: http://localhost:8080/api/v1/health
- Swagger local: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Coleção Postman: `docs/postman/raizes-do-nordeste.postman_collection.json`
- DER: `docs/der.md`, `docs/der.puml` e `docs/der.png`
- LGPD: `docs/lgpd.md`
- Diagramas: `docs/casos-de-uso.png`, `docs/classes.png` e `docs/sequencia-pedido-pagamento.png`

---

## Descrição

O sistema implementa uma API Back-end para uma rede de lanchonetes em expansão, contemplando atendimento multicanal, gestão de pedidos, controle de estoque por unidade, pagamento mock, fidelidade, promoções e auditoria.

O fluxo principal implementado é:

1. Cliente autentica com JWT.
2. Cliente consulta o cardápio por unidade.
3. Cliente cria pedido informando `canalPedido`.
4. Pedido é criado com status inicial `AGUARDANDO_PAGAMENTO`.
5. Cliente solicita pagamento mock.
6. Pagamento pode ser aprovado ou recusado.
7. Em caso de aprovação, o sistema baixa estoque, registra movimentação, gera auditoria e pontua fidelidade se houver consentimento.
8. Time operacional evolui o status do pedido até `ENTREGUE`.

---

## Tecnologias utilizadas

- Java 21
- Spring Boot 3
- Spring Web
- Spring Validation
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Swagger/OpenAPI com Springdoc
- JUnit 5
- MockMvc
- Embedded PostgreSQL para testes
- Docker Compose
- Postman

---

## Requisitos para execução

Antes de iniciar, instale:

- Git
- JDK 21
- Docker
- Docker Compose
- Postman, opcional para executar a coleção de testes

O Maven não precisa ser instalado manualmente, pois o projeto já inclui Maven Wrapper.

---

## Preparação do ambiente

No Windows e macOS, mantenha o Docker Desktop aberto antes de executar os comandos do projeto.

Para confirmar a instalação das ferramentas, rode:

```bash
git --version
java -version
docker --version
docker compose version
```

Para verificar o Maven Wrapper no Linux/macOS:

```bash
./mvnw --version
```

Para verificar o Maven Wrapper no Windows PowerShell:

```powershell
.\mvnw.cmd --version
```

Caso algum comando não seja reconhecido, instale ou configure a ferramenta correspondente antes de continuar.

---

## Como executar o projeto localmente

### Windows PowerShell

```powershell
git clone https://github.com/hyaza01/projeto-final-uninter-4662552-antonio-bernabio.git
cd projeto-final-uninter-4662552-antonio-bernabio
copy .env.example .env
docker compose config
docker compose up -d
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
git clone https://github.com/hyaza01/projeto-final-uninter-4662552-antonio-bernabio.git
cd projeto-final-uninter-4662552-antonio-bernabio
cp .env.example .env
docker compose config
docker compose up -d
./mvnw clean test
./mvnw spring-boot:run
```

Após iniciar a API, acesse:

```txt
http://localhost:8080/api/v1/health
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/v3/api-docs
```

---

## Variáveis de ambiente

O arquivo `.env.example` contém as variáveis necessárias para execução local.

Exemplo:

```env
APP_PORT=8080
SPRING_PROFILES_ACTIVE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=raizes_db
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=raizes_do_nordeste_jwt_secret_com_32_caracteres_minimo
JWT_EXPIRATION_MINUTES=60
SEED_TEST_USERS_ENABLED=true
```

Para executar localmente, copie o arquivo de exemplo:

### Windows PowerShell

```powershell
copy .env.example .env
```

### Linux/macOS

```bash
cp .env.example .env
```

---

## Banco de dados com Docker Compose

Para subir o PostgreSQL:

```bash
docker compose up -d
```

Para verificar se o container está rodando:

```bash
docker compose ps
```

Para validar o arquivo `docker-compose.yml`:

```bash
docker compose config
```

Para resetar completamente o banco local, removendo os dados persistidos:

```bash
docker compose down -v
docker compose up -d
```

---

## Migrations e seed

As migrations Flyway são executadas automaticamente ao iniciar a API.

Arquivos principais:

```txt
src/main/resources/db/migration/V1__create_core_schema.sql
src/main/resources/db/migration/V2__seed_initial_data.sql
src/main/resources/db/migration/V3__add_final_fields_for_promocao_auditoria_fidelidade.sql
src/main/resources/db/migration/V4__fix_seed_test_users_passwords.sql
```

A migration `V4__fix_seed_test_users_passwords.sql` garante usuários de teste com senhas BCrypt válidas para execução local e validação dos fluxos protegidos.

---

## Iniciando a API

### Windows PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
./mvnw spring-boot:run
```

A aplicação ficará disponível em:

```txt
http://localhost:8080
```

Também é possível executar via JAR.

### Windows PowerShell

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target\raizes-backend-0.0.1-SNAPSHOT.jar
```

### Linux/macOS

```bash
./mvnw clean package -DskipTests
java -jar target/raizes-backend-0.0.1-SNAPSHOT.jar
```

---

## Health check

Endpoint público para verificar se a API está no ar:

```txt
GET /api/v1/health
```

URL local:

```txt
http://localhost:8080/api/v1/health
```

---

## Swagger/OpenAPI

Swagger UI:

```txt
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```txt
http://localhost:8080/v3/api-docs
```

---

## Usuários de teste

Os usuários abaixo são criados automaticamente pelas migrations/seed.

| Perfil | E-mail | Senha |
|---|---|---|
| ADMIN | admin@raizes.local | Admin@123 |
| GERENTE | gerente@raizes.local | Gerente@123 |
| ATENDENTE | atendente@raizes.local | Atendente@123 |
| COZINHA | cozinha@raizes.local | Cozinha@123 |
| CLIENTE | cliente@raizes.local | Cliente@123 |

Observação: o endpoint público `/api/v1/auth/register` cria usuários com perfil `CLIENTE` por segurança. Usuários operacionais são disponibilizados via seed/migration para permitir testes das rotas protegidas por perfil.

---

## Estrutura de pastas

```txt
src/main/java/br/com/raizesdonordeste/backend/
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
  der.png
  casos-de-uso.puml
  casos-de-uso.png
  classes.puml
  classes.png
  sequencia-pedido-pagamento.puml
  sequencia-pedido-pagamento.png
  lgpd.md
  postman/
    raizes-do-nordeste.postman_collection.json

src/test/
```

---

## Endpoints principais

### Auth

| Método | Rota | Finalidade |
|---|---|---|
| POST | `/api/v1/auth/register` | Cadastrar usuário cliente |
| POST | `/api/v1/auth/login` | Autenticar usuário e retornar token JWT |
| GET | `/api/v1/auth/me` | Consultar dados do usuário autenticado |

### Catálogo e unidades

| Método | Rota | Finalidade |
|---|---|---|
| GET | `/api/v1/catalogo/produtos` | Listar produtos do catálogo |
| GET | `/api/v1/catalogo/produtos/{produtoId}` | Consultar produto por identificador |
| GET | `/api/v1/unidades/{unidadeId}/cardapio` | Consultar cardápio de uma unidade |

### Estoque

| Método | Rota | Finalidade |
|---|---|---|
| GET | `/api/v1/estoque/unidades/{unidadeId}` | Consultar estoque de uma unidade |
| GET | `/api/v1/estoque/unidades/{unidadeId}/produtos/{produtoId}` | Consultar estoque de um produto em uma unidade |
| POST | `/api/v1/estoque/movimentacoes` | Registrar entrada ou saída manual de estoque |
| GET | `/api/v1/estoque/{estoqueId}/movimentacoes` | Consultar histórico de movimentações de um estoque |

### Pedidos

| Método | Rota | Finalidade |
|---|---|---|
| POST | `/api/v1/pedidos` | Criar pedido |
| GET | `/api/v1/pedidos/me` | Listar pedidos do cliente autenticado |
| GET | `/api/v1/pedidos/me/{pedidoId}` | Consultar pedido próprio por identificador |
| GET | `/api/v1/pedidos?canalPedido=&status=&unidadeId=&clienteId=&page=&limit=&sort=` | Consultar pedidos com filtros operacionais |
| GET | `/api/v1/pedidos/unidade/{unidadeId}` | Consultar pedidos de uma unidade |
| PATCH | `/api/v1/pedidos/{pedidoId}/status` | Atualizar status operacional do pedido |

### Pagamentos

| Método | Rota | Finalidade |
|---|---|---|
| POST | `/api/v1/pedidos/{pedidoId}/pagamentos/mock` | Processar pagamento simulado |
| GET | `/api/v1/pagamentos/{pagamentoId}` | Consultar pagamento por identificador |

### Fidelidade

| Método | Rota | Finalidade |
|---|---|---|
| GET | `/api/v1/fidelidade/me` | Consultar saldo de fidelidade do cliente autenticado |
| GET | `/api/v1/fidelidade/me/historico` | Consultar histórico de fidelidade do cliente autenticado |
| PATCH | `/api/v1/fidelidade/me/consentimento` | Atualizar consentimento de fidelidade do cliente autenticado |
| GET | `/api/v1/clientes/{clienteId}/fidelidade/saldo` | Consultar saldo de fidelidade por cliente |
| GET | `/api/v1/clientes/{clienteId}/fidelidade/historico` | Consultar histórico de fidelidade por cliente |
| PATCH | `/api/v1/clientes/{clienteId}/fidelidade/consentimento` | Atualizar consentimento de fidelidade por cliente |

### Promoções

| Método | Rota | Finalidade |
|---|---|---|
| POST | `/api/v1/promocoes` | Criar promoção |
| GET | `/api/v1/promocoes` | Listar promoções |
| PATCH | `/api/v1/promocoes/{id}/status` | Alterar status de promoção |

### Auditoria

| Método | Rota | Finalidade |
|---|---|---|
| GET | `/api/v1/auditorias?entidade=&entidadeId=&acao=&usuarioId=&page=&limit=` | Consultar registros de auditoria com filtros |

### Health

| Método | Rota | Finalidade |
|---|---|---|
| GET | `/api/v1/health` | Verificar se a API está no ar |

---

## Fluxo principal implementado

### 1. Login do cliente

```txt
POST /api/v1/auth/login
```

Exemplo de request:

```json
{
  "email": "cliente@raizes.local",
  "senha": "Cliente@123"
}
```

A resposta retorna um token JWT que deve ser usado nas próximas requisições protegidas.

---

### 2. Consulta de cardápio por unidade

```txt
GET /api/v1/unidades/1/cardapio
```

Produtos sem estoque podem aparecer como indisponíveis, mantendo a visibilidade do catálogo da unidade.

---

### 3. Criação de pedido

```txt
POST /api/v1/pedidos
```

Exemplo de request:

```json
{
  "unidadeId": 1,
  "canalPedido": "APP",
  "formaPagamento": "MOCK",
  "itens": [
    {
      "produtoId": 1,
      "quantidade": 1
    }
  ]
}
```

Regras aplicadas:

- `canalPedido` é obrigatório.
- Unidade deve existir.
- Produto deve existir.
- Estoque deve ser suficiente.
- Pedido nasce como `AGUARDANDO_PAGAMENTO`.
- Estoque ainda não é baixado na criação.

---

### 4. Pagamento mock

```txt
POST /api/v1/pedidos/{pedidoId}/pagamentos/mock
```

O contrato exato do request pode ser consultado no Swagger e na coleção Postman. O pagamento mock registra payload de envio, payload de retorno, código de transação simulado e status do pagamento.

Resultado esperado quando aprovado:

- Pagamento registrado.
- Pedido atualizado para `PAGAMENTO_APROVADO`.
- Estoque baixado.
- Movimento de estoque registrado.
- Auditoria registrada.
- Pontos de fidelidade gerados se houver consentimento.

Resultado esperado quando recusado:

- Pagamento registrado como recusado.
- Pedido atualizado para `PAGAMENTO_RECUSADO`.
- Estoque não é alterado.
- Auditoria registrada.

---

### 5. Evolução operacional do pedido

Rotas de alteração de status exigem perfil operacional.

```txt
PATCH /api/v1/pedidos/{pedidoId}/status
```

Perfis operacionais esperados:

```txt
ADMIN
GERENTE
ATENDENTE
COZINHA
```

---

## Regras de negócio relevantes

### Pagamento mock

- Não há integração real de pagamento.
- O pagamento é totalmente simulado.
- Payload de envio e retorno são registrados.
- Pagamento duplicado é bloqueado.
- Pedido `CANCELADO`, `ENTREGUE` ou já aprovado não pode ser pago novamente.
- Pagamento aprovado baixa estoque.
- Pagamento recusado não altera estoque.

### Canal de pedido

O pedido possui o campo `canalPedido`, com valores como:

```txt
APP
TOTEM
BALCAO
PICKUP
WEB
```

Esse campo é obrigatório na criação do pedido e pode ser usado como filtro em consultas operacionais.

### Estoque

- Estoque é controlado por unidade e produto.
- Entrada aumenta saldo.
- Saída diminui saldo.
- Saída maior que saldo retorna conflito de regra de negócio.
- Movimentação manual registra histórico e auditoria.
- Cancelamento de pedido aprovado pode estornar estoque.

### Fidelidade

- Regra simples: 1 ponto por R$ 1,00 inteiro em pedido aprovado.
- Sem consentimento ativo, pontos não são gerados.
- Alteração de consentimento registra auditoria e timestamp.

### Promoções

- Percentual deve estar entre 0 e 100.
- Promoção ativa e dentro da vigência pode ser aplicada ao pedido.
- Promoções podem ser gerais, por unidade ou por produto.
- Alteração de status de promoção gera auditoria.

### Auditoria

Ações sensíveis registradas:

```txt
LOGIN_REALIZADO
CRIACAO_PEDIDO
PAGAMENTO_APROVADO
PAGAMENTO_RECUSADO
ALTERACAO_STATUS_PEDIDO
CANCELAMENTO_PEDIDO
MOVIMENTACAO_ESTOQUE
ALTERACAO_CONSENTIMENTO_FIDELIDADE
CRIACAO_PROMOCAO
ALTERACAO_STATUS_PROMOCAO
```

---

## Segurança e perfis

A API usa autenticação JWT e autorização por perfis.

Rotas públicas principais:

```txt
GET  /api/v1/health
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/catalogo/produtos
GET  /api/v1/catalogo/produtos/{produtoId}
GET  /api/v1/unidades/{unidadeId}/cardapio
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

Rotas protegidas exigem token JWT.

Exemplos:

- Cliente pode criar e consultar seus próprios pedidos.
- Perfis operacionais podem consultar pedidos e atualizar status.
- Estoque, promoções e auditoria exigem perfis autorizados.
- Auditoria é restrita a perfis administrativos/gerenciais.

---

## LGPD e privacidade

A documentação LGPD está em:

```txt
docs/lgpd.md
```

Resumo dos controles aplicados:

- Senhas armazenadas com hash BCrypt.
- Autenticação via JWT.
- Autorização por perfis.
- Respostas da API não expõem senha ou hash.
- Programa de fidelidade depende de consentimento.
- Auditoria registra ações sensíveis.
- Pagamento é mock e não coleta dados reais de cartão.

---

## Padrão de erro da API

Formato padrão:

```json
{
  "error": "CODIGO_DO_ERRO",
  "message": "Mensagem legível",
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

| Status | Significado |
|---|---|
| 400 | Requisição inválida |
| 401 | Não autenticado |
| 403 | Sem permissão |
| 404 | Recurso inexistente |
| 409 | Conflito de regra de negócio |
| 422 | Validação de campo |
| 500 | Erro inesperado |

---

## Testes automatizados

### Windows PowerShell

```powershell
.\mvnw.cmd clean test
```

### Linux/macOS

```bash
./mvnw clean test
```

Resultado esperado:

```txt
BUILD SUCCESS
```

Na validação local, foram executados testes de autenticação, catálogo, estoque, pedidos, pagamento, fidelidade, promoções, auditoria e validação de schema.

---

## Postman

Coleção final:

```txt
docs/postman/raizes-do-nordeste.postman_collection.json
```

### Importação

1. Abra o Postman.
2. Clique em **Import**.
3. Selecione o arquivo `docs/postman/raizes-do-nordeste.postman_collection.json`.
4. Configure a variável:

```txt
baseUrl = http://localhost:8080
```

### Ordem recomendada de execução

```txt
T01  - Login CLIENTE válido
T01B - Login GERENTE válido
T02  - Consultar cardápio por unidade
T03  - Criar pedido com canalPedido APP
T04  - Pagamento mock aprovado
T05  - Atualizar pedido para EM_PREPARO
T06  - Atualizar pedido para PRONTO
T07  - Atualizar pedido para ENTREGUE
T08  - Consultar pedidos por canalPedido
T09  - Consultar fidelidade própria
T10  - Consultar auditoria como GERENTE ou ADMIN
T11  - Login inválido
T12  - Rota protegida sem token
T13  - Perfil sem permissão
T14  - Pedido sem canalPedido
T15  - Pedido com estoque insuficiente
T16  - Pagamento duplicado
T17  - Transição inválida de status
T18  - Saída de estoque maior que saldo
```

---

## Diagramas e evidências

Arquivos disponíveis na pasta `docs`:

```txt
docs/der.md
docs/der.puml
docs/der.png

docs/casos-de-uso.puml
docs/casos-de-uso.png

docs/classes.puml
docs/classes.png

docs/sequencia-pedido-pagamento.puml
docs/sequencia-pedido-pagamento.png

docs/lgpd.md
docs/postman/raizes-do-nordeste.postman_collection.json
```

---

## DER / modelo de dados

O modelo contempla as principais entidades:

```txt
Usuario
Cliente
Unidade
Produto
Estoque
MovimentoEstoque
Pedido
PedidoItem
Pagamento
FidelidadeHistorico
Promocao
Auditoria
```

Relacionamentos principais:

- Um usuário pode estar vinculado a um cliente.
- Um cliente pode realizar vários pedidos.
- Uma unidade possui estoque próprio por produto.
- Um pedido pertence a uma unidade e possui vários itens.
- Um item de pedido referencia um produto.
- Um pedido pode possuir um pagamento mock.
- Movimentações de estoque registram entradas, saídas e estornos.
- Fidelidade registra histórico de pontos por cliente.
- Promoções podem ser associadas a unidade, produto ou regra geral.
- Auditoria registra ações sensíveis do sistema.

---

## Decisões técnicas documentadas

- O cadastro público cria usuários com perfil `CLIENTE`, evitando criação indevida de usuários administrativos.
- Usuários operacionais são disponibilizados por migration/seed.
- O pagamento permanece 100% mock, sem armazenamento de dados reais de cartão.
- O pedido nasce como `AGUARDANDO_PAGAMENTO`.
- A baixa de estoque ocorre somente após pagamento aprovado.
- Produtos sem estoque podem aparecer como indisponíveis no cardápio da unidade.
- A API usa padrão único de erro JSON.
- A documentação Swagger/OpenAPI reflete os endpoints principais da aplicação.

---

## Solução de problemas comuns

### Docker não reconhecido

Verifique se o Docker Desktop está instalado e em execução:

```bash
docker --version
docker compose version
```

### Porta 5432 ocupada

Altere `DB_PORT` no arquivo `.env` ou pare o serviço local que está usando a porta.

### Banco com dados antigos

Execute:

```bash
docker compose down -v
docker compose up -d
```

### API não sobe

Verifique se o banco está ativo:

```bash
docker compose ps
```

Depois tente:

```bash
./mvnw clean test
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

---

## Validação recomendada

Para validar o funcionamento completo do projeto em ambiente local:

```txt
1. Clonar o repositório.
2. Copiar .env.example para .env.
3. Subir PostgreSQL com Docker Compose.
4. Rodar os testes automatizados.
5. Iniciar a API.
6. Abrir o health check.
7. Abrir o Swagger.
8. Importar a coleção Postman.
9. Executar o fluxo Pedido → Pagamento mock → Atualização de status.
10. Executar cenários negativos de autenticação, autorização, validação e regra de negócio.
```
