-- Initialize ChitSmart databases
-- Runs automatically when the PostgreSQL container starts for the first time

-- Create test database alongside dev database
SELECT 'CREATE DATABASE chitsmart_test'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'chitsmart_test')\gexec

-- Enable pgvector on dev database
\c chitsmart_dev
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_trgm;  -- for fast text search

-- Enable pgvector on test database
\c chitsmart_test
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
