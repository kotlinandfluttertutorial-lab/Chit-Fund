# Kiro Prompt — KAN-269: Payment Summary Screen — Step 1

**Ticket:** KAN-269  
**Epic:** KAN-240 — Installment Payments  
**Priority:** Highest  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Payment-3.6-PaymentFlow-Default` — node `1:939` (Step 1)  
**File:** `app/src/main/java/com/chitsmart/ui/payment/PaymentSummaryScreen.kt`

---

## Task

Implement Step 1 of the payment flow — the summary screen before method selection.

## Layout

- Top app bar: "Pay Installment" + back arrow
- Summary card (white, 12dp corners):
  - Group name — `headlineSmall`
  - Installment month — "October 2026" — `bodyMedium`, `SecondaryText`
  - Divider
  - Row: "Installment amount" + "₹2,500" 
  - Row: "Late fee" + "₹50" — visible only if late fee > 0, amber colour, info icon tooltip
  - Divider
  - Row: "Total payable" (bold) + "₹2,550" (large, teal, bold)
- "Pay ₹2,550" primary `Button` — full width, PrimaryNavy
  - Amount in button label updates dynamically from API

## Navigation
- Back → `AppRoutes.PAYMENTS`
- Pay CTA → `AppRoutes.PAYMENT_METHOD` with installmentId

## Acceptance criteria
- [ ] Late fee row hidden when late fee is ₹0
- [ ] Total updates correctly when late fee is present
- [ ] Pay button label shows the actual total amount
- [ ] Back arrow returns to Payments tab
