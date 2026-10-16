import { useState, useMemo } from "react";
import { Download, ChevronLeft, ChevronRight, ChevronDown, ChevronUp } from "lucide-react";
import type { AdminRole } from "../types";

// ── Types ─────────────────────────────────────────────────────────────────────

type ActionType = "Payment" | "Member" | "Group" | "System";

interface AuditEntry {
  id: string;
  timestamp: string;
  adminName: string;
  adminRole: AdminRole;
  actionType: ActionType;
  entityType: string;
  entityId: string;
  ipAddress: string;
  before: Record<string, unknown>;
  after: Record<string, unknown>;
}

// ── Sample data ───────────────────────────────────────────────────────────────

const sampleEntries: AuditEntry[] = [
  {
    id: "al-001",
    timestamp: "2026-10-14T09:15:00Z",
    adminName: "Rajesh Menon",
    adminRole: "SUPER_ADMIN",
    actionType: "Payment",
    entityType: "Payment",
    entityId: "PAY-2348",
    ipAddress: "203.0.113.12",
    before: { status: "PENDING", amount: 2500 },
    after: { status: "SUCCESSFUL", amount: 2500, gatewayRef: "GP2348XY" },
  },
  {
    id: "al-002",
    timestamp: "2026-10-14T08:42:00Z",
    adminName: "Kavitha Nair",
    adminRole: "FINANCE_MANAGER",
    actionType: "Member",
    entityType: "Member",
    entityId: "MEM-1024",
    ipAddress: "198.51.100.44",
    before: { accountStatus: "INACTIVE" },
    after: { accountStatus: "ACTIVE" },
  },
  {
    id: "al-003",
    timestamp: "2026-10-13T17:30:00Z",
    adminName: "Suresh Patel",
    adminRole: "SUPPORT_AGENT",
    actionType: "Member",
    entityType: "Member",
    entityId: "MEM-0987",
    ipAddress: "198.51.100.78",
    before: { preferredLanguage: "en", email: "" },
    after: { preferredLanguage: "hi", email: "ravi.k@example.com" },
  },
  {
    id: "al-004",
    timestamp: "2026-10-13T15:10:00Z",
    adminName: "Rajesh Menon",
    adminRole: "SUPER_ADMIN",
    actionType: "Group",
    entityType: "ChitGroup",
    entityId: "GRP-0021",
    ipAddress: "203.0.113.12",
    before: { status: "PENDING", enrolledMembers: 0 },
    after: { status: "ACTIVE", enrolledMembers: 5 },
  },
  {
    id: "al-005",
    timestamp: "2026-10-13T11:55:00Z",
    adminName: "Kavitha Nair",
    adminRole: "FINANCE_MANAGER",
    actionType: "Payment",
    entityType: "Refund",
    entityId: "REF-0112",
    ipAddress: "198.51.100.44",
    before: { status: "PENDING", amount: 5000 },
    after: { status: "PROCESSING", amount: 5000, initiatedBy: "kavitha.nair" },
  },
  {
    id: "al-006",
    timestamp: "2026-10-12T16:20:00Z",
    adminName: "Rajesh Menon",
    adminRole: "SUPER_ADMIN",
    actionType: "System",
    entityType: "Settings",
    entityId: "CFG-001",
    ipAddress: "203.0.113.12",
    before: { maintenanceMode: false },
    after: { maintenanceMode: true, scheduledEnd: "2026-10-12T18:00:00Z" },
  },
  {
    id: "al-007",
    timestamp: "2026-10-12T10:05:00Z",
    adminName: "Arun Sharma",
    adminRole: "SUPPORT_AGENT",
    actionType: "Member",
    entityType: "Member",
    entityId: "MEM-1101",
    ipAddress: "192.0.2.99",
    before: { enrolledGroupsCount: 0 },
    after: { enrolledGroupsCount: 1, groupId: "GRP-0018" },
  },
  {
    id: "al-008",
    timestamp: "2026-10-11T14:45:00Z",
    adminName: "Kavitha Nair",
    adminRole: "FINANCE_MANAGER",
    actionType: "Group",
    entityType: "ChitGroup",
    entityId: "GRP-0019",
    ipAddress: "198.51.100.44",
    before: { monthlyInstallment: 4000, description: "" },
    after: { monthlyInstallment: 5000, description: "Revised installment for Oct cycle." },
  },
  {
    id: "al-009",
    timestamp: "2026-10-11T09:30:00Z",
    adminName: "Rajesh Menon",
    adminRole: "SUPER_ADMIN",
    actionType: "System",
    entityType: "AdminUser",
    entityId: "AU-005",
    ipAddress: "203.0.113.12",
    before: { role: "AUDITOR" },
    after: { role: "SUPPORT_AGENT" },
  },
];

