CREATE TABLE IF NOT EXISTS app_user (
    id VARCHAR(36) PRIMARY KEY,
    -- github_id remains for compatibility with accounts created before the
    -- provider-neutral identity columns were added.
    github_id VARCHAR(32) UNIQUE,
    provider VARCHAR(32),
    provider_id VARCHAR(320),
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(255) NOT NULL
);

-- Keep the schema compatible with the original GitHub-only deployment.
ALTER TABLE app_user ALTER COLUMN github_id DROP NOT NULL;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS provider VARCHAR(32);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS provider_id VARCHAR(320);
UPDATE app_user SET provider = 'github', provider_id = github_id
WHERE provider IS NULL AND github_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS app_user_provider_identity_key
    ON app_user (provider, provider_id);
CREATE UNIQUE INDEX IF NOT EXISTS app_user_email_key
    ON app_user (email);

CREATE TABLE IF NOT EXISTS species (
    id VARCHAR(36) PRIMARY KEY,
    common_name VARCHAR(255) NOT NULL,
    identification_details VARCHAR(1000) NOT NULL,
    habitat VARCHAR(1000) NOT NULL,
    conservation_information VARCHAR(1000) NOT NULL
);

CREATE TABLE IF NOT EXISTS sighting (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    species_id VARCHAR(36) NOT NULL REFERENCES species(id) ON DELETE RESTRICT,
    sighting_date DATE NOT NULL CHECK (sighting_date <= CURRENT_DATE),
    notes VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS sighting_user_date_idx ON sighting (user_id, sighting_date DESC);
