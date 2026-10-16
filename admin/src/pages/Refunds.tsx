import { useState } from "react";
import { Plus, Mail, AlertCircle, Download } from "lucide-react";

// ── Types ──────────────────────────────────────────────────────────────────

type DisputeStatus = "Open" | "Under Review" | "Resolved" | "Rejected";

interface Dispute {
  id: string;
  memberName: string;
  mobile: string;
  amount: number;
  reason: string;
  dateFiled: string;
  assignedAdmin: string;
  status: DisputeStatus;
  messages: { author: string; text: string; timestamp: string }[];
}

// ── Mock data ──────────────────────────────────────────────────────────────

const MOCK_DISPUTES: Dispute[] = [
  {
    id: "d1",
    memberName: "Priya Sharma",
    mobile: "9876543210",
    amount: 2500,
    reason: "Duplicate payment deducted for October installment",
    dateFiled: "2026-10-12",
    assignedAdmin: "Rahul (Finance)",
    status: "Open",
    messages: [
      { author: "Priya Sharma", text: "I was charged twice for October. Please refund.", timestamp: "12 Oct, 10:30" },
      { author: "Rahul (Finance)", text: "We have received your complaint and are reviewing.", timestamp: "12 Oct, 11:00" },
    ],
  },
  {
    id: "d2",
    memberName: "Suresh Patel",
    mobile: "9123456789",
    amount: 5000,
    reason: "Payment failed but amount debited from account",
    dateFiled: "2026-10-10",
    assignedAdmin: "Anita (Support)",
    status: "Under Review",
    messages: [
      { author: "Suresh Patel", text: "Money deducted but status shows failed.", timestamp: "10 Oct, 09:15" },
      { author: "Anita (Support)", text: "We are coordinating with the payment gateway.", timestamp: "10 Oct, 14:00" },
      { author: "Suresh Patel", text: "Any update?", timestamp: "11 Oct, 09:00" },
    ],
  },
  {
    id: "d3",
    memberName: "Meena Iyer",
    mobile: "9988776655",
    amount: 1500,
    reason: "Wrong group installment charged",
    dateFiled: "2026-09-28",
    assignedAdmin: "Rahul (Finance)",
    status: "Resolved",
    messages: [
      { author: "Meena Iyer", text: "Charged for wrong group.", timestamp: "28 Sep, 16:00" },
      { author: "Rahul (Finance)", text: "Refund of ₹1,500 initiated.", timestamp: "30 Sep, 10:00" },
    ],
  },
  {
    id: "d4",
    memberName: "Arjun Nair",
    mobile: "9654321098",
    amount: 2500,
    reason: "Left group before installment due but still charged",
    dateFiled: "2026-10-05",
    assignedAdmin: "Anita (Support)",
    status: "Rejected",
    messages: [
      { author: "Arjun Nair", text: "Please refund my installment.", timestamp: "05 Oct, 11:00" },
      { author: "Anita (Support)", text: "Refund denied as per group exit policy. Installment was due before exit.", timestamp: "07 Oct, 15:30" },
    ],
  },
];

// ── Helpers ────────────────────────────────────────────────────────────────

function formatINR(v: number) {
  return `₹${v.toLocaleString("en-IN")}`;
}

const statusStyles: Record<DisputeStatus, string> = {
  "Open": "bg-amber-50 text-amber-700 border-amber-200",
  "Under Review": "bg-blue-50 text-blue-700 border-blue-200",
  "Resolved": "bg-green-50 text-green-700 border-green-200",
  "Rejected": "bg-red-50 text-red-700 border-red-200",
};

