import { useNavigate } from "react-router-dom";
import { BarChart3, Users, Wallet, Bot, Download, Play, Clock, CalendarClock } from "lucide-react";

// ── Report categories ─────────────────────────────────────────────────────────

const reportCategories = [
  {
    id: "collections",
    icon: BarChart3,
    title: "Collections",
    description: "Monthly installment collection analysis — amounts received, overdue, and collection rates by group.",
    lastGenerated: "14 Oct 2026",
    accent: "#073763",
  },
  {
    id: "member-activity",
    icon: Users,
    title: "Member Activity",
    description: "Enrollment trends, payment patterns, active vs inactive members, and churn analysis.",
    lastGenerated: "13 Oct 2026",
    accent: "#14B8A6",
  },
  {
    id: "payouts",
    icon: Wallet,
    title: "Payouts",
    description: "Disbursement history, foreman commissions, net payouts, and projections for upcoming cycles.",
    lastGenerated: "10 Oct 2026",
    accent: "#7C3AED",
  },
  {
    id: "ai-usage",
    icon: Bot,
    title: "AI Usage",
    description: "Assistant queries, tool call breakdown, response quality scores, and multilingual usage metrics.",
    lastGenerated: "12 Oct 2026",
    accent: "#F59E0B",
  },
];

// ── Quick reports ─────────────────────────────────────────────────────────────

const quickReports = [
  {
    name: "Outstanding Installments",
    description: "Members with overdue installments across all active groups.",
  },
  {
    name: "Collection Rate by Group",
    description: "Side-by-side collection rate comparison for all chit groups.",
  },
  {
    name: "Member Enrollment Timeline",
    description: "Weekly enrollment counts for the last 12 months.",
  },
];

// ── Report history ────────────────────────────────────────────────────────────

const reportHistory = [
  {
    id: "rh-001",
    name: "Collections_Oct2026.csv",
    category: "Collections",
    generatedOn: "14 Oct 2026, 09:15",
    generatedBy: "Kavitha Nair",
    format: "CSV",
    size: "84 KB",
  },
  {
    id: "rh-002",
    name: "MemberActivity_Q3_2026.xlsx",
    category: "Member Activity",
    generatedOn: "13 Oct 2026, 16:40",
    generatedBy: "Rajesh Menon",
    format: "Excel",
    size: "212 KB",
  },
  {
    id: "rh-003",
    name: "Payouts_Sep2026.pdf",
    category: "Payouts",
    generatedOn: "10 Oct 2026, 11:00",
    generatedBy: "Kavitha Nair",
    format: "PDF",
    size: "156 KB",
  },
  {
    id: "rh-004",
    name: "AIUsage_Oct2026.csv",
    category: "AI Usage",
    generatedOn: "12 Oct 2026, 14:22",
    generatedBy: "Rajesh Menon",
    format: "CSV",
    size: "36 KB",
  },
  {
    id: "rh-005",
    name: "Collections_Sep2026.csv",
    category: "Collections",
    generatedOn: "01 Oct 2026, 08:05",
    generatedBy: "Kavitha Nair",
    format: "CSV",
    size: "78 KB",
  },
];

const formatChipStyles: Record<string, string> = {
  CSV: "bg-green-50 text-green-700",
  Excel: "bg-teal-50 text-teal-700",
  PDF: "bg-red-50 text-red-600",
};

// ── Page ──────────────────────────────────────────────────────────────────────

export default function ReportsPage() {
  const navigate = useNavigate();

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Reports</h2>
          <p className="text-sm text-[#64748B]">Generate, schedule, and export financial and operational reports.</p>
        </div>
        <button
          onClick={() => navigate("/reports/builder")}
          className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors"
        >
          <BarChart3 size={14} />
          Report Builder
        </button>
      </div>

      {/* Report category cards */}
      <div className="grid grid-cols-2 gap-5">
        {reportCategories.map((cat) => (
          <div
            key={cat.id}
            className="bg-white rounded-xl border border-[#E2E8F0] p-5 shadow-sm flex flex-col gap-4"
          >
            <div className="flex items-start gap-3">
              <div
                className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
                style={{ background: `${cat.accent}15` }}
              >
                <cat.icon size={20} style={{ color: cat.accent }} />
              </div>
              <div className="flex-1">
                <h3 className="font-semibold text-[#041B3C]">{cat.title}</h3>
                <p className="text-xs text-[#64748B] mt-1 leading-relaxed">{cat.description}</p>
              </div>
            </div>
            <div className="flex items-center justify-between pt-1 border-t border-[#E2E8F0]">
              <div className="flex items-center gap-1.5 text-xs text-[#64748B]">
                <Clock size={12} />
                <span>Last generated: {cat.lastGenerated}</span>
              </div>
              <div className="flex gap-2">
                <button className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-[#E2E8F0] bg-white text-xs font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
                  <CalendarClock size={12} />
                  Schedule
                </button>
                <button className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#073763] text-white text-xs font-medium hover:bg-[#1769AA] transition-colors">
                  <Play size={12} />
                  Run Report
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Quick reports */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-[#E2E8F0]">
          <h3 className="font-semibold text-[#041B3C]">Quick Reports</h3>
          <p className="text-xs text-[#64748B] mt-0.5">Pre-built reports — run instantly with default settings.</p>
        </div>
        <div className="divide-y divide-[#E2E8F0]">
          {quickReports.map((report) => (
            <div key={report.name} className="px-5 py-4 flex items-center justify-between hover:bg-[#F7FAFC] transition-colors">
              <div>
                <p className="font-medium text-[#041B3C] text-sm">{report.name}</p>
                <p className="text-xs text-[#64748B] mt-0.5">{report.description}</p>
              </div>
              <button className="text-[#073763] hover:underline text-sm font-medium shrink-0 ml-4">
                Run
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* Report history */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-[#E2E8F0] flex items-center justify-between">
          <h3 className="font-semibold text-[#041B3C]">Recent Reports</h3>
          <span className="text-xs text-[#64748B]">Last 5 generated</span>
        </div>
        <table className="w-full text-sm">
          <thead className="bg-[#F7FAFC] border-b border-[#E2E8F0]">
            <tr>
              {["Report Name", "Category", "Generated On", "Generated By", "Format", "Download"].map((h) => (
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
            {reportHistory.map((r) => (
              <tr key={r.id} className="hover:bg-[#F7FAFC] transition-colors">
                <td className="px-5 py-3.5 font-medium text-[#041B3C] text-xs font-mono">{r.name}</td>
                <td className="px-5 py-3.5 text-[#64748B] text-sm">{r.category}</td>
                <td className="px-5 py-3.5 text-xs text-[#64748B]">{r.generatedOn}</td>
                <td className="px-5 py-3.5 text-xs text-[#64748B]">{r.generatedBy}</td>
                <td className="px-5 py-3.5">
                  <span
                    className={`px-2.5 py-1 rounded-full text-xs font-semibold ${formatChipStyles[r.format] ?? "bg-slate-100 text-slate-600"}`}
                  >
                    {r.format}
                  </span>
                </td>
                <td className="px-5 py-3.5">
                  <button className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg border border-[#E2E8F0] bg-white text-xs font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
                    <Download size={12} />
                    {r.size}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
