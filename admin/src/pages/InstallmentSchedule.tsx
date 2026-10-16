import { useState } from "react";
import { Bell, Edit } from "lucide-react";

// ── Mock data ──────────────────────────────────────────────────────────────

const MOCK_GROUPS = [
  { id: "g1", name: "Family Savings 50K" },
  { id: "g2", name: "Community Chit 1L" },
  { id: "g3", name: "Monthly Circle 25K" },
  { id: "g4", name: "Sunrise Savings 2L" },
];

type ScheduleStatus = "COLLECTED" | "PARTIAL" | "OVERDUE" | "UPCOMING";

interface ScheduleRow {
  id: string;
  month: number;
  dueDate: string;
  amountPerMember: number;
  maxMembers: number;
  totalExpected: number;
  totalCollected: number;
  status: ScheduleStatus;
}

function generateSchedule(groupId: string): ScheduleRow[] {
  const configs: Record<string, { value: number; members: number; startDate: string }> = {
    g1: { value: 2500, members: 20, startDate: "2026-01-15" },
    g2: { value: 5000, members: 17, startDate: "2026-03-20" },
    g3: { value: 1250, members: 20, startDate: "2025-06-10" },
    g4: { value: 10000, members: 15, startDate: "2026-06-25" },
  };
  const c = configs[groupId] ?? configs.g1;
  const statuses: ScheduleStatus[] = ["COLLECTED", "COLLECTED", "COLLECTED", "COLLECTED", "COLLECTED", "COLLECTED", "PARTIAL", "OVERDUE", "UPCOMING", "UPCOMING"];

  return Array.from({ length: 10 }, (_, i) => {
    const d = new Date(c.startDate);
    d.setMonth(d.getMonth() + i);
    const expected = c.value * c.members;
    const status = statuses[i] ?? "UPCOMING";
    const collected =
      status === "COLLECTED" ? expected
      : status === "PARTIAL" ? Math.round(expected * 0.65)
      : status === "OVERDUE" ? Math.round(expected * 0.3)
      : 0;
    return {
      id: `${groupId}-${i + 1}`,
      month: i + 1,
      dueDate: d.toISOString().split("T")[0],
      amountPerMember: c.value,
      maxMembers: c.members,
      totalExpected: expected,
      totalCollected: collected,
      status,
    };
  });
}

// ── Helpers ────────────────────────────────────────────────────────────────

function formatINR(v: number) {
  return `₹${v.toLocaleString("en-IN")}`;
}

const statusStyles: Record<ScheduleStatus, string> = {
  COLLECTED: "bg-green-50 text-green-700 border-green-200",
  PARTIAL: "bg-amber-50 text-amber-700 border-amber-200",
  OVERDUE: "bg-red-50 text-red-700 border-red-200",
  UPCOMING: "bg-[#F1F3FF] text-[#64748B] border-[#E2E8F0]",
};

