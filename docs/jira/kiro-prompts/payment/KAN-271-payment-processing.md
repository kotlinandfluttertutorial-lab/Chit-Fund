# Kiro Prompt — KAN-271: Payment Processing Screen — Step 3

**Ticket:** KAN-271  
**Epic:** KAN-240 — Installment Payments  
**Priority:** Highest  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Payment-3.6-PaymentFlow-Default` — node `1:939` (Step 3)  
**File:** `app/src/main/java/com/chitsmart/ui/payment/PaymentProcessingScreen.kt`

---

## Task

Implement Step 3 of the payment flow — the full-screen processing state.

## Implementation

- Full-screen composable, `background(MaterialTheme.colorScheme.background)`
- Centred `Column`:
  - ChitSmart logo (top, 64dp)
  - `Spacer(weight = 1f)`
  - `CircularProgressIndicator` — 56dp, teal (`MaterialTheme.colorScheme.tertiary`)
  - "Processing your payment…" — `headlineSmall`, PrimaryNavy
  - Spacer 16dp
  - "Please do not press back or close the app" — `bodyMedium`, `SecondaryText`
  - `Spacer(weight = 1f)`
  - Amount being processed (small, `SecondaryText`) at bottom

### Back navigation BLOCKED
```kotlin
BackHandler(enabled = true) { /* do nothing — intentionally block back */ }
```

### State machine
- ViewModel calls `POST /api/v1/payments` to create payment intent
- Polls or waits for gateway webhook result
- On `SUCCESSFUL` → navigate to `AppRoutes.PAYMENT_SUCCESS`
- On `FAILED` → navigate to `AppRoutes.PAYMENT_FAILURE`
- Timeout after 120s → navigate to failure screen with message "Payment timed out"

## Acceptance criteria
- [ ] Back button does nothing (hardware + gesture)
- [ ] Spinner is teal and animated
- [ ] Navigates to Success on successful payment
- [ ] Navigates to Failure on failed payment
- [ ] Timeout after 120s shows failure screen
