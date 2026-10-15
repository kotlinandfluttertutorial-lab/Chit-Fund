# Kiro Implementation Prompt — CS-EP-03: Installment Payments

**Epic:** CS-EP-03  
**Figma file:** https://www.figma.com/design/H3sDKKBcj9dveyJdgs7Jn5/Untitled  
**Figma frames:** App-Payment-3.6-PaymentFlow-Default (1:939), App-Payment-3.7-PaymentHistory-Default (1:2165), App-Payments-4.1-PaymentsHome-Default (1:717), App-Payments-4.2-PayoutHistory-Default (1:2471)  
**Spec:** .kiro/specs/payment/  
**Stack:** Android (Kotlin, Jetpack Compose) + FastAPI backend + Razorpay gateway

---

## Task

Implement the complete installment payment flow, payment history, digital receipts, and the Payments tab.

---

## Android — screens to implement

### Payments Home Tab (`ui/payment/PaymentsHomeScreen.kt`)
- Active dues card: list each group with overdue/due amount + "Pay" CTA per row
- Quick links: "View Payment History", "View Payout History"
- Quick filters: Due Today | Overdue | Paid This Month (chips)
- Empty state: "All installments are up to date ✓"

### Payment Flow (multi-step, single `NavBackStackEntry` ViewModel)

**Step 1 — Summary** (`ui/payment/PaymentSummaryScreen.kt`)
- Group name, installment month, due amount (₹)
- Late fee row (visible only if > ₹0) with info icon tooltip
- Total amount (larger, teal)
- "Pay ₹X,XXX" primary CTA

**Step 2 — Select Method** (`ui/payment/SelectMethodScreen.kt`)
- Segmented cards: UPI (default selected, teal border), Net Banking, Debit Card, NEFT/RTGS
- UPI section: text field for VPA, or toggle to QR code view
- Saved UPI IDs from API: radio list
- "Add New UPI ID" TextButton
- "Continue" CTA

**Step 3 — Processing** (`ui/payment/PaymentProcessingScreen.kt`)
- Full-screen, no back navigation (`BackHandler { /* do nothing */ }`)
- Large `CircularProgressIndicator` (teal)
- "Processing your payment…"
- "Please do not press back or close the app"
- ChitSmart logo bottom-centre

**Step 4A — Success** (`ui/payment/PaymentSuccessScreen.kt`)
- Animated green checkmark (Lottie or `AnimatedVectorDrawable`)
- Amount, transaction ID, date/time, group, installment month
- "Download Receipt" outlined CTA, "View Statement" text CTA, "Done" filled CTA

**Step 4B — Failure** (`ui/payment/PaymentFailureScreen.kt`)
- Animated red X
- Failure reason from API
- "Retry Payment" filled CTA, "Try Different Method" outlined CTA, "Contact Support" text link

### Payment History (`ui/payment/PaymentHistoryScreen.kt`)
- Outstanding balance card (green if ₹0, red with Pay CTA if overdue)
- Date range picker + group selector + status filter chips
- `LazyColumn` of payment rows: date, installment month, amount, method icon, status chip, receipt icon
- "Download Statement" FAB (generates PDF via API)

### Digital Receipt (`ui/payment/ReceiptScreen.kt`)
- Perforated top-border card (use `Canvas` with dashed path)
- All payment details including amount in words (implement Indian number-to-words conversion)
- "Download PDF", "Share via WhatsApp", "Share via Email" bottom sheet actions

### Payout History — read-only (`ui/payment/PayoutHistoryScreen.kt`)
- Header "Payouts Received"
- `LazyColumn` of payout rows: group, month, amount (green), bank ref, status chip
- Empty state: "No payouts received yet"
- Each row tappable → bottom sheet with full payout details

---

## Backend — endpoints to implement

### POST `/api/v1/payments`
Create a payment intent with the chosen gateway.
```
Request: { "installment_schedule_id": "uuid", "payment_method": "UPI", "upi_vpa": "user@upi" }
Response: { "payment_id": "uuid", "gateway_order_id": "...", "amount": 2500.00 }
```

### POST `/api/v1/payments/webhook`
Handle Razorpay payment callbacks (mark success/failure, write ledger entry, send notification).

### GET `/api/v1/payments`
Paginated payment history for the authenticated member.
```
Query params: page, page_size, group_id, status, from_date, to_date
```

### GET `/api/v1/payments/{id}/receipt`
Return receipt data (JSON). PDF generation is client-side or via a `/receipt/pdf` endpoint.

### GET `/api/v1/payouts`
Paginated read-only payout history for the authenticated member.

---

## Acceptance criteria

- [ ] Payment flow completes end-to-end: Summary → Method → Processing → Success
- [ ] Back navigation is blocked on the Processing screen
- [ ] Failure screen shows the gateway failure reason
- [ ] Receipt shows amount in both numerals and Indian words
- [ ] Payment history filter by date range + group + status works
- [ ] Payout history shows empty state correctly when no payouts exist
- [ ] Webhook correctly updates payment status and creates ledger entry
- [ ] No auction-related UI elements appear anywhere in the Payments tab
