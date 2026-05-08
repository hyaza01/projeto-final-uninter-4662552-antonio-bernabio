# Diagrama de Casos de Uso

```mermaid
graph LR
    C[Cliente]
    A[Atendente]
    K[Cozinha]
    G[Gerente]
    D[Admin]

    UC1((Cadastrar/Login))
    UC2((Consultar catalogo))
    UC3((Criar pedido))
    UC4((Pagar pedido mock))
    UC5((Consultar meus pedidos))
    UC6((Consultar/alterar consentimento fidelidade))

    UO1((Listar pedidos operacionais))
    UO2((Atualizar status de pedido))
    UO3((Consultar estoque por unidade))

    UG1((Gerir produtos/unidades))
    UG2((Acompanhar auditoria e operacao))

    C --> UC1
    C --> UC2
    C --> UC3
    C --> UC4
    C --> UC5
    C --> UC6

    A --> UO1
    A --> UO2
    A --> UO3

    K --> UO1
    K --> UO2

    G --> UO1
    G --> UO2
    G --> UO3
    G --> UG1
    G --> UG2

    D --> UG1
    D --> UG2
```
