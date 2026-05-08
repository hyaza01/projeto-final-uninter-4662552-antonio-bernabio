# Nota Tecnica LGPD

## Escopo

Aplicacao backend para operacao de pedidos, pagamento mock, estoque e fidelidade.

## Dados pessoais tratados

- Identificacao basica: nome e email do usuario.
- Credenciais: senha (armazenada somente como hash BCrypt).
- Dados de operacao: pedidos do cliente, status de pagamento e historico de fidelidade.
- Logs de seguranca e auditoria: acao executada, entidade afetada, usuario responsavel e timestamp.

## Finalidade do tratamento

- Autenticacao e autorizacao de acesso.
- Processamento de pedidos e pagamento mock.
- Controle operacional de estoque e status.
- Registro de auditoria para rastreabilidade.
- Programa de fidelidade mediante consentimento explicito.

## Base legal aplicada

- Execucao de contrato para operacao de pedidos.
- Legitimo interesse para seguranca da plataforma e trilha de auditoria.
- Consentimento para fidelidade (`consentimentoFidelidade`).

## Medidas tecnicas adotadas

- Senha armazenada com hash BCrypt.
- JWT para autenticacao stateless.
- Controle de acesso por perfil em rotas sensiveis.
- Resposta padronizada de erro sem exposicao de detalhes internos.
- Auditoria de eventos criticos (criacao de pedido, pagamento, alteracao de status).

## Consentimento de fidelidade

- Cliente inicia com consentimento desativado.
- Endpoint dedicado para opt-in e opt-out:
  - `PATCH /api/v1/fidelidade/me/consentimento`
- Pontos so sao gerados quando o consentimento esta ativo.

## Minimização e retenção

- Coleta restrita ao necessario para operacao.
- Nao ha armazenamento de dados reais de cartao no mock de pagamento.
- Logs de auditoria e operacao devem seguir politica institucional de retenção e descarte.

## Pontos de melhoria recomendados

- Definir politica formal de retenção por tipo de dado.
- Implementar anonimização/pseudonimizacao para exportacao analitica.
- Criar endpoint de exportacao de dados do titular.
- Criar endpoint de exclusao/encerramento conforme governanca da organizacao.
