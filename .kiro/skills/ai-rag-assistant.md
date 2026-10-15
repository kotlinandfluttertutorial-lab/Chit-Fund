---
name: ai-rag-assistant
description: Instructions for implementing the ChitSmart GenAI assistant backend — RAG pipeline, policy document ingestion, OpenAI tool calls, streaming, and multilingual support.
---

# Skill: Implement the ChitSmart AI Assistant

Use this skill when working on the `/ai/chat` endpoint, RAG pipeline, policy document ingestion, or OpenAI tool integrations.

## Stack

- **LLM:** OpenAI `gpt-4o` (configurable via `settings.OPENAI_MODEL`)
- **Embeddings:** OpenAI `text-embedding-3-small`
- **Vector store:** pgvector (PostgreSQL extension)
- **Orchestration:** LangChain (Python)
- **Streaming:** Server-Sent Events (SSE) via FastAPI `StreamingResponse`
- **Languages:** English + Hindi minimum; auto-detect from user input

---

## Hard rules for the AI assistant

1. The assistant may **only read** data. It must **never** write, update, approve, or initiate transactions.
2. The assistant may **only read the authenticated user's own account data**. It must never return data for another member.
3. Every policy citation must include the **document name and section reference** (e.g. "RBI Chit Fund Circular 2023, Section 4.2").
4. If the assistant cannot answer confidently from approved documents or account data, it must say so and offer human support escalation — never hallucinate.
5. The assistant must **not** answer questions about KYC (deferred), auctions or bidding (deferred).

---

## File structure

```
app/ai/
├── chat_service.py          — main chat endpoint logic, streaming
├── rag_pipeline.py          — document retrieval from pgvector
├── tools.py                 — OpenAI function tools (read-only account queries)
├── embeddings.py            — text → vector via OpenAI embeddings API
├── document_ingestion.py    — CLI script to ingest policy PDFs into pgvector
└── prompts.py               — system prompt templates
```

---

## Step 1 — Vector store model

```python
# app/models/policy_document.py
from sqlalchemy import Column, String, Text, Integer
from sqlalchemy.dialects.postgresql import UUID, ARRAY
from pgvector.sqlalchemy import Vector
from app.models.base import Base, UUIDMixin, TimestampMixin


class PolicyChunk(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "policy_chunks"

    document_name = Column(String(255), nullable=False)
    section_reference = Column(String(255), nullable=False)   # e.g. "Section 4.2"
    content = Column(Text, nullable=False)
    embedding = Column(Vector(1536), nullable=False)          # text-embedding-3-small dimension
    language = Column(String(10), default="en")               # "en" | "hi" | "ta" etc.
    page_number = Column(Integer, nullable=True)
```

---

## Step 2 — RAG retrieval

```python
# app/ai/rag_pipeline.py
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from app.models.policy_document import PolicyChunk
from app.ai.embeddings import get_embedding


async def retrieve_relevant_chunks(
    query: str,
    db: AsyncSession,
    top_k: int = 5,
    language: str = "en",
) -> list[PolicyChunk]:
    """Retrieve top-k policy chunks most similar to the query."""
    query_embedding = await get_embedding(query)

    result = await db.execute(
        select(PolicyChunk)
        .order_by(PolicyChunk.embedding.cosine_distance(query_embedding))
        .limit(top_k)
    )
    return list(result.scalars().all())
```

---

## Step 3 — Read-only account tools

```python
# app/ai/tools.py
from openai.types.chat import ChatCompletionToolParam

# These tools are passed to OpenAI as function definitions.
# The assistant calls them; the backend executes them against the DB for the authenticated member.
# They are READ-ONLY. Never add write operations.

ACCOUNT_TOOLS: list[ChatCompletionToolParam] = [
    {
        "type": "function",
        "function": {
            "name": "get_outstanding_balance",
            "description": "Get the total outstanding installment balance for the authenticated member",
            "parameters": {"type": "object", "properties": {}, "required": []},
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_next_installment_due",
            "description": "Get the next installment due date and amount for the authenticated member",
            "parameters": {"type": "object", "properties": {}, "required": []},
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_active_memberships",
            "description": "List the active chit group memberships for the authenticated member",
            "parameters": {"type": "object", "properties": {}, "required": []},
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_payment_history",
            "description": "Get recent payment history for the authenticated member",
            "parameters": {
                "type": "object",
                "properties": {
                    "limit": {"type": "integer", "description": "Number of recent payments to return, default 5"}
                },
                "required": [],
            },
        },
    },
]
```

