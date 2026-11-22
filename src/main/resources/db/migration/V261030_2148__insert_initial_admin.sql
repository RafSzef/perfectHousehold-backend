-- Seed initial superuser / admin
-- Password is 'admin123' hashed with BCrypt ($2a$10$ha0TssDqZ2FR54.pOwbH6e8w1m49xxY9S8MtAznnoelwDobrIjXxe)

INSERT INTO users (username, email, password_hash, role, enabled, created_at, updated_at)
VALUES (
    'admin',
    'admin@perfecthousehold.com',
    '$2a$10$ha0TssDqZ2FR54.pOwbH6e8w1m49xxY9S8MtAznnoelwDobrIjXxe',
    'ROLE_ADMIN',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING;
