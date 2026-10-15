# Kiro Prompt — KAN-274: Payment History & Statement

**Ticket:** KAN-274  
**Epic:** KAN-240 — Installment Payments  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Payment-3.7-PaymentHistory-Default` — node `1:2165`  
**File:** `app/src/main/java/com/chitsmart/ui/payment/PaymentHistoryScreen.kt`

---

## Task

Implement the payment history and account statement screen.

## Sections

### Outstanding Balance card (top)
- Green card: "₹0 — All paid up ✓" when no outstanding balance
- Red card: "₹X,XXX outstanding" + "Pay Now" `Button` (teal) when overdue

### Filter row
- Date range picker (start date → end date, `DateRangePicker`)
- Group selector dropdown
- Status chips: All | Paid | Failed | Pending

### Transaction list (`LazyColumn`)
Each row:
- Date (left) — `bodySmall`, `SecondaryText`
- Description: "Installment — Oct 2026, Family Savings 50K" — `bodyMedium`
- Amount — `bodyMedium`, right-aligned
- Status chip: green=Paid, red=Failed, amber=Pending
- Receipt icon `IconButton` (trailing) → `AppRoutes.RECEIPT` for that payment

### Download Statement FAB
- Extended FAB: "Download Statement" + download icon
- Calls `GET /api/v1/payments/statement?format=pdf` → opens PDF in device viewer

## API
`GET /api/v1/payments` — query: `group_id`, `status`, `from_date`, `to_date`, `page`, `page_size`

## Acceptance criteria
- [ ] Outstanding balance card shows correct colour and state
- [ ] Filters work correctly together
- [ ] Receipt icon opens the receipt for that specific payment
- [ ] Download Statement generates and opens a PDF
- [ ] Infinite scroll or pagination works for large transaction lists
- [ ] Matches Figma frame `App-Payment-3.7-PaymentHistory-Default`