function StatusChip({ status }: { status: DisputeStatus }) {
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${statusStyles[status]}`}>
      {status}
    </span>
  );
}

// ── Component ──────────────────────────────────────────────────────────────

export default function RefundsPage() {
  const [disputes, setDisputes] = useState<Dispute[]>(MOCK_DISPUTES);
  const [selected, setSelected] = useState<Dispute | null>(null);
  const [actionReason, setActionReason] = useState("");
  const [refundAmount, setRefundAmount] = useState("");
  const [toast, setToast] = useState<string | null>(null);

  function showToast(msg: string) {
    setToast(msg);
    setTimeout(() => setToast(null), 3500);
  }

  function approveRefund() {
    if (!actionReason.trim()) return;
    if (!selected) return;
    setDisputes((prev) =>
      prev.map((d) => d.id === selected.id ? { ...d, status: "Resolved" as DisputeStatus } : d)
    );
    showToast(`Refund of ${refundAmount || formatINR(selected.amount)} approved for ${selected.memberName}`);
    setSelected(null);
    setActionReason("");
    setRefundAmount("");
  }

  function rejectDispute() {
    if (!actionReason.trim()) return;
    if (!selected) return;
    setDisputes((prev) =>
      prev.map((d) => d.id === selected.id ? { ...d, status: "Rejected" as DisputeStatus } : d)
    );
    showToast(`Dispute by ${selected.memberName} has been rejected`);
    setSelected(null);
    setActionReason("");
  }

  return (
    <div className="p-6 space-y-5 relative">
      {/* Toast */}
      {toast && (
        <div className="fixed top-5 right-6 z-50 flex items-center gap-2 bg-[#073763] text-white text-sm px-4 py-2.5 rounded-lg shadow-lg">
          <span>✓</span>
          {toast}
        </div>
      )}

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Refunds &amp; Disputes</h2>
          <p className="text-sm text-[#64748B]">Manage member refund requests and dispute resolutions</p>
        </div>
        <button
          onClick={() => showToast("Log New Dispute form — coming soon")}
          className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors"
        >
          <Plus size={14} />
          Log New Dispute
        </button>
      </div>

      {/* Disputes table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-[#E2E8F0]">
          <h3 className="font-semibold text-[#041B3C]">Open Disputes ({disputes.filter((d) => d.status === "Open" || d.status === "Under Review").length})</h3>
        </div>
        <table className="w-full text-sm">
          <thead className="bg-[#F7FAFC]">
            <tr>
              {["Member Name", "Amount", "Reason", "Date Filed", "Assigned To", "Status"].map((h) => (
                <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[#E2E8F0]">
            {disputes.map((d) => (
              <tr
                key={d.id}
                onClick={() => setSelected(d)}
                className="hover:bg-[#F7FAFC] cursor-pointer transition-colors"
              >
                <td className="px-5 py-3.5 font-medium text-[#041B3C]">{d.memberName}</td>
                <td className="px-5 py-3.5 font-semibold text-[#041B3C]">{formatINR(d.amount)}</td>
                <td className="px-5 py-3.5 text-[#64748B] max-w-[200px] truncate">{d.reason}</td>
                <td className="px-5 py-3.5 text-[#64748B]">
                  {new Date(d.dateFiled).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })}
                </td>
                <td className="px-5 py-3.5 text-[#64748B]">{d.assignedAdmin}</td>
                <td className="px-5 py-3.5">
                  <StatusChip status={d.status} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Slide-in panel */}
      {selected && (
        <>
          {/* Overlay */}
          <div
            className="fixed inset-0 z-30 bg-black/30"
            onClick={() => setSelected(null)}
          />
          {/* Panel */}
          <div className="fixed top-0 right-0 h-full w-[480px] z-40 bg-white border-l border-[#E2E8F0] shadow-2xl flex flex-col overflow-hidden">
            {/* Panel header */}
            <div className="flex items-center justify-between px-5 py-4 border-b border-[#E2E8F0]">
              <div>
                <h3 className="font-semibold text-[#041B3C]">{selected.memberName}</h3>
                <p className="text-xs text-[#64748B]">{selected.mobile}</p>
              </div>
              <div className="flex items-center gap-3">
                <StatusChip status={selected.status} />
                <button onClick={() => setSelected(null)} className="p-1.5 rounded hover:bg-[#F1F3FF] text-[#64748B]">
                  <AlertCircle size={16} />
                </button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto">
              {/* Dispute details */}
              <div className="px-5 py-4 border-b border-[#E2E8F0]">
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <p className="text-xs text-[#64748B]">Amount</p>
                    <p className="text-lg font-bold text-[#041B3C]">{formatINR(selected.amount)}</p>
                  </div>
                  <div>
                    <p className="text-xs text-[#64748B]">Date Filed</p>
                    <p className="text-sm font-medium text-[#041B3C]">
                      {new Date(selected.dateFiled).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })}
                    </p>
                  </div>
                  <div className="col-span-2">
                    <p className="text-xs text-[#64748B]">Reason</p>
                    <p className="text-sm text-[#172B4D] mt-0.5">{selected.reason}</p>
                  </div>
                  <div className="col-span-2">
                    <p className="text-xs text-[#64748B]">Assigned To</p>
                    <p className="text-sm font-medium text-[#041B3C]">{selected.assignedAdmin}</p>
                  </div>
                </div>
              </div>

              {/* Communication thread */}
              <div className="px-5 py-4 border-b border-[#E2E8F0]">
                <h4 className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-3 flex items-center gap-1.5">
                  <Mail size={12} />
                  Communication Thread
                </h4>
                <div className="space-y-3">
                  {selected.messages.map((msg, i) => {
                    const isAdmin = msg.author !== selected.memberName;
                    return (
                      <div key={i} className={`flex gap-2 ${isAdmin ? "flex-row-reverse" : ""}`}>
                        <div
                          className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-semibold shrink-0 ${
                            isAdmin ? "bg-[#073763] text-white" : "bg-[#14B8A6]/20 text-[#14B8A6]"
                          }`}
                        >
                          {msg.author[0]}
                        </div>
                        <div className={`flex-1 ${isAdmin ? "items-end" : "items-start"} flex flex-col`}>
                          <div
                            className={`text-xs px-3 py-2 rounded-lg max-w-[90%] ${
                              isAdmin ? "bg-[#073763] text-white" : "bg-[#F7FAFC] text-[#172B4D] border border-[#E2E8F0]"
                            }`}
                          >
                            {msg.text}
                          </div>
                          <p className="text-[10px] text-[#64748B] mt-0.5 px-1">
                            {msg.author} · {msg.timestamp}
                          </p>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* File attachments */}
              <div className="px-5 py-4 border-b border-[#E2E8F0]">
                <h4 className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2 flex items-center gap-1.5">
                  <Download size={12} />
                  Attachments
                </h4>
                <p className="text-xs text-[#64748B]">No attachments for this dispute.</p>
              </div>

              {/* Refund approval form */}
              {(selected.status === "Open" || selected.status === "Under Review") && (
                <div className="px-5 py-4">
                  <h4 className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-3">Refund Action</h4>
                  <div className="space-y-3">
                    <div>
                      <label className="block text-xs font-semibold text-[#64748B] mb-1.5">Refund Amount (₹)</label>
                      <input
                        type="number"
                        value={refundAmount}
                        onChange={(e) => setRefundAmount(e.target.value)}
                        placeholder={selected.amount.toString()}
                        className="w-full px-3 py-2 rounded-lg border border-[#E2E8F0] text-sm focus:outline-none focus:ring-2 focus:ring-[#14B8A6]"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-[#64748B] mb-1.5">
                        Reason / Notes <span className="text-red-500">*</span>
                      </label>
                      <textarea
                        value={actionReason}
                        onChange={(e) => setActionReason(e.target.value)}
                        rows={3}
                        placeholder="Enter a mandatory reason for approval or rejection..."
                        className="w-full px-3 py-2 rounded-lg border border-[#E2E8F0] text-sm focus:outline-none focus:ring-2 focus:ring-[#14B8A6] resize-none"
                      />
                    </div>
                    {!actionReason.trim() && (
                      <p className="text-xs text-red-500">Reason is required to proceed.</p>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* Panel footer CTAs */}
            {(selected.status === "Open" || selected.status === "Under Review") && (
              <div className="px-5 py-4 border-t border-[#E2E8F0] flex gap-3">
                <button
                  onClick={approveRefund}
                  disabled={!actionReason.trim()}
                  className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-[#14B8A6] text-white text-sm font-medium hover:bg-[#00B3A1] disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                >
                  <Download size={14} />
                  Approve Refund
                </button>
                <button
                  onClick={rejectDispute}
                  disabled={!actionReason.trim()}
                  className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-red-500 text-white text-sm font-medium hover:bg-red-600 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                >
                  <AlertCircle size={14} />
                  Reject Dispute
                </button>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
