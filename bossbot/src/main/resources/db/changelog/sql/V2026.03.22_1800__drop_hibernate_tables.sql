--liquibase formatted sql
--changeset team12:000-drop-hibernate-created-tables

-- Drop tables if they were created by Hibernate (not Liquibase)
-- This handles migration from hibernate.ddl-auto=update to Liquibase
-- These drops are conditional - won't fail if tables don't exist

DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS stamp_answers CASCADE;
