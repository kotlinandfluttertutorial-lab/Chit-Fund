# Kiro Prompt — KAN-280: AI Chat Interface

**Ticket:** KAN-280  
**Epic:** KAN-237 — GenAI Assistant  
**Priority:** High  
**Skill:** Use the `android-screen` skill + `ai-rag-assistant` skill  
**Figma:** `App-Assistant-5.2-AIChat-Default` — node `1:1220`  
**File:** `app/src/main/java/com/chitsmart/ui/assistant/AIChatScreen.kt`

---

## Task

Implement the AI chat interface — the conversation screen for ChitSmart AI.

## Chat bubbles

### User messages (right-aligned)
- Background: `PrimaryNavy`, corner 12dp (top-right corner 4dp)
- White text

### AI messages (left-aligned)
- White card with 2dp teal left border
- `MainText` colour
- Policy citation chips below AI message: teal chip "RBI Chit Fund Regulation §4.2" (tappable → `AppRoutes.POLICY_REFERENCE`)
- Per-message action row: Copy icon | Share icon | Thumbs Up | Thumbs Down

### Typing indicator
- Three animated dots (fade in/out sequence) while AI is generating

### Quick reply chips
- Appears below the last AI message
- 2–3 contextual suggestions (e.g. "Tell me more", "Show my balance")

## Input bar
- `TextField` multiline (max 4 lines), "Type your message…"
- Microphone `IconButton`
- Send `IconButton` (teal)

## Streaming response
- Connect to `POST /api/v1/ai/chat` — handles SSE (Server-Sent Events)
- Use OkHttp `EventSource` for streaming
- Append characters to AI message bubble as they arrive

## Conversation history
- Store in memory (ViewModel) for multi-turn context
- Pass previous messages in each API request

## Acceptance criteria
- [ ] User and AI bubbles display on correct sides
- [ ] Typing indicator (3 dots) shown while waiting for response
- [ ] Policy citation chips appear and navigate to policy reference modal
- [ ] Streaming: text appears character-by-character (or token-by-token)
- [ ] Copy action copies the AI message text
- [ ] Thumbs up/down sends feedback to API
- [ ] Quick reply chips appear after AI response
- [ ] Matches Figma frame `App-Assistant-5.2-AIChat-Default`
