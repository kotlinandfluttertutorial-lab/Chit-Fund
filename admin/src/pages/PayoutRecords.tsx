import { useState, useMemo } from "react";
import { Download, AlertCircle } from "lucide-react";
import type { Payout, PayoutStatus } from "../types";

// ── Sample data ───────────────────────────────────────────────────────────────

interface PayoutRecord extends Payout {
  bankAccount: string;
}

const samplePayouts: PayoutRecord[] = [
  {
    id: "po-001",
    memberId: "mem-0042",
    memberName: "Ananya Singh",
    chitGroupId: "grp-0012",
    chitGroupName: "Community Chit 1L",
    monthNumber: 6,
    grossAmount: 100000,
    foremanCommission: 5000,
    netAmount: 95000,
    bankAccount: "****4821",
    bankReference: "NEFT/2610142348",
    status: "CREDITED",
    creditDate: "2026-10-05",
    createdAt: "2026-10-04T10:00:00Z",
  },
  {
    id: "po-002",
    memberId: "mem-0017",
    memberName: "Ravi Kumar",
    chitGroupId: "grp-0009",
    chitGroupName: "Family Savings 50K",
    monthNumber: 4,
    grossAmount: 50000,
    foremanCommission: 2500,
    netAmount: 47500,
    bankAccount: "****9134",
    bankReference: "RTGS/2610082211",
    status: "CREDITED",
    creditDate: "2026-10-08",
    createdAt: "2026-10-07T14:30:00Z",
  },
  {
    id: "po-003",
    memberId: "mem-0088",
    memberName: "Deepak Patel",
    chitGroupId: "grp-0021",
    chitGroupName: "Sunrise Savings 2L",
    monthNumber: 3,
    grossAmount: 200000,
    foremanCommission: 10000,
    netAmount: 190000,
    bankAccount: "****7705",
    bankReference: undefined,
    status: "PROCESSING",
    creditDate: undefined,
    createdAt: "2026-10-12T09:15:00Z",
  },
  {
    id: "po-004",
    memberId: "mem-0031",
    memberName: "Sunita Rao",
    chitGroupId: "grp-0015",
    chitGroupName: "Heritage Chit 75K",
    monthNumber: 5,
    grossAmount: 75000,
    foremanCommission: 3750,
    netAmount: 71250,
    bankAccount: "****3362",
    bankReference: undefined,
    status: "PENDING",
    creditDate: undefined,
    createdAt: "2026-10-13T11:00:00Z",
  },
  {
    id: "po-005",
    memberId: "mem-0055",
    memberName: "Priya Sharma",
    chitGroupId: "grp-0009",
    chitGroupName: "Family Savings 50K",
    monthNumber: 7,
    grossAmount: 50000,
    foremanCommission: 2500,
    netAmount: 47500,
    bankAccount: "****6601",
    bankReference: undefined,
    status: "FAILED",
    creditDate: undefined,
    createdAt: "2026-10-10T16:45:00Z",
  },
];

// ── Style helpers ─────────────────────────────────────────────────────────────

const statusStyles: Record<PayoutStatus, string> = {
  CREDITED: "bg-green-50 text-green-700",
  PROCESSING: "bg-blue-50 text-blue-700",
  PENDING: "bg-amber-50 text-amber-700",
  FAILED: "bg-red-50 text-red-600",
  APPROVED: "bg-teal-50 text-teal-700",
};

const statusLabels: Record<PayoutStatus, string> = {
  CREDITED: "Credited",
  PROCESSING: "Processing",
  PENDING: "Pending",
  FAILED: "Failed",
  APPROVED: "Approved",
};

const STATUS_CHIPS: (PayoutStatus | "ALL")[] = ["ALL", "CREDITED", "PROCESSING", "PENDING", "FAILED"];

const formatINR = (amount: number) =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(amount);

// ── Page ──────────────────────────────────────────────────────────────────────

