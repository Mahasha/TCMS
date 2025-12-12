-- 1. Families
CREATE TABLE families
(
    id               BIGSERIAL PRIMARY KEY,
    reference_number VARCHAR(255),
    address          VARCHAR(255),
    organization_id  BIGINT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255),
    CONSTRAINT fk_families_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
);

-- 2. Residents
CREATE TABLE residents
(
    id                     BIGSERIAL PRIMARY KEY,
    first_name             VARCHAR(255),
    last_name              VARCHAR(255),
    id_number              VARCHAR(255),
    phone_number           VARCHAR(255),
    email                  VARCHAR(255),
    is_head_of_household   BOOLEAN   DEFAULT FALSE NOT NULL,
    family_id              BIGINT,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by             VARCHAR(255),
    updated_by             VARCHAR(255),
    CONSTRAINT fk_residents_family FOREIGN KEY (family_id) REFERENCES families (id)
);
