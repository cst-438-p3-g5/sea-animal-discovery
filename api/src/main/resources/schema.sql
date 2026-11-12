CREATE TABLE IF NOT EXISTS app_user (
    id VARCHAR(36) PRIMARY KEY,
    github_id VARCHAR(32) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(255) NOT NULL
);

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
