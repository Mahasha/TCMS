-- Levy payments per family per financial year
CREATE TABLE levy_payments
(
    id              BIGSERIAL PRIMARY KEY,
    financial_year  INTEGER      NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    payment_date    DATE,
    status          VARCHAR(50)  NOT NULL,
    family_id       BIGINT       NOT NULL,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),

    CONSTRAINT fk_levy_payments_family FOREIGN KEY (family_id) REFERENCES families (id),
    CONSTRAINT uq_family_year UNIQUE (family_id, financial_year)
);

-- Helpful index for lookups
CREATE INDEX idx_levy_payments_family_year ON levy_payments(family_id, financial_year);

-- Village events
CREATE TABLE village_events
(
    id                    BIGSERIAL PRIMARY KEY,

    name                  VARCHAR(255),
    description           TEXT,
    event_date            DATE,
    location              VARCHAR(255),

    fee_amount            NUMERIC(12,2),

    status                VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    type                  VARCHAR(50),

    death_cert_url         VARCHAR(255),
    id_copy_url            VARCHAR(255),

    has_death_certificate  BOOLEAN NOT NULL DEFAULT FALSE,
    has_id_copies          BOOLEAN NOT NULL DEFAULT FALSE,

    organization_id       BIGINT,
    family_id             BIGINT,

    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255),

    CONSTRAINT fk_village_events_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),

    CONSTRAINT fk_village_events_family
        FOREIGN KEY (family_id) REFERENCES families (id)
);

CREATE INDEX idx_village_events_org ON village_events(organization_id);
CREATE INDEX idx_village_events_family ON village_events(family_id);
CREATE INDEX idx_village_events_event_date ON village_events(event_date);