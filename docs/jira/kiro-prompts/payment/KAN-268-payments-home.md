# Kiro Prompt — KAN-268: Payments Home Screen

**Ticket:** KAN-268  
**Epic:** KAN-240 — Installment Payments  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Payments-4.1-PaymentsHome-Default` — node `1:717`  
**File:** `app/src/main/java/com/chitsmart/ui/payment/PaymentsHomeScreen.kt`

---

## Task

Implement the Payments tab landing screen — replaces what was the Auctions tab.

## Sections

### Active dues card
- "Installments Due" section header
- `LazyColumn` of due installment rows per group:
  - Group name | Amount | Due date | "Pay" `Button` (teal)
- If nothing due: green card "All installments are up to date ✓"

### Filter chips row
- Due Today | Overdue | Paid This Month
- Filters the dues list

### Quick links
- "View Payment History" → `AppRoutes.PAYMENT_HISTORY`
- "View Payout History" → `AppRoutes.PAYOUT_HISTORY`

## API
`GET /api/v1/installments/due` — returns list of due installments for the authenticated member

## Acceptance criteria
- [ ] Shows all due installments grouped by group
- [ ] "Pay" button per row navigates to payment summary for that installment
- [ ] Empty state ("all up to date") shown when nothing is due
- [ ] Filter chips correctly filter the list
- [ ] Matches Figma frame `App-Payments-4.1-PaymentsHome-Default`
