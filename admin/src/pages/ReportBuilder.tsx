import { useState } from "react";
import { BarChart3, Users, Wallet, Bot, ChevronLeft, ChevronRight, Download, CalendarClock } from "lucide-react";
import { useNavigate } from "react-router-dom";

// ── Types ─────────────────────────────────────────────────────────────────────

type ReportType = "Collections" | "Member Activity" | "Payouts" | "AI Usage" | null;

// ── Step 1 data ───────────────────────────────────────────────────────────────

const reportTypes = [
  {
    id: "Collections" as ReportType,
    icon: BarChart3,
    title: "Collections",
    description: "Installment collections, overdue, and rates by group.",
    accent: "#073763",
  },
  {
    id: "Member Activity" as ReportType,
    icon: Users,
    title: "Member Activity",
    description: "Enrollment trends, payment patterns, and churn.",
    accent: "#14B8A6",
  },
  {
    id: "Payouts" as ReportType,
    icon: Wallet,
    title: "Payouts",
    description: "Disbursement records, commissions, and projections.",
    accent: "#7C3AED",
  },
  {
    id: "AI Usage" as ReportType,
    icon: Bot,
    title: "AI Usage",
    description: "Assistant usage, tool calls, and response quality.",
    accent: "#F59E0B",
  },
];

// ── Step 2 groups ─────────────────────────────────────────────────────────────

const availableGroups = [
  "Family Savings 50K",
  "Community Chit 1L",
  "Monthly Circle 25K",
  "Sunrise Savings 2L",
  "Heritage Chit 75K",
];

// ── Step 3 columns ────────────────────────────────────────────────────────────

const allColumns = [
  "Member Name",
  "Group Name",
  "Amount (₹)",
  "Date",
  "Status",
  "Reference",
  "Payment Method",
  "Foreman Commission (₹)",
  "Net Amount (₹)",
  "Cycle Month",
];

// ── Step 4 preview data ───────────────────────────────────────────────────────

const previewRows = [
  { "Member Name": "Priya Sharma", "Group Name": "Family Savings 50K", "Amount (₹)": "₹2,500", "Date": "01 Oct 2026", "Status": "Paid", "Reference": "UPI-23481", "Payment Method": "UPI", "Foreman Commission (₹)": "₹125", "Net Amount (₹)": "₹2,375", "Cycle Month": "Oct 2026" },
  { "Member Name": "Ravi Kumar", "Group Name": "Community Chit 1L", "Amount (₹)": "₹5,000", "Date": "02 Oct 2026", "Status": "Paid", "Reference": "NB-98123", "Payment Method": "Net Banking", "Foreman Commission (₹)": "₹250", "Net Amount (₹)": "₹4,750", "Cycle Month": "Oct 2026" },
  { "Member Name": "Ananya Singh", "Group Name": "Family Savings 50K", "Amount (₹)": "₹2,500", "Date": "03 Oct 2026", "Status": "Paid", "Reference": "UPI-23510", "Payment Method": "UPI", "Foreman Commission (₹)": "₹125", "Net Amount (₹)": "₹2,375", "Cycle Month": "Oct 2026" },
  { "Member Name": "Deepak Patel", "Group Name": "Sunrise Savings 2L", "Amount (₹)": "₹10,000", "Date": "05 Oct 2026", "Status": "Overdue", "Reference": "—", "Payment Method": "—", "Foreman Commission (₹)": "—", "Net Amount (₹)": "—", "Cycle Month": "Oct 2026" },
  { "Member Name": "Sunita Rao", "Group Name": "Community Chit 1L", "Amount (₹)": "₹5,000", "Date": "04 Oct 2026", "Status": "Paid", "Reference": "DC-44512", "Payment Method": "Debit Card", "Foreman Commission (₹)": "₹250", "Net Amount (₹)": "₹4,750", "Cycle Month": "Oct 2026" },
];

const STEPS = ["Report Type", "Filters", "Columns", "Preview"];

// ── Page ──────────────────────────────────────────────────────────────────────

