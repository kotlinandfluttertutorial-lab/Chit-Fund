import { useState, useMemo } from "react";
import { Download, BookOpen } from "lucide-react";

// ── Mock data ──────────────────────────────────────────────────────────────

const MOCK_GROUPS = [
  { id: "g1", name: "Family Savings 50K" },
  { id: "g2", name: "Community Chit 1L" },
  { id: "g3", name: "Monthly Circle 25K" },
  { id: "g4", name: "Sunrise Savings 2L" },
];

type EntryType = "INSTALLMENT" | "LATE_FEE" | "PAYOUT" | "FOREMAN_COMMISSION" | "REFUND" | "ADJUSTMENT";

interface LedgerRow {
  id: string;
  date: string;
  narration: string;
  entryType: EntryType;
  debit: number;
  credit: number;
}

const MOCK_ENTRIES: LedgerRow[] = [
  { id: "e1", date: "2026-01-15", narration: "Opening balance — Family Savings 50K", entryType: "ADJUSTMENT", debit: 0, credit: 0 },
  { id: "e2", date: "2026-01-20", narration: "Month 1 installment — 18/20 members paid", entryType: "INSTALLMENT", debit: 0, credit: 45000 },
  { id: "e3", date: "2026-01-22", narration: "Late fee — Suresh Patel", entryType: "LATE_FEE", debit: 0, credit: 250 },
  { id: "e4", date: "2026-02-15", narration: "Payout — Priya Sharma (Month 2 winner)", entryType: "PAYOUT", debit: 47500, credit: 0 },
  { id: "e5", date: "2026-02-20", narration: "Month 2 installment — 18/20 members paid", entryType: "INSTALLMENT", debit: 0, credit: 45000 },
  { id: "e6", date: "2026-02-25", narration: "Foreman commission — Month 2", entryType: "FOREMAN_COMMISSION", debit: 2250, credit: 0 },
  { id: "e7", date: "2026-03-20", narration: "Month 3 installment — 18/20 members paid", entryType: "INSTALLMENT", debit: 0, credit: 45000 },
  { id: "e8", date: "2026-04-15", narration: "Payout — Ravi Kumar (Month 4 winner)", entryType: "PAYOUT", debit: 47500, credit: 0 },
  { id: "e9", date: "2026-04-20", narration: "Month 4 installment — 18/20 members paid", entryType: "INSTALLMENT", debit: 0, credit: 45000 },
  { id: "e10", date: "2026-04-25", narration: "Foreman commission — Month 4", entryType: "FOREMAN_COMMISSION", debit: 2250, credit: 0 },
  { id: "e11", date: "2026-05-05", narration: "Refund — Ananya Singh (duplicate charge)", entryType: "REFUND", debit: 2500, credit: 0 },
  { id: "e12", date: "2026-05-20", narration: "Month 5 installment — 17/20 members paid", entryType: "INSTALLMENT", debit: 0, credit: 42500 },
];

const OPENING_BALANCE = 0;

// ── Helpers ────────────────────────────────────────────────────────────────

function formatINR(v: number) {
  if (v === 0) return "—";
  return `₹${v.toLocaleString("en-IN")}`;
}

const entryTypeColors: Record<EntryType, string> = {
  INSTALLMENT: "bg-green-50 text-green-700",
  LATE_FEE: "bg-amber-50 text-amber-700",
  PAYOUT: "bg-blue-50 text-blue-700",
  FOREMAN_COMMISSION: "bg-purple-50 text-purple-700",
  REFUND: "bg-red-50 text-red-700",
  ADJUSTMENT: "bg-[#F1F3FF] text-[#64748B]",
};

// ── Component ──────────────────────────────────────────────────────────────

