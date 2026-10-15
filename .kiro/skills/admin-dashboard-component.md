---
name: admin-dashboard-component
description: Step-by-step instructions for implementing a React/TypeScript admin dashboard page or component in the ChitSmart admin dashboard.
---

# Skill: Implement a ChitSmart Admin Dashboard Page

Use this skill whenever you are adding or updating an admin dashboard page, table, chart, or form.

## Stack

- **Framework:** React 18, TypeScript
- **Build:** Vite
- **Routing:** React Router v6
- **Data fetching:** TanStack Query v5
- **Forms:** React Hook Form + Zod
- **Tables:** TanStack Table v8
- **Charts:** Recharts
- **Styling:** Tailwind CSS
- **Icons:** Lucide React
- **HTTP client:** `src/api/client.ts` (Axios with JWT interceptor)

---

## File structure for a page

```
src/
├── pages/<PageName>.tsx               — Page component (route-level)
├── features/<domain>/
│   ├── components/
│   │   ├── <Domain>Table.tsx          — TanStack Table wrapper
│   │   ├── <Domain>Filters.tsx        — Filter bar
│   │   └── <Domain>DetailPanel.tsx    — Slide-in detail view
│   ├── hooks/
│   │   └── use<Domain>.ts             — TanStack Query hooks
│   └── api/
│       └── <domain>Api.ts             — Axios calls
```

---

## Step 1 — API hook (TanStack Query)

```typescript
// src/features/members/hooks/useMembers.ts
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "../../../api/client";
import type { Member, PaginatedResponse } from "../../../types";

interface MembersParams {
  page?: number;
  pageSize?: number;
  search?: string;
  accountStatus?: string;
}

export function useMembers(params: MembersParams = {}) {
  return useQuery<PaginatedResponse<Member>>({
    queryKey: ["members", params],
    queryFn: async () => {
      const { data } = await apiClient.get("/admin/members", { params });
      return data;
    },
    staleTime: 30_000,
  });
}

export function useSuspendMember() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (memberId: string) =>
      apiClient.patch(`/admin/members/${memberId}/suspend`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["members"] }),
  });
}
```

---

## Step 2 — TanStack Table

```tsx
// src/features/members/components/MembersTable.tsx
import { useReactTable, getCoreRowModel, createColumnHelper, flexRender } from "@tanstack/react-table";
import type { Member } from "../../../types";
import { StatusChip } from "../../ui/StatusChip";

const columnHelper = createColumnHelper<Member>();

const columns = [
  columnHelper.accessor("fullName", { header: "Name" }),
  columnHelper.accessor("mobileNumber", { header: "Mobile" }),
  columnHelper.accessor("accountStatus", {
    header: "Status",
    cell: (info) => <StatusChip status={info.getValue()} />,
  }),
  columnHelper.accessor("enrolledGroupsCount", { header: "Groups" }),
  columnHelper.accessor("joinDate", { header: "Joined" }),
];

interface Props {
  data: Member[];
  isLoading: boolean;
}

export function MembersTable({ data, isLoading }: Props) {
  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
  });

  if (isLoading) return <TableSkeleton rows={10} cols={5} />;

  return (
    <div className="overflow-x-auto rounded-xl border border-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-slate-600">
          {table.getHeaderGroups().map((hg) => (
            <tr key={hg.id}>
              {hg.headers.map((h) => (
                <th key={h.id} className="px-4 py-3 text-left font-medium">
                  {flexRender(h.column.columnDef.header, h.getContext())}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody className="divide-y divide-slate-100">
          {table.getRowModel().rows.map((row) => (
            <tr key={row.id} className="hover:bg-slate-50 transition-colors">
              {row.getVisibleCells().map((cell) => (
                <td key={cell.id} className="px-4 py-3 text-slate-700">
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

---

## Step 3 — Page component

```tsx
// src/pages/MemberSearch.tsx
import { useState } from "react";
import { useMembers } from "../features/members/hooks/useMembers";
import { MembersTable } from "../features/members/components/MembersTable";
import { MembersFilter } from "../features/members/components/MembersFilter";
import { PageHeader } from "../components/layout/PageHeader";

export function MemberSearchPage() {
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);

  const { data, isLoading } = useMembers({ search, page, pageSize: 50 });

  return (
    <div className="p-6 space-y-6">
      <PageHeader title="Members" subtitle={`${data?.total ?? 0} total members`} />

      <MembersFilter search={search} onSearchChange={setSearch} />

      <MembersTable data={data?.items ?? []} isLoading={isLoading} />

      {/* Pagination */}
      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 1}
        onPageChange={setPage}
      />
    </div>
  );
}
```

---

## Design system (Tailwind tokens)

Map Tailwind classes to the ChitSmart brand:

| Element | Tailwind class |
|---|---|
| Primary Navy background | `bg-[#073763]` |
| Primary Navy text | `text-[#073763]` |
| Teal accent | `text-[#14B8A6]` / `bg-[#14B8A6]` |
| Page background | `bg-[#F7FAFC]` |
| Main text | `text-[#172B4D]` |
| Secondary text | `text-[#64748B]` |
| Success green | `text-[#22C55E]` / `bg-green-50` |
| Warning amber | `text-[#F59E0B]` / `bg-amber-50` |
| Error red | `text-[#EF4444]` / `bg-red-50` |
| Card | `bg-white rounded-xl border border-slate-200 shadow-sm p-4` |
| Primary button | `bg-[#073763] text-white px-4 py-2 rounded-lg hover:bg-[#1769AA] transition-colors` |

---

## Status chip component pattern

```tsx
// src/components/ui/StatusChip.tsx
const statusStyles: Record<string, string> = {
  ACTIVE: "bg-green-50 text-green-700 border-green-200",
  INACTIVE: "bg-slate-50 text-slate-600 border-slate-200",
  SUSPENDED: "bg-red-50 text-red-700 border-red-200",
  PENDING: "bg-amber-50 text-amber-700 border-amber-200",
  SUCCESSFUL: "bg-green-50 text-green-700 border-green-200",
  FAILED: "bg-red-50 text-red-700 border-red-200",
};

export function StatusChip({ status }: { status: string }) {
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${statusStyles[status] ?? "bg-slate-50 text-slate-600 border-slate-200"}`}>
      {status.replace(/_/g, " ")}
    </span>
  );
}
```

---

## Sidebar navigation (current release)

The sidebar items are: Dashboard | Members | Chit Groups | Installments | Payments | Ledger | Reports | AI Tools | Settings

**Do NOT add:** KYC, Auctions. These are deferred. See `docs/scope-decisions.md`.

---

## Form pattern (React Hook Form + Zod)

```tsx
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

const schema = z.object({
  fullName: z.string().min(2, "Name must be at least 2 characters"),
  email: z.string().email("Invalid email").optional().or(z.literal("")),
  mobileNumber: z.string().regex(/^\+91[6-9]\d{9}$/, "Invalid Indian mobile number"),
});

type FormData = z.infer<typeof schema>;

export function MemberCreateForm() {
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });
  // ...
}
```

---

## Scope guards

- **No KYC filter column** in Member Search table
- **No Documents tab** in Member Details
- **No KYC Data step** in Member Create/Edit form
- **No Auctions tab** in Chit Group Detail view
- **No Auction Rules section** in Chit Group Create/Edit form
- **No Payout approval actions** — payout table is read-only
- **No KYC or Auctions categories** in Reports dashboard
