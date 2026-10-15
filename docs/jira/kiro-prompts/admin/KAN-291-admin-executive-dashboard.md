# Kiro Prompt — KAN-291: Admin Executive Dashboard

**Ticket:** KAN-291  
**Epic:** KAN-242 — Admin — Member Management  
**Priority:** Highest  
**Skill:** Use the `admin-dashboard-component` skill  
**Figma:** Admin-Dashboard-A.1-Executive-Default  
**File:** `admin/src/pages/Dashboard.tsx`

---

## Task

Implement the main admin executive dashboard — the first screen after admin login.

## Layout

### KPI cards row (5 cards)
```tsx
const kpis = [
  { label: "Total Active Members", value: "1,247", delta: "+12%", trend: "up" },
  { label: "Chit Value Under Management", value: "₹3.2 Cr", delta: "+8%", trend: "up" },
  { label: "Collections This Month", value: "₹12.4 L", delta: "-3%", trend: "down" },
  { label: "Pending Enrollments", value: "23", delta: "+5", trend: "up" },
  { label: "Overdue Installments", value: "47", delta: "+12", trend: "up" },
];
```
**NO "Pending KYC" KPI. NO "Upcoming Auctions" KPI.**

Each KPI card: label | large value | delta chip (green=up, red=down) | 7-day sparkline (Recharts `LineChart`)

### Collection Analytics chart
- Stacked `BarChart` (Recharts), 12 months
- Bars: Collected (teal) | Overdue (amber)
- X-axis: month labels, Y-axis: ₹ formatted

### Group Performance table
Columns: Group Name | Chit Value | Members | Collection Rate % | Next Installment | Status
- Status chip: Active (green) | Pending (amber) | Completed (blue)
- Sortable columns (TanStack Table)

### Recent Activity feed
- Last 20 events: icon + description + relative time
- Event types: Payment received, Enrollment, Member added, Group created, Installment reminder sent

### Quick Actions
- "+ New Group" → `/admin/chit-groups/new`
- "Send Reminder" → opens bulk reminder modal
- "Export Report" → opens report builder
- **NO "Schedule Auction" action**

## API
`GET /api/v1/admin/dashboard` — returns all KPI data, chart data, recent activity

## Acceptance criteria
- [ ] 5 KPI cards with correct labels (no KYC or auctions)
- [ ] Collection chart renders with 12 months of data
- [ ] Group performance table is sortable
- [ ] Quick actions: 3 only (no Schedule Auction)
- [ ] Recent activity feed shows last 20 events
