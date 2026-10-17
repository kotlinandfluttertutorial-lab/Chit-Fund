# Admin Dashboard Pages — RBAC, AdminUsers, AuditLog, Reports, ReportBuilder, PayoutRecords

Six new admin pages, Settings hub, and wiring into App.tsx and AdminLayout.tsx for the ChitSmart admin dashboard. The change completes the Role-Based Access Control, Admin Users management, Audit Log, Reports, Report Builder, and Payout Records (read-only) screens. Watch for: inline `style={}` props for dynamic icon/accent colors in Reports, ReportBuilder, and Settings violate the "Tailwind only" design rule (confirmed); the AuditLog fragment loop emits a React key warning (confirmed).

**Verdict**: APPROVED

---

## High-level view

The permission matrix in RBAC.tsx correctly shows 4 roles, locks Super Admin to Write across all 9 modules, and provides per-module selects for the other three roles. The sticky first-column implementation uses `sticky left-0 z-10` on both header and data cells — correct for `overflow-x-auto` containers — but the body cell uses a static `bg-white` class that won't inherit the row hover color, leaving a visual blip on the first column during hover.

Reports.tsx, ReportBuilder.tsx, and Settings.tsx use a dynamic accent pattern (`style={{ background: \`${accent}15\` }}`, `style={{ color: accent }}`) to tint per-card icon containers at runtime. This is not expressible as static Tailwind without config extension, so it violates the "Tailwind only, no inline styles" constraint. The same pattern exists in pre-existing pages (Dashboard, CollectionAnalytics) but those are out of scope.

AuditLog.tsx maps paginated rows as `<>...</>` fragments without a `key` on the fragment wrapper, which will produce a React key warning in development.

PayoutRecords.tsx correctly implements the deferred-workflow constraint: amber banner, read-only table, Certificate button gated strictly on `status === "CREDITED"`. No approve/reject buttons anywhere. The `APPROVED` status value is present in the type union and the style map but absent from all 5 sample records — any payout that reaches `APPROVED` before the approval workflow ships will render with a teal chip and a dash in Actions (no certificate), which is correct behavior for this release.

App.tsx registers all 5 new routes (`/settings/rbac`, `/settings/users`, `/audit`, `/reports/builder`, `/payouts`) nested under AdminLayout. AdminLayout.tsx adds Payout Records and Audit Log to the sidebar and covers all new routes in the PageTitle map. Settings.tsx is a full hub page, not a stub.

---

<details>
<summary>Issues (3)</summary>

1. **Inline styles for dynamic accent colors** — `Reports.tsx`, `ReportBuilder.tsx`, and `Settings.tsx` all use `style={{ background: \`${cat.accent}15\` }}` and `style={{ color: cat.accent }}` for icon container tinting. Replace the `accent` string field with two Tailwind class strings per card (e.g., `iconBg: "bg-[#073763]/10"` and `iconColor: "text-[#073763]"`) and apply as `className` to remove all `style={}` usage.

2. **AuditLog fragment missing key** — The `paginated.map()` loop wraps each row pair in `<>...</>` without a `key` prop. Change to `<React.Fragment key={entry.id}>` to suppress the React key warning.

3. **RBAC sticky cell hover blip** — The sticky role-name cell uses `bg-white` directly, so it doesn't inherit `hover:bg-[#F7FAFC]` from the `<tr>` hover class. Add `group-hover:bg-[#F7FAFC]` to the `<tr>` and the sticky cell, or use a Tailwind `group` pattern, to keep the highlight consistent across the full row.

</details>

<details>
<summary>Details</summary>

### Inline styles on icon/accent containers

`Reports.tsx` (~lines 146–149), `ReportBuilder.tsx` (~lines 187–190), and `Settings.tsx` (~lines 46–49) share this pattern:

```tsx
style={{ background: `${cat.accent}15` }}
style={{ color: cat.accent }}
```

The hex values are stored in each card's data object and applied at render time. Because Tailwind's JIT compiler cannot see dynamic string interpolations, these classes cannot be expressed as static Tailwind utilities without either extending the Tailwind config with custom color tokens or inlining the class strings per-card. The fix is to replace the `accent: "#073763"` field with `iconBg: "bg-[#073763]/10"` and `iconColor: "text-[#073763]"` per card and render them as `className`. No style props anywhere in the component tree.

### AuditLog — React key warning

```tsx
paginated.map((entry) => (
  <>
    <tr key={entry.id} ...>
    {expandedRow === entry.id && (
      <tr key={`${entry.id}-expanded`} ...>
    )}
  </>
))
```

The shorthand fragment `<>` cannot take a `key` prop. React requires a key on the outermost element in a `.map()` return. Change to `<React.Fragment key={entry.id}>` to resolve the warning. The keys on the inner `<tr>` elements are redundant once the fragment carries the key, but harmless.

### RBAC sticky column hover inconsistency

The sticky body cell (`sticky left-0 bg-white z-10`) uses a hardcoded background that doesn't respond to the `hover:bg-[#F7FAFC]` applied on the parent `<tr>`. When a user hovers, the rest of the row turns `#F7FAFC` but the first column stays white, creating a visible blip at the left edge. Fixing this requires either using a CSS `group` approach or setting `hover:bg-[#F7FAFC]` directly on the sticky cell as well.

### Loose cast in ReportBuilder preview

```tsx
{(row as Record<string, string>)[col] ?? "—"}
```

`previewRows` is typed as an array of object literals with explicit string-keyed properties. Typing the array as `Array<Record<string, string>>` would satisfy the type checker without the cast and is the cleaner approach.

</details>

---

<details>
<summary>File map</summary>

| File | What changed |
|---|---|
| `admin/src/pages/RBAC.tsx` | New — permission matrix for 4 roles, sticky first column, locked Super Admin |
| `admin/src/pages/AdminUsers.tsx` | New — user table with invite modal and deactivate dialog, 5 sample users |
| `admin/src/pages/AuditLog.tsx` | New — read-only audit table, filters, pagination, expandable diff rows, 9 entries |
| `admin/src/pages/Reports.tsx` | New — 4 category cards, quick reports section, report history table |
| `admin/src/pages/ReportBuilder.tsx` | New — 4-step wizard with type selection, filters, column selector, preview + export + schedule |
| `admin/src/pages/PayoutRecords.tsx` | New — amber deferred banner, read-only payout table, Certificate download for CREDITED only |
| `admin/src/pages/Settings.tsx` | Replaced stub — hub page with RBAC and Admin Users cards + system info |
| `admin/src/App.tsx` | Added 5 new routes: `/payouts`, `/settings/rbac`, `/settings/users`, `/audit`, `/reports/builder` |
| `admin/src/components/layout/AdminLayout.tsx` | Added Payout Records and Audit Log to sidebar nav; added PageTitle entries for new routes |

</details>
