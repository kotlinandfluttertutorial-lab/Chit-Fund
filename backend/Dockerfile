# =============================================================================
# ChitSmart API — Multi-stage Dockerfile
#
# Build variants
# --------------
#   Local (dev):   docker compose up           (uses docker-compose.yml)
#   Cloud (prod):  docker compose -f docker-compose.yml \
#                                -f docker-compose.cloud.yml up
#
# Stages
# ------
#   base   — shared Python + system deps
#   dev    — hot-reload, volume-mounted source, debug on
#   prod   — lean, no dev tools, source baked in, gunicorn-style workers
# =============================================================================

# ── base ──────────────────────────────────────────────────────────────────────
FROM python:3.12-slim AS base

WORKDIR /app

# System deps needed by asyncpg, cryptography, psycopg
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt


# ── dev ───────────────────────────────────────────────────────────────────────
# Source is mounted at runtime via docker-compose volume — not copied in.
# Uvicorn --reload watches for changes.
FROM base AS dev

ENV ENV=local \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

EXPOSE 8000

# Command is overridden in docker-compose.yml for hot-reload.
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]


# ── prod ──────────────────────────────────────────────────────────────────────
# Source is baked in; no volume mount; multiple workers via WEB_CONCURRENCY.
FROM base AS prod

ENV ENV=cloud \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    WEB_CONCURRENCY=2

# Copy only the application code (exclude .env*, tests, scripts, etc.)
COPY app ./app
COPY alembic ./alembic
COPY alembic.ini .

EXPOSE 8000

# Run Alembic upgrade then start uvicorn with multiple workers.
# WEB_CONCURRENCY can be overridden at runtime (e.g. via cloud env vars).
CMD ["sh", "-c", \
     "alembic upgrade head && \
      uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers ${WEB_CONCURRENCY:-2}"]