export default function PayoutRecordsPage() {
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [groupFilter, setGroupFilter] = useState("All");
  const [statusFilter, setStatusFilter] = useState<PayoutStatus | "ALL">("ALL");

  const uniqueGroups = useMemo(() => {
    const names = Array.from(new Set(samplePayouts.map((p) => p.chitGroupName)));
    return ["All", ...names];
  }, []);

  const filtered = useMemo(() => {
    return samplePayouts.filter((p) => {
      const matchStatus = statusFilter === "ALL" || p.status === statusFilter;
      const matchGroup = groupFilter === "All" || p.chitGroupName === groupFilter;
      const recordDate = p.createdAt.slice(0, 10);
      const matchFrom = !dateFrom || recordDate >= dateFrom;
      const matchTo = !dateTo || recordDate <= dateTo;
      return matchStatus && matchGroup && matchFrom && matchTo;
    });
  }, [statusFilter, groupFilter, dateFrom, dateTo]);

  return (
    <div className="p-6 space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Payout Records</h2>
          <p className="text-sm text-[#64748B]">{filtered.length} payout records</p>
        </div>
        <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
          <Download size={14} />
          Export CSV
        </button>
      </div>

      {/* Amber info banner */}
      <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 flex items-start gap-3">
        <AlertCircle size={18} className="text-amber-600 shrink-0 mt-0.5" />
        <p className="text-sm text-amber-800">
          <span className="font-semibold">Payout approval workflow is deferred.</span> This view shows read-only payout records. No approval or rejection actions are available in this release.
        </p>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] p-4 shadow-sm flex items-center gap-3 flex-wrap">
        {/* Date range */}
        <div className="flex items-center gap-2">
          <span className="text-xs text-[#64748B] font-medium">From:</span>
          <input
            type="date"
            value={dateFrom}
            onChange={(e) => setDateFrom(e.target.value)}
            className="px-3 py-1.5 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
          />
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs text-[#64748B] font-medium">To:</span>
          <input
            type="date"
            value={dateTo}
            onChange={(e) => setDateTo(e.target.value)}
            className="px-3 py-1.5 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
          />
        </div>

        {/* Group filter */}
        <select
          value={groupFilter}
          onChange={(e) => setGroupFilter(e.target.value)}
          className="px-3 py-1.5 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] bg-white focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
        >
          {uniqueGroups.map((g) => (
            <option key={g} value={g}>{g}</option>
          ))}
        </select>

        {/* Status chips */}
        <div className="flex items-center gap-1.5">
          {STATUS_CHIPS.map((s) => (
            <button
              key={s}
              onClick={() => setStatusFilter(s)}
              className={`px-3 py-1.5 rounded-full text-xs font-medium transition-colors ${
                statusFilter === s
                  ? "bg-[#073763] text-white"
                  : "bg-[#F1F3FF] text-[#64748B] hover:bg-[#E0E8FF]"
              }`}
            >
              {s === "ALL" ? "All" : statusLabels[s as PayoutStatus]}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden overflow-x-auto">
        <table className="w-full text-sm min-w-[1100px]">
          <thead className="bg-[#F7FAFC] border-b border-[#E2E8F0]">
            <tr>
              {[
                "Member Name",
                "Group Name",
                "Cycle Month",
                "Gross Payout",
                "Foreman Comm.",
                "Net Payout",
                "Bank Account",
                "Transfer Date",
                "Bank Reference",
                "Status",
                "Actions",
              ].map((h) => (
                <th
                  key={h}
                  className="px-4 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide whitespace-nowrap"
                >
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[#E2E8F0]">
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={11} className="px-5 py-12 text-center text-[#64748B] text-sm">
                  No payout records match your filters.
                </td>
              </tr>
            ) : (
              filtered.map((payout) => (
                <tr key={payout.id} className="hover:bg-[#F7FAFC] transition-colors">
                  <td className="px-4 py-3.5 font-medium text-[#041B3C]">{payout.memberName}</td>
                  <td className="px-4 py-3.5 text-[#64748B] text-xs">{payout.chitGroupName}</td>
                  <td className="px-4 py-3.5 text-[#64748B] text-xs">Month {payout.monthNumber}</td>
                  <td className="px-4 py-3.5 font-medium text-[#041B3C]">{formatINR(payout.grossAmount)}</td>
                  <td className="px-4 py-3.5 text-[#64748B]">{formatINR(payout.foremanCommission)}</td>
                  <td className="px-4 py-3.5 font-semibold text-[#041B3C]">{formatINR(payout.netAmount)}</td>
                  <td className="px-4 py-3.5 font-mono text-xs text-[#64748B]">{payout.bankAccount}</td>
                  <td className="px-4 py-3.5 text-xs text-[#64748B]">
                    {payout.creditDate
                      ? new Date(payout.creditDate).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })
                      : "—"}
                  </td>
                  <td className="px-4 py-3.5 font-mono text-xs text-[#64748B]">
                    {payout.bankReference ?? "—"}
                  </td>
                  <td className="px-4 py-3.5">
                    <span
                      className={`px-2.5 py-1 rounded-full text-xs font-semibold ${statusStyles[payout.status]}`}
                    >
                      {statusLabels[payout.status]}
                    </span>
                  </td>
                  <td className="px-4 py-3.5">
                    {payout.status === "CREDITED" ? (
                      <button className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg border border-[#E2E8F0] bg-white text-xs font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors whitespace-nowrap">
                        <Download size={12} />
                        Certificate
                      </button>
                    ) : (
                      <span className="text-[#94A3B8] text-xs">—</span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
