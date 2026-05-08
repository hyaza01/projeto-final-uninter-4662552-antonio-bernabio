# Plano Técnico para Agente IA - Projeto Final Back-end

**Projeto:** API Back-end para a rede de lanchonetes **Raízes do Nordeste**  
**Objetivo:** implementar uma API REST funcional, segura, documentada e testável, com fluxo crítico de pedido, pagamento mock, status, estoque, fidelização, auditoria e documentação completa.

> **Orientação de uso:** este arquivo deve ser usado como roteiro técnico de implementação por etapas. Cada etapa corresponde a um commit. Ao finalizar uma etapa, validar os critérios daquela etapa, fazer o commit e só depois avançar para a próxima.

---

## 1. Visão geral do produto

A solução deve atender uma rede de lanchonetes com múltiplas unidades e múltiplos canais de pedido. O back-end deve permitir que clientes, atendentes, cozinha, gerentes e administradores interajam com pedidos, produtos, estoque, pagamentos simulados, fidelização e auditoria.

O sistema deve demonstrar domínio técnico de:

- Levantamento e priorização de requisitos.
- Modelagem de domínio e banco de dados.
- Arquitetura em camadas.
- API REST com contratos claros.
- Swagger/OpenAPI.
- Persistência real com migrations.
- Autenticação e autorização.
- Segurança e LGPD.
- Pagamento externo simulado por mock.
- Testes e evidências reproduzíveis.

---

## 2. Stack recomendada

Use preferencialmente:

- Java 21.
- Spring Boot 3.
- Spring Web.
- Spring Security.
- JWT.
- Spring Data JPA.
- PostgreSQL.
- Flyway ou Liquibase.
- Bean Validation.
- Swagger/OpenAPI.
- JUnit e Mockito.
- Postman ou Insomnia.
- GitHub público.

Caso outra stack já esteja iniciada, manter os mesmos requisitos funcionais, arquiteturais e de documentação.

---

## 3. Arquitetura obrigatória

Organizar o projeto em camadas claras:

