CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    consentimento_fidelidade BOOLEAN NOT NULL DEFAULT FALSE,
    pontos_saldo INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_clientes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE unidades (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    cidade VARCHAR(80) NOT NULL,
    bairro VARCHAR(80) NOT NULL,
    endereco VARCHAR(255) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE produtos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(600) NOT NULL,
    preco NUMERIC(12, 2) NOT NULL,
    categoria VARCHAR(40) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE pedidos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    unidade_id BIGINT NOT NULL,
    canal_pedido VARCHAR(30) NOT NULL,
    status VARCHAR(40) NOT NULL,
    forma_pagamento VARCHAR(40) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    total NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_pedidos_unidade FOREIGN KEY (unidade_id) REFERENCES unidades (id)
);

CREATE TABLE pedido_itens (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_pedido_itens_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id),
    CONSTRAINT fk_pedido_itens_produto FOREIGN KEY (produto_id) REFERENCES produtos (id)
);

CREATE TABLE estoques (
    id BIGSERIAL PRIMARY KEY,
    unidade_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade_atual INTEGER NOT NULL,
    estoque_minimo INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_estoques_unidade FOREIGN KEY (unidade_id) REFERENCES unidades (id),
    CONSTRAINT fk_estoques_produto FOREIGN KEY (produto_id) REFERENCES produtos (id),
    CONSTRAINT uk_estoque_unidade_produto UNIQUE (unidade_id, produto_id)
);

CREATE TABLE pagamentos (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    metodo VARCHAR(40) NOT NULL,
    valor NUMERIC(12, 2) NOT NULL,
    payload_envio TEXT NOT NULL,
    payload_retorno TEXT NOT NULL,
    codigo_transacao_mock VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_pagamentos_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id)
);

CREATE TABLE movimentos_estoque (
    id BIGSERIAL PRIMARY KEY,
    estoque_id BIGINT NOT NULL,
    pedido_id BIGINT,
    tipo VARCHAR(30) NOT NULL,
    quantidade INTEGER NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_movimentos_estoque_estoque FOREIGN KEY (estoque_id) REFERENCES estoques (id),
    CONSTRAINT fk_movimentos_estoque_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id)
);

CREATE TABLE fidelidade_historico (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    pedido_id BIGINT,
    tipo VARCHAR(20) NOT NULL,
    pontos INTEGER NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_fidelidade_historico_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_fidelidade_historico_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id)
);

CREATE TABLE promocoes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT FALSE,
    percentual_desconto NUMERIC(5, 2) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    produto_id BIGINT,
    unidade_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_promocoes_produto FOREIGN KEY (produto_id) REFERENCES produtos (id),
    CONSTRAINT fk_promocoes_unidade FOREIGN KEY (unidade_id) REFERENCES unidades (id)
);

CREATE TABLE auditorias (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT,
    acao VARCHAR(100) NOT NULL,
    entidade VARCHAR(100) NOT NULL,
    entidade_id BIGINT NOT NULL,
    detalhes TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_auditorias_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
