# Kiro Prompt — KAN-281: AI Backend — Chat Endpoint with RAG

**Ticket:** KAN-281  
**Epic:** KAN-251 — Backend — Core API  
**Priority:** High  
**Skill:** Use the `ai-rag-assistant` skill  
**File:** `backend/app/api/v1/endpoints/ai_assistant.py` + `backend/app/ai/`

---

## Task

Implement the `POST /api/v1/ai/chat` endpoint with RAG pipeline, read-only account tools, response streaming, and multilingual support.

## Full implementation follows the `ai-rag-assistant` skill exactly.

### Endpoint
`POST /api/v1/ai/chat`
- Auth: Bearer token (member)
- Returns: `text/event-stream` (SSE)
- Request: `{ "message": "...", "conversation_history": [...] }`

### RAG pipeline steps
1. Generate embedding for user message via `text-embedding-3-small`
2. Query `policy_chunks` table using pgvector cosine similarity (top 5 chunks)
3. Build system prompt with policy context + member ID
4. Call `gpt-4o` with tools (`ACCOUNT_TOOLS`) + streaming
5. Execute tool calls server-side (read-only DB queries for the authenticated member)
6. Stream response tokens back via SSE

### Read-only tools to implement
- `get_outstanding_balance` → `SELECT SUM(total_amount) FROM payments WHERE member_id=... AND status=PENDING`
- `get_next_installment_due` → nearest due installment schedule entry
- `get_active_memberships` → list of active `ChitMembership` records
- `get_payment_history` → last N payments for the member

### Language support
- Detect language from user message (use `langdetect` Python library)
- Pass language hint in system prompt
- Respond in detected language (English or Hindi)

### Hard guardrails (in system prompt)
- Never discuss KYC verification
- Never discuss auctions or bidding
- Never write/modify data
- Only access data for the authenticated member's ID

## Acceptance criteria
- [ ] SSE streams response tokens to the client
- [ ] Policy chunks retrieved correctly from pgvector
- [ ] Tool calls execute read-only queries and return data to the LLM
- [ ] Response in Hindi when user writes in Hindi
- [ ] If asked about auctions or KYC: responds with "not available yet" message
- [ ] Response time (first token) < 2 seconds under normal load
- [ ] All tool calls are read-only — no DB writes allowed
