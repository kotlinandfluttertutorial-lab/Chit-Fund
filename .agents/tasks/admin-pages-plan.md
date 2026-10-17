# Implementation Plan — Admin Dashboard Remaining Pages

**Project root:** `j:\Android\AndroidStudioProjects\Kiro\Chit-Fund\admin`  
**Build command:** `npm run build` (runs `tsc && vite build`)  
**Type-check command:** `npm run type-check` (runs `tsc --noEmit`)  
**Full check:** `npm run check` (lint + type-check + test + build)  
**Dev server:** `npm run dev` (port 5173)

---

## Patterns to follow (grounded in codebase reading)

- **Page wrapper:** `<div className="p-6 space-y-5">` or `space-y-6` (Dashboard uses 6, MemberSearch uses 5 — match the page density)
- **Page header row:** `<div className="flex items-center justify-between">` with `<h2 className="text-xl font-semibold text-[#041B3C]">` + subtitle `<p className="text-sm text-[#64748B]">`
- **Primary button:** `px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors`
- **Secondary button:** `px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors`
- **Card:** `bg-white rounded-xl border border-[#E2E8F0] shadow-sm`
- **Filter bar:** `bg-white rounded-xl border border-[#E2E8F0] p-4 shadow-sm flex items-center gap-3 flex-wrap`
- **Search input wrapper:** `bg-[#F7FAFC] rounded-lg px-3 py-2 border border-[#E2E8F0]` with `<Search size={15} className="text-[#64748B]">`
- **Filter chip active:** `bg-[#073763] text-white` / inactive: `bg-[#F1F3FF] text-[#64748B] hover:bg-[#E0E8FF]`
- **Table header row:** `bg-[#F7FAFC] border-b border-[#E2E8F0]` — `th` class: `px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide`
- **Table body row:** `divide-y divide-[#E2E8F0]` + `hover:bg-[#F7FAFC] transition-colors`
- **Table cell:** `px-5 py-3.5`
- **Status chip:** `px-2.5 py-1 rounded-full text-xs font-semibold`
- **Pagination row:** `px-5 py-3.5 border-t border-[#E2E8F0] flex items-center justify-between bg-[#F7FAFC]`
- **Icons:** lucide-react, `size={14}` in buttons, `size={17}` in sidebar nav
- **No inline styles** — Tailwind only (exception: dynamic percentage widths like progress bars may use style prop)
- **Sample data:** defined as `const` arrays at module top (not fetched), matching the pattern in Dashboard.tsx and MemberSearch.tsx
- **Types:** use `AdminRole`, `AdminUser`, `Payout`, `PayoutStatus` from `../types` — these already exist

---

## Items

- [ ] 1. Create `admin/src/pages/RBAC.tsx` — Role-Based Access Control page.

  Define 4 system roles with their default permissions, a permission matrix using `<select>` dropdowns (disabled/grayed for Super Admin row), and a "Save Changes" button per-role. The permission matrix has a sticky first column and 9 module columns. Super Admin cells are always Write and non-editable. Use `useState` to hold the mutable permission state for the other 3 roles.

  **Default permissions to encode:**
  - Super Admin: Write all (locked)
  - Finance Manager: Write Payments/Ledger/Reports; Read Members/Chit Groups/Installments/AI Tools; None Settings
  - Support Agent: Write Members; Read Payments/Installments/Chit Groups; None Ledger/Reports/Settings/AI Tools
  - Auditor: Read Dashboard/Members/Chit Groups/Installments/Payments/Ledger/Reports/AI Tools; None Settings

  **Modules (columns):** Dashboard | Members | Chit Groups | Installments | Payments | Ledger | Reports | AI Tools | Settings (9 total — NO KYC)

  **Structure:**
  1. Page header: "Role-Based Access Control" + "+ New Role" button (secondary button style, disabled/placeholder for now)
  2. Roles summary table: 4 rows, columns = Role Name | Description | Members | Action (Edit button)
  3. Permission Matrix card with `overflow-x-auto` wrapper, sticky first column using `sticky left-0 bg-white z-10`, `<select>` dropdowns with None/Read/Write options, Super Admin row: `<select disabled className="opacity-50 cursor-not-allowed bg-[#F7FAFC]">` showing "Write"
  4. "Save Changes" button per-role row (not for Super Admin)

  **Files:** `admin/src/pages/RBAC.tsx`  
  **Verify:** `cd admin && npm run type-check` — zero TypeScript errors on the new file.

