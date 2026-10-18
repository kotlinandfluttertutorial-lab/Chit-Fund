"""
Chat service — orchestrates the RAG + GPT-4o streaming response pipeline.

Flow for each user message:
1. Embed the user query.
2. Retrieve relevant policy chunks from pgvector.
3. Build the system prompt with policy context.
4. Call GPT-4o with streaming enabled and the four read-only tools.
5. Handle tool calls (execute against DB, append results, continue streaming).
6. Yield SSE-formatted tokens back to the HTTP response.

Design decisions:
- Uses Server-Sent Events (SSE) format: data: <json>\n\n
- Tool calls are fully resolved before streaming resumes (not interleaved).
- Maximum one round of tool calls per message to keep latency bounded.
- Never writes data — all tool calls are delegated to tool_executor.execute_tool.
"""

import json
import logging
from typing import AsyncGenerator, cast

from openai import AsyncOpenAI
from openai.types.chat import ChatCompletionMessageParam, ChatCompletionToolParam
from sqlalchemy.ext.asyncio import AsyncSession

from app.ai.prompts import build_system_prompt
from app.ai.rag_pipeline import retrieve_relevant_chunks
from app.ai.tool_executor import execute_tool
from app.ai.tools import ACCOUNT_TOOLS
from app.core.config import settings

logger = logging.getLogger(__name__)

# Lazy-initialised OpenAI client
_client: AsyncOpenAI | None = None


def _get_client() -> AsyncOpenAI:
    global _client
    if _client is None:
        if not settings.OPENAI_API_KEY:
            raise RuntimeError(
                "OPENAI_API_KEY is not configured. Set it in the .env file before using the AI assistant."
            )
        _client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
    return _client


def _sse(event: str, data: dict) -> str:
    """Format a Server-Sent Event."""
    return f"event: {event}\ndata: {json.dumps(data)}\n\n"


async def stream_chat_response(
    user_message: str,
    history: list[dict],
    member_id: str,
    db: AsyncSession,
    language: str = "en",
) -> AsyncGenerator[str, None]:
    try:
        chunks = await retrieve_relevant_chunks(
            query=user_message,
            db=db,
            top_k=5,
            language=language,
        )

        for policy_chunk in chunks:
            yield _sse(
                "policy",
                {
                    "document_name": policy_chunk.document_name,
                    "section_reference": policy_chunk.section_reference,
                    "similarity": round(policy_chunk.similarity, 3),
                },
            )

        system_prompt = build_system_prompt(
            policy_context=chunks,
            member_id=member_id,
        )

        messages: list[ChatCompletionMessageParam] = [
            {"role": "system", "content": system_prompt},
        ]

        for msg in history[-20:]:
            role = msg.get("role", "user")
            if role in ("user", "assistant"):
                messages.append({"role": role, "content": msg.get("content", "")})

        messages.append({"role": "user", "content": user_message})

        client = _get_client()

        # First streaming call with tools
        stream = await client.chat.completions.create(
            model=settings.OPENAI_MODEL,
            messages=messages,
            tools=cast(list[ChatCompletionToolParam], ACCOUNT_TOOLS),
            tool_choice="auto",
            stream=True,
            temperature=0.2,
            max_tokens=1024,
        )

        collected_content = []
        tool_calls: list[dict] = []
        finish_reason: str | None = None

        async for chunk in stream:
            delta = chunk.choices[0].delta if chunk.choices else None
            if delta is None:
                continue

            finish_reason = chunk.choices[0].finish_reason or finish_reason

            if delta.content:
                collected_content.append(delta.content)
                yield _sse("token", {"delta": delta.content})

            if delta.tool_calls:
                for tc_delta in delta.tool_calls:
                    if tc_delta.index is not None:
                        idx = tc_delta.index
                        while len(tool_calls) <= idx:
                            tool_calls.append({"id": "", "name": "", "arguments": ""})
                        if tc_delta.id:
                            tool_calls[idx]["id"] = tc_delta.id
                        if tc_delta.function:
                            if tc_delta.function.name:
                                tool_calls[idx]["name"] += tc_delta.function.name
                            if tc_delta.function.arguments:
                                tool_calls[idx]["arguments"] += tc_delta.function.arguments

        if tool_calls:
            messages.append(
                {
                    "role": "assistant",
                    "content": "".join(collected_content) or None,
                    "tool_calls": [
                        {
                            "id": tc["id"],
                            "type": "function",
                            "function": {"name": tc["name"], "arguments": tc["arguments"]},
                        }
                        for tc in tool_calls
                    ],
                }
            )

            for tc in tool_calls:
                yield _sse("tool", {"name": tc["name"]})
                try:
                    args = json.loads(tc["arguments"] or "{}")
                    tool_result = await execute_tool(
                        tool_name=tc["name"],
                        arguments=args,
                        member_id=member_id,
                        db=db,
                    )
                except Exception as exc:
                    logger.exception("Tool execution failed: tool=%r", tc["name"])
                    tool_result = json.dumps({"error": str(exc)})

                messages.append(
                    {
                        "role": "tool",
                        "tool_call_id": tc["id"],
                        "content": tool_result,
                    }
                )

            # Second streaming call after tool results
            stream2 = await client.chat.completions.create(
                model=settings.OPENAI_MODEL,
                messages=messages,
                stream=True,
                temperature=0.2,
                max_tokens=1024,
            )

            async for chunk2 in stream2:
                delta2 = chunk2.choices[0].delta if chunk2.choices else None
                if delta2 is None:
                    continue
                finish_reason = chunk2.choices[0].finish_reason or finish_reason
                if delta2.content:
                    yield _sse("token", {"delta": delta2.content})

        yield _sse("done", {"finish_reason": finish_reason or "stop"})

    except Exception:
        logger.exception("stream_chat_response failed for member %s", member_id)
        yield _sse("error", {"message": "An error occurred. Please try again."})
