# Kiro Prompt — KAN-263: Browse Chit Groups Screen

**Ticket:** KAN-263  
**Epic:** KAN-239 — Chit Group Management (Customer)  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-MyChits-3.1-BrowseGroups-Default` — node `1:1737`  
**File:** `app/src/main/java/com/chitsmart/ui/chits/BrowseGroupsScreen.kt`

---

## Task

Implement the browse chit groups screen — the main discovery screen for available groups.

## Implementation

### Search + filters
- `SearchBar` (Material 3) at top — searches group name
- Filter chips row below: All | ₹25K | ₹50K | ₹1L | ₹5L | Duration (6M, 12M, 24M)
- Single-select filter chips; "All" deselects others

### Group cards (`LazyColumn`)
Each card (`ChitGroupCard.kt`):
- Group name — `headlineSmall`, PrimaryNavy
- Chit value — large, bold, teal
- Row: Duration badge | Members `X/Y` | Monthly ₹X,XXX
- Next installment due date (e.g. "Next: 01 Nov 2026") — **NO "next auction date"**
- "Full" `Badge` overlay when `enrolledMembers >= maxMembers`
- Two CTAs: "Know More" (outlined) → `AppRoutes.GROUP_DETAILS` | "Enroll Now" (filled, teal) → `AppRoutes.ENROLLMENT`
- Enroll Now disabled + grey when Full

### Referral banner
- Between items 2 and 3: teal card "Refer a friend and earn ₹500 →"

## API
`GET /api/v1/chit-groups` — query params: `search`, `chit_value`, `duration_months`, `page`

## Acceptance criteria
- [ ] Search filters groups by name in real time (debounced 300ms)
- [ ] Chit value filter chips work correctly
- [ ] "Full" badge shows when group is at max members; Enroll Now is disabled
- [ ] NO "next auction date" anywhere on group cards
- [ ] Referral banner appears in the list
- [ ] Empty state shown when no groups match filters
- [ ] Matches Figma frame `App-MyChits-3.1-BrowseGroups-Default`
