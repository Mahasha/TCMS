-- 1. Organizations
CREATE TABLE organizations
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    type       VARCHAR(255),
    parent_id  BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_organization_parent FOREIGN KEY (parent_id) REFERENCES organizations (id)
);

-- 2. Roles
CREATE TABLE roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

-- 3. Users
CREATE TABLE users
(
    id                      BIGSERIAL PRIMARY KEY,
    full_name               VARCHAR(255) NOT NULL,
    lineage                 VARCHAR(255),
    birth_date              DATE,
    disqualified            BOOLEAN   DEFAULT FALSE,
    disqualification_reason VARCHAR(255),
    organization_id         BIGINT       NOT NULL,
    heir_to_id              BIGINT,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_users_heir FOREIGN KEY (heir_to_id) REFERENCES users (id)
);

-- 4. User_Roles (Join Table)
CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- 5. Land Stands
CREATE TABLE land_stands
(
    id                    BIGSERIAL PRIMARY KEY,
    stand_number          VARCHAR(255),
    type                  VARCHAR(50),
    size_in_square_meters DOUBLE PRECISION,
    allocated             BOOLEAN   DEFAULT FALSE,
    allocation_date       DATE,
    fee_paid              BOOLEAN   DEFAULT FALSE,
    allocated_to_id       BIGINT,
    applicant_id          BIGINT,
    application_date      DATE,
    organization_id       BIGINT,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255),
    CONSTRAINT fk_land_stands_allocated_to FOREIGN KEY (allocated_to_id) REFERENCES users (id),
    CONSTRAINT fk_land_stands_applicant FOREIGN KEY (applicant_id) REFERENCES users (id),
    CONSTRAINT fk_land_stands_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
);

-- 6. Dispute Cases
CREATE TABLE dispute_cases
(
    id                BIGSERIAL PRIMARY KEY,
    description       TEXT,
    opened_date       DATE,
    closed_date       DATE,
    status            VARCHAR(50),
    notices_sent      INTEGER   DEFAULT 0,
    accused_user_id   BIGINT,
    complainant_id    BIGINT,
    organization_id   BIGINT,
    defense_statement TEXT,
    defense_date      DATE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255),
    CONSTRAINT fk_dispute_cases_accused FOREIGN KEY (accused_user_id) REFERENCES users (id),
    CONSTRAINT fk_dispute_cases_complainant FOREIGN KEY (complainant_id) REFERENCES users (id),
    CONSTRAINT fk_dispute_cases_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
);

-- 7. Dispute Cases Adjudicators (Join Table)
CREATE TABLE dispute_cases_adjudicators
(
    dispute_case_id BIGINT NOT NULL,
    adjudicators_id BIGINT NOT NULL,
    PRIMARY KEY (dispute_case_id, adjudicators_id),
    CONSTRAINT fk_dca_case FOREIGN KEY (dispute_case_id) REFERENCES dispute_cases (id),
    CONSTRAINT fk_dca_user FOREIGN KEY (adjudicators_id) REFERENCES users (id)
);