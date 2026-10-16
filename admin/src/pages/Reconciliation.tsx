import { useState } from "react";
import { AlertTriangle, Check, X } from "lucide-react";

// ── Mock data ──────────────────────────────────────────────────────────────

interface Transaction {
  id: string;
  ref: string;
  type: "PAYMENT" | "REVERSAL" | "SETTLEMENT";
  amount: number;
  timestamp: string;
  source: "GATEWAY" | "BANK";
  possibleMatch: string | null;
}

const mockExpected = [
  { time: "09:14", ref: "INST-2026100-001", amount: 2500 },
  { time: "09:45", ref: "INST-2026100-002", amount: 5000 },
  { time: "10:12", ref: "INST-2026100-003", amount: 2500 },
  { time: "11:00", ref: "INST-2026100-004", amount: 10000 },
  { time: "14:30", ref: "INST-2026100-005", amount: 5000 },
];

const mockGateway = [
  { time: "09:16", ref: "RZP-pay_001xyz", amount: 2500 },
  { time: "09:47", ref: "RZP-pay_002xyz", amount: 5000 },
  { time: "10:14", ref: "RZP-pay_003xyz", amount: 2500 },
  { time: "11:02", ref: "RZP-pay_004xyz", amount: 10000 },
  { time: "15:20", ref: "RZP-pay_005xyz", amount: 4800 }, // mismatch
];

const mockBank = [
  { time: "09:20", ref: "HDFC-UTR-001", amount: 2500 },
  { time: "09:50", ref: "HDFC-UTR-002", amount: 5000 },
  { time: "10:18", ref: "HDFC-UTR-003", amount: 2500 },
  { time: "11:05", ref: "HDFC-UTR-004", amount: 10000 },
  { time: "15:25", ref: "HDFC-UTR-005", amount: 4800 },
  { time: "16:10", ref: "HDFC-UTR-006", amount: 1200 }, // extra bank credit
];

const mockUnmatched: Transaction[] = [
  {
    id: "u1",
    ref: "RZP-pay_005xyz",
    type: "PAYMENT",
    amount: 4800,
    timestamp: "15:20",
    source: "GATEWAY",
    possibleMatch: "INST-2026100-005 (₹5,000)",
  },
  {
    id: "u2",
    ref: "HDFC-UTR-006",
    type: "SETTLEMENT",
    amount: 1200,
    timestamp: "16:10",
    source: "BANK",
    possibleMatch: null,
  },
];

// ── Helpers ────────────────────────────────────────────────────────────────

function formatINR(v: number) {
  return `₹${v.toLocaleString("en-IN")}`;
}

type ReconciliationStatus = "BALANCED" | "DISCREPANCY" | "UNREVIEWED";

const statusStyles: Record<ReconciliationStatus, string> = {
  BALANCED: "bg-green-50 text-green-700 border-green-200",
  DISCREPANCY: "bg-red-50 text-red-700 border-red-200",
  UNREVIEWED: "bg-[#F1F3FF] text-[#64748B] border-[#E2E8F0]",
};

// ── Component ──────────────────────────────────────────────────────────────

