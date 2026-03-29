--liquibase formatted sql
--changeset team12:006-update-test-users-with-emails context:dev,local

-- Update existing test users with email addresses
-- This runs after the users table has been altered to include email column
UPDATE users SET email = 'johan@someemail.com' WHERE name = 'Johan' AND email IS NULL;
UPDATE users SET email = 'merily@someemail.com' WHERE name = 'Merily' AND email IS NULL;
UPDATE users SET email = 'milly@someemail.com' WHERE name = 'Milli' AND email IS NULL;
UPDATE users SET email = 'villi@someemail.com' WHERE name = 'Villi' AND email IS NULL;

--rollback UPDATE users SET email = NULL WHERE name IN ('Johan', 'Merily', 'Milli', 'Villi');
