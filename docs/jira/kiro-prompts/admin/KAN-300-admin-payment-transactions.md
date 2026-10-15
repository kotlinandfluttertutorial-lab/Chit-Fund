# Kiro Prompt — KAN-300: Admin Payment Transactions

**Ticket:** KAN-300  
**Epic:** KAN-245 — Admin — Payment Operations  
**Priority:** Highest  
**Skill:** Use the `admin-dashboard-component` skill  
**File:** `admin/src/pages/PaymentTransactions.tsx`

---

## Task

Implement the admin payment transactions table — master view of all payments.

## Layout

- Page header: "Payment Transactions" + total amount collected badge + "Export CSV" button
- Filter bar:
  - Date range picker
  - Group selector (multi-select)
  - Payment method chips
  - Status chips: All | Successful | Failed | Pending | Refunded

### Transactions table (TanStack Table)
Columns: Date/Time | Member Name | Member ID | Group | Installment Month | Amount | Method | Gateway Ref | Status | Receipt | Actions

Row actions:
- "View Details" → slide-in detail panel (not a new page)
- "Download Receipt" → PDF
- "Initiate Refund" → opens refund form dialog (only for Successful payments)

### Detail panel (slide-in from right, 480px wide)
- All fields from table + Bank Reference + Gateway Response Code + Processing Time
- Member snapshot: name, ID, group, outstanding balance
- Action buttons: Download Receipt | Initiate Refund | Flag for Review

## API
`GET /api/v1/admin/payments` — query: date range, group_id, method, status, page, page_size

## Acceptance criteria
- [ ] Date range filter works correctly
- [ ] Status chips filter correctly
- [ ] View Details opens slide-in panel (does not navigate away)
- [ ] Initiate Refund only available for Successful payments
- [ ] Export CSV exports filtered results
