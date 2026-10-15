# Kiro Prompt — KAN-261: Home Dashboard Screen

**Ticket:** KAN-261  
**Epic:** KAN-239 — Chit Group Management (Customer)  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Home-2.1-Dashboard-Default` — node `1:467`  
**File:** `app/src/main/java/com/chitsmart/ui/home/HomeScreen.kt`

---

## Task

Implement the main home dashboard — the first screen after login.

## Sections (top to bottom)

### 1. Greeting card
- "Good morning, Priya 👋" — `headlineMedium`, dynamic greeting based on time
- Avatar circle (initials if no photo)
- Notification bell `IconButton` (top-right) — badge dot if unread notifications exist

### 2. Active Memberships summary card
- Card: white, 12dp corners, 2dp elevation
- Total invested amount (large, PrimaryNavy)
- Current month installment: amount + due date (e.g. "₹5,000 due 15 Oct 2026")
- "Pay Now" `Button` (teal fill) — navigates to Payments tab
- If no memberships: empty state "No active memberships — Browse chit groups"

### 3. Quick Actions row
- 4 icon+label chips in a horizontal `Row`:
  - Pay Installment → `AppRoutes.PAYMENTS`
  - My Statement → `AppRoutes.PAYMENT_HISTORY`
  - Download Receipt → `AppRoutes.PAYMENT_HISTORY`
  - Contact Support → `AppRoutes.HUMAN_SUPPORT`
- **NO "Bid in Auction" action**

### 4. Recent Transactions (last 3)
- Section header "Recent Transactions" + "View All" `TextButton`
- Each row: date, description, amount, status chip (Paid/Failed/Pending)

### 5. AI Assistant promo banner
- Teal gradient card, chat bubble icon
- "Ask ChitSmart AI anything" headline
- "Try it now →" link → `AppRoutes.ASSISTANT`

### 6. Announcements carousel
- 2-item `HorizontalPager`, auto-scroll every 4s
- Each item: title + subtitle (from API or static for now)

## API
`GET /api/v1/members/dashboard` — returns greeting data, membership summary, recent transactions

## Acceptance criteria
- [ ] Greeting changes based on time of day (Good morning/afternoon/evening)
- [ ] Active membership card shows correct total and next due amount
- [ ] Quick actions have exactly 4 items — NO Bid in Auction
- [ ] Recent transactions shows last 3 with correct status chips
- [ ] Empty state shown when no active memberships
- [ ] Matches Figma frame `App-Home-2.1-Dashboard-Default`
