--liquibase formatted sql
--changeset <author>:<id> [context:<dev,local,production>]
-- Examples:
--   --changeset john.doe:003-add-email-column
--   --changeset team12:004-seed-production-data context:production

-- Add your SQL statements here
-- Example: ALTER TABLE messages ADD COLUMN email VARCHAR(255);

-- Best practices:
-- 1. Use descriptive changeset IDs
-- 2. Never modify already-deployed changesets
-- 3. Use contexts to control where migrations run (dev/local/production)
-- 4. Always provide rollback for data changes
-- 5. Test migrations locally before deploying

--rollback <your rollback SQL here>
-- Example: --rollback ALTER TABLE messages DROP COLUMN email;
