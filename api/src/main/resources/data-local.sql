-- Local-only demo account used by POST /auth/test when the local profile is active.
INSERT INTO app_user (id, provider, provider_id, email, display_name)
SELECT 'local-test-user', 'local', 'demo', 'demo@sea-animal-discovery.local', 'Test User'
WHERE NOT EXISTS (
    SELECT 1 FROM app_user WHERE provider = 'local' AND provider_id = 'demo'
);

CREATE TABLE IF NOT EXISTS local_test_credential (
    username VARCHAR(64) PRIMARY KEY,
    password_hash VARCHAR(100) NOT NULL,
    user_id VARCHAR(36) NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE CASCADE
);

INSERT INTO local_test_credential (username, password_hash, user_id)
SELECT 'testuser', '$2y$10$CqCtm9CZl0V2IdBf.ua0KuPtsUr/rYS8dDznbzhS2paZQhzi.z2yC', 'local-test-user'
WHERE NOT EXISTS (
    SELECT 1 FROM local_test_credential WHERE username = 'testuser'
);
