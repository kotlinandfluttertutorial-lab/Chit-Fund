# Kiro Prompt — KAN-277: Payments API — Create Intent + Webhook

**Ticket:** KAN-277  
**Epic:** KAN-251 — Backend — Core API  
**Priority:** Highest  
**Skill:** Use the `fastapi-endpoint` skill  
**File:** `backend/app/api/v1/endpoints/payments.py`

---

## Task

Implement `POST /api/v1/payments` (create payment intent) and `POST /api/v1/payments/webhook` (Razorpay gateway callback).

## POST `/api/v1/payments`

```
Auth: Bearer token (member)
Request:
{
  "installment_schedule_id": "uuid",
  "payment_method": "UPI",
  "upi_vpa": "user@upi"
}
Response 201:
{
  "payment_id": "uuid",
  "gateway_order_id": "order_XXXX",
  "amount": 2500.00,
  "currency": "INR",
  "status": "PENDING"
}
```

### Service logic
1. Verify installment belongs to the authenticated member's group
2. Calculate late fee (if due date passed + grace period)
3. Create `Payment` record with `status=PENDING`
4. Call Razorpay API to create order
5. Write a `LedgerEntry` of type `INSTALLMENT` when payment is SUCCESSFUL (in webhook handler)

## POST `/api/v1/payments/webhook` (Razorpay)

```
No auth (verified by Razorpay signature header)
Request: Razorpay webhook payload (JSON)
Response 200: { "status": "ok" }
```

### Webhook handler
1. Verify `X-Razorpay-Signature` header using HMAC-SHA256
2. Parse event type: `payment.captured` | `payment.failed`
3. `payment.captured`: update `Payment.status = SUCCESSFUL`, create ledger entry
4. `payment.failed`: update `Payment.status = FAILED`, store `failure_reason`
5. Trigger push notification to member (async)
6. All DB operations in a single transaction

### Ledger entry on success
```python
LedgerEntry(
    chit_group_id=payment.chit_group_id,
    member_id=payment.member_id,
    payment_id=payment.id,
    entry_type=LedgerEntryType.INSTALLMENT,
    narration=f"Installment payment — Month {month_number}",
    debit=0,
    credit=payment.total_amount,
    running_balance=...,  # calculated from previous balance
)
```

## Acceptance criteria
- [ ] Payment intent creation validates the installment belongs to the member
- [ ] Razorpay order created and order_id returned
- [ ] Webhook signature verification — reject invalid signatures with 400
- [ ] `payment.captured` creates ledger entry in the same DB transaction
- [ ] `payment.failed` stores the failure reason
- [ ] Financial records never deleted — VOIDED status only
- [ ] Unit tests cover: success, failure, invalid signature, duplicate webhook
