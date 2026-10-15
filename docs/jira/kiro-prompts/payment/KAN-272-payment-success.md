# Kiro Prompt — KAN-272: Payment Success Screen — Step 4A

**Ticket:** KAN-272  
**Epic:** KAN-240 — Installment Payments  
**Priority:** Highest  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Payment-3.6-PaymentFlow-Default` — node `1:939` (Step 4A)  
**File:** `app/src/main/java/com/chitsmart/ui/payment/PaymentSuccessScreen.kt`

---

## Task

Implement Step 4A — the payment success confirmation screen.

## Layout

- Full-screen, background `#F7FAFC`
- Animated green checkmark (Lottie `success_check.json` or `AnimatedVectorDrawable`)
  - Play once on entry, settle into a static check
- Amount paid — "₹2,500" — `headlineLarge`, bold, PrimaryNavy
- "Payment Successful!" — `headlineMedium`, SuccessGreen
- Details card (white, 12dp corners):
  - Transaction ID: `TXN-20261015-XXXXX`
  - Date & Time: `15 Oct 2026, 10:32 AM`
  - Group: `Family Savings 50K`
  - Installment: `October 2026`
  - Payment method: `UPI — user@upi`
- Three CTAs (bottom):
  - "Download Receipt" — outlined button, teal → `AppRoutes.RECEIPT`
  - "View Statement" — text button → `AppRoutes.PAYMENT_HISTORY`
  - "Done" — filled button, PrimaryNavy, full width → `AppRoutes.HOME`

### Back navigation
- Hardware back and gesture both go to `AppRoutes.HOME` (clear payment flow from back stack)

## Acceptance criteria
- [ ] Checkmark animation plays on screen entry
- [ ] All payment details shown correctly from API response
- [ ] Download Receipt navigates to receipt screen
- [ ] Done clears payment flow from back stack and returns to Home
