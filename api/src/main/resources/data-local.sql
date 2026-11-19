-- Local-only demo account used by POST /auth/test when the local profile is active.
INSERT INTO app_user (id, provider, provider_id, email, display_name)
VALUES ('local-test-user', 'local', 'demo', 'demo@sea-animal-discovery.local', 'Test User')
ON CONFLICT (provider, provider_id) DO NOTHING;

CREATE TABLE IF NOT EXISTS local_test_credential (
    username VARCHAR(64) PRIMARY KEY,
    password_hash VARCHAR(100) NOT NULL,
    user_id VARCHAR(36) NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE CASCADE
);

INSERT INTO local_test_credential (username, password_hash, user_id)
VALUES ('testuser', '$2y$10$CqCtm9CZl0V2IdBf.ua0KuPtsUr/rYS8dDznbzhS2paZQhzi.z2yC', 'local-test-user')
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash, user_id = EXCLUDED.user_id;
