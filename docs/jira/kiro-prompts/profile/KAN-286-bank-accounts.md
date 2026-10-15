# Kiro Prompt — KAN-286: Bank Accounts Screen

**Ticket:** KAN-286  
**Epic:** KAN-236 — Customer Profile & Settings  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**File:** `app/src/main/java/com/chitsmart/ui/profile/BankAccountsScreen.kt`

---

## Task

Implement the bank accounts management screen.

## Layout

- Top app bar: "Bank Accounts" + back
- `LazyColumn` of `BankAccountCard` composables
- "Add Bank Account" extended FAB (teal) at bottom

### `BankAccountCard`
- Bank name + logo icon
- Account type: Savings / Current
- Masked number: XXXX XXXX XXXX 1234
- UPI IDs linked (comma-separated)
- Primary indicator: teal star badge "Primary" chip (only on primary account)
- "Set as Primary" `TextButton` (if not primary)
- "Remove" `TextButton` → confirmation `AlertDialog`

### Add Bank Account flow (bottom sheet or new screen)
- Account number input
- IFSC code input (6+5 = 11 chars)
- "Verify with penny-drop" `Button`
  - Calls `POST /api/v1/members/bank-accounts/verify` with account + IFSC
  - Shows verified account holder name from bank
  - "Confirm Add" CTA after verification

## Acceptance criteria
- [ ] Masked account number shown correctly
- [ ] Primary account has teal star badge
- [ ] Set as Primary updates correctly via API
- [ ] Remove shows confirmation dialog before deleting
- [ ] Penny-drop verification flow works: enter details → verify → see name → confirm
- [ ] Cannot remove last bank account if it is linked to an active mandate
