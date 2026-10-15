# Kiro Prompt — KAN-262: Notifications Screen

**Ticket:** KAN-262  
**Epic:** KAN-238 — Notifications  
**Priority:** Medium  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Home-2.2-Notifications-Default` — node `1:1965`  
**File:** `app/src/main/java/com/chitsmart/ui/notifications/NotificationsScreen.kt`

---

## Task

Implement the in-app notification centre.

## Notification types (current release)

| Type | Icon | Colour |
|---|---|---|
| `PAYMENT_DUE` | Bell | Amber |
| `PAYMENT_CONFIRMED` | Check circle | Green |
| `INSTALLMENT_REMINDER` | Calendar | Blue |
| `PAYOUT_CREDITED` | Rupee coin | Green |
| `SYSTEM_ALERT` | Info | Gray |

**DO NOT include:** `KYC_UPDATE`, `AUCTION_REMINDER` — these are deferred.

## Implementation

### `NotificationsScreen.kt`
- Top app bar: "Notifications" + "Mark All Read" `TextButton`
- `LazyColumn` grouped by:
  - **Today** (notifications from today)
  - **This Week** (earlier this week)
  - **Earlier** (older)
- Each notification row:
  - Leading icon (type-specific, coloured)
  - Title (`bodyMedium`, bold if unread)
  - Body snippet (`bodySmall`, `SecondaryText`)
  - Relative time (`bodySmall`, gray)
  - Unread blue dot indicator (right side)
  - Swipe-to-dismiss (`SwipeToDismiss` composable)
- Empty state: "You're all caught up!" with check illustration

## Acceptance criteria
- [ ] Grouped correctly into Today / This Week / Earlier
- [ ] Unread notifications shown with blue dot
- [ ] Mark All Read clears all dots
- [ ] Swipe-to-dismiss works per item
- [ ] No KYC or Auction notification types present
- [ ] Matches Figma frame `App-Home-2.2-Notifications-Default`
