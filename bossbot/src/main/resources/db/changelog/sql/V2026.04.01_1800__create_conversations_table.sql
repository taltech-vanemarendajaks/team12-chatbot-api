--liquibase formatted sql
--changeset team12:007-create-conversations-table

-- Create conversations table
CREATE TABLE IF NOT EXISTS conversations (
                                             id BIGSERIAL PRIMARY KEY,
                                             user_id BIGINT,
                                             title VARCHAR(255),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    active BOOLEAN DEFAULT true,
    CONSTRAINT fk_conversations_user FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- Add foreign key constraint to messages table
-- (This links the existing conversation_id in messages to the new conversations table)
ALTER TABLE messages
    ADD CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations (user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_active ON conversations (active);
CREATE INDEX IF NOT EXISTS idx_conversations_updated_at ON conversations (updated_at);

--rollback ALTER TABLE messages DROP CONSTRAINT IF EXISTS fk_messages_conversation;
--rollback DROP TABLE IF EXISTS conversations CASCADE;