---

- [ ] 2. Create `admin/src/pages/AdminUsers.tsx` — Admin Users management page.

  Depends on: types already include `AdminUser` and `AdminRole` from `../types`.

  **Sample data:** 4+ admin users of different roles (Super Admin, Finance Manager, Support Agent, Auditor). At least one Inactive status.

  **Structure:**
  1. Page header: "Admin Users" + "Invite Admin" button (primary button style)
  2. Table: Name (avatar initial + name + email stacked) | Role chip | Last Login | Status chip (Active=green, Inactive=slate) | Actions (Edit Role | Deactivate | Reset Password | View Activity — inline text buttons)
  3. "Invite Admin" modal: controlled by `useState(false)` for open/close; contains email `<input>`, role `<select>` with 4 roles, "Send Invite" + "Cancel" buttons; modal backdrop `fixed inset-0 bg-black/40 flex items-center justify-center z-50`; inner modal `bg-white rounded-xl p-6 w-full max-w-md shadow-xl`
  4. "Deactivate" confirmation dialog: second modal state, asks "Are you sure you want to deactivate [name]?" with Confirm (red) + Cancel buttons

  **Role chip colors:** Super Admin = `bg-[#073763] text-white` | Finance Manager = `bg-teal-50 text-teal-700` | Support Agent = `bg-blue-50 text-blue-700` | Auditor = `bg-purple-50 text-purple-700`

  **Files:** `admin/src/pages/AdminUsers.tsx`  
  **Verify:** `cd admin && npm run type-check` — zero TypeScript errors on the new file.

---

- [ ] 3. Create `admin/src/pages/AuditLog.tsx` — Audit Log page (read-only, non-deletable).

  **Sample data:** 8+ audit log entries with realistic values. Each entry has: id, timestamp, admin name, admin role, action type (Payment | Member | Group | System), entity type, entity ID, IP address, before/after change object.

  **Structure:**
  1. Page header: "Audit Log" + "Export CSV" button (secondary)
  2. Filter bar: date-from/date-to `<input type="date">` inputs | admin user `<select>` | action type filter chips (All | Payment | Member | Group | System)
  3. Table: Timestamp | Admin User (name + role chip) | Action Type chip | Entity Type | Entity ID | IP Address | Changes (collapsed by default — show "View" text link)
  4. Expandable row: clicking a row (or the "View" link) toggles a sub-row spanning all columns; sub-row shows a two-column before/after diff using `<pre className="text-xs bg-[#F7FAFC] rounded p-2">JSON.stringify(before, null, 2)</pre>` pattern
  5. Pagination: 25 per page, same pattern as MemberSearch.tsx (`ChevronLeft`/`ChevronRight`)
  6. **No delete buttons anywhere** on this page

  **Action type chip colors:** Payment = `bg-green-50 text-green-700` | Member = `bg-blue-50 text-blue-700` | Group = `bg-purple-50 text-purple-700` | System = `bg-slate-100 text-slate-600`

  **Files:** `admin/src/pages/AuditLog.tsx`  
  **Verify:** `cd admin && npm run type-check` — zero TypeScript errors on the new file.

---

- [ ] 4. Replace `admin/src/pages/Reports.tsx` — full Reports hub (replaces 2-line stub entirely).

  **Structure:**
  1. Page header: "Reports" + "New Report" button navigating to `/reports/builder`
  2. Four report category cards in a 2×2 grid (`grid grid-cols-2 gap-5`), each card:
     - Icon (lucide-react), title, description, "Last generated: [date]" text
     - Two CTAs: "Run Report" (primary) + "Schedule Report" (secondary/ghost)
     - Categories: Collections (`BarChart3`), Member Activity (`Users`), Payouts (`Wallet`), AI Usage (`Bot`)
     - **NO KYC, NO Auctions categories**
  3. "Quick Reports" section: card with title "Quick Reports", then 3 rows (horizontal rule separated):
     - Outstanding Installments | Collection Rate by Group | Member Enrollment Timeline
     - Each row: report name, description, "Run" text button (`text-[#073763] hover:underline text-sm font-medium`)
  4. "Report History" section: table of last 5 generated reports — columns: Report Name | Category | Generated On | Generated By | Format | Download
     - Download column: PDF/CSV icon-link using `<Download size={14}>` in a small button

  **Files:** `admin/src/pages/Reports.tsx` (full overwrite of stub)  
  **Verify:** `cd admin && npm run type-check` — zero TypeScript errors on the new file.

