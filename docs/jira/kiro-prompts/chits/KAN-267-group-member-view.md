# Kiro Prompt — KAN-267: Group Member View

**Ticket:** KAN-267  
**Epic:** KAN-239 — Chit Group Management (Customer)  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-MyChits-3.5-GroupMemberView-Default`  
**File:** `app/src/main/java/com/chitsmart/ui/chits/GroupMemberViewScreen.kt`

---

## Task

Implement the group member view — the detailed view of a single membership. Opens from Active Memberships card tap.

## Sections

### Header
- Group name + my member ID (e.g. "Member #07")
- Status chip: Active / Completed

### Installment Calendar
- Grid layout: 6 columns × N rows (all months)
- Each cell: month number + status colour
  - Paid: teal fill + white text
  - Due: amber fill + white text
  - Upcoming: light gray fill + `SecondaryText`
  - Overdue: red fill + white text
- Tap on a paid cell → navigate to `AppRoutes.RECEIPT` for that payment

### Floating Action Button
- "Pay Now" FAB (teal, `₹` icon) — visible only when an installment is due
- Taps → `AppRoutes.PAYMENT_SUMMARY`

### Payout History section (read-only)
- Section header: "Payout History"
- If payout received: list row — date, amount (green), bank reference
- If no payout: "No payout received yet"
- **NO "Bid history" accordion — this is deferred with auctions**

## API
`GET /api/v1/members/memberships/{membershipId}` — returns full membership details including calendar + payout history

## Acceptance criteria
- [ ] Installment calendar shows correct colours for each status
- [ ] Tapping a paid cell opens the receipt for that payment
- [ ] FAB only visible when installment is due
- [ ] Payout History section is read-only — no actions
- [ ] NO bid history accordion anywhere on this screen
