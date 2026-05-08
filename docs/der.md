# DER Final - Raízes do Nordeste

## Entidades principais

- Usuario
- Cliente
- Unidade
- Produto
- Estoque
- MovimentoEstoque
- Pedido
- PedidoItem
- Pagamento
- FidelidadeHistorico
- Promocao
- Auditoria

## Relacionamentos

- Usuario 1:1 Cliente
- Cliente 1:N Pedido
- Unidade 1:N Pedido
- Pedido 1:N PedidoItem
- Produto 1:N PedidoItem
- Unidade 1:N Estoque
- Produto 1:N Estoque
- Estoque 1:N MovimentoEstoque
- Pedido 1:N MovimentoEstoque (movimentacoes de pagamento/cancelamento)
- Pedido 1:1 Pagamento
- Cliente 1:N FidelidadeHistorico
- Pedido 1:N FidelidadeHistorico
- Produto 1:N Promocao (opcional)
- Unidade 1:N Promocao (opcional)
- Usuario 1:N Auditoria (opcional)

## Observações de domínio

- Pedido nasce em AGUARDANDO_PAGAMENTO.
- Estoque so e baixado no pagamento APROVADO.
- Pagamento mock registra payload de envio e retorno.
- Fidelidade so pontua quando houver consentimento ativo.
- Promocao ativa e no periodo e aplicada ao preco unitario congelado no PedidoItem.
- Auditoria registra acoes sensiveis (login, pedido, pagamento, fidelidade, estoque, promocao).

## Coerência com as migrations

- V1 cria a estrutura principal das tabelas operacionais e de seguranca.
- V2 semeia dados iniciais de usuarios, cliente, unidade, produtos e estoques.
- V3 adiciona `descricao` em Promocao, `ip_origem` em Auditoria e `consentimento_fidelidade_atualizado_em` em Cliente.
- V4 ajusta apenas os usuarios seedados para credenciais de avaliacao com hash BCrypt valido, sem alterar a estrutura do schema.