export default function ReconciliationPage() {
  const [selectedDate, setSelectedDate] = useState("2026-10-15");
  const [unmatchedList, setUnmatchedList] = useState<Transaction[]>(mockUnmatched);
  const [showConfirm, setShowConfirm] = useState(false);
  const [approved, setApproved] = useState(false);

  const totalExpected = mockExpected.reduce((s, r) => s + r.amount, 0);
  const totalReceived = mockGateway.reduce((s, r) => s + r.amount, 0);
  const unmatchedCount = unmatchedList.length;
  const discrepancy = totalReceived - totalExpected;

  const status: ReconciliationStatus =
    unmatchedCount === 0 && discrepancy === 0
      ? "BALANCED"
      : unmatchedCount > 0 || discrepancy !== 0
      ? "DISCREPANCY"
      : "UNREVIEWED";

  function dismissUnmatched(id: string) {
    setUnmatchedList((prev) => prev.filter((t) => t.id !== id));
  }

  function approveReconciliation() {
    setShowConfirm(false);
    setApproved(true);
  }

  return (
    <div className="p-6 space-y-5 relative">
      {/* Approval toast */}
      {approved && (
        <div className="fixed top-5 right-6 z-50 flex items-center gap-2 bg-green-600 text-white text-sm px-4 py-2.5 rounded-lg shadow-lg">
          <Check size={14} />
          Reconciliation approved for {selectedDate}
        </div>
      )}

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Payment Reconciliation</h2>
          <p className="text-sm text-[#64748B]">Match gateway settlements with bank credits</p>
        </div>
        <div className="flex items-center gap-3">
          {/* Status badge */}
          <span className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold border ${statusStyles[status]}`}>
            {status === "DISCREPANCY" && <AlertTriangle size={12} />}
            {status === "BALANCED" && <Check size={12} />}
            {status}
          </span>

          {/* Day selector */}
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6]"
          />

          {/* Approve CTA */}
          <button
            disabled={status === "DISCREPANCY"}
            onClick={() => setShowConfirm(true)}
            className="px-4 py-2 rounded-lg bg-[#14B8A6] text-white text-sm font-medium hover:bg-[#00B3A1] disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          >
            Approve Reconciliation
          </button>
        </div>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-4 gap-4">
        {[
          { label: "Total Expected", value: formatINR(totalExpected), color: "#1769AA", bgColor: "#EFF6FF" },
          { label: "Total Received", value: formatINR(totalReceived), color: "#14B8A6", bgColor: "#F0FDFA" },
          { label: "Unmatched Count", value: unmatchedCount.toString(), color: "#F59E0B", bgColor: "#FFFBEB" },
          {
            label: "Discrepancy",
            value: formatINR(Math.abs(discrepancy)),
            color: discrepancy !== 0 ? "#EF4444" : "#22C55E",
            bgColor: discrepancy !== 0 ? "#FEF2F2" : "#F0FDF4",
          },
        ].map(({ label, value, color, bgColor }) => (
          <div key={label} className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-4">
            <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">{label}</p>
            <p className="text-2xl font-bold" style={{ color }}>
              {value}
            </p>
          </div>
        ))}
      </div>

      {/* 3-column comparison */}
      <div className="grid grid-cols-3 gap-4">
        {/* Expected */}
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
          <div className="px-4 py-3 border-b border-[#E2E8F0] bg-[#EFF6FF]">
            <h3 className="text-sm font-semibold text-[#1769AA]">Expected Collections</h3>
            <p className="text-xs text-[#64748B]">{formatINR(totalExpected)} total</p>
          </div>
          <table className="w-full text-xs">
            <thead className="bg-[#F7FAFC]">
              <tr>
                <th className="px-3 py-2 text-left text-[#64748B] font-semibold">Time</th>
                <th className="px-3 py-2 text-left text-[#64748B] font-semibold">Reference</th>
                <th className="px-3 py-2 text-right text-[#64748B] font-semibold">Amount</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {mockExpected.map((r) => (
                <tr key={r.ref}>
                  <td className="px-3 py-2 text-[#64748B]">{r.time}</td>
                  <td className="px-3 py-2 text-[#172B4D] font-mono">{r.ref}</td>
                  <td className="px-3 py-2 text-right font-medium text-[#041B3C]">{formatINR(r.amount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Gateway */}
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
          <div className="px-4 py-3 border-b border-[#E2E8F0] bg-[#F0FDFA]">
            <h3 className="text-sm font-semibold text-[#14B8A6]">Gateway Settlements</h3>
            <p className="text-xs text-[#64748B]">Razorpay · {formatINR(totalReceived)} settled</p>
          </div>
          <table className="w-full text-xs">
            <thead className="bg-[#F7FAFC]">
              <tr>
                <th className="px-3 py-2 text-left text-[#64748B] font-semibold">Time</th>
                <th className="px-3 py-2 text-left text-[#64748B] font-semibold">Reference</th>
                <th className="px-3 py-2 text-right text-[#64748B] font-semibold">Amount</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {mockGateway.map((r, i) => {
                const exp = mockExpected[i];
                const mismatch = !exp || exp.amount !== r.amount;
                return (
                  <tr key={r.ref} className={mismatch ? "bg-amber-50" : ""}>
                    <td className="px-3 py-2 text-[#64748B]">{r.time}</td>
                    <td className="px-3 py-2 text-[#172B4D] font-mono">{r.ref}</td>
                    <td className={`px-3 py-2 text-right font-medium ${mismatch ? "text-amber-700" : "text-[#041B3C]"}`}>
                      {formatINR(r.amount)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {/* Bank */}
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
          <div className="px-4 py-3 border-b border-[#E2E8F0] bg-[#F7FAFC]">
            <h3 className="text-sm font-semibold text-[#041B3C]">Bank Credits</h3>
            <p className="text-xs text-[#64748B]">HDFC Bank Statement</p>
          </div>
          <table className="w-full text-xs">
            <thead className="bg-[#F7FAFC]">
              <tr>
                <th className="px-3 py-2 text-left text-[#64748B] font-semibold">Time</th>
                <th className="px-3 py-2 text-left text-[#64748B] font-semibold">Reference</th>
                <th className="px-3 py-2 text-right text-[#64748B] font-semibold">Amount</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {mockBank.map((r, i) => {
                const extra = i >= mockExpected.length;
                return (
                  <tr key={r.ref} className={extra ? "bg-amber-50" : ""}>
                    <td className="px-3 py-2 text-[#64748B]">{r.time}</td>
                    <td className="px-3 py-2 text-[#172B4D] font-mono">{r.ref}</td>
                    <td className={`px-3 py-2 text-right font-medium ${extra ? "text-amber-700" : "text-[#041B3C]"}`}>
                      {formatINR(r.amount)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Unmatched transactions */}
      {unmatchedList.length > 0 && (
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-[#E2E8F0] flex items-center justify-between bg-red-50">
            <h3 className="font-semibold text-red-700 flex items-center gap-2">
              <AlertTriangle size={15} />
              Unmatched Transactions ({unmatchedList.length})
            </h3>
          </div>
          <table className="w-full text-sm">
            <thead className="bg-[#F7FAFC]">
              <tr>
                {["Reference", "Type", "Amount", "Time", "Source", "Possible Match", "Actions"].map((h) => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {unmatchedList.map((t) => (
                <tr key={t.id} className="hover:bg-[#F7FAFC]">
                  <td className="px-4 py-3.5 font-mono text-xs text-[#172B4D]">{t.ref}</td>
                  <td className="px-4 py-3.5 text-[#64748B]">{t.type}</td>
                  <td className="px-4 py-3.5 font-semibold text-[#041B3C]">{formatINR(t.amount)}</td>
                  <td className="px-4 py-3.5 text-[#64748B]">{t.timestamp}</td>
                  <td className="px-4 py-3.5">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                      t.source === "GATEWAY" ? "bg-[#EFF6FF] text-[#1769AA]" : "bg-[#F0FDF4] text-green-700"
                    }`}>
                      {t.source}
                    </span>
                  </td>
                  <td className="px-4 py-3.5 text-[#64748B] text-xs">{t.possibleMatch ?? "None found"}</td>
                  <td className="px-4 py-3.5">
                    <div className="flex items-center gap-1.5">
                      <button className="px-2 py-1 rounded text-xs font-medium bg-blue-50 text-blue-700 hover:bg-blue-100">Match</button>
                      <button className="px-2 py-1 rounded text-xs font-medium bg-amber-50 text-amber-700 hover:bg-amber-100">Investigate</button>
                      <button className="px-2 py-1 rounded text-xs font-medium bg-red-50 text-red-700 hover:bg-red-100">Escalate</button>
                      <button onClick={() => dismissUnmatched(t.id)} className="p-1 rounded text-[#64748B] hover:text-red-500">
                        <X size={12} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Approve confirm dialog */}
      {showConfirm && (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-xl p-6 w-[420px]">
            <h3 className="font-semibold text-[#041B3C] text-lg mb-2">Approve Reconciliation?</h3>
            <p className="text-sm text-[#64748B] mb-5">
              You are approving the reconciliation for {selectedDate}. This action will be logged in the audit trail.
            </p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setShowConfirm(false)}
                className="px-4 py-2 rounded-lg border border-[#E2E8F0] text-sm font-medium text-[#64748B] hover:bg-[#F1F3FF]"
              >
                Cancel
              </button>
              <button
                onClick={approveReconciliation}
                className="px-4 py-2 rounded-lg bg-[#14B8A6] text-white text-sm font-medium hover:bg-[#00B3A1]"
              >
                Approve
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
