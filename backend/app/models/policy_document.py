"""
PolicyChunk model — stores chunked policy document text for RAG retrieval.

The `embedding` column is created via a raw ALTER TABLE in migration 001
(using pgvector). It is NOT defined as a SQLAlchemy Column here to avoid
requiring pgvector to be installed as a Python package in all environments.
Instead, it is referenced as a mapped_column for type-checked access where
pgvector is available, but gracefully omitted otherwise.

API exposure: read-only; no endpoints exposed to customers.
Admin RAG pipeline uses this model internally via rag_pipeline.py.
"""

import uuid
from datetime import datetime, timezone
from typing import Optional

from sqlalchemy import DateTime, Integer, String, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base


class PolicyChunk(Base):
    """
    A chunk of a policy document for vector similarity search.

    Columns:
        id              UUID primary key
        document_name   Human-readable document name (e.g. "Chit Funds Act 1982")
        section_reference  Section number / header (e.g. "Section 4")
        content         Raw text of the chunk
        language        BCP-47 language code, default 'en'
        page_number     Optional source page number
        created_at      Timestamp when the chunk was ingested

    The `embedding` column (vector(1536)) is added to the table via:
        ALTER TABLE policy_chunks ADD COLUMN embedding vector(1536);
    in Alembic migration 001. It is not mapped here to avoid hard-coupling
    the Python model layer to the pgvector extension at import time.
    """

    __tablename__ = "policy_chunks"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        nullable=False,
    )
    document_name: Mapped[str] = mapped_column(String(512), nullable=False, index=True)
    section_reference: Mapped[str] = mapped_column(String(255), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    language: Mapped[str] = mapped_column(String(10), default="en", nullable=False)
    page_number: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        nullable=False,
    )

    def __repr__(self) -> str:
        return f"<PolicyChunk id={self.id!r} doc={self.document_name!r} section={self.section_reference!r}>"