export default function LedgerPage() {
  const [selectedGroup, setSelectedGroup] = useState("g1");
  const [fromDate, setFromDate] = useState("2026-01-01");
  const [toDate, setToDate] = useState("2026-10-31");

  const groupName = MOCK_GROUPS.find((g) => g.id === selectedGroup)?.name ?? "";

  const { rows, closingBalance } = useMemo(() => {
    const filtered = MOCK_ENTRIES.filter((e) => {
      return e.date >= fromDate && e.date <= toDate;
    });

    let running = OPENING_BALANCE;
    const rows = filtered.map((e) => {
      running = running + e.credit - e.debit;
      return { ...e, runningBalance: running };
    });

    return { rows, closingBalance: running };
  }, [fromDate, toDate]);

  function downloadExcel() {
    const header = "Date,Narration,Type,Debit,Credit,Running Balance";
    const lines = rows.map((r) =>
      `${r.date},"${r.narration}",${r.entryType},${r.debit},${r.credit},${r.runningBalance}`
    );
    const csv = [header, ...lines].join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `ledger-${groupName}-${fromDate}-${toDate}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  function downloadPDF() {
    console.log("Generating PDF for", groupName, fromDate, toDate);
    // stub — would use jsPDF or server-side PDF generation
  }

  return (
    <div className="p-6 space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Ledger</h2>
          <p className="text-sm text-[#64748B]">Double-entry ledger for chit group financials</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={downloadPDF}
            className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
          >
            <BookOpen size={14} />
            Download PDF
          </button>
          <button
            onClick={downloadExcel}
            className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
          >
            <Download size={14} />
            Download Excel
          </button>
        </div>
      </div>

      {/* Filter bar */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-4 flex items-center gap-4 flex-wrap">
        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-[#64748B] uppercase tracking-wide">Group</label>
          <select
            value={selectedGroup}
            onChange={(e) => setSelectedGroup(e.target.value)}
            className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6] bg-white"
          >
            {MOCK_GROUPS.map((g) => (
              <option key={g.id} value={g.id}>{g.name}</option>
            ))}
          </select>
        </div>
        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-[#64748B] uppercase tracking-wide">From</label>
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6]"
          />
        </div>
        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-[#64748B] uppercase tracking-wide">To</label>
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6]"
          />
        </div>
      </div>

      {/* Balance cards */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5">
          <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">Opening Balance</p>
          <p className="text-2xl font-bold text-[#041B3C]">{formatINR(OPENING_BALANCE)}</p>
          <p className="text-xs text-[#64748B] mt-1">As of {fromDate}</p>
        </div>
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5">
          <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">Closing Balance</p>
          <p className={`text-2xl font-bold ${closingBalance >= 0 ? "text-[#14B8A6]" : "text-red-500"}`}>
            ₹{Math.abs(closingBalance).toLocaleString("en-IN")}
            {closingBalance < 0 && <span className="text-sm ml-1">(Dr)</span>}
          </p>
          <p className="text-xs text-[#64748B] mt-1">As of {toDate}</p>
        </div>
      </div>

      {/* Ledger table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-[#E2E8F0] flex items-center justify-between">
          <h3 className="font-semibold text-[#041B3C]">{groupName} — Ledger Entries</h3>
          <span className="text-xs text-[#64748B]">{rows.length} entries</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-[#F7FAFC]">
              <tr>
                <th className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">Date</th>
                <th className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">Narration</th>
                <th className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">Type</th>
                <th className="px-5 py-3 text-right text-xs font-semibold text-[#64748B] uppercase tracking-wide">Debit (₹)</th>
                <th className="px-5 py-3 text-right text-xs font-semibold text-[#64748B] uppercase tracking-wide">Credit (₹)</th>
                <th className="px-5 py-3 text-right text-xs font-semibold text-[#64748B] uppercase tracking-wide">Balance (₹)</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {rows.map((row) => (
                <tr key={row.id} className="hover:bg-[#F7FAFC] transition-colors">
                  <td className="px-5 py-3.5 text-[#64748B] text-xs">
                    {new Date(row.date).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })}
                  </td>
                  <td className="px-5 py-3.5 text-[#172B4D]">{row.narration}</td>
                  <td className="px-5 py-3.5">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${entryTypeColors[row.entryType]}`}>
                      {row.entryType.replace(/_/g, " ")}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 text-right font-medium text-red-500">
                    {row.debit > 0 ? `₹${row.debit.toLocaleString("en-IN")}` : "—"}
                  </td>
                  <td className="px-5 py-3.5 text-right font-medium text-green-600">
                    {row.credit > 0 ? `₹${row.credit.toLocaleString("en-IN")}` : "—"}
                  </td>
                  <td className={`px-5 py-3.5 text-right font-semibold ${row.runningBalance >= 0 ? "text-[#041B3C]" : "text-red-500"}`}>
                    ₹{Math.abs(row.runningBalance).toLocaleString("en-IN")}
                    {row.runningBalance < 0 && <span className="text-xs ml-0.5">Dr</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
