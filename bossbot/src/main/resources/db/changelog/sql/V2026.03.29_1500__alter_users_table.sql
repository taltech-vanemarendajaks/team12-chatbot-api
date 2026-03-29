--liquibase formatted sql
--changeset team12:005-alter-users-table

-- Rename username column to name
ALTER TABLE users RENAME COLUMN username TO name;

-- Add email column (unique and required)
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255) UNIQUE;

-- Update existing rows to have email (temporary fix for non-null constraint)
UPDATE users SET email = LOWER(name) || '@example.com' WHERE email IS NULL;

-- Make email non-null after setting values
ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- Add role_id foreign key column
ALTER TABLE users ADD COLUMN IF NOT EXISTS role_id BIGINT;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id);

-- Add last_updated_at column for tracking
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_updated_at TIMESTAMP;

--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_role;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS last_updated_at;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS role_id;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS email;
--rollback ALTER TABLE users RENAME COLUMN name TO username;
