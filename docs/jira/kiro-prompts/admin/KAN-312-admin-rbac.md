# Kiro Prompt — KAN-312: Admin RBAC — 4 Roles

**Ticket:** KAN-312  
**Epic:** KAN-250 — Admin — Settings & RBAC  
**Priority:** High  
**Skill:** Use the `admin-dashboard-component` skill  
**File:** `admin/src/pages/RBAC.tsx`

---

## Task

Implement the role-based access control settings page.

## Roles (4 in this release)

| Role | Description |
|---|---|
| Super Admin | Full access to all modules |
| Finance Manager | Read/write: Payments, Ledger, Reports. Read: Members, Chit Groups, Installments |
| Support Agent | Read/write: Members. Read: Payments, Installments |
| Auditor | Read-only access to all modules |

**NO KYC Officer role in this release.**

## Modules (columns in permission matrix)
Dashboard | Members | Chit Groups | Installments | Payments | Ledger | Reports | AI Tools | Settings

## Permission values per cell
- **None** (gray dash)
- **Read** (blue eye icon)
- **Write** (green pencil icon — includes read)

## Layout

### Roles table
- Header row: Role Name | Description | Edit | Delete
- 4 rows (one per role)
- Edit role → opens role edit form
- Delete: only allowed for custom roles (not the 4 system roles)

### Permission matrix
- Sticky first column (role names)
- Cell = `Select` dropdown: None | Read | Write
- Super Admin row: all cells locked to Write (cannot be changed)
- "Save Changes" `Button` per role row

### "+ New Role" CTA
- Opens modal form: role name + description + permission matrix for new role

## API
`GET /api/v1/admin/roles` — list roles with permissions
`PUT /api/v1/admin/roles/{id}` — update role permissions

## Acceptance criteria
- [ ] 4 roles displayed with correct default permissions
- [ ] Super Admin row is read-only (cannot change)
- [ ] Permission changes saved via API
- [ ] New custom role can be created
- [ ] NO KYC Officer role