---

- [ ] 5. Create `admin/src/pages/ReportBuilder.tsx` — stepped Report Builder.

  **Structure:** Multi-step wizard using `useState<number>` for `currentStep` (1–4). A step indicator bar at the top shows 4 steps: "1 Report Type | 2 Filters | 3 Columns | 4 Preview". Active step in `bg-[#073763] text-white`, completed in `bg-[#14B8A6] text-white`, future in `bg-[#E2E8F0] text-[#64748B]`.

  **Step 1 — Report Type:** 4 cards in a row (`grid grid-cols-4 gap-4`), each selectable (selected = `ring-2 ring-[#073763]`), icons + title + description. Types: Collections, Member Activity, Payouts, AI Usage.

  **Step 2 — Filters panel:**
  - Date range: two `<input type="date">` (From / To)
  - Group selector: `<select multiple>` or tag-style multi-select using checkboxes in a dropdown — choose checkboxes in a scrollable container (`max-h-40 overflow-y-auto border rounded-lg p-2`)
  - Member selector: text search input that filters a sample list
  - Status filter: checkboxes for relevant statuses

  **Step 3 — Column selector:** 6+ columns listed with toggle checkboxes. Columns vary by report type but always include at least: Member Name, Group Name, Amount, Date, Status, Reference. Each toggle is a `<label>` with `<input type="checkbox">`.

  **Step 4 — Preview table:** First 20 rows of sample data matching the selected report type and columns. Below the preview:
  - Export buttons: three buttons in a row: "Export PDF" | "Export Excel" | "Export CSV" (all secondary style)
  - Schedule section: `<select>` for frequency (Daily/Weekly/Monthly) + email `<input>` + "Add Schedule" button
  - "Save as Custom Report" primary button

  **Navigation:** "Back" (secondary) + "Next" (primary) buttons at the bottom right. On Step 1 only "Next" shown. On Step 4 "Next" is replaced by the export/save controls.

  **Files:** `admin/src/pages/ReportBuilder.tsx`  
  **Verify:** `cd admin && npm run type-check` — zero TypeScript errors on the new file.

---

- [ ] 6. Create `admin/src/pages/PayoutRecords.tsx` — read-only payout records.

  **Key constraint:** Strictly read-only. No approve/reject/action buttons of any kind. The only row action is "Download Certificate" and only for rows with status `Credited`.

  **Sample data:** 5 payout records using the existing `Payout` type from `../types`. Include all 4 statuses: Credited (2), Processing (1), Pending (1), Failed (1). Bank account masked as `****1234` pattern.

  **Structure:**
  1. Page header: "Payout Records" + "Export CSV" button (secondary)
  2. Amber info banner: `bg-amber-50 border border-amber-200 rounded-xl p-4 flex items-start gap-3` with `AlertCircle` icon (amber) + text "Payout approval workflow is deferred. This view shows read-only payout records."
  3. Filter bar: date range inputs | group `<select>` | status filter chips (All | Credited | Processing | Pending | Failed)
  4. Table columns: Member Name | Group Name | Cycle Month | Gross Payout | Foreman Commission | Net Payout | Bank Account | Transfer Date | Bank Reference | Status | Actions
  5. Status chips: Credited=`bg-green-50 text-green-700` | Processing=`bg-blue-50 text-blue-700` | Pending=`bg-amber-50 text-amber-700` | Failed=`bg-red-50 text-red-600`
  6. Actions cell: "Download Certificate" button only when `status === "Credited"` else empty cell (`—`)

  **Files:** `admin/src/pages/PayoutRecords.tsx`  
  **Verify:** `cd admin && npm run type-check` — zero TypeScript errors on the new file.

