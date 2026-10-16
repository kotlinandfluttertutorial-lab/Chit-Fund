import { useState, useMemo } from "react";
import { Search, Download, Send, ChevronRight } from "lucide-react";

// ── Mock data ──────────────────────────────────────────────────────────────

interface MemberSuggestion {
  id: string;
  name: string;
  mobile: string;
}

const MOCK_MEMBERS: MemberSuggestion[] = [
  { id: "m1", name: "Priya Sharma", mobile: "9876543210" },
  { id: "m2", name: "Ravi Kumar", mobile: "9765432109" },
  { id: "m3", name: "Ananya Singh", mobile: "9654321098" },
  { id: "m4", name: "Suresh Patel", mobile: "9543210987" },
  { id: "m5", name: "Meena Iyer", mobile: "9432109876" },
  { id: "m6", name: "Arjun Nair", mobile: "9321098765" },
  { id: "m7", name: "Kavitha Reddy", mobile: "9210987654" },
  { id: "m8", name: "Vikram Mehta", mobile: "9109876543" },
];

interface StatementRow {
  id: string;
  date: string;
  description: string;
  debit: number;
  credit: number;
}

const MOCK_STATEMENTS: Record<string, StatementRow[]> = {
  m1: [
    { id: "s1", date: "2026-01-20", description: "Month 1 installment — Family Savings 50K", debit: 2500, credit: 0 },
    { id: "s2", date: "2026-02-20", description: "Month 2 installment — Family Savings 50K", debit: 2500, credit: 0 },
    { id: "s3", date: "2026-03-20", description: "Month 3 installment — Family Savings 50K", debit: 2500, credit: 0 },
    { id: "s4", date: "2026-03-22", description: "Late fee waiver adjustment", debit: 0, credit: 150 },
    { id: "s5", date: "2026-04-20", description: "Month 4 installment — Family Savings 50K", debit: 2500, credit: 0 },
    { id: "s6", date: "2026-05-05", description: "Refund — duplicate charge reversal", debit: 0, credit: 2500 },
    { id: "s7", date: "2026-05-20", description: "Month 5 installment — Family Savings 50K", debit: 2500, credit: 0 },
    { id: "s8", date: "2026-06-20", description: "Month 6 installment — Family Savings 50K", debit: 2500, credit: 0 },
    { id: "s9", date: "2026-07-20", description: "Month 7 installment — Family Savings 50K", debit: 2500, credit: 0 },
    { id: "s10", date: "2026-08-20", description: "Month 8 installment — Family Savings 50K", debit: 2500, credit: 0 },
    { id: "s11", date: "2026-09-20", description: "Month 9 installment — Family Savings 50K", debit: 2500, credit: 0 },
  ],
  m2: [
    { id: "t1", date: "2026-03-25", description: "Month 1 installment — Community Chit 1L", debit: 5000, credit: 0 },
    { id: "t2", date: "2026-04-25", description: "Month 2 installment — Community Chit 1L", debit: 5000, credit: 0 },
    { id: "t3", date: "2026-04-26", description: "Late fee — 6 days overdue", debit: 250, credit: 0 },
    { id: "t4", date: "2026-05-25", description: "Month 3 installment — Community Chit 1L", debit: 5000, credit: 0 },
    { id: "t5", date: "2026-06-25", description: "Month 4 installment — Community Chit 1L", debit: 5000, credit: 0 },
    { id: "t6", date: "2026-07-25", description: "Month 5 installment — Community Chit 1L", debit: 5000, credit: 0 },
    { id: "t7", date: "2026-08-10", description: "Payout received — winner Month 6", debit: 0, credit: 95000 },
    { id: "t8", date: "2026-08-25", description: "Month 6 installment — Community Chit 1L", debit: 5000, credit: 0 },
  ],
};

const OPENING_BALANCES: Record<string, number> = {
  m1: 0,
  m2: 0,
};

// ── Helpers ────────────────────────────────────────────────────────────────

function formatINR(v: number) {
  if (v === 0) return "—";
  return `₹${v.toLocaleString("en-IN")}`;
}

// ── Component ──────────────────────────────────────────────────────────────

type PeriodMode = "month" | "range";

