--liquibase formatted sql
--changeset team12:002-seed-initial-users context:dev,local

-- Insert test users for development and local environments only
-- This changeset will NOT run in production (no 'production' context)
INSERT INTO users (username, created_at) 
VALUES 
  ('Johan', CURRENT_TIMESTAMP),
  ('Merily', CURRENT_TIMESTAMP),
  ('Milli', CURRENT_TIMESTAMP),
  ('Villi', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

--rollback DELETE FROM users WHERE username IN ('Johan', 'Merily', 'Milli', 'Villi');
