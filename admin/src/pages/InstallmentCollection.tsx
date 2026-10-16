import { useState } from "react";
import { Download, Send, AlertCircle } from "lucide-react";

// ── Mock data ──────────────────────────────────────────────────────────────

const MOCK_GROUPS = [
  { id: "g1", name: "Family Savings 50K", members: 20, amountPerMember: 2500 },
  { id: "g2", name: "Community Chit 1L", members: 17, amountPerMember: 5000 },
  { id: "g3", name: "Monthly Circle 25K", members: 20, amountPerMember: 1250 },
  { id: "g4", name: "Sunrise Savings 2L", members: 15, amountPerMember: 10000 },
];

type CollectionStatus = "PAID" | "PENDING" | "OVERDUE" | "OFFLINE_MARKED";
type PaymentMethod = "UPI" | "Net Banking" | "Debit Card" | "NEFT" | "Offline" | "—";

interface CollectionMember {
  id: string;
  name: string;
  due: number;
  paid: number;
  method: PaymentMethod;
  receipt: string | null;
  status: CollectionStatus;
}

function generateMembers(groupId: string): CollectionMember[] {
  const group = MOCK_GROUPS.find((g) => g.id === groupId) ?? MOCK_GROUPS[0];
  const names = [
    "Priya Sharma", "Ravi Kumar", "Ananya Singh", "Suresh Patel", "Meena Iyer",
    "Arjun Nair", "Kavitha Reddy", "Vikram Mehta", "Sunita Rao", "Deepak Joshi",
    "Lakshmi Venkat", "Ramesh Pillai", "Sita Krishnan", "Arun Gupta", "Nisha Tiwari",
    "Harish Yadav", "Pooja Bhat", "Kiran More", "Divya Pandey", "Sunil Patil",
  ];
  const statuses: CollectionStatus[] = [
    "PAID", "PAID", "PAID", "PAID", "PAID", "PAID", "PAID",
    "PAID", "PAID", "PAID", "PAID", "PAID", "PAID", "PAID",
    "PENDING", "PENDING", "OVERDUE", "OVERDUE", "OFFLINE_MARKED", "PENDING",
  ];
  const methods: PaymentMethod[] = [
    "UPI", "Net Banking", "UPI", "Debit Card", "UPI", "NEFT", "UPI",
    "Net Banking", "UPI", "UPI", "UPI", "Debit Card", "UPI", "UPI",
    "—", "—", "—", "—", "Offline", "—",
  ];
  return names.slice(0, group.members).map((name, i) => {
    const status = statuses[i] ?? "PENDING";
    const paid =
      status === "PAID" || status === "OFFLINE_MARKED" ? group.amountPerMember
      : status === "PENDING" ? 0
      : 0;
    return {
      id: `${groupId}-m${i + 1}`,
      name,
      due: group.amountPerMember,
      paid,
      method: methods[i] ?? "—",
      receipt: (status === "PAID" || status === "OFFLINE_MARKED") ? `CS-202610-${(i + 1).toString().padStart(6, "0")}` : null,
      status,
    };
  });
}

// ── Helpers ────────────────────────────────────────────────────────────────

function formatINR(v: number) {
  return `₹${v.toLocaleString("en-IN")}`;
}

const statusStyles: Record<CollectionStatus, string> = {
  PAID: "bg-green-50 text-green-700 border-green-200",
  PENDING: "bg-amber-50 text-amber-700 border-amber-200",
  OVERDUE: "bg-red-50 text-red-700 border-red-200",
  OFFLINE_MARKED: "bg-blue-50 text-blue-700 border-blue-200",
};

