# Kiro Prompt — KAN-270: Select Payment Method — Step 2

**Ticket:** KAN-270  
**Epic:** KAN-240 — Installment Payments  
**Priority:** Highest  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Payment-3.6-PaymentFlow-Default` — node `1:939` (Step 2)  
**File:** `app/src/main/java/com/chitsmart/ui/payment/SelectMethodScreen.kt`

---

## Task

Implement Step 2 of the payment flow — method selection.

## Payment method options

Displayed as selectable cards (teal border when selected):
1. **UPI** — default selected
2. **Net Banking**
3. **Debit Card**
4. **NEFT / RTGS**

### UPI expanded section (visible when UPI selected)
- Saved UPI IDs from API — radio button list
- Each item: UPI VPA + bank name
- "Add New UPI ID" `TextButton` at bottom
- OR toggle to "Show QR Code" — shows QR image from API

### Other methods
- Net Banking: bank selector dropdown
- Debit Card: card number + expiry + CVV fields
- NEFT/RTGS: show bank account details to transfer to

## CTA
- "Continue" `Button` — full width, PrimaryNavy
- Disabled until a method + required fields are filled

## Navigation
- Continue → `AppRoutes.PAYMENT_PROCESSING`

## Acceptance criteria
- [ ] UPI selected by default
- [ ] Saved UPI IDs loaded from API and selectable
- [ ] Add New UPI navigates to UPI addition flow
- [ ] Continue disabled until valid selection made
