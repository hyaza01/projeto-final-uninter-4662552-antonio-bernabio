# DER Preliminar - Raizes do Nordeste

Documento textual do modelo relacional implementado nas migrations `V1__create_core_schema.sql` e `V2__seed_initial_data.sql`.

## Entidades principais

- `usuarios`
- `clientes`
- `unidades`
- `produtos`
- `estoques`
- `movimentos_estoque`
- `pedidos`
- `pedido_itens`
- `pagamentos`
- `fidelidade_historico`
- `promocoes`
- `auditorias`

## Chaves e cardinalidades

- `clientes.usuario_id -> usuarios.id` (1:1)
- `pedidos.cliente_id -> clientes.id` (N:1)
- `pedidos.unidade_id -> unidades.id` (N:1)
- `pedido_itens.pedido_id -> pedidos.id` (N:1)
- `pedido_itens.produto_id -> produtos.id` (N:1)
- `estoques.unidade_id -> unidades.id` (N:1)
- `estoques.produto_id -> produtos.id` (N:1)
- `pagamentos.pedido_id -> pedidos.id` (1:1)
- `movimentos_estoque.estoque_id -> estoques.id` (N:1)
- `movimentos_estoque.pedido_id -> pedidos.id` (N:1, opcional)
- `fidelidade_historico.cliente_id -> clientes.id` (N:1)
- `fidelidade_historico.pedido_id -> pedidos.id` (N:1, opcional)
- `promocoes.produto_id -> produtos.id` (N:1, opcional)
- `promocoes.unidade_id -> unidades.id` (N:1, opcional)
- `auditorias.usuario_id -> usuarios.id` (N:1, opcional)

## Regras estruturais relevantes

- `usuarios.email` unico.
- `clientes.usuario_id` unico.
- `estoques` possui chave unica composta (`unidade_id`, `produto_id`).
- `pagamentos.pedido_id` unico para garantir um pagamento por pedido.
- Colunas de auditoria temporal (`created_at`, `updated_at`) em tabelas de dominio.

## Impacto no fluxo de negocio

- Pedido e itens preservam preco congelado para rastreabilidade de compra.
- Pagamento desacoplado de pedido via relacao 1:1 permite guardar payload mock de integracao.
- Baixa e estorno de estoque sao historizados em `movimentos_estoque`.
- Fidelidade com consentimento e saldo no cliente, com trilha em `fidelidade_historico`.
- Tabela `auditorias` registra acoes sensiveis por entidade e usuario.

## Validacao executada

O schema foi validado via testes de integracao em banco PostgreSQL embarcado (`SchemaValidationIntegrationTests`), incluindo:

- existencia das tabelas obrigatorias,
- existencia de chaves primarias,
- existencia das principais chaves estrangeiras,
- compatibilidade dos enums usados no dominio.