// ── Style helpers ─────────────────────────────────────────────────────────────

const actionChipStyles: Record<ActionType, string> = {
  Payment: "bg-blue-50 text-blue-700",
  Member: "bg-green-50 text-green-700",
  Group: "bg-purple-50 text-purple-700",
  System: "bg-slate-100 text-slate-600",
};

const roleLabels: Record<AdminRole, string> = {
  SUPER_ADMIN: "Super Admin",
  FINANCE_MANAGER: "Finance Manager",
  SUPPORT_AGENT: "Support Agent",
  AUDITOR: "Auditor",
};

const roleChipStyles: Record<AdminRole, string> = {
  SUPER_ADMIN: "bg-[#073763] text-white",
  FINANCE_MANAGER: "bg-teal-50 text-teal-700",
  SUPPORT_AGENT: "bg-blue-50 text-blue-700",
  AUDITOR: "bg-purple-50 text-purple-700",
};

const formatTs = (iso: string) =>
  new Date(iso).toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });

const ACTION_TYPES: (ActionType | "All")[] = ["All", "Payment", "Member", "Group", "System"];
const PAGE_SIZE = 25;

// ── Page ──────────────────────────────────────────────────────────────────────

export default function AuditLogPage() {
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [adminFilter, setAdminFilter] = useState("All");
  const [actionFilter, setActionFilter] = useState<ActionType | "All">("All");
  const [expandedRow, setExpandedRow] = useState<string | null>(null);
  const [page, setPage] = useState(1);

  const uniqueAdmins = useMemo(() => {
    const names = Array.from(new Set(sampleEntries.map((e) => e.adminName)));
    return ["All", ...names];
  }, []);

  const filtered = useMemo(() => {
    return sampleEntries.filter((e) => {
      const matchAction = actionFilter === "All" || e.actionType === actionFilter;
      const matchAdmin = adminFilter === "All" || e.adminName === adminFilter;
      const entryDate = e.timestamp.slice(0, 10);
      const matchFrom = !dateFrom || entryDate >= dateFrom;
      const matchTo = !dateTo || entryDate <= dateTo;
      return matchAction && matchAdmin && matchFrom && matchTo;
    });
  }, [actionFilter, adminFilter, dateFrom, dateTo]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const toggleRow = (id: string) => {
    setExpandedRow((prev) => (prev === id ? null : id));
  };

  return (
    <div className="p-6 space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Audit Log</h2>
          <p className="text-sm text-[#64748B]">Read-only record of all admin actions.</p>
        </div>
        <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
          <Download size={14} />
          Export CSV
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] p-4 shadow-sm flex items-center gap-3 flex-wrap">
        {/* Date range */}
        <div className="flex items-center gap-2">
          <span className="text-xs text-[#64748B] font-medium">From:</span>
          <input
            type="date"
            value={dateFrom}
            onChange={(e) => { setDateFrom(e.target.value); setPage(1); }}
            className="px-3 py-1.5 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
          />
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs text-[#64748B] font-medium">To:</span>
          <input
            type="date"
            value={dateTo}
            onChange={(e) => { setDateTo(e.target.value); setPage(1); }}
            className="px-3 py-1.5 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
          />
        </div>

        {/* Admin filter */}
        <select
          value={adminFilter}
          onChange={(e) => { setAdminFilter(e.target.value); setPage(1); }}
          className="px-3 py-1.5 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] bg-white focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
        >
          {uniqueAdmins.map((a) => (
            <option key={a} value={a}>{a}</option>
          ))}
        </select>

        {/* Action type chips */}
        <div className="flex items-center gap-1.5">
          {ACTION_TYPES.map((t) => (
            <button
              key={t}
              onClick={() => { setActionFilter(t); setPage(1); }}
              className={`px-3 py-1.5 rounded-full text-xs font-medium transition-colors ${
                actionFilter === t
                  ? "bg-[#073763] text-white"
                  : "bg-[#F1F3FF] text-[#64748B] hover:bg-[#E0E8FF]"
              }`}
            >
              {t}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-[#F7FAFC] border-b border-[#E2E8F0]">
            <tr>
              {["Timestamp", "Admin User", "Action Type", "Entity Type", "Entity ID", "IP Address", "Changes"].map((h) => (
                <th
                  key={h}
                  className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide"
                >
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[#E2E8F0]">
            {paginated.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-5 py-12 text-center text-[#64748B] text-sm">
                  No audit log entries match your filters.
                </td>
              </tr>
            ) : (
              paginated.map((entry) => (
                <>
                  <tr
                    key={entry.id}
                    className="hover:bg-[#F7FAFC] transition-colors cursor-pointer"
                    onClick={() => toggleRow(entry.id)}
                  >
                    <td className="px-5 py-3.5 text-xs text-[#64748B] whitespace-nowrap">
                      {formatTs(entry.timestamp)}
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="font-medium text-[#041B3C] text-sm">{entry.adminName}</div>
                      <span
                        className={`px-2 py-0.5 rounded-full text-xs font-semibold ${roleChipStyles[entry.adminRole]}`}
                      >
                        {roleLabels[entry.adminRole]}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <span
                        className={`px-2.5 py-1 rounded-full text-xs font-semibold ${actionChipStyles[entry.actionType]}`}
                      >
                        {entry.actionType}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-[#64748B] text-xs">{entry.entityType}</td>
                    <td className="px-5 py-3.5">
                      <span className="font-mono text-xs text-[#041B3C] bg-[#F7FAFC] px-2 py-0.5 rounded border border-[#E2E8F0]">
                        {entry.entityId}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 font-mono text-xs text-[#64748B]">{entry.ipAddress}</td>
                    <td className="px-5 py-3.5">
                      <button
                        className="flex items-center gap-1 text-xs font-medium text-[#073763] hover:underline"
                        onClick={(e) => { e.stopPropagation(); toggleRow(entry.id); }}
                      >
                        {expandedRow === entry.id ? (
                          <>Hide <ChevronUp size={12} /></>
                        ) : (
                          <>View <ChevronDown size={12} /></>
                        )}
                      </button>
                    </td>
                  </tr>

                  {/* Expandable diff row */}
                  {expandedRow === entry.id && (
                    <tr key={`${entry.id}-expanded`} className="bg-[#F7FAFC]">
                      <td colSpan={7} className="px-5 py-4">
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">
                              Before
                            </p>
                            <pre className="text-xs bg-white rounded-lg border border-[#E2E8F0] p-3 overflow-x-auto text-[#041B3C]">
                              {JSON.stringify(entry.before, null, 2)}
                            </pre>
                          </div>
                          <div>
                            <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">
                              After
                            </p>
                            <pre className="text-xs bg-white rounded-lg border border-[#E2E8F0] p-3 overflow-x-auto text-[#041B3C]">
                              {JSON.stringify(entry.after, null, 2)}
                            </pre>
                          </div>
                        </div>
                      </td>
                    </tr>
                  )}
                </>
              ))
            )}
          </tbody>
        </table>

        {/* Pagination */}
        <div className="px-5 py-3.5 border-t border-[#E2E8F0] flex items-center justify-between bg-[#F7FAFC]">
          <span className="text-xs text-[#64748B]">
            Showing {filtered.length === 0 ? 0 : (page - 1) * PAGE_SIZE + 1}–{Math.min(page * PAGE_SIZE, filtered.length)} of {filtered.length} entries
          </span>
          <div className="flex items-center gap-1">
            <button
              disabled={page === 1}
              onClick={() => setPage((p) => p - 1)}
              className="p-1.5 rounded-lg border border-[#E2E8F0] bg-white hover:bg-[#F1F3FF] disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronLeft size={14} />
            </button>
            {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => i + 1).map((p) => (
              <button
                key={p}
                onClick={() => setPage(p)}
                className={`w-8 h-8 rounded-lg text-xs font-medium transition-colors ${
                  page === p
                    ? "bg-[#073763] text-white"
                    : "bg-white border border-[#E2E8F0] text-[#64748B] hover:bg-[#F1F3FF]"
                }`}
              >
                {p}
              </button>
            ))}
            <button
              disabled={page === totalPages}
              onClick={() => setPage((p) => p + 1)}
              className="p-1.5 rounded-lg border border-[#E2E8F0] bg-white hover:bg-[#F1F3FF] disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronRight size={14} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