function StatusChip({ status }: { status: ScheduleStatus }) {
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${statusStyles[status]}`}>
      {status}
    </span>
  );
}

// ── Component ──────────────────────────────────────────────────────────────

export default function InstallmentSchedulePage() {
  const [selectedGroup, setSelectedGroup] = useState("g1");
  const [schedule, setSchedule] = useState<ScheduleRow[]>(() => generateSchedule("g1"));
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editDate, setEditDate] = useState("");
  const [showReminderConfirm, setShowReminderConfirm] = useState(false);
  const [reminderSent, setReminderSent] = useState(false);

  function changeGroup(groupId: string) {
    setSelectedGroup(groupId);
    setSchedule(generateSchedule(groupId));
    setEditingId(null);
  }

  function startEdit(row: ScheduleRow) {
    setEditingId(row.id);
    setEditDate(row.dueDate);
  }

  function saveEdit(id: string) {
    setSchedule((prev) =>
      prev.map((r) => (r.id === id ? { ...r, dueDate: editDate } : r))
    );
    setEditingId(null);
  }

  function cancelEdit() {
    setEditingId(null);
    setEditDate("");
  }

  function sendReminder() {
    setShowReminderConfirm(false);
    setReminderSent(true);
    setTimeout(() => setReminderSent(false), 4000);
  }

  const overdueCount = schedule.filter((r) => r.status === "OVERDUE").length;
  const groupName = MOCK_GROUPS.find((g) => g.id === selectedGroup)?.name ?? "";

  return (
    <div className="p-6 space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Installment Schedule</h2>
          <p className="text-sm text-[#64748B]">View and manage installment due dates by group</p>
        </div>
        <div className="flex items-center gap-3">
          {/* Group selector */}
          <select
            value={selectedGroup}
            onChange={(e) => changeGroup(e.target.value)}
            className="px-3 py-2 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] focus:outline-none focus:ring-2 focus:ring-[#14B8A6] bg-white"
          >
            {MOCK_GROUPS.map((g) => (
              <option key={g.id} value={g.id}>{g.name}</option>
            ))}
          </select>

          {/* Send reminder */}
          <button
            onClick={() => setShowReminderConfirm(true)}
            className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors"
          >
            <Bell size={14} />
            Send Reminder
          </button>
        </div>
      </div>

      {/* Reminder confirm strip */}
      {showReminderConfirm && (
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 flex items-center justify-between">
          <div>
            <p className="text-sm font-semibold text-amber-800">
              Send reminder to {overdueCount} overdue member(s) in {groupName}?
            </p>
            <p className="text-xs text-amber-600 mt-0.5">An SMS/WhatsApp reminder will be sent to all overdue installment members.</p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setShowReminderConfirm(false)}
              className="px-3 py-1.5 rounded-lg border border-amber-300 text-xs text-amber-700 hover:bg-amber-100"
            >
              Cancel
            </button>
            <button
              onClick={sendReminder}
              className="px-3 py-1.5 rounded-lg bg-amber-600 text-white text-xs hover:bg-amber-700"
            >
              Confirm Send
            </button>
          </div>
        </div>
      )}

      {/* Reminder success toast */}
      {reminderSent && (
        <div className="flex items-center gap-2 bg-green-50 border border-green-200 text-green-700 text-sm px-4 py-2.5 rounded-xl">
          <span>✓</span>
          Reminder sent to {overdueCount} member(s) in {groupName}
        </div>
      )}

      {/* Schedule table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-[#E2E8F0] flex items-center justify-between">
          <h3 className="font-semibold text-[#041B3C]">{groupName}</h3>
          <span className="text-xs text-[#64748B]">{schedule.length} installments</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-[#F7FAFC]">
              <tr>
                {[
                  "Month",
                  "Due Date",
                  "Amount / Member",
                  "Total Expected",
                  "Total Collected",
                  "Collection Rate",
                  "Variance",
                  "Status",
                ].map((h) => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {schedule.map((row) => {
                const rate = Math.round((row.totalCollected / row.totalExpected) * 100);
                const variance = row.totalCollected - row.totalExpected;
                const isEditing = editingId === row.id;
                return (
                  <tr key={row.id} className="hover:bg-[#F7FAFC] transition-colors">
                    <td className="px-4 py-3.5 font-medium text-[#041B3C]">Month {row.month}</td>

                    {/* Inline-editable due date */}
                    <td className="px-4 py-3.5">
                      {isEditing ? (
                        <div className="flex items-center gap-2">
                          <input
                            type="date"
                            value={editDate}
                            onChange={(e) => setEditDate(e.target.value)}
                            className="px-2 py-1 rounded border border-[#14B8A6] text-sm text-[#172B4D] focus:outline-none"
                          />
                          <button onClick={() => saveEdit(row.id)} className="text-green-600 hover:text-green-700">
                            <span>✓</span>
                          </button>
                          <button onClick={cancelEdit} className="text-red-400 hover:text-red-600">
                            <span>✕</span>
                          </button>
                        </div>
                      ) : (
                        <button
                          onClick={() => startEdit(row)}
                          className="flex items-center gap-1.5 text-[#172B4D] hover:text-[#14B8A6] group"
                        >
                          {new Date(row.dueDate).toLocaleDateString("en-IN", {
                            day: "2-digit",
                            month: "short",
                            year: "numeric",
                          })}
                          <Edit size={11} className="opacity-0 group-hover:opacity-100 transition-opacity text-[#64748B]" />
                        </button>
                      )}
                    </td>

                    <td className="px-4 py-3.5 text-[#64748B]">{formatINR(row.amountPerMember)}</td>
                    <td className="px-4 py-3.5 text-[#041B3C]">{formatINR(row.totalExpected)}</td>
                    <td className="px-4 py-3.5 text-green-600 font-medium">{formatINR(row.totalCollected)}</td>

                    {/* Progress bar */}
                    <td className="px-4 py-3.5">
                      <div className="flex items-center gap-2">
                        <div className="w-16 h-1.5 bg-[#E2E8F0] rounded-full overflow-hidden">
                          <div
                            className="h-full rounded-full bg-[#14B8A6]"
                            style={{ width: `${Math.min(rate, 100)}%` }}
                          />
                        </div>
                        <span className="text-xs font-medium text-[#041B3C]">{rate}%</span>
                      </div>
                    </td>

                    <td className={`px-4 py-3.5 font-medium text-xs ${variance < 0 ? "text-red-500" : "text-green-600"}`}>
                      {variance < 0 ? "-" : "+"}{formatINR(Math.abs(variance))}
                    </td>
                    <td className="px-4 py-3.5">
                      <StatusChip status={row.status} />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
