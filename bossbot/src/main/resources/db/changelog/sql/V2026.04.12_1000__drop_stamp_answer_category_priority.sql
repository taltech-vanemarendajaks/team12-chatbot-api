--liquibase formatted sql
--changeset team12:009-drop-stamp-answer-category-priority

-- Remove category and priority columns from stamp_answers table
-- These fields are not used in the chat flow and add unnecessary complexity
ALTER TABLE stamp_answers DROP COLUMN IF EXISTS category;
ALTER TABLE stamp_answers DROP COLUMN IF EXISTS priority;

--rollback ALTER TABLE stamp_answers ADD COLUMN category VARCHAR(50);
--rollback ALTER TABLE stamp_answers ADD COLUMN priority INTEGER NOT NULL DEFAULT 0;
