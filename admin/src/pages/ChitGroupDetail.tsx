import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Edit } from "lucide-react";

// ── Mock data ──────────────────────────────────────────────────────────────

interface GroupData {
  id: string;
  name: string;
  status: "Active" | "Draft" | "Completed" | "Suspended";
  chitValue: number;
  startDate: string;
  duration: number;
  enrolledMembers: number;
  maxMembers: number;
  commissionPct: number;
  description: string;
}

const MOCK_GROUPS: Record<string, GroupData> = {
  "1": {
    id: "1", name: "Family Savings 50K", status: "Active",
    chitValue: 50000, startDate: "2026-01-15", duration: 20,
    enrolledMembers: 18, maxMembers: 20, commissionPct: 5,
    description: "A 20-month family savings chit group with monthly installments of ₹2,500 per member.",
  },
  "2": {
    id: "2", name: "Community Chit 1L", status: "Active",
    chitValue: 100000, startDate: "2026-03-20", duration: 17,
    enrolledMembers: 17, maxMembers: 17, commissionPct: 5,
    description: "A fully enrolled community chit group with ₹1,00,000 chit value.",
  },
};

const DEFAULT_GROUP: GroupData = {
  id: "?", name: "Sample Group 75K", status: "Draft",
  chitValue: 75000, startDate: "2026-11-01", duration: 15,
  enrolledMembers: 3, maxMembers: 15, commissionPct: 5,
  description: "A new chit group awaiting members.",
};

const MOCK_MEMBERS = [
  { id: "m1", name: "Priya Sharma", joinDate: "2026-01-20", installmentsPaid: 9, outstanding: 0, status: "Active" },
  { id: "m2", name: "Ravi Kumar", joinDate: "2026-01-18", installmentsPaid: 9, outstanding: 0, status: "Active" },
  { id: "m3", name: "Ananya Singh", joinDate: "2026-02-01", installmentsPaid: 8, outstanding: 2500, status: "Active" },
  { id: "m4", name: "Suresh Patel", joinDate: "2026-01-25", installmentsPaid: 7, outstanding: 5000, status: "Active" },
  { id: "m5", name: "Meena Iyer", joinDate: "2026-01-22", installmentsPaid: 9, outstanding: 0, status: "Active" },
];

const MOCK_INSTALLMENTS = [
  { month: 1, dueDate: "15 Jan 2026", collected: 45000, expected: 45000, status: "COLLECTED" },
  { month: 2, dueDate: "15 Feb 2026", collected: 45000, expected: 45000, status: "COLLECTED" },
  { month: 3, dueDate: "15 Mar 2026", collected: 45000, expected: 45000, status: "COLLECTED" },
  { month: 4, dueDate: "15 Apr 2026", collected: 45000, expected: 45000, status: "COLLECTED" },
  { month: 5, dueDate: "15 May 2026", collected: 42500, expected: 45000, status: "PARTIAL" },
  { month: 6, dueDate: "15 Jun 2026", collected: 45000, expected: 45000, status: "COLLECTED" },
  { month: 7, dueDate: "15 Jul 2026", collected: 45000, expected: 45000, status: "COLLECTED" },
  { month: 8, dueDate: "15 Aug 2026", collected: 45000, expected: 45000, status: "COLLECTED" },
  { month: 9, dueDate: "15 Sep 2026", collected: 40000, expected: 45000, status: "PARTIAL" },
  { month: 10, dueDate: "15 Oct 2026", collected: 0, expected: 45000, status: "UPCOMING" },
];

const MOCK_FINANCIAL_MONTHS = [
  { month: "Jan 2026", collected: 45000, disbursed: 0, commission: 2250 },
  { month: "Feb 2026", collected: 45000, disbursed: 47500, commission: 2250 },
  { month: "Mar 2026", collected: 45000, disbursed: 0, commission: 2250 },
  { month: "Apr 2026", collected: 45000, disbursed: 47500, commission: 2250 },
  { month: "May 2026", collected: 42500, disbursed: 0, commission: 2250 },
  { month: "Jun 2026", collected: 45000, disbursed: 47500, commission: 2250 },
  { month: "Jul 2026", collected: 45000, disbursed: 0, commission: 2250 },
  { month: "Aug 2026", collected: 45000, disbursed: 47500, commission: 2250 },
  { month: "Sep 2026", collected: 40000, disbursed: 0, commission: 2250 },
];

// ── Helpers ────────────────────────────────────────────────────────────────

function formatINR(v: number) {
  return `₹${v.toLocaleString("en-IN")}`;
}