---

- [ ] 7. Replace `admin/src/pages/Settings.tsx` — proper settings hub (replaces 2-line stub).

  **Structure:** Settings hub page that links out to RBAC and Admin Users sub-pages.

  1. Page header: "Settings"
  2. Two setting category cards in a row (`grid grid-cols-2 gap-5`), each:
     - Icon, title, description, "Manage →" link button using `useNavigate` from react-router-dom
     - Card 1: Shield icon, "Role-Based Access Control", "Manage permissions for each admin role", links to `/settings/rbac`
     - Card 2: Users icon, "Admin Users", "Invite, manage roles, and deactivate admin accounts", links to `/settings/users`
  3. A "System" section card below: shows app version (`v0.1.0`), environment (Production), last deploy date — purely informational, no editable fields

  **Files:** `admin/src/pages/Settings.tsx` (full overwrite of stub)  
  **Verify:** `cd admin && npm run type-check` — zero TypeScript errors.

---

- [ ] 8. Update `admin/src/App.tsx` — add all new routes.

  Add these imports at the top (after existing imports):
  ```ts
  import RBACPage from "./pages/RBAC";
  import AdminUsersPage from "./pages/AdminUsers";
  import AuditLogPage from "./pages/AuditLog";
  import ReportBuilderPage from "./pages/ReportBuilder";
  import PayoutRecordsPage from "./pages/PayoutRecords";
  ```

  Add these routes inside the `<Route path="/" element={<AdminLayout />}>` block:
  ```tsx
  {/* Settings sub-pages */}
  <Route path="settings/rbac" element={<RBACPage />} />
  <Route path="settings/users" element={<AdminUsersPage />} />

  {/* Audit Log */}
  <Route path="audit" element={<AuditLogPage />} />

  {/* Report Builder */}
  <Route path="reports/builder" element={<ReportBuilderPage />} />

  {/* Payout Records (read-only) */}
  <Route path="payouts" element={<PayoutRecordsPage />} />
  ```

  Place settings sub-routes immediately after `<Route path="settings" ...>`. Place `audit` after the AI Tools route. Place `reports/builder` after `reports/analytics`. Place `payouts` after the Ledger block.

  **Files:** `admin/src/App.tsx`  
  **Verify:** `cd admin && npm run type-check` — zero TypeScript errors.

---

- [ ] 9. Update `admin/src/components/layout/AdminLayout.tsx` — sidebar nav + PageTitle map.

  **Two changes:**

  **A. Add new nav items** to the `navItems` array. Import `FileText` and `Wallet` from lucide-react (add to existing import). Add after "Ledger":
  ```ts
  { path: "/payouts", label: "Payout Records", icon: Wallet },
  ```
  Add after "Settings":
  ```ts
  { path: "/audit", label: "Audit Log", icon: FileText },
  ```

  The RBAC and Admin Users pages live under `/settings/*` — they are reachable via the Settings nav item (active state already covers `startsWith("/settings")`), so no separate nav entries needed for them.

  **B. Extend the `titles` map** in `PageTitle`:
  ```ts
  "/payouts": "Payout Records",
  "/audit": "Audit Log",
  "/settings/rbac": "Role-Based Access Control",
  "/settings/users": "Admin Users",
  "/reports/builder": "Report Builder",
  ```

  **Files:** `admin/src/components/layout/AdminLayout.tsx`  
  **Verify:** `cd admin && npm run type-check` — zero TypeScript errors.

---

- [ ] 10. Full build verification.

  Run the complete check suite from the `admin` directory:
  ```
  cd admin
  npm run check
  ```
  Expected: lint passes, type-check passes, all tests pass, vite build completes with no errors. Fix any type errors or lint warnings before considering this done.

  **Files:** no new files  
  **Verify:** `npm run check` exits with code 0.

---

## Dependency order

Items 1–7 are independent of each other and can be worked in any order; each produces one new/replaced `.tsx` file. Items 8 and 9 depend on items 1–7 being present (they import the new pages). Item 10 depends on 8 and 9.

Recommended order: 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10.