```txt
src/main/java/.../
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

### 3.1 Domain

Responsável por entidades, enums, regras de negócio e validações centrais.

### 3.2 Application

Responsável pelos casos de uso e orquestração dos fluxos.

### 3.3 Infrastructure

Responsável por banco de dados, repositórios, migrations, segurança, pagamento mock e auditoria.

### 3.4 API

Responsável por controllers, DTOs, autenticação, autorização, responses, erros e documentação Swagger.

---

## 4. MVP obrigatório escolhido

Implementar o fluxo:

```txt
Pedido -> Pagamento mock -> Atualização de status -> Baixa de estoque -> Auditoria -> Fidelidade
```

Fluxo mínimo esperado:

1. Usuário autenticado cria pedido.
2. API valida unidade, produtos e estoque.
3. API exige o campo `canalPedido`.
4. API calcula subtotal e total.
5. API cria pedido com status `AGUARDANDO_PAGAMENTO`.
6. API solicita pagamento mock.
7. Pagamento mock retorna `APROVADO` ou `RECUSADO`.
8. Se aprovado, registra pagamento, baixa estoque, atualiza status e registra auditoria.
9. Se recusado, registra pagamento recusado, não baixa estoque e retorna mensagem coerente.
10. Pedido pode evoluir para `EM_PREPARO`, `PRONTO`, `ENTREGUE` ou `CANCELADO`, respeitando regras de transição.

---

## 5. Requisitos funcionais

### RF01 - Cadastro de usuários

Implementar cadastro de usuário com:

- Nome.
- E-mail único.
- Senha com hash.
- Perfil.
- Status ativo/inativo.

Perfis mínimos:

- `CLIENTE`.
- `ATENDENTE`.
- `COZINHA`.
- `GERENTE`.
- `ADMIN`.

### RF02 - Autenticação

Implementar login com e-mail e senha. O login deve retornar JWT válido e dados mínimos do usuário autenticado.

### RF03 - Autorização por perfil

Aplicar regras:

- `CLIENTE`: cria e consulta seus pedidos.
- `ATENDENTE`: cria pedidos de balcão e consulta pedidos operacionais.
- `COZINHA`: atualiza status de preparo.
- `GERENTE`: gerencia produtos, estoque, promoções e relatórios.
- `ADMIN`: gerencia usuários, unidades e configurações gerais.

### RF04 - Gestão de unidades

Cadastrar, listar, buscar, atualizar e desativar unidades.

Campos sugeridos:

- `id`.
- `nome`.
- `cidade`.
- `bairro`.
- `endereco`.
- `ativa`.
- `createdAt`.
- `updatedAt`.

### RF05 - Gestão de produtos

Cadastrar, listar, buscar, atualizar e desativar produtos.

Campos sugeridos:

- `id`.
- `nome`.
- `descricao`.
- `preco`.
- `categoria`.
- `ativo`.
- `createdAt`.
- `updatedAt`.

### RF06 - Cardápio por unidade

Permitir consulta dos produtos disponíveis em uma unidade.

Regra mínima:

- Produto deve estar ativo.
- Unidade deve estar ativa.
- Produto deve possuir estoque na unidade.
- Produto sem estoque deve não aparecer ou aparecer como indisponível, conforme decisão documentada.

### RF07 - Estoque por unidade

Controlar estoque separadamente por unidade e produto.

Campos sugeridos:

- `id`.
- `unidadeId`.
- `produtoId`.
- `quantidadeAtual`.
- `estoqueMinimo`.
- `updatedAt`.

Também deve existir histórico de movimentações:

- Entrada.
- Saída.
- Ajuste.
- Saída vinculada a pedido.

### RF08 - Criação de pedido

Criar pedido com:

- `clienteId`.
- `unidadeId`.
- `canalPedido`.
- `itens`.
- `formaPagamento`.

O campo `canalPedido` é obrigatório.

Valores permitidos:

- `APP`.
- `TOTEM`.
- `BALCAO`.
- `PICKUP`.
- `WEB`.

### RF09 - Itens do pedido

Cada pedido deve possuir ao menos um item. Cada item deve armazenar:

- Produto.
- Quantidade.
- Preço unitário no momento do pedido.
- Subtotal.

O preço do produto deve ser congelado no pedido.

### RF10 - Status do pedido

Status mínimos:

- `AGUARDANDO_PAGAMENTO`.
- `PAGAMENTO_APROVADO`.
- `PAGAMENTO_RECUSADO`.
- `RECEBIDO`.
- `EM_PREPARO`.
- `PRONTO`.
- `ENTREGUE`.
- `CANCELADO`.

Regras:

- Pedido cancelado não pode voltar ao fluxo operacional.
- Pedido entregue não pode voltar para preparo.
- Pedido com pagamento recusado não pode seguir para preparo.
- Apenas perfis autorizados podem alterar status.

### RF11 - Pagamento mock

Implementar integração simulada, sem pagamento real.

Pagamento deve registrar:

- `pedidoId`.
- `status`.
- `metodo`.
- `valor`.
- `payloadEnvio`.
- `payloadRetorno`.
- `codigoTransacaoMock`.
- `createdAt`.

Cenários obrigatórios:

- Pagamento aprovado.
- Pagamento recusado.

### RF12 - Fidelização

Implementar programa simples de pontos.

Regras:

- Cliente precisa dar consentimento.
- Sem consentimento, não gerar pontos.
- Com consentimento, gerar pontos após pagamento aprovado ou pedido entregue.
- Sugestão: 1 ponto a cada R$ 1,00.

### RF13 - Promoções e campanhas

Implementar ou documentar regra simples de promoção.

Sugestão:

- Promoção ativa.
- Percentual de desconto.
- Data inicial e final.
- Produto opcional.
- Unidade opcional.

### RF14 - Consulta e filtro de pedidos

Permitir listagem paginada com filtros:

- `status`.
- `canalPedido`.
- `unidadeId`.
- `clienteId`.
- `dataInicio`.
- `dataFim`.

Exemplo:

```http
GET /pedidos?canalPedido=TOTEM&status=AGUARDANDO_PAGAMENTO&page=1&limit=10
```

### RF15 - Auditoria

Registrar ações sensíveis:

- Login.
- Criação de pedido.
- Pagamento aprovado.
- Pagamento recusado.
- Alteração de status.
- Cancelamento de pedido.
- Movimentação de estoque.
- Acesso a dados sensíveis.

---

## 6. Requisitos não funcionais

### RNF01 - Segurança

- Usar JWT.
- Usar BCrypt para senha.
- Não retornar senha ou hash em responses.
- Validar DTOs.
- Aplicar autorização por perfil.
- Padronizar erros.

### RNF02 - LGPD

Documentar e aplicar o mínimo técnico:

- Dados coletados.
- Finalidade.
- Base legal.
- Consentimento de fidelização.
- Minimização de dados.
- Hash de senha.
- Logs de auditoria.
- Estratégia de anonimização documentada.

### RNF03 - Swagger/OpenAPI

Swagger deve refletir endpoints reais, com:

- Descrição.
- Request.
- Response.
- Status codes.
- Erros possíveis.
- Autenticação necessária.

### RNF04 - Padrão de erro

Todas as falhas devem retornar o mesmo formato:

```json
{
  "error": "CODIGO_DO_ERRO",
  "message": "Mensagem legível",
  "details": [
    {
      "field": "campo",
      "issue": "descrição do problema"
    }
  ],
  "timestamp": "2026-05-02T12:00:00Z",
  "path": "/rota",
  "requestId": "uuid"
}
```

### RNF05 - Paginação

Listagens devem aceitar:

- `page`.
- `limit`.
- `sort`.

### RNF06 - Reprodutibilidade

O projeto deve rodar a partir do README, com:

- `.env.example`.
- Migrations.
- Seed.
- Instrução de execução.
- Instrução do Swagger.
- Instrução da coleção Postman/Insomnia.

---

## 7. Modelo de dados sugerido

Entidades principais:

- `Usuario`.
- `Cliente`.
- `Unidade`.
- `Produto`.
- `Estoque`.
- `MovimentoEstoque`.
- `Pedido`.
- `PedidoItem`.
- `Pagamento`.
- `FidelidadeHistorico`.
- `Promocao`.
- `Auditoria`.

Relacionamentos mínimos:

- Usuário pode estar associado a Cliente.
- Unidade possui vários estoques.
- Produto aparece em vários estoques.
- Pedido pertence a Cliente.
- Pedido pertence a Unidade.
- Pedido possui vários itens.
- Pedido possui pagamento.
- Cliente possui histórico de fidelidade.
- Auditoria pode estar associada a usuário e entidade afetada.

---

# Etapas de implementação e commits

## Commit 1 - Estrutura inicial do projeto

### Mensagem de commit

```bash
git commit -m "chore: configura estrutura inicial do projeto backend"
```

### Objetivo

Criar a base técnica do projeto e deixar a API iniciando sem regra de negócio ainda.

### Implementar

- Criar projeto Spring Boot.
- Adicionar dependências principais.
- Configurar estrutura de pacotes por camadas.
- Criar arquivo `.env.example`.
- Criar README inicial.
- Criar configuração inicial de ambiente local.
- Criar endpoint simples de saúde ou teste.

### Validação técnica da etapa

- Projeto compila.
- API inicia sem erro.
- Endpoint simples responde.
- Estrutura de pastas está organizada.
- README inicial informa stack e objetivo.

---

## Commit 2 - Modelo de domínio, banco e migrations

### Mensagem de commit

```bash
git commit -m "feat: cria modelo inicial de dominio e migrations"
```

### Objetivo

Criar as principais entidades e garantir persistência real em banco.

### Implementar

- Entidades principais.
- Enums principais.
- Repositórios iniciais.
- Migrations do banco.
- Relacionamentos com PK/FK.
- Configuração de banco local.
- Seed inicial opcional.

### Validação técnica da etapa

- Banco é criado por migration.
- Tabelas principais existem.
- Chaves estrangeiras foram criadas.
- Aplicação inicia conectando ao banco.
- DER preliminar pode ser desenhado a partir das tabelas.

---

## Commit 3 - Autenticação JWT e perfis

### Mensagem de commit

```bash
git commit -m "feat: implementa autenticacao jwt e controle de perfis"
```

### Objetivo

Permitir login seguro e proteger rotas por autenticação e autorização.

### Implementar

- Cadastro de usuário.
- Login.
- BCrypt.
- Geração de JWT.
- Middleware/filtro de autenticação.
- Perfis/roles.
- Rotas públicas e privadas.
- Seed com usuários de teste.

### Validação técnica da etapa

- Cadastro salva senha com hash.
- Login válido retorna token.
- Login inválido retorna erro.
- Rota protegida sem token retorna 401.
- Rota restrita por perfil retorna 403 para usuário sem permissão.
- Response não expõe senha nem hash.

---

## Commit 4 - Unidades, produtos e cardápio

### Mensagem de commit

```bash
git commit -m "feat: implementa unidades produtos e consulta de cardapio"
```

### Objetivo

Disponibilizar os cadastros base para operação do pedido.

### Implementar

- CRUD de unidades.
- CRUD de produtos.
- Ativação/desativação lógica.
- Consulta de cardápio por unidade.
- Paginação em listagens.
- Validações de campos obrigatórios.

### Validação técnica da etapa

- Gerente/Admin cadastra unidade.
- Gerente/Admin cadastra produto.
- Cliente consulta cardápio.
- Produto inativo não aparece como disponível.
- Listagens usam paginação.
- Erros seguem padrão definido.

---

## Commit 5 - Estoque por unidade e movimentações

### Mensagem de commit

```bash
git commit -m "feat: implementa controle de estoque por unidade"
```

### Objetivo

Controlar saldo de produtos por unidade e impedir inconsistência de estoque.

### Implementar

- Cadastro/consulta de estoque por unidade e produto.
- Movimento de entrada.
- Movimento de saída.
- Histórico de movimentações.
- Validação de quantidade positiva.
- Bloqueio de saída acima do saldo.

### Validação técnica da etapa

- Entrada aumenta o saldo.
- Saída reduz o saldo.
- Saída maior que saldo retorna 409.
- Quantidade negativa retorna 422.
- Histórico de movimentação é gravado.
- Estoque é separado por unidade.

---

## Commit 6 - Criação de pedido com canalPedido

### Mensagem de commit

```bash
git commit -m "feat: implementa criacao de pedidos com canal de origem"
```

### Objetivo

Implementar a criação de pedido com rastreabilidade multicanal.

### Implementar

- Endpoint `POST /pedidos`.
- DTO de criação de pedido.
- Validação de `canalPedido` obrigatório.
- Enum `CanalPedido`.
- Validação de unidade existente.
- Validação de produtos existentes.
- Validação de estoque disponível.
- Cálculo de subtotal e total.
- Registro dos itens com preço congelado.
- Status inicial `AGUARDANDO_PAGAMENTO`.
- Consulta e filtro de pedidos por canal.

### Validação técnica da etapa

- Pedido válido retorna 201.
- Pedido sem `canalPedido` retorna 422.
- Pedido com canal inválido retorna 422.
- Produto inexistente retorna 404.
- Estoque insuficiente retorna 409.
- Filtro `GET /pedidos?canalPedido=TOTEM` funciona.
- Pedido salva itens e valores corretamente.

---

## Commit 7 - Pagamento mock

### Mensagem de commit

```bash
git commit -m "feat: adiciona pagamento mock ao fluxo de pedidos"
```

### Objetivo

Simular o fluxo de pagamento externo sem depender de provedor real.

### Implementar

- Serviço `PaymentMockService`.
- Endpoint de pagamento mock.
- Registro da solicitação de pagamento.
- Registro de payload de envio e retorno.
- Cenário aprovado.
- Cenário recusado.
- Atualização do status do pedido.
- Baixa de estoque apenas no pagamento aprovado.

### Validação técnica da etapa

- Pagamento aprovado atualiza pedido.
- Pagamento recusado atualiza status coerente.
- Pagamento aprovado reduz estoque.
- Pagamento recusado não reduz estoque.
- Registro de pagamento fica persistido.
- Response informa status do pagamento e pedido.

---

## Commit 8 - Status, fidelidade, promoções e auditoria

### Mensagem de commit

```bash
git commit -m "feat: implementa status de pedido fidelidade promocoes e auditoria"
```

### Objetivo

Fechar as regras complementares do fluxo operacional e de rastreabilidade.

### Implementar

- Endpoint de alteração de status.
- Regras de transição de status.
- Cancelamento de pedido.
- Auditoria de ações sensíveis.
- Consentimento de fidelidade.
- Geração de pontos.
- Consulta de saldo e histórico de pontos.
- Promoção simples ou regra documentada e aplicada.

### Validação técnica da etapa

- Cozinha altera pedido para `EM_PREPARO`.
- Cozinha altera pedido para `PRONTO`.
- Atendente/Gerente altera pedido para `ENTREGUE`.
- Cliente não altera status operacional.
- Transição inválida retorna 409.
- Auditoria registra alteração de status.
- Fidelidade gera pontos apenas com consentimento.
- Promoção válida aplica desconto conforme regra.

---

## Commit 9 - Swagger, erros globais e coleção Postman/Insomnia

### Mensagem de commit

```bash
git commit -m "docs: documenta api swagger e adiciona colecao de testes"
```

### Objetivo

Tornar a API testável e compreensível para o avaliador.

### Implementar

- Configuração Swagger/OpenAPI.
- Descrição dos endpoints.
- Exemplos de request e response.
- Códigos de status.
- Exception handler global.
- JSON padrão de erro.
- Coleção Postman/Insomnia com testes positivos e negativos.

### Validação técnica da etapa

- Swagger abre localmente.
- Swagger mostra endpoints reais.
- Todos os erros usam mesmo padrão.
- Coleção tem pastas organizadas.
- Coleção executa login e usa token.
- Coleção cobre ao menos 10 cenários.
- Existem pelo menos 6 positivos e 4 negativos.

---

## Commit 10 - Documentação final e evidências

### Mensagem de commit

```bash
git commit -m "docs: finaliza readme diagramas evidencias e documentacao academica"
```

### Objetivo

Preparar a entrega final com material técnico completo e reprodutível.

### Implementar

- README final.
- DER.
- Diagrama de casos de uso.
- Diagrama de classes.
- Diagrama de sequência ou atividade.
- Coleção Postman no repositório.
- Evidências de execução.
- Instruções de Swagger.
- Instruções de banco, migrations e seed.
- Declaração de uso de IA, se aplicável.

### Validação técnica da etapa

- Repositório está público.
- README permite rodar do zero.
- `.env.example` existe.
- Diagramas estão no repositório.
- Coleção Postman/Insomnia está no repositório.
- Swagger é acessível localmente.
- PDF final segue o roteiro da atividade.
- Links do repositório estão funcionando.

---

# Ordem recomendada para execução diária

Use uma etapa por dia:

1. Dia 1: Commit 1.
2. Dia 2: Commit 2.
3. Dia 3: Commit 3.
4. Dia 4: Commit 4.
5. Dia 5: Commit 5.
6. Dia 6: Commit 6.
7. Dia 7: Commit 7.
8. Dia 8: Commit 8.
9. Dia 9: Commit 9.
10. Dia 10: Commit 10.

Ao solicitar uma etapa ao agente, peça sempre:

```txt
Execute somente a etapa X do plano técnico.
Não avance para a próxima etapa.
Explique os arquivos que serão criados ou alterados.
Após implementar, gere um resumo do que foi feito e quais testes devo executar antes do commit.
```

---

# Critério final de aceite técnico

O projeto estará tecnicamente pronto quando:

- API rodar localmente.
- Banco for criado por migration.
- Seed funcionar.
- Login retornar token.
- Rotas protegidas recusarem acesso sem token.
- Pelo menos uma rota retornar 403 por perfil incorreto.
- Pedido exigir `canalPedido`.
- Pedido permitir filtro por `canalPedido`.
- Pagamento mock tiver sucesso e falha.
- Estoque baixar apenas em pagamento aprovado.
- Pagamento recusado não baixar estoque.
- Erros seguirem JSON padrão.
- Swagger documentar endpoints reais.
- Coleção Postman executar o fluxo principal.
- README permitir reprodução do zero.
- Diagramas baterem com a implementação.
- PDF final explicar o que foi implementado, testado e documentado.
