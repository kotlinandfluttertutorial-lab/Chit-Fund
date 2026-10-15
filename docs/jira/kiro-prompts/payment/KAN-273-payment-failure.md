# Kiro Prompt — KAN-273: Payment Failure Screen — Step 4B

**Ticket:** KAN-273  
**Epic:** KAN-240 — Installment Payments  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Payment-3.6-PaymentFlow-Default` — node `1:939` (Step 4B)  
**File:** `app/src/main/java/com/chitsmart/ui/payment/PaymentFailureScreen.kt`

---

## Task

Implement Step 4B — the payment failure screen.

## Layout

- Animated red X (Lottie or AnimatedVectorDrawable)
- "Payment Failed" — `headlineMedium`, `ErrorRed`
- Failure reason from API — `bodyMedium`, `SecondaryText` (e.g. "Your bank declined this transaction")
- Amount attempted — `bodyMedium`
- Three CTAs:
  - "Retry Payment" — filled, teal, full width → back to `AppRoutes.PAYMENT_SUMMARY`
  - "Try Different Method" — outlined, teal → back to `AppRoutes.PAYMENT_METHOD`
  - "Contact Support" — text link → `AppRoutes.HUMAN_SUPPORT`

## Failure reason messages
Map gateway error codes to user-friendly messages:
- `INSUFFICIENT_FUNDS` → "Insufficient funds in your account"
- `BANK_DECLINED` → "Your bank declined this transaction"
- `TIMEOUT` → "Payment timed out. No amount was deducted."
- `INVALID_UPI` → "Invalid UPI ID. Please check and try again."
- Default → "Payment could not be processed. Please try again."

## Acceptance criteria
- [ ] Correct failure reason displayed for each error code
- [ ] Retry navigates back to payment summary
- [ ] Try Different Method navigates back to method selection
- [ ] Contact Support opens human support screen
