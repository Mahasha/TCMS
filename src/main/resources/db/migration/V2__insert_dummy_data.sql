-- Insert Organizations
INSERT INTO organizations (name, type, parent_id)
VALUES ('Royal House', 'Monarchy', NULL),
       ('Northern Council', 'Regional', 1),
       ('Eastern Council', 'Regional', 1);
-- Insert Roles
INSERT INTO roles (name, description)
VALUES ('ADMIN', 'System Administrator'),
       ('CHIEF', 'Traditional Leader'),
       ('CLERK', 'Administrative Staff'),
       ('CITIZEN', 'Standard User');

-- Insert Users
-- 1. The King (Head of Royal House)
INSERT INTO users (full_name, lineage, birth_date, organization_id, heir_to_id)
VALUES ('King Zwelithini', 'Royal Bloodline', '1948-07-14', 1, NULL);

-- 2. A Chief (Head of Northern Council)
INSERT INTO users (full_name, lineage, birth_date, organization_id, heir_to_id)
VALUES ('Chief Buthelezi', 'Buthelezi Clan', '1955-09-01', 2, 1);

-- 3. A Citizen (Applicant)
INSERT INTO users (full_name, lineage, birth_date, organization_id, heir_to_id)
VALUES ('Sipho Dlamini', 'Dlamini Clan', '1990-05-15', 3, NULL);

-- 4. Another Citizen (Accused in a dispute)
INSERT INTO users (full_name, lineage, birth_date, organization_id, heir_to_id)
VALUES ('Thabo Molefe', 'Molefe Clan', '1988-11-20', 3, NULL);

-- Assign Roles
-- King gets ADMIN and CHIEF
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 2);
-- Chief gets CHIEF
INSERT INTO user_roles (user_id, role_id)
VALUES (2, 2);
-- Citizens get CITIZEN
INSERT INTO user_roles (user_id, role_id)
VALUES (3, 4);
INSERT INTO user_roles (user_id, role_id)
VALUES (4, 4);

-- Insert Land Stands
INSERT INTO land_stands (stand_number, type, size_in_square_meters, allocated, allocation_date, fee_paid,
                         allocated_to_id, applicant_id, application_date, organization_id)
VALUES ('STAND-001', 'RESIDENTIAL', 500.0, TRUE, '2023-01-15', TRUE, 3, 3, '2022-12-01', 3), -- Allocated to Sipho
       ('STAND-002', 'AGRICULTURAL', 2000.0, FALSE, NULL, FALSE, NULL, 4, '2023-06-10', 3);
-- Thabo applied

-- Insert Dispute Cases
INSERT INTO dispute_cases (description, opened_date, status, notices_sent, accused_user_id, complainant_id,
                           organization_id, defense_statement, defense_date)
VALUES ('Boundary dispute regarding STAND-001 fence encroachment', '2023-08-01', 'OPEN', 1, 4, 3, 3,
        'I built the fence where my grandfather told me to.', '2023-08-05');

-- Assign Adjudicator to the case (The Chief)
INSERT INTO dispute_cases_adjudicators (dispute_case_id, adjudicators_id)
VALUES (1, 2);