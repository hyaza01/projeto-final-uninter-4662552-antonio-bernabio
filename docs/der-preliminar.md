# DER Preliminar - Etapa 2

Este DER preliminar descreve as entidades principais e seus relacionamentos implementados no schema inicial.

## Entidades

- usuarios
- clientes
- unidades
- produtos
- estoques
- movimentos_estoque
- pedidos
- pedido_itens
- pagamentos
- fidelidade_historico
- promocoes
- auditorias

## Relacionamentos principais

- clientes.usuario_id -> usuarios.id (1:1)
- pedidos.cliente_id -> clientes.id (N:1)
- pedidos.unidade_id -> unidades.id (N:1)
- pedido_itens.pedido_id -> pedidos.id (N:1)
- pedido_itens.produto_id -> produtos.id (N:1)
- estoques.unidade_id -> unidades.id (N:1)
- estoques.produto_id -> produtos.id (N:1)
- pagamentos.pedido_id -> pedidos.id (1:1)
- movimentos_estoque.estoque_id -> estoques.id (N:1)
- movimentos_estoque.pedido_id -> pedidos.id (N:1, opcional)
- fidelidade_historico.cliente_id -> clientes.id (N:1)
- fidelidade_historico.pedido_id -> pedidos.id (N:1, opcional)
- promocoes.produto_id -> produtos.id (N:1, opcional)
- promocoes.unidade_id -> unidades.id (N:1, opcional)
- auditorias.usuario_id -> usuarios.id (N:1, opcional)

## Observacao

As tabelas e constraints foram definidas em src/main/resources/db/migration/V1__create_core_schema.sql.

## Validacao em banco real de teste

O DER foi confrontado com um PostgreSQL local em execucao durante os testes de integracao da classe
src/test/java/br/com/raizesdonordeste/backend/SchemaValidationIntegrationTests.java.

Nessa validacao, as migrations V1 e V2 executaram a partir de schema vazio e as tabelas/relacionamentos
foram conferidos via information_schema e metadata JDBC.
