"""
RAG retrieval pipeline — fetches relevant policy chunks for a user query.

Uses pgvector cosine distance on the policy_chunks.embedding column to
return the top-k most semantically similar chunks to the query.

The embedding column is created by Alembic migration 001 as:
    ALTER TABLE policy_chunks ADD COLUMN embedding vector(1536);

We issue a raw SQL query so this module does not require pgvector as a
Python dependency in environments where pgvector is not installed.
"""

import logging
from dataclasses import dataclass

from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncSession

from app.ai.embeddings import get_embedding

logger = logging.getLogger(__name__)


@dataclass
class RetrievedChunk:
    """A policy chunk returned from the RAG retrieval step."""

    chunk_id: str
    document_name: str
    section_reference: str
    content: str
    language: str
    similarity: float  # cosine similarity (0–1, higher = more similar)


async def retrieve_relevant_chunks(
    query: str,
    db: AsyncSession,
    top_k: int = 5,
    language: str = "en",
) -> list[RetrievedChunk]:
    """
    Retrieve the top-k policy chunks most relevant to the query.

    Steps:
    1. Embed the query using text-embedding-3-small.
    2. Run a pgvector cosine distance query against policy_chunks.embedding.
    3. Return up to top_k chunks ordered by similarity descending.

    Args:
        query:    The user's natural-language question.
        db:       Async SQLAlchemy session.
        top_k:    Number of chunks to retrieve (default 5).
        language: Preferred language for chunk retrieval. Falls back to 'en'
                  if no chunks are available in the requested language.

    Returns:
        List of RetrievedChunk ordered by similarity (highest first).
        Returns an empty list if the policy_chunks table is empty or if
        the pgvector extension is not installed.
    """
    query_vector = await get_embedding(query)

    # pgvector <=> operator = cosine distance (0 = identical, 2 = opposite)
    # Convert to similarity = 1 - (cosine_distance)
    sql = text(
        """
        SELECT
            id::text                       AS chunk_id,
            document_name,
            section_reference,
            content,
            language,
            1 - (embedding <=> CAST(:embedding AS vector)) AS similarity
        FROM policy_chunks
        WHERE language = :language
          AND embedding IS NOT NULL
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :top_k
        """
    )

    try:
        result = await db.execute(
            sql,
            {
                "embedding": str(query_vector),  # pgvector accepts '[x, y, ...]' string
                "language": language,
                "top_k": top_k,
            },
        )
        rows = result.fetchall()
    except Exception as exc:
        # pgvector not installed or table empty — degrade gracefully
        logger.warning(
            "RAG retrieval failed (pgvector may not be installed): %s", exc
        )
        return []

    chunks = [
        RetrievedChunk(
            chunk_id=row.chunk_id,
            document_name=row.document_name,
            section_reference=row.section_reference,
            content=row.content,
            language=row.language,
            similarity=float(row.similarity),
        )
        for row in rows
    ]

    logger.debug(
        "RAG retrieved %d chunks for query %r (language=%s)",
        len(chunks),
        query[:80],
        language,
    )
    return chunks
