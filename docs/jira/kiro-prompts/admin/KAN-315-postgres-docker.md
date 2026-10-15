# Kiro Prompt — KAN-315: PostgreSQL Docker Setup

**Ticket:** KAN-315  
**Epic:** KAN-252 — Infrastructure  
**Priority:** High  
**Skill:** Use the `database-migration` skill  
**File:** `docker-compose.yml` (repo root)

---

## Task

Create the Docker Compose configuration for local development and the CI/CD database.

## `docker-compose.yml`

```yaml
version: "3.9"

services:
  db:
    image: pgvector/pgvector:pg16
    container_name: chitsmart_db
    environment:
      POSTGRES_USER: chitsmart
      POSTGRES_PASSWORD: password
      POSTGRES_DB: chitsmart_dev
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./backend/scripts/init_db.sql:/docker-entrypoint-initdb.d/init_db.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U chitsmart -d chitsmart_dev"]
      interval: 5s
      timeout: 5s
      retries: 10

volumes:
  pgdata:
    driver: local
```

## `backend/scripts/init_db.sql`

```sql
-- Create test database alongside dev database
CREATE DATABASE chitsmart_test;
-- Enable pgvector on both databases
\c chitsmart_dev
CREATE EXTENSION IF NOT EXISTS vector;
\c chitsmart_test
CREATE EXTENSION IF NOT EXISTS vector;
```

## `.gitignore` additions
```
# Docker
.env
docker-compose.override.yml
pgdata/
```

## Quick start docs (update `backend/README.md`)
Add section: "Start the database" with the `docker-compose up -d db` command.

## Acceptance criteria
- [ ] `docker-compose up -d db` starts PostgreSQL 16 with pgvector
- [ ] Both `chitsmart_dev` and `chitsmart_test` databases exist
- [ ] pgvector extension enabled on both databases
- [ ] Health check passes before other services start
- [ ] `alembic upgrade head` succeeds against the Docker container
