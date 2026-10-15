# Kiro Prompt — KAN-264: Chit Group Details Screen

**Ticket:** KAN-264  
**Epic:** KAN-239 — Chit Group Management (Customer)  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-MyChits-3.2-GroupDetails-Default`  
**File:** `app/src/main/java/com/chitsmart/ui/chits/GroupDetailsScreen.kt`

---

## Task

Implement the chit group details screen. Opens when user taps "Know More" on a group card.

## Sections

### Hero card
- Group name (large, PrimaryNavy)
- Chit value (very large, teal)
- Grid stats: Monthly Installment | Duration | Members | Foreman Commission %
- Enrollment status: "18 of 20 seats filled" with progress bar

### Tabs (Material 3 `TabRow` + `HorizontalPager`)

**Tab 1 — Overview**
- Summary paragraph about the group
- Key stats: start date, end date, next installment date
- Group manager name

**Tab 2 — Schedule**
- `LazyColumn` table: Month # | Due Date | Amount
- Status chip per row: Upcoming / Due / Paid / Overdue
- **NO auction calendar entries — installment schedule only**

**Tab 3 — Members**
- Count: "18 members enrolled"
- Anonymised list: Member #01, Member #02 ... (no real names for privacy)
- Highlight "You" if user is enrolled

**Tab 4 — FAQs**
- `LazyColumn` of expandable `Card` items
- Topics: installment late fees, how payouts work, what happens if I miss a payment
- **NO auction or bidding FAQs**

### Sticky bottom bar
- "Enroll Now" filled button (full width, teal)
- Disabled + "Already Enrolled" if member is already in this group

## API
`GET /api/v1/chit-groups/{id}` — returns full group details including schedule

## Acceptance criteria
- [ ] All 4 tabs work with correct content
- [ ] Schedule tab shows installment dates only (no auction dates)
- [ ] FAQs have no auction/bidding content
- [ ] Enroll Now CTA disabled if already enrolled
- [ ] Sticky bottom bar remains visible during scroll