export default function ReportBuilderPage() {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedType, setSelectedType] = useState<ReportType>(null);
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [selectedGroups, setSelectedGroups] = useState<string[]>([]);
  const [statusFilter, setStatusFilter] = useState<string[]>([]);
  const [selectedColumns, setSelectedColumns] = useState<string[]>(allColumns.slice(0, 6));
  const [scheduleFreq, setScheduleFreq] = useState("Monthly");
  const [scheduleEmail, setScheduleEmail] = useState("");

  const toggleGroup = (g: string) => {
    setSelectedGroups((prev) =>
      prev.includes(g) ? prev.filter((x) => x !== g) : [...prev, g]
    );
  };

  const toggleStatus = (s: string) => {
    setStatusFilter((prev) =>
      prev.includes(s) ? prev.filter((x) => x !== s) : [...prev, s]
    );
  };

  const toggleColumn = (col: string) => {
    setSelectedColumns((prev) =>
      prev.includes(col) ? prev.filter((x) => x !== col) : [...prev, col]
    );
  };

  const canProceed =
    (currentStep === 1 && selectedType !== null) ||
    currentStep === 2 ||
    (currentStep === 3 && selectedColumns.length > 0) ||
    currentStep === 4;

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Report Builder</h2>
          <p className="text-sm text-[#64748B]">Build a custom report in 4 steps.</p>
        </div>
        <button
          onClick={() => navigate("/reports")}
          className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
        >
          <ChevronLeft size={14} />
          Back to Reports
        </button>
      </div>

      {/* Step indicator */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] p-4 shadow-sm">
        <div className="flex items-center gap-0">
          {STEPS.map((step, i) => {
            const stepNum = i + 1;
            const isActive = currentStep === stepNum;
            const isCompleted = currentStep > stepNum;
            return (
              <div key={step} className="flex items-center flex-1 last:flex-none">
                <button
                  onClick={() => isCompleted && setCurrentStep(stepNum)}
                  className={`flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? "bg-[#073763] text-white"
                      : isCompleted
                      ? "bg-[#14B8A6] text-white cursor-pointer hover:bg-[#0d9488]"
                      : "bg-[#F1F3FF] text-[#64748B] cursor-default"
                  }`}
                >
                  <span className="w-5 h-5 rounded-full bg-white/20 flex items-center justify-center text-xs font-bold shrink-0">
                    {stepNum}
                  </span>
                  {step}
                </button>
                {i < STEPS.length - 1 && (
                  <ChevronRight size={16} className="text-[#E2E8F0] mx-1 shrink-0" />
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Step content */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
        {/* ── Step 1: Report Type ──────────────────────────────────── */}
        {currentStep === 1 && (
          <div>
            <h3 className="font-semibold text-[#041B3C] mb-1">Select Report Type</h3>
            <p className="text-sm text-[#64748B] mb-5">Choose the type of report you want to generate.</p>
            <div className="grid grid-cols-4 gap-4">
              {reportTypes.map((rt) => (
                <button
                  key={rt.id}
                  onClick={() => setSelectedType(rt.id)}
                  className={`flex flex-col items-start p-4 rounded-xl border-2 transition-all text-left ${
                    selectedType === rt.id
                      ? "border-[#073763] bg-[#F1F3FF] ring-2 ring-[#073763]/20"
                      : "border-[#E2E8F0] bg-white hover:border-[#073763]/30 hover:bg-[#F7FAFC]"
                  }`}
                >
                  <div
                    className="w-9 h-9 rounded-lg flex items-center justify-center mb-3"
                    style={{ background: `${rt.accent}15` }}
                  >
                    <rt.icon size={18} style={{ color: rt.accent }} />
                  </div>
                  <p className="font-semibold text-[#041B3C] text-sm">{rt.title}</p>
                  <p className="text-xs text-[#64748B] mt-1 leading-relaxed">{rt.description}</p>
                </button>
              ))}
            </div>
          </div>
        )}

        {/* ── Step 2: Filters ──────────────────────────────────────── */}
        {currentStep === 2 && (
          <div>
            <h3 className="font-semibold text-[#041B3C] mb-1">Configure Filters</h3>
            <p className="text-sm text-[#64748B] mb-5">Narrow down the data to include in your report.</p>
            <div className="space-y-5">
              {/* Date range */}
              <div>
                <label className="block text-sm font-medium text-[#041B3C] mb-2">Date Range</label>
                <div className="flex items-center gap-3">
                  <input
                    type="date"
                    value={dateFrom}
                    onChange={(e) => setDateFrom(e.target.value)}
                    className="px-3 py-2 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
                  />
                  <span className="text-[#64748B] text-sm">to</span>
                  <input
                    type="date"
                    value={dateTo}
                    onChange={(e) => setDateTo(e.target.value)}
                    className="px-3 py-2 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
                  />
                </div>
              </div>

              {/* Group selector */}
              <div>
                <label className="block text-sm font-medium text-[#041B3C] mb-2">Chit Groups</label>
                <div className="max-h-40 overflow-y-auto border border-[#E2E8F0] rounded-lg p-2 space-y-1">
                  {availableGroups.map((g) => (
                    <label key={g} className="flex items-center gap-2.5 px-2 py-1.5 rounded-lg hover:bg-[#F7FAFC] cursor-pointer">
                      <input
                        type="checkbox"
                        checked={selectedGroups.includes(g)}
                        onChange={() => toggleGroup(g)}
                        className="accent-[#073763]"
                      />
                      <span className="text-sm text-[#041B3C]">{g}</span>
                    </label>
                  ))}
                </div>
                {selectedGroups.length === 0 && (
                  <p className="text-xs text-[#64748B] mt-1">No groups selected — all groups will be included.</p>
                )}
              </div>

              {/* Status filter */}
              <div>
                <label className="block text-sm font-medium text-[#041B3C] mb-2">Status</label>
                <div className="flex flex-wrap gap-2">
                  {["Paid", "Overdue", "Pending", "Failed"].map((s) => (
                    <label key={s} className="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-[#E2E8F0] hover:bg-[#F7FAFC] cursor-pointer">
                      <input
                        type="checkbox"
                        checked={statusFilter.includes(s)}
                        onChange={() => toggleStatus(s)}
                        className="accent-[#073763]"
                      />
                      <span className="text-sm text-[#041B3C]">{s}</span>
                    </label>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ── Step 3: Columns ──────────────────────────────────────── */}
        {currentStep === 3 && (
          <div>
            <h3 className="font-semibold text-[#041B3C] mb-1">Select Columns</h3>
            <p className="text-sm text-[#64748B] mb-5">
              Choose which columns to include. {selectedColumns.length} of {allColumns.length} selected.
            </p>
            <div className="grid grid-cols-2 gap-2">
              {allColumns.map((col) => (
                <label
                  key={col}
                  className="flex items-center gap-3 px-4 py-3 rounded-xl border border-[#E2E8F0] hover:bg-[#F7FAFC] cursor-pointer transition-colors"
                >
                  <input
                    type="checkbox"
                    checked={selectedColumns.includes(col)}
                    onChange={() => toggleColumn(col)}
                    className="accent-[#073763]"
                  />
                  <span className="text-sm font-medium text-[#041B3C]">{col}</span>
                </label>
              ))}
            </div>
          </div>
        )}

        {/* ── Step 4: Preview ──────────────────────────────────────── */}
        {currentStep === 4 && (
          <div>
            <div className="flex items-center justify-between mb-5">
              <div>
                <h3 className="font-semibold text-[#041B3C]">Preview — {selectedType ?? "Report"}</h3>
                <p className="text-sm text-[#64748B]">Showing first 5 rows of sample data.</p>
              </div>
              <div className="flex gap-2">
                <button className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
                  <Download size={13} />
                  Export PDF
                </button>
                <button className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
                  <Download size={13} />
                  Export Excel
                </button>
                <button className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
                  <Download size={13} />
                  Export CSV
                </button>
              </div>
            </div>

            {/* Preview table */}
            <div className="overflow-x-auto rounded-xl border border-[#E2E8F0]">
              <table className="w-full text-sm min-w-max">
                <thead className="bg-[#F7FAFC] border-b border-[#E2E8F0]">
                  <tr>
                    {selectedColumns.map((col) => (
                      <th
                        key={col}
                        className="px-4 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide whitespace-nowrap"
                      >
                        {col}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#E2E8F0]">
                  {previewRows.map((row, i) => (
                    <tr key={i} className="hover:bg-[#F7FAFC] transition-colors">
                      {selectedColumns.map((col) => (
                        <td key={col} className="px-4 py-3 text-xs text-[#041B3C] whitespace-nowrap">
                          {(row as Record<string, string>)[col] ?? "—"}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Schedule section */}
            <div className="mt-6 p-5 rounded-xl border border-[#E2E8F0] bg-[#F7FAFC]">
              <div className="flex items-center gap-2 mb-4">
                <CalendarClock size={16} className="text-[#073763]" />
                <h4 className="font-semibold text-[#041B3C]">Schedule This Report</h4>
              </div>
              <div className="flex items-end gap-3 flex-wrap">
                <div>
                  <label className="block text-xs font-medium text-[#64748B] mb-1.5">Frequency</label>
                  <select
                    value={scheduleFreq}
                    onChange={(e) => setScheduleFreq(e.target.value)}
                    className="px-3 py-2 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] bg-white focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
                  >
                    <option value="Daily">Daily</option>
                    <option value="Weekly">Weekly</option>
                    <option value="Monthly">Monthly</option>
                  </select>
                </div>
                <div className="flex-1 min-w-48">
                  <label className="block text-xs font-medium text-[#64748B] mb-1.5">Deliver to email</label>
                  <input
                    type="email"
                    value={scheduleEmail}
                    onChange={(e) => setScheduleEmail(e.target.value)}
                    placeholder="admin@chitsmart.in"
                    className="w-full px-3 py-2 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] placeholder:text-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
                  />
                </div>
                <button className="px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
                  Add Schedule
                </button>
              </div>
            </div>

            {/* Save CTA */}
            <div className="mt-4">
              <button className="w-full px-4 py-3 rounded-xl bg-[#073763] text-white text-sm font-semibold hover:bg-[#1769AA] transition-colors">
                Save as Custom Report
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Navigation buttons */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => setCurrentStep((s) => s - 1)}
          disabled={currentStep === 1}
          className="flex items-center gap-2 px-4 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
        >
          <ChevronLeft size={14} />
          Back
        </button>
        {currentStep < 4 && (
          <button
            onClick={() => setCurrentStep((s) => s + 1)}
            disabled={!canProceed}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
          >
            Next
            <ChevronRight size={14} />
          </button>
        )}
      </div>
    </div>
  );
}
