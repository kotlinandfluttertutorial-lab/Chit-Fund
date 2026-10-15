# Kiro Prompt — KAN-276: Payout History Screen — Read-Only

**Ticket:** KAN-276  
**Epic:** KAN-240 — Installment Payments  
**Priority:** Medium  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Payments-4.2-PayoutHistory-Default` — node `1:2471`  
**File:** `app/src/main/java/com/chitsmart/ui/payment/PayoutHistoryScreen.kt`

---

## Task

Implement the payout history screen — read-only display of payouts received.

## Layout

- Top app bar: "Payouts Received"
- `LazyColumn` of payout rows:
  - Group name — `headlineSmall`
  - Payout month + year — `bodyMedium`, `SecondaryText`
  - Net amount — large, SuccessGreen
  - Bank reference — `bodySmall`, `SecondaryText`
  - Status chip: Credited (green) | Processing (amber) | Pending (gray)
  - "Download Certificate" `TextButton` per row (where status = Credited)

### Payout detail bottom sheet
- Tap row → expand bottom sheet with:
  - Gross amount, foreman commission deducted, net amount
  - Bank account (masked: XXXX 1234)
  - Transfer date, bank reference
  - UPI/IMPS reference (if available)

### Empty state
- "No payouts received yet"
- Body: "Active members may receive a payout when a chit cycle completes"

## API
`GET /api/v1/payouts` — paginated read-only payout history for authenticated member

## Acceptance criteria
- [ ] Read-only — no action buttons other than Download Certificate
- [ ] Empty state shown when no payouts
- [ ] Bottom sheet expands with full payout details on row tap
- [ ] Download Certificate opens PDF
- [ ] NO approval or request actions anywhere on this screen
- [ ] Matches Figma frame `App-Payments-4.2-PayoutHistory-Default`
