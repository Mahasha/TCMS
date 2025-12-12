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
