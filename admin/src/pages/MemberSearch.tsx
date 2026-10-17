import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Filter, Download, UserPlus, ChevronLeft, ChevronRight, ArrowUpDown } from "lucide-react";
import type { Member } from "../types";

// ── Sample data ───────────────────────────────────────────────────────────────
const sampleMembers: Member[] = Array.from({ length: 24 }, (_, i) => ({
  id: `member-${i + 1}`,
  fullName: ["Priya Sharma", "Ravi Kumar", "Ananya Singh", "Deepak Patel", "Sunita Rao", "Arjun Nair"][i % 6],
  mobileNumber: `+9198765${String(43210 + i).padStart(5, "0")}`,
  email: `user${i + 1}@example.com`,
  accountStatus: (["ACTIVE", "ACTIVE", "ACTIVE", "INACTIVE", "SUSPENDED"][i % 5]) as Member["accountStatus"],
  enrolledGroupsCount: (i % 3) + 1,
  joinDate: `${String(Math.floor(i / 2) + 1).padStart(2, "0")}/03/2026`,
  lastPaymentDate: i % 4 === 0 ? undefined : `${String((i % 15) + 1).padStart(2, "0")}/10/2026`,
  isMobileVerified: true,
  preferredLanguage: "en",
  createdAt: "2026-03-01T00:00:00Z",
}));

const statusStyles: Record<string, string> = {
  ACTIVE: "bg-green-50 text-green-700",
  INACTIVE: "bg-slate-100 text-slate-500",
  SUSPENDED: "bg-red-50 text-red-600",
};

export default function MemberSearchPage() {
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [page, setPage] = useState(1);
  const pageSize = 10;

  const filtered = useMemo(() =>
    sampleMembers.filter((m) => {
      const matchSearch =
        !search ||
        m.fullName.toLowerCase().includes(search.toLowerCase()) ||
        m.mobileNumber.includes(search) ||
        m.id.includes(search);
      const matchStatus = statusFilter === "ALL" || m.accountStatus === statusFilter;
      return matchSearch && matchStatus;
    }),
  [search, statusFilter]);

  const paginated = filtered.slice((page - 1) * pageSize, page * pageSize);
  const totalPages = Math.ceil(filtered.length / pageSize);

  return (
    <div className="p-6 space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Members</h2>
          <p className="text-sm text-[#64748B]">{filtered.length} total members</p>
        </div>
        <div className="flex gap-2">
          <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
            <Download size={14} />
            Export CSV
          </button>
          <button className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors">
            <UserPlus size={14} />
            Add Member
          </button>
        </div>
      </div>

      {/* Filters — NO KYC status filter */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] p-4 shadow-sm flex items-center gap-3 flex-wrap">
        <div className="flex items-center gap-2 flex-1 min-w-60 bg-[#F7FAFC] rounded-lg px-3 py-2 border border-[#E2E8F0]">
          <Search size={15} className="text-[#64748B] shrink-0" />
          <input
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(1); }}
            placeholder="Search by name, mobile, member ID…"
            className="flex-1 bg-transparent text-sm text-[#041B3C] outline-none placeholder:text-[#94A3B8]"
          />
        </div>
        <div className="flex items-center gap-2">
          <Filter size={14} className="text-[#64748B]" />
          <span className="text-sm text-[#64748B]">Status:</span>
          {["ALL", "ACTIVE", "INACTIVE", "SUSPENDED"].map((s) => (
            <button
              key={s}
              onClick={() => { setStatusFilter(s); setPage(1); }}
              className={`px-3 py-1.5 rounded-full text-xs font-medium transition-colors ${
                statusFilter === s
                  ? "bg-[#073763] text-white"
                  : "bg-[#F1F3FF] text-[#64748B] hover:bg-[#E0E8FF]"
              }`}
            >
              {s === "ALL" ? "All" : s.charAt(0) + s.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-[#F7FAFC] border-b border-[#E2E8F0]">
            <tr>
              {["Name", "Mobile", "Account Status", "Groups", "Join Date", "Last Payment", "Actions"].map((h) => (
                <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">
                  <span className="flex items-center gap-1">
                    {h}
                    {["Name", "Join Date"].includes(h) && <ArrowUpDown size={11} className="opacity-50" />}
                  </span>
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[#E2E8F0]">
            {paginated.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-5 py-12 text-center text-[#64748B] text-sm">
                  No members found matching your search.
                </td>
              </tr>
            ) : (
              paginated.map((m) => (
                <tr
                  key={m.id}
                  className="hover:bg-[#F7FAFC] transition-colors cursor-pointer"
                  onClick={() => navigate(`/members/${m.id}`)}
                >
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-[#073763] flex items-center justify-center shrink-0">
                        <span className="text-white text-xs font-semibold">{m.fullName.charAt(0)}</span>
                      </div>
                      <div>
                        <div className="font-medium text-[#041B3C]">{m.fullName}</div>
                        <div className="text-xs text-[#64748B]">#{m.id.slice(-4).toUpperCase()}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-3.5 text-[#64748B] font-mono text-xs">{m.mobileNumber}</td>
                  <td className="px-5 py-3.5">
                    <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${statusStyles[m.accountStatus]}`}>
                      {m.accountStatus}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 text-[#64748B] text-center">{m.enrolledGroupsCount}</td>
                  <td className="px-5 py-3.5 text-[#64748B] text-xs">{m.joinDate}</td>
                  <td className="px-5 py-3.5 text-[#64748B] text-xs">{m.lastPaymentDate ?? "—"}</td>
                  <td className="px-5 py-3.5">
                    <div className="flex gap-2" onClick={(e) => e.stopPropagation()}>
                      <button onClick={() => navigate(`/members/${m.id}`)} className="px-2.5 py-1 text-xs font-medium text-[#073763] border border-[#073763] rounded-lg hover:bg-[#F1F3FF] transition-colors">
                        View
                      </button>
                      <button className="px-2.5 py-1 text-xs font-medium text-[#64748B] border border-[#E2E8F0] rounded-lg hover:bg-[#F7FAFC] transition-colors">
                        Edit
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {/* Pagination */}
        <div className="px-5 py-3.5 border-t border-[#E2E8F0] flex items-center justify-between bg-[#F7FAFC]">
          <span className="text-xs text-[#64748B]">
            Showing {Math.min((page - 1) * pageSize + 1, filtered.length)}–{Math.min(page * pageSize, filtered.length)} of {filtered.length}
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
                  page === p ? "bg-[#073763] text-white" : "bg-white border border-[#E2E8F0] text-[#64748B] hover:bg-[#F1F3FF]"
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