---

## Step 4 — Chat service with streaming

```python
# app/ai/chat_service.py
import json
from openai import AsyncOpenAI
from fastapi.responses import StreamingResponse
from app.core.config import settings
from app.ai.rag_pipeline import retrieve_relevant_chunks
from app.ai.tools import ACCOUNT_TOOLS
from app.ai.prompts import build_system_prompt

client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)


async def stream_chat_response(
    user_message: str,
    conversation_history: list[dict],
    member_id: str,
    db,
) -> StreamingResponse:
    # 1. Retrieve relevant policy chunks
    chunks = await retrieve_relevant_chunks(user_message, db)
    policy_context = "\n\n".join(
        f"[{c.document_name}, {c.section_reference}]\n{c.content}"
        for c in chunks
    )

    # 2. Build messages
    messages = [
        {"role": "system", "content": build_system_prompt(policy_context, member_id)},
        *conversation_history,
        {"role": "user", "content": user_message},
    ]

    # 3. Stream from OpenAI
    async def generate():
        async with client.chat.completions.stream(
            model=settings.OPENAI_MODEL,
            messages=messages,
            tools=ACCOUNT_TOOLS,
            stream=True,
        ) as stream:
            async for chunk in stream:
                delta = chunk.choices[0].delta
                if delta.content:
                    yield f"data: {json.dumps({'type': 'text', 'content': delta.content})}\n\n"
        yield "data: [DONE]\n\n"

    return StreamingResponse(generate(), media_type="text/event-stream")
```

---

## Step 5 — System prompt template

```python
# app/ai/prompts.py

def build_system_prompt(policy_context: str, member_id: str) -> str:
    return f"""You are ChitSmart AI, a helpful assistant for ChitSmart chit fund members.

RULES:
1. You may ONLY read data. Never write, update, approve, or initiate transactions.
2. You may ONLY access account data for member ID: {member_id}. Never return data for others.
3. Cite every policy statement with its document name and section (e.g. "RBI Chit Fund Circular 2023, §4.2").
4. If you cannot answer confidently, say so and suggest the user contacts support.
5. Do NOT answer questions about KYC verification or auction/bidding (these features are not active).
6. Respond in the same language the user writes in. Support English and Hindi.

POLICY CONTEXT (from approved documents):
{policy_context}

Use the provided tools to look up the member's live account data when relevant.
"""
```

---

## Step 6 — API endpoint

```python
# app/api/v1/endpoints/ai_assistant.py
from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.deps import get_db, get_current_member
from app.ai.chat_service import stream_chat_response

router = APIRouter()


class ChatRequest(BaseModel):
    message: str
    conversation_history: list[dict] = []


@router.post("/chat")
async def chat(
    request: ChatRequest,
    current_member=Depends(get_current_member),
    db: AsyncSession = Depends(get_db),
):
    """Stream AI assistant response. Returns Server-Sent Events."""
    return await stream_chat_response(
        user_message=request.message,
        conversation_history=request.conversation_history,
        member_id=str(current_member.id),
        db=db,
    )
```

---

## Document ingestion (one-time CLI)

```python
# app/ai/document_ingestion.py
# Run: python -m app.ai.document_ingestion --pdf path/to/policy.pdf --name "RBI Chit Fund Circular 2023"
# Chunks the PDF by section, generates embeddings, inserts into policy_chunks table.
```

Place policy PDFs in `backend/data/approved_policies/`. Only documents in this directory may be ingested.

---

## Scope guard

The assistant must **not** discuss or invoke:
- KYC verification status or document upload
- Auction dates, bid amounts, or bidding advice
- Payout approval status or approval actions

If asked, respond: "This feature isn't available yet. Please contact our support team for assistance."
