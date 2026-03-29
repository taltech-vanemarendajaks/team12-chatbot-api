--liquibase formatted sql
--changeset team12:003-create-banned-words-table

-- Create banned_words table for content moderation
CREATE TABLE IF NOT EXISTS banned_words (
  id BIGSERIAL PRIMARY KEY,
  word VARCHAR(100) NOT NULL UNIQUE,
  category VARCHAR(50),
  severity VARCHAR(20),
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  created_by BIGINT NOT NULL,
  updated_by BIGINT
);

-- Indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_banned_words_is_active ON banned_words (is_active);
CREATE INDEX IF NOT EXISTS idx_banned_words_category ON banned_words (category);
CREATE INDEX IF NOT EXISTS idx_banned_words_word_lookup ON banned_words (word) WHERE is_active = true;

--rollback DROP TABLE IF EXISTS banned_words;