function StatusChip({ status }: { status: CollectionStatus }) {
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${statusStyles[status]}`}>
      {status.replace("_", " ")}
    </span>
  );
}

// ── Component ──────────────────────────────────────────────────────────────

export default function InstallmentCollectionPage() {
  const [selectedMonth, setSelectedMonth] = useState("2026-10");
  const [selectedGroup, setSelectedGroup] = useState("g1");
  const [members, setMembers] = useState<CollectionMember[]>(() => generateMembers("g1"));
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [showMarkModal, setShowMarkModal] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  function changeGroup(gid: string) {
    setSelectedGroup(gid);
    setMembers(generateMembers(gid));
    setSelectedIds(new Set());
  }

  function toggleRow(id: string) {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function toggleAll() {
    if (selectedIds.size === members.length) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(members.map((m) => m.id)));
    }
  }

  function showToast(msg: string) {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  }

  function markAsPaid() {
    setMembers((prev) =>
      prev.map((m) =>
        selectedIds.has(m.id) ? { ...m, status: "OFFLINE_MARKED", paid: m.due, method: "Offline", receipt: `CS-OFFLINE-${m.id}` } : m
      )
    );
    setSelectedIds(new Set());
    setShowMarkModal(false);
    showToast(`${selectedIds.size} member(s) marked as paid (offline)`);
  }

  function sendReminder() {
    console.log("Sending reminder to", Array.from(selectedIds));
    showToast(`Reminder sent to ${selectedIds.size} member(s)`);
  }

  function exportSelected() {
    const rows = members.filter((m) => selectedIds.has(m.id));
    const csv = ["Name,Due,Paid,Outstanding,Method,Status",
      ...rows.map((r) => `${r.name},${r.due},${r.paid},${r.due - r.paid},${r.method},${r.status}`)
    ].join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "collection-export.csv";
    a.click();
    URL.revokeObjectURL(url);
  }

  const group = MOCK_GROUPS.find((g) => g.id === selectedGroup) ?? MOCK_GROUPS[0];
  const paidCount = members.filter((m) => m.status === "PAID" || m.status === "OFFLINE_MARKED").length;
  const totalCollected = members.reduce((s, m) => s + m.paid, 0);
  const totalExpected = group.members * group.amountPerMember;
  const progressPct = totalExpected > 0 ? Math.round((totalCollected / totalExpected) * 100) : 0;

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
      <div>
        <h2 className="text-xl font-semibold text-[#041B3C]">Installment Collection</h2>
        <p className="text-sm text-[#64748B]">Month-level collection tracking per group</p>
      </div>

      {/* Filter bar */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-4 flex items-center gap-4">
        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-[#64748B] uppercase tracking-wide">Month</label>
          <input
            type="month"
            value={selectedMonth}
            onChange={(e) => setSelectedMonth(e.target.value)}
            className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6]"
          />
        </div>
        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-[#64748B] uppercase tracking-wide">Group</label>
          <select
            value={selectedGroup}
            onChange={(e) => changeGroup(e.target.value)}
            className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6] bg-white"
          >
            {MOCK_GROUPS.map((g) => (
              <option key={g.id} value={g.id}>{g.name}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Progress banner */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5">
        <div className="flex items-center justify-between mb-3">
          <div>
            <p className="text-sm font-semibold text-[#041B3C]">
              {new Date(selectedMonth).toLocaleDateString("en-IN", { month: "long", year: "numeric" })} — {group.name}
            </p>
            <p className="text-xs text-[#64748B] mt-0.5">
              {paidCount}/{group.members} members paid · {formatINR(totalCollected)} of {formatINR(totalExpected)} collected
            </p>
          </div>
          <span className="text-2xl font-bold text-[#14B8A6]">{progressPct}%</span>
        </div>
        <div className="w-full h-3 bg-[#E2E8F0] rounded-full overflow-hidden">
          <div
            className="h-full rounded-full bg-[#14B8A6] transition-all duration-500"
            style={{ width: `${progressPct}%` }}
          />
        </div>
      </div>

      {/* Bulk action bar */}
      {selectedIds.size > 0 && (
        <div className="bg-[#073763] rounded-xl px-5 py-3 flex items-center justify-between">
          <span className="text-white text-sm font-medium">{selectedIds.size} member(s) selected</span>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowMarkModal(true)}
              className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-white text-[#073763] text-xs font-semibold hover:bg-[#F1F3FF] transition-colors"
            >
              <Download size={13} />
              Mark as Paid (Offline)
            </button>
            <button
              onClick={sendReminder}
              className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-amber-500 text-white text-xs font-semibold hover:bg-amber-600 transition-colors"
            >
              <Send size={13} />
              Send Reminder
            </button>
            <button
              onClick={exportSelected}
              className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-[#1769AA] text-white text-xs font-semibold hover:bg-[#073763] transition-colors"
            >
              <Download size={13} />
              Export
            </button>
            <button
              onClick={() => setSelectedIds(new Set())}
              className="p-1.5 rounded text-blue-200 hover:text-white"
            >
              <AlertCircle size={14} />
            </button>
          </div>
        </div>
      )}

      {/* Collection table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-[#E2E8F0]">
          <h3 className="font-semibold text-[#041B3C]">Member Collection Details</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-[#F7FAFC]">
              <tr>
                <th className="px-4 py-3 text-left">
                  <input
                    type="checkbox"
                    checked={selectedIds.size === members.length}
                    onChange={toggleAll}
                    className="accent-[#14B8A6] w-4 h-4"
                  />
                </th>
                {["Member Name", "Due Amount", "Paid Amount", "Outstanding", "Method", "Receipt", "Status"].map((h) => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {members.map((m) => {
                const outstanding = m.due - m.paid;
                return (
                  <tr key={m.id} className={`hover:bg-[#F7FAFC] transition-colors ${selectedIds.has(m.id) ? "bg-[#F1F3FF]" : ""}`}>
                    <td className="px-4 py-3.5">
                      <input
                        type="checkbox"
                        checked={selectedIds.has(m.id)}
                        onChange={() => toggleRow(m.id)}
                        className="accent-[#14B8A6] w-4 h-4"
                      />
                    </td>
                    <td className="px-4 py-3.5 font-medium text-[#041B3C]">{m.name}</td>
                    <td className="px-4 py-3.5 text-[#041B3C]">{formatINR(m.due)}</td>
                    <td className="px-4 py-3.5 text-green-600 font-medium">{formatINR(m.paid)}</td>
                    <td className={`px-4 py-3.5 font-medium ${outstanding > 0 ? "text-red-500" : "text-[#64748B]"}`}>
                      {formatINR(outstanding)}
                    </td>
                    <td className="px-4 py-3.5 text-[#64748B]">{m.method}</td>
                    <td className="px-4 py-3.5">
                      {m.receipt ? (
                        <button className="text-xs text-[#1769AA] hover:underline font-medium">{m.receipt}</button>
                      ) : (
                        <span className="text-xs text-[#64748B]">—</span>
                      )}
                    </td>
                    <td className="px-4 py-3.5">
                      <StatusChip status={m.status} />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Mark as Paid Modal */}
      {showMarkModal && (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-xl p-6 w-[400px]">
            <h3 className="font-semibold text-[#041B3C] text-lg mb-2">Mark as Paid (Offline)?</h3>
            <p className="text-sm text-[#64748B] mb-5">
              This will mark {selectedIds.size} member(s) as paid via offline method. A receipt will be generated for each.
            </p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setShowMarkModal(false)}
                className="px-4 py-2 rounded-lg border border-[#E2E8F0] text-sm font-medium text-[#64748B] hover:bg-[#F1F3FF]"
              >
                Cancel
              </button>
              <button
                onClick={markAsPaid}
                className="px-4 py-2 rounded-lg bg-[#14B8A6] text-white text-sm font-medium hover:bg-[#00B3A1]"
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
