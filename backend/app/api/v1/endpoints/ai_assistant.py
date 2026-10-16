"""
AI Assistant endpoints.

POST /api/v1/ai/chat
    Accepts a user message and conversation history, returns a
    Server-Sent Events stream of tokens from GPT-4o.

Authentication: JWT Bearer (get_current_member dependency).
No write operations are performed by any endpoint in this module.
"""

from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.ai.chat_service import stream_chat_response
from app.core.database import get_db
from app.core.deps import get_current_member
from app.models.member import Member

router = APIRouter()


# ── Request schema ─────────────────────────────────────────────────────────────


class ChatMessage(BaseModel):
    """A single message in the conversation history."""

    role: str = Field(..., pattern="^(user|assistant)$")
    content: str = Field(..., min_length=1, max_length=4096)


class ChatRequest(BaseModel):
    """Request body for POST /ai/chat."""

    message: str = Field(..., min_length=1, max_length=2048, description="The user's message.")
    history: list[ChatMessage] = Field(
        default_factory=list,
        max_length=20,
        description="Previous conversation turns (user + assistant pairs). Max 20 messages.",
    )
    language: str = Field(
        default="en",
        pattern="^[a-z]{2,3}$",
        description="BCP-47 language code for RAG retrieval (e.g. 'en', 'hi', 'ta').",
    )


# ── Endpoints ──────────────────────────────────────────────────────────────────


@router.post(
    "/chat",
    summary="Stream an AI assistant response",
    description=(
        "Accepts the member's message and optional conversation history. "
        "Returns a Server-Sent Events stream of tokens. "
        "The assistant can access the member's own account data (read-only) "
        "and approved policy documents via RAG. "
        "No transactions are initiated by this endpoint."
    ),
    response_description="SSE stream: events are 'token', 'policy', 'tool', 'done', 'error'.",
)
async def chat(
    body: ChatRequest,
    current_member: Member = Depends(get_current_member),
    db: AsyncSession = Depends(get_db),
) -> StreamingResponse:
    """
    Stream a GPT-4o response for the authenticated member.

    SSE event types:
    - **token**:  {"delta": "<partial text>"} — stream this to the chat UI
    - **policy**: {"document_name": "...", "section_reference": "..."}
    - **tool**:   {"name": "<tool_name>"} — AI is fetching account data
    - **done**:   {"finish_reason": "stop"} — response complete
    - **error**:  {"message": "..."} — something went wrong
    """
    history = [{"role": m.role, "content": m.content} for m in body.history]
    member_id = str(current_member.id)

    generator = stream_chat_response(
        user_message=body.message,
        history=history,
        member_id=member_id,
        db=db,
        language=body.language,
    )

    return StreamingResponse(
        generator,
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",  # disable nginx buffering for SSE
        },
    )
