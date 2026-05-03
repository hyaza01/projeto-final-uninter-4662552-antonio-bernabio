INSERT INTO usuarios (nome, email, senha_hash, perfil, ativo, created_at, updated_at)
VALUES
    ('Administrador', 'admin@raizes.local', '$2a$10$placeholderhashnaoutilizar', 'ADMIN', TRUE, NOW(), NOW()),
    ('Gerente Teste', 'gerente@raizes.local', '$2a$10$placeholderhashnaoutilizar', 'GERENTE', TRUE, NOW(), NOW()),
    ('Cliente Teste', 'cliente@raizes.local', '$2a$10$placeholderhashnaoutilizar', 'CLIENTE', TRUE, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO clientes (usuario_id, consentimento_fidelidade, pontos_saldo, created_at, updated_at)
SELECT u.id, TRUE, 0, NOW(), NOW()
FROM usuarios u
WHERE u.email = 'cliente@raizes.local'
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO unidades (nome, cidade, bairro, endereco, ativa, created_at, updated_at)
VALUES ('Raizes Centro', 'Curitiba', 'Centro', 'Rua das Araucarias, 100', TRUE, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO produtos (nome, descricao, preco, categoria, ativo, created_at, updated_at)
VALUES
    ('X-Nordestino', 'Pao artesanal, carne e queijo coalho', 29.90, 'LANCHE', TRUE, NOW(), NOW()),
    ('Suco de Caju', 'Suco natural de caju 400ml', 9.50, 'BEBIDA', TRUE, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO estoques (unidade_id, produto_id, quantidade_atual, estoque_minimo, created_at, updated_at)
SELECT un.id, pr.id, 50, 10, NOW(), NOW()
FROM unidades un
CROSS JOIN produtos pr
WHERE un.nome = 'Raizes Centro'
ON CONFLICT (unidade_id, produto_id) DO NOTHING;