const statusColors: Record<string, string> = {
  Active: "bg-green-50 text-green-700 border-green-200",
  Draft: "bg-[#F1F3FF] text-[#64748B] border-[#E2E8F0]",
  Completed: "bg-blue-50 text-blue-700 border-blue-200",
  Suspended: "bg-red-50 text-red-700 border-red-200",
};

const installmentStatusStyles: Record<string, string> = {
  COLLECTED: "bg-green-50 text-green-700 border-green-200",
  PARTIAL: "bg-amber-50 text-amber-700 border-amber-200",
  UPCOMING: "bg-[#F1F3FF] text-[#64748B] border-[#E2E8F0]",
  OVERDUE: "bg-red-50 text-red-700 border-red-200",
};

const TABS = ["Overview", "Members", "Installments", "Financials"] as const;
type Tab = typeof TABS[number];

// ── Component ──────────────────────────────────────────────────────────────

export default function ChitGroupDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<Tab>("Overview");

  const group = (id && MOCK_GROUPS[id]) ? MOCK_GROUPS[id] : DEFAULT_GROUP;

  const totalCollected = MOCK_INSTALLMENTS.reduce((s, r) => s + r.collected, 0);
  const totalDisbursed = MOCK_FINANCIAL_MONTHS.reduce((s, r) => s + r.disbursed, 0);
  const totalCommission = MOCK_FINANCIAL_MONTHS.reduce((s, r) => s + r.commission, 0);
  const netBalance = totalCollected - totalDisbursed - totalCommission;
  const nextInstallment = MOCK_INSTALLMENTS.find((r) => r.status === "UPCOMING");

  return (
    <div className="p-6 space-y-5">
      {/* Back + Edit header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate("/chit-groups")}
            className="p-2 rounded-lg border border-[#E2E8F0] hover:bg-[#F1F3FF] transition-colors"
          >
            <ArrowLeft size={16} className="text-[#64748B]" />
          </button>
          <h2 className="text-xl font-semibold text-[#041B3C]">Chit Group Detail</h2>
        </div>
        <button
          onClick={() => navigate(`/chit-groups/${id}/edit`)}
          className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
        >
          <Edit size={14} />
          Edit Group
        </button>
      </div>

      {/* Header card */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
        <div className="flex items-start justify-between mb-4">
          <div>
            <div className="flex items-center gap-3 mb-1">
              <h3 className="text-xl font-bold text-[#041B3C]">{group.name}</h3>
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${statusColors[group.status] ?? statusColors.Draft}`}>
                {group.status}
              </span>
            </div>
            <p className="text-sm text-[#64748B]">{group.description}</p>
          </div>
          <div className="text-right">
            <p className="text-2xl font-bold text-[#14B8A6]">{formatINR(group.chitValue)}</p>
            <p className="text-xs text-[#64748B]">Chit Value</p>
          </div>
        </div>
        <div className="grid grid-cols-4 gap-4 pt-4 border-t border-[#E2E8F0]">
          {[
            { label: "Duration", value: `${group.duration} months` },
            { label: "Members", value: `${group.enrolledMembers} / ${group.maxMembers}` },
            { label: "Start Date", value: new Date(group.startDate).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" }) },
            { label: "Commission", value: `${group.commissionPct}%` },
          ].map(({ label, value }) => (
            <div key={label}>
              <p className="text-xs text-[#64748B]">{label}</p>
              <p className="text-sm font-semibold text-[#041B3C] mt-0.5">{value}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-[#E2E8F0]">
        <div className="flex gap-0">
          {TABS.map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-5 py-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === tab
                  ? "border-[#14B8A6] text-[#14B8A6]"
                  : "border-transparent text-[#64748B] hover:text-[#041B3C]"
              }`}
            >
              {tab}
            </button>
          ))}
        </div>
      </div>

      {/* ── Overview Tab ── */}
      {activeTab === "Overview" && (
        <div className="space-y-5">
          <div className="grid grid-cols-2 gap-4">
            {[
              { label: "Total Collected", value: formatINR(totalCollected), color: "text-[#14B8A6]" },
              { label: "Total Disbursed", value: formatINR(totalDisbursed), color: "text-[#1769AA]" },
              { label: "Next Installment", value: nextInstallment?.dueDate ?? "—", color: "text-[#041B3C]" },
              { label: "Members Joined", value: `${group.enrolledMembers} / ${group.maxMembers}`, color: "text-[#041B3C]" },
            ].map(({ label, value, color }) => (
              <div key={label} className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5">
                <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">{label}</p>
                <p className={`text-2xl font-bold ${color}`}>{value}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ── Members Tab ── */}
      {activeTab === "Members" && (
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-[#E2E8F0]">
            <h3 className="font-semibold text-[#041B3C]">Group Members ({MOCK_MEMBERS.length})</h3>
          </div>
          <table className="w-full text-sm">
            <thead className="bg-[#F7FAFC]">
              <tr>
                {["Name", "Join Date", "Installments Paid", "Outstanding", "Status"].map((h) => (
                  <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {MOCK_MEMBERS.map((m) => (
                <tr key={m.id} className="hover:bg-[#F7FAFC]">
                  <td className="px-5 py-3.5 font-medium text-[#041B3C]">{m.name}</td>
                  <td className="px-5 py-3.5 text-[#64748B]">
                    {new Date(m.joinDate).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })}
                  </td>
                  <td className="px-5 py-3.5 text-[#041B3C]">{m.installmentsPaid}</td>
                  <td className={`px-5 py-3.5 font-medium ${m.outstanding > 0 ? "text-red-500" : "text-green-600"}`}>
                    {m.outstanding > 0 ? formatINR(m.outstanding) : "—"}
                  </td>
                  <td className="px-5 py-3.5">
                    <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-50 text-green-700 border border-green-200">
                      {m.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ── Installments Tab ── */}
      {activeTab === "Installments" && (
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-[#E2E8F0]">
            <h3 className="font-semibold text-[#041B3C]">Installment Schedule</h3>
          </div>
          <table className="w-full text-sm">
            <thead className="bg-[#F7FAFC]">
              <tr>
                {["Month", "Due Date", "Collected", "Expected", "Status"].map((h) => (
                  <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {MOCK_INSTALLMENTS.map((r) => (
                <tr key={r.month} className="hover:bg-[#F7FAFC]">
                  <td className="px-5 py-3.5 font-medium text-[#041B3C]">Month {r.month}</td>
                  <td className="px-5 py-3.5 text-[#64748B]">{r.dueDate}</td>
                  <td className="px-5 py-3.5 text-green-600 font-medium">{formatINR(r.collected)}</td>
                  <td className="px-5 py-3.5 text-[#041B3C]">{formatINR(r.expected)}</td>
                  <td className="px-5 py-3.5">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${installmentStatusStyles[r.status] ?? installmentStatusStyles.UPCOMING}`}>
                      {r.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ── Financials Tab ── */}
      {activeTab === "Financials" && (
        <div className="space-y-5">
          {/* Summary cards */}
          <div className="grid grid-cols-4 gap-4">
            {[
              { label: "Total Collected", value: formatINR(totalCollected), color: "text-[#14B8A6]" },
              { label: "Total Disbursed", value: formatINR(totalDisbursed), color: "text-[#1769AA]" },
              { label: "Commission Earned", value: formatINR(totalCommission), color: "text-[#F59E0B]" },
              { label: "Net Balance", value: formatINR(netBalance), color: netBalance >= 0 ? "text-green-600" : "text-red-500" },
            ].map(({ label, value, color }) => (
              <div key={label} className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-4">
                <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">{label}</p>
                <p className={`text-xl font-bold ${color}`}>{value}</p>
              </div>
            ))}
          </div>

          {/* Month-by-month breakdown */}
          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
            <div className="px-5 py-4 border-b border-[#E2E8F0]">
              <h3 className="font-semibold text-[#041B3C]">Month-by-Month Breakdown</h3>
            </div>
            <table className="w-full text-sm">
              <thead className="bg-[#F7FAFC]">
                <tr>
                  {["Month", "Collected", "Disbursed", "Commission", "Net"].map((h) => (
                    <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-[#E2E8F0]">
                {MOCK_FINANCIAL_MONTHS.map((r) => {
                  const net = r.collected - r.disbursed - r.commission;
                  return (
                    <tr key={r.month} className="hover:bg-[#F7FAFC]">
                      <td className="px-5 py-3.5 font-medium text-[#041B3C]">{r.month}</td>
                      <td className="px-5 py-3.5 text-green-600 font-medium">{formatINR(r.collected)}</td>
                      <td className="px-5 py-3.5 text-[#1769AA]">{r.disbursed > 0 ? formatINR(r.disbursed) : "—"}</td>
                      <td className="px-5 py-3.5 text-[#F59E0B]">{formatINR(r.commission)}</td>
                      <td className={`px-5 py-3.5 font-medium ${net >= 0 ? "text-green-600" : "text-red-500"}`}>
                        {formatINR(net)}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
