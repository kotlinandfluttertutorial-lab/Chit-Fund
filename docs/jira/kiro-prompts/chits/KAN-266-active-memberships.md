# Kiro Prompt — KAN-266: My Active Memberships Screen

**Ticket:** KAN-266  
**Epic:** KAN-239 — Chit Group Management (Customer)  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-MyChits-3.4-ActiveMemberships-Default` — node `1:263`  
**File:** `app/src/main/java/com/chitsmart/ui/chits/ActiveMembershipsScreen.kt`

---

## Task

Implement the "My Chits" tab landing screen — lists all the user's memberships.

## Implementation

### `ActiveMembershipsScreen.kt`
- Top bar: "My Chits" + filter chips: All | Active | Completed
- `LazyColumn` of `MembershipCard` composables
- Empty state: "You have no active memberships. Browse available groups →"

### `MembershipCard.kt`
Each card shows:
- Group name — `headlineSmall`
- Chit value — large, teal
- Progress row: "6 / 24 installments paid" with `LinearProgressIndicator`
- Payout received indicator: teal checkmark if received, "—" if not
- Next due date: "Next: 01 Nov 2026 • ₹2,500"
- Status chip: `Active` (green) | `Completed` (blue) | `Pending Enrollment` (amber)
- **NO "Pending KYC" status chip**
- Tap → navigate to `AppRoutes.GROUP_MEMBER_VIEW` with membershipId

## API
`GET /api/v1/members/memberships` — returns list of memberships with stats

## Acceptance criteria
- [ ] All | Active | Completed filter chips work
- [ ] Progress bar shows correct installment ratio
- [ ] Payout received checkmark appears correctly
- [ ] NO "Pending KYC" status anywhere on this screen
- [ ] Tap on card navigates to Group Member View
- [ ] Empty state shown when no memberships
- [ ] Matches Figma frame `App-MyChits-3.4-ActiveMemberships-Default`