export default function AccountStatementPage() {
  const [search, setSearch] = useState("");
  const [selectedMember, setSelectedMember] = useState<MemberSuggestion | null>(null);
  const [showDropdown, setShowDropdown] = useState(false);
  const [periodMode, setPeriodMode] = useState<PeriodMode>("month");
  const [selectedMonth, setSelectedMonth] = useState("2026-10");
  const [fromDate, setFromDate] = useState("2026-01-01");
  const [toDate, setToDate] = useState("2026-10-31");
  const [showSendMenu, setShowSendMenu] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const suggestions = MOCK_MEMBERS.filter(
    (m) =>
      search.length > 0 &&
      (m.name.toLowerCase().includes(search.toLowerCase()) ||
        m.mobile.includes(search))
  );

  function selectMember(m: MemberSuggestion) {
    setSelectedMember(m);
    setSearch(m.name);
    setShowDropdown(false);
  }

  function showToast(msg: string) {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  }

  const selectedMemberId = selectedMember?.id;
  const openingBalance = selectedMemberId ? (OPENING_BALANCES[selectedMemberId] ?? 0) : 0;

  const { rows, closingBalance } = useMemo(() => {
    const rawRows = selectedMemberId ? (MOCK_STATEMENTS[selectedMemberId] ?? []) : [];
    let filtered = rawRows;

    if (periodMode === "month") {
      filtered = rawRows.filter((r) => r.date.startsWith(selectedMonth));
    } else {
      filtered = rawRows.filter((r) => r.date >= fromDate && r.date <= toDate);
    }

    let running = openingBalance;
    const rows = filtered.map((r) => {
      running = running + r.credit - r.debit;
      return { ...r, closingBalance: running };
    });

    return { rows, closingBalance: running };
  }, [selectedMemberId, periodMode, selectedMonth, fromDate, toDate, openingBalance]);

  function downloadPDF() {
    console.log("Generating PDF statement...");
    showToast("Generating PDF statement...");
  }

  function sendVia(channel: "Email" | "WhatsApp") {
    setShowSendMenu(false);
    showToast(`Statement sent via ${channel}`);
  }

  return (
    <div className="p-6 space-y-5 relative">
      {/* Toast */}
      {toast && (
        <div className="fixed top-5 right-6 z-50 flex items-center gap-2 bg-[#073763] text-white text-sm px-4 py-2.5 rounded-lg shadow-lg">
          {toast}
        </div>
      )}

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Account Statements</h2>
          <p className="text-sm text-[#64748B]">Generate and share member account statements</p>
        </div>
        {selectedMember && (
          <div className="flex gap-2">
            {/* Send to member */}
            <div className="relative">
              <button
                onClick={() => setShowSendMenu((o) => !o)}
                className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
              >
                <Send size={14} />
                Send to Member
                <ChevronRight size={12} />
              </button>
              {showSendMenu && (
                <div className="absolute top-full mt-1 right-0 bg-white rounded-lg border border-[#E2E8F0] shadow-lg z-10 min-w-[140px] py-1">
                  <button
                    onClick={() => sendVia("Email")}
                    className="w-full text-left px-4 py-2 text-sm text-[#172B4D] hover:bg-[#F7FAFC]"
                  >
                    Email
                  </button>
                  <button
                    onClick={() => sendVia("WhatsApp")}
                    className="w-full text-left px-4 py-2 text-sm text-[#172B4D] hover:bg-[#F7FAFC]"
                  >
                    WhatsApp
                  </button>
                </div>
              )}
            </div>
            <button
              onClick={downloadPDF}
              className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors"
            >
              <Download size={14} />
              Download PDF
            </button>
          </div>
        )}
      </div>

      {/* Member search */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5">
        <h3 className="font-semibold text-[#041B3C] mb-4">Select Member</h3>
        <div className="relative max-w-md">
          <div className="relative">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#64748B]" />
            <input
              type="text"
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setShowDropdown(true);
                if (e.target.value === "") setSelectedMember(null);
              }}
              onFocus={() => setShowDropdown(true)}
              placeholder="Search by name or mobile..."
              className="w-full pl-9 pr-3 py-2 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6]"
            />
          </div>
          {showDropdown && suggestions.length > 0 && (
            <div className="absolute top-full mt-1 left-0 right-0 bg-white rounded-xl border border-[#E2E8F0] shadow-lg z-10 py-1">
              {suggestions.map((m) => (
                <button
                  key={m.id}
                  onClick={() => selectMember(m)}
                  className="w-full text-left px-4 py-2.5 hover:bg-[#F7FAFC] flex items-center justify-between"
                >
                  <span className="text-sm font-medium text-[#041B3C]">{m.name}</span>
                  <span className="text-xs text-[#64748B]">{m.mobile}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        {selectedMember && (
          <div className="mt-3 p-3 bg-[#F0FDFA] border border-[#14B8A6]/30 rounded-lg inline-flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-[#14B8A6] flex items-center justify-center text-white text-sm font-semibold">
              {selectedMember.name[0]}
            </div>
            <div>
              <p className="text-sm font-semibold text-[#041B3C]">{selectedMember.name}</p>
              <p className="text-xs text-[#64748B]">{selectedMember.mobile}</p>
            </div>
          </div>
        )}
      </div>

      {selectedMember && (
        <>
          {/* Period selector */}
          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-4 flex items-center gap-4 flex-wrap">
            <div className="flex rounded-lg border border-[#E2E8F0] overflow-hidden">
              <button
                onClick={() => setPeriodMode("month")}
                className={`px-3 py-1.5 text-sm font-medium ${
                  periodMode === "month" ? "bg-[#073763] text-white" : "bg-white text-[#64748B] hover:bg-[#F7FAFC]"
                }`}
              >
                Month Picker
              </button>
              <button
                onClick={() => setPeriodMode("range")}
                className={`px-3 py-1.5 text-sm font-medium border-l border-[#E2E8F0] ${
                  periodMode === "range" ? "bg-[#073763] text-white" : "bg-white text-[#64748B] hover:bg-[#F7FAFC]"
                }`}
              >
                Custom Range
              </button>
            </div>

            {periodMode === "month" ? (
              <input
                type="month"
                value={selectedMonth}
                onChange={(e) => setSelectedMonth(e.target.value)}
                className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6]"
              />
            ) : (
              <div className="flex items-center gap-2">
                <input
                  type="date"
                  value={fromDate}
                  onChange={(e) => setFromDate(e.target.value)}
                  className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6]"
                />
                <span className="text-[#64748B] text-sm">→</span>
                <input
                  type="date"
                  value={toDate}
                  onChange={(e) => setToDate(e.target.value)}
                  className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6]"
                />
              </div>
            )}
          </div>

          {/* Balance cards */}
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5">
              <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">Opening Balance</p>
              <p className="text-2xl font-bold text-[#041B3C]">
                {openingBalance === 0 ? "₹0" : formatINR(openingBalance)}
              </p>
            </div>
            <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5">
              <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">Closing Balance</p>
              <p className={`text-2xl font-bold ${closingBalance >= 0 ? "text-[#14B8A6]" : "text-red-500"}`}>
                {closingBalance >= 0 ? `₹${closingBalance.toLocaleString("en-IN")}` : `-₹${Math.abs(closingBalance).toLocaleString("en-IN")}`}
              </p>
            </div>
          </div>

          {/* Statement table */}
          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
            <div className="px-5 py-4 border-b border-[#E2E8F0] flex items-center justify-between">
              <h3 className="font-semibold text-[#041B3C]">
                Statement — {selectedMember.name}
              </h3>
              <span className="text-xs text-[#64748B]">{rows.length} transactions</span>
            </div>
            {rows.length === 0 ? (
              <div className="py-12 text-center text-sm text-[#64748B]">
                No transactions found for the selected period.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-[#F7FAFC]">
                    <tr>
                      {["Date", "Description", "Debit (₹)", "Credit (₹)", "Closing Balance (₹)"].map((h) => (
                        <th
                          key={h}
                          className={`px-5 py-3 text-xs font-semibold text-[#64748B] uppercase tracking-wide ${
                            h === "Date" || h === "Description" ? "text-left" : "text-right"
                          }`}
                        >
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#E2E8F0]">
                    {rows.map((row) => (
                      <tr key={row.id} className="hover:bg-[#F7FAFC] transition-colors">
                        <td className="px-5 py-3.5 text-[#64748B] text-xs">
                          {new Date(row.date).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })}
                        </td>
                        <td className="px-5 py-3.5 text-[#172B4D]">{row.description}</td>
                        <td className="px-5 py-3.5 text-right font-medium text-red-500">
                          {row.debit > 0 ? `₹${row.debit.toLocaleString("en-IN")}` : "—"}
                        </td>
                        <td className="px-5 py-3.5 text-right font-medium text-green-600">
                          {row.credit > 0 ? `₹${row.credit.toLocaleString("en-IN")}` : "—"}
                        </td>
                        <td className={`px-5 py-3.5 text-right font-semibold ${row.closingBalance >= 0 ? "text-[#041B3C]" : "text-red-500"}`}>
                          {row.closingBalance >= 0
                            ? `₹${row.closingBalance.toLocaleString("en-IN")}`
                            : `-₹${Math.abs(row.closingBalance).toLocaleString("en-IN")}`}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
