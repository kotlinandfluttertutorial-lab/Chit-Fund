# Kiro Prompt — KAN-279: AI Assistant Home Screen

**Ticket:** KAN-279  
**Epic:** KAN-237 — GenAI Assistant  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Assistant-5.1-AssistantHome-Default`  
**File:** `app/src/main/java/com/chitsmart/ui/assistant/AssistantHomeScreen.kt`

---

## Task

Implement the AI assistant home screen — the landing page for the Assistant tab.

## Layout

### Header
- "ChitSmart AI" — `headlineMedium`
- Teal robot/assistant avatar (48dp circle)
- Language selector chip row (horizontal scroll):
  - English | हिंदी | தமிழ் | తెలుగు | ಕನ್ನಡ | മലയാളം
  - Selected chip: teal fill + white text
  - Saves selection to DataStore

### Suggested questions section
Three tappable suggestion cards:
1. "What is my outstanding balance?"
2. "When is my next installment due?"
3. "Explain how a chit fund works"

**NO auction-related suggested questions.**

Each card: question text + chevron right icon. Tap → navigate to AI Chat with the question pre-filled.

### Recent conversations list
- If previous conversations exist: list with title + timestamp + chevron
- Empty state: "Start a conversation below"

### Input bar (bottom, always visible)
- `TextField` with placeholder "Ask me anything…"
- Microphone `IconButton` (right)
- Send `IconButton` (right, teal, enabled when text non-empty)
- On Send: navigate to `AppRoutes.AI_CHAT` with message

## Acceptance criteria
- [ ] Language selector saves preference and shows correct selection
- [ ] 3 suggested questions visible with NO auction question
- [ ] Tapping a suggestion opens chat with that question pre-filled
- [ ] Send button navigates to AI chat
- [ ] Recent conversations show previous sessions
