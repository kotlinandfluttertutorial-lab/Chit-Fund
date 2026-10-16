"""
Embedding utility — wraps the OpenAI embeddings API.

Uses text-embedding-3-small which produces 1536-dimensional vectors.
These vectors are stored in the policy_chunks.embedding column (pgvector)
and used for cosine-similarity retrieval in rag_pipeline.py.
"""

import logging
from typing import Optional

from openai import AsyncOpenAI

from app.core.config import settings

logger = logging.getLogger(__name__)

# Lazy-initialised client — created once on first use
_client: Optional[AsyncOpenAI] = None


def _get_client() -> AsyncOpenAI:
    global _client
    if _client is None:
        if not settings.OPENAI_API_KEY:
            raise RuntimeError(
                "OPENAI_API_KEY is not configured. Set it in the .env file before using the AI assistant."
            )
        _client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
    return _client


async def get_embedding(text: str) -> list[float]:
    """
    Generate a 1536-dimensional embedding vector for the given text.

    Args:
        text: The text to embed. Whitespace is normalised before sending
              to the API to avoid billing for redundant tokens.

    Returns:
        A list of 1536 floats representing the embedding vector.

    Raises:
        RuntimeError: If OPENAI_API_KEY is not set.
        openai.OpenAIError: On API errors (rate limit, auth failure, etc.).
    """
    # Normalise whitespace — embedding quality is not affected and it
    # reduces token count for very long chunks.
    normalised = " ".join(text.split())

    client = _get_client()
    response = await client.embeddings.create(
        input=normalised,
        model=settings.OPENAI_EMBEDDING_MODEL,  # text-embedding-3-small
    )

    vector: list[float] = response.data[0].embedding
    logger.debug(
        "Generated embedding: model=%s, dimensions=%d",
        settings.OPENAI_EMBEDDING_MODEL,
        len(vector),
    )
    return vector
