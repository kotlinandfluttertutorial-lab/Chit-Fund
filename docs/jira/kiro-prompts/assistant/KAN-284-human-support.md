# Kiro Prompt — KAN-284: Human Support Escalation

**Ticket:** KAN-284  
**Epic:** KAN-237 — GenAI Assistant  
**Priority:** Medium  
**Skill:** Use the `android-screen` skill  
**File:** `app/src/main/java/com/chitsmart/ui/assistant/HumanSupportScreen.kt`

---

## Task

Implement the human support escalation flow — accessible from AI chat and Help Centre.

## Three screens in this flow

### 1. Connecting screen (`HumanSupportConnectingScreen.kt`)
- "Connecting you to a support agent…" headline
- Teal animated `CircularProgressIndicator`
- "Estimated wait: ~5 minutes" — `bodyMedium`, `SecondaryText`
- "Cancel" `TextButton` → back to AI chat

### 2. Support chat screen (`HumanSupportChatScreen.kt`)
- Top bar: agent name + avatar + "Online" green dot
- Chat bubbles same pattern as AI chat, but:
  - Agent messages: white card, left-aligned, no policy chips
  - User messages: PrimaryNavy fill, right-aligned
- "Share AI conversation with agent" toggle (above input bar) — on by default
- Input bar: text field + send button

### 3. Session ended screen
- "Support session ended" headline
- "Rate your support experience" — 5-star rating
- "Go to Home" `Button`

## Note (current release)
In this release, the support chat can be implemented as a stub that:
- Shows the connecting animation for 2 seconds
- Then displays a "Support agents are currently unavailable — please email support@chitsmart.in" message with an email CTA

Real WebSocket-based live chat is a future enhancement.

## Acceptance criteria
- [ ] Connecting animation plays on entry
- [ ] Share conversation toggle is available
- [ ] Stub message shown with email contact (current release)
- [ ] Cancel returns to previous screen
