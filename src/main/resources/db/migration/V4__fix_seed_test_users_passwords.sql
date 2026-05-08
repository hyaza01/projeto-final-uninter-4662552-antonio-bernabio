INSERT INTO usuarios (nome, email, senha_hash, perfil, ativo, created_at, updated_at)
VALUES
    ('Administrador', 'admin@raizes.local', '$2a$10$y0a4k9FuYgPG35M7TXubU.4HVqNUSQfB/DIcV4pepagOtMHsejf76', 'ADMIN', TRUE, NOW(), NOW()),
    ('Gerente', 'gerente@raizes.local', '$2a$10$ffA5H6aI1DpOnfW7kUT3L.BSl5n8tl.piJcqPdGHF3uYBkYbfej9e', 'GERENTE', TRUE, NOW(), NOW()),
    ('Atendente', 'atendente@raizes.local', '$2a$10$Pkg0ViHrZnTKUnKWtH/FJeq.sUsLlzHjun0CxQQ33XUYLj5Vmoqjq', 'ATENDENTE', TRUE, NOW(), NOW()),
    ('Cozinha', 'cozinha@raizes.local', '$2a$10$qHO8aGinNlmCuwewV5pxbe9MUBP0b35lrUPScqmlNuo.yfSz923JS', 'COZINHA', TRUE, NOW(), NOW()),
    ('Cliente', 'cliente@raizes.local', '$2a$10$X2UscHEkwqRTT1TpE96WuuSbErGueC1CIKUan.tYU7e3xHzByWTom', 'CLIENTE', TRUE, NOW(), NOW())
ON CONFLICT (email) DO UPDATE
SET
    senha_hash = EXCLUDED.senha_hash,
    perfil = EXCLUDED.perfil,
    ativo = TRUE,
    updated_at = NOW();

INSERT INTO clientes (usuario_id, consentimento_fidelidade, pontos_saldo, created_at, updated_at)
SELECT u.id, TRUE, 0, NOW(), NOW()
FROM usuarios u
WHERE u.email = 'cliente@raizes.local'
ON CONFLICT (usuario_id) DO NOTHING;
