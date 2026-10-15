# Kiro Prompt — KAN-292: Admin Member Search & List

**Ticket:** KAN-292  
**Epic:** KAN-242 — Admin — Member Management  
**Priority:** Highest  
**Skill:** Use the `admin-dashboard-component` skill  
**File:** `admin/src/pages/MemberSearch.tsx`

---

## Task

Implement the admin member search and list page.

## Layout

- Page header: "Members" + total count badge + "Export CSV" button
- Global search bar (type-ahead, debounced 300ms)
- Filter panel (collapsible sidebar or drawer):
  - Account Status: All | Active | Inactive | Suspended (multi-select)
  - Enrolled Groups: group name search
  - Join Date: date range
  - State / City: text inputs
  - **NO KYC Status filter**
- "Apply Filters" / "Clear Filters" buttons

### Members table (TanStack Table)
Columns: Avatar+Name | Mobile | Member ID | Account Status | Groups | Join Date | Last Payment | Actions

- Column visibility toggle (show/hide columns)
- Sortable: Name, Join Date, Last Payment
- Pagination: 50 per page
- Row actions: View (→ `/admin/members/{id}`) | Edit | Suspend (with confirmation)
- Bulk select checkboxes + "Export Selected" action

## API
`GET /api/v1/admin/members` — query: `search`, `account_status`, `page`, `page_size`, `sort_by`, `sort_dir`

## Acceptance criteria
- [ ] Type-ahead search filters table within 300ms
- [ ] NO KYC status filter anywhere
- [ ] Column sort works on at least Name, Join Date, Last Payment
- [ ] Pagination works correctly
- [ ] Export exports only the filtered+selected rows
- [ ] Suspend shows confirmation dialog
