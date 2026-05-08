# LGPD - Nota Técnica Final

## Dados pessoais coletados

- Nome
- E-mail
- Perfil de acesso
- Identificador do cliente
- Dados operacionais de pedido (sem dados de cartao)

## Finalidade do tratamento

- Autenticacao e autorizacao de usuarios.
- Operacao de pedidos, estoque e fidelidade.
- Rastreabilidade de acoes sensiveis por auditoria.

## Base legal

- Execucao de contrato para processamento de pedidos.
- Legitimo interesse para seguranca e auditoria.
- Consentimento explicito para programa de fidelidade.

## Consentimento de fidelidade

- O consentimento e opt-in e pode ser alterado via API.
- Registro de consentimento inclui timestamp de atualizacao.
- Sem consentimento ativo, nao ha geracao de pontos.

## Minimização de dados

- Dados de pagamento real nao sao coletados.
- Fluxo de pagamento e mock, sem cartao, CVV ou tokenizacao bancaria.
- Responses da API nao expõem senha nem senhaHash.

## Proteção de credenciais

- Senhas sao armazenadas com hash BCrypt.
- JWT usado para autenticacao de sessao stateless.

## Controle de acesso por perfil

- CLIENTE: operacoes proprias.
- ATENDENTE/COZINHA: operacoes de pedido/estoque conforme permissao.
- GERENTE/ADMIN: acessos administrativos (auditoria, promocao, estoque manual).

## Logs e auditoria

- Eventos criticos auditados: login, criacao de pedido, pagamento aprovado/recusado,
  cancelamento/alteracao de status, movimentacao de estoque, alteracao de consentimento,
  criacao/alteracao de promocao.
- Auditoria inclui usuario, acao, entidade, entidadeId, detalhes, ip de origem (quando disponivel), timestamp.

## Retenção e anonimização (proposta técnica)

- Definir politica de retencao por tipo de dado (operacional x auditoria).
- Aplicar anonimização/pseudonimizacao para dados historicos fora de janela operacional.
- Implementar processo de descarte seguro com trilha de evidencias.

## Observação sobre pagamento mock

- O projeto utiliza PaymentMockService para simulacao.
- Nao ha integracao com gateway real e nao ha coleta de dados reais de cartao.
