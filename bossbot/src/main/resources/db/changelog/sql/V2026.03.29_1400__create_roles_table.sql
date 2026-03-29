--liquibase formatted sql
--changeset team12:004-create-roles-table

-- Create roles table for user role management
CREATE TABLE IF NOT EXISTS roles (
  id BIGSERIAL PRIMARY KEY,
  role_name VARCHAR(50) NOT NULL UNIQUE
);

-- Seed initial roles (must match RoleName enum)
INSERT INTO roles (role_name) VALUES 
  ('ADMIN'),
  ('USER')
ON CONFLICT (role_name) DO NOTHING;

--rollback DROP TABLE IF EXISTS roles CASCADE;
