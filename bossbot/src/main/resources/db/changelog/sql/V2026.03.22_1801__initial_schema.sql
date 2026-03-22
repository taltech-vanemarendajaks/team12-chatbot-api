--liquibase formatted sql
--changeset team12:001-create-tables

-- create users table
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  discord_id BIGINT,
  username VARCHAR(255),
  created_at TIMESTAMP,
  last_active_at TIMESTAMP,
  is_blocked BOOLEAN,
  blocked_reason VARCHAR(255)
);

-- create messages table
CREATE TABLE IF NOT EXISTS messages (
  id BIGSERIAL PRIMARY KEY,
  conversation_id BIGINT NOT NULL,
  role VARCHAR(10) NOT NULL,
  content TEXT NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP NOT NULL,
  created_by BIGINT NOT NULL
);

-- create stamp_answers table
CREATE TABLE IF NOT EXISTS stamp_answers (
  id BIGSERIAL PRIMARY KEY,
  question TEXT NOT NULL,
  keywords TEXT,
  answer TEXT,
  category VARCHAR(50),
  priority INTEGER NOT NULL DEFAULT 0,
  is_active BOOLEAN NOT NULL DEFAULT true,
  usage_count INTEGER DEFAULT 0,
  last_used_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  created_by BIGINT NOT NULL,
  updated_by BIGINT
);

-- indexes
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages (conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages (created_at);
CREATE INDEX IF NOT EXISTS idx_stamp_answers_category ON stamp_answers (category);
CREATE INDEX IF NOT EXISTS idx_stamp_answers_is_active ON stamp_answers (is_active);
