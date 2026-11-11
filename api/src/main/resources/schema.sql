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
