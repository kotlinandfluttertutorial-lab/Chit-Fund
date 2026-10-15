# Kiro Prompt — KAN-275: Digital Receipt Screen

**Ticket:** KAN-275  
**Epic:** KAN-240 — Installment Payments  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Payment-3.8-DigitalReceipt-Default`  
**File:** `app/src/main/java/com/chitsmart/ui/payment/ReceiptScreen.kt`

---

## Task

Implement the digital receipt screen shown after payment success or accessed from payment history.

## Receipt card design

- White card with perforated-edge top border:
  ```kotlin
  // Draw dashed horizontal line using Canvas
  drawPath(
      path = Path().apply {
          moveTo(0f, 0f)
          lineTo(size.width, 0f)
      },
      color = BorderDivider,
      style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))),
  )
  ```
- ChitSmart logo (small, top-left) + "Receipt" heading
- Receipt number: `RCP-20261015-XXXXX`
- Divider

### Receipt details (label-value pairs)
| Label | Value |
|---|---|
| Member Name | Priya Sharma |
| Group | Family Savings 50K |
| Installment | October 2026 |
| Amount | ₹2,500 |
| Amount in words | Two Thousand Five Hundred Only |
| Late Fee | ₹0 (hidden if zero) |
| Total Paid | ₹2,500 |
| Payment Method | UPI — user@upi |
| Transaction Ref | TXN-20261015-XXXXX |
| Gateway Ref | RZP_XXXXXXXXXXXX |
| Date & Time | 15 Oct 2026, 10:32 AM |

- "Thank you for your payment" footer — `bodySmall`, centred, `SecondaryText`

### Amount in words
Implement `amountToIndianWords(amount: Long): String` in `utils/CurrencyUtils.kt`:
- "Two Thousand Five Hundred Only"
- Must handle lakhs and crores (Indian number system)

### Action buttons (bottom sheet)
- "Download PDF" — filled, PrimaryNavy → generates PDF from receipt data
- "Share via WhatsApp" — outlined → opens WhatsApp share intent with receipt image/PDF
- "Share via Email" — outlined → opens email intent

## Acceptance criteria
- [ ] Perforated border renders correctly
- [ ] Amount in words is accurate in Indian number system (lakhs/crores)
- [ ] PDF download works and opens in device viewer
- [ ] WhatsApp share intent opens with receipt
- [ ] Receipt number and all payment details match the API response
