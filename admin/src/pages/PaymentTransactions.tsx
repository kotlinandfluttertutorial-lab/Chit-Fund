import { useState } from "react";
import { Download, Search, Receipt } from "lucide-react";

const transactions = [
  { id: "TXN-001", date: "12/10/2026 10:32", member: "Priya Sharma", memberId: "CS-8402", group: "Family Savings 50K", month: "Oct 2026", amount: "₹2,500", method: "UPI", gateway: "RZP_001234", status: "SUCCESSFUL" },
  { id: "TXN-002", date: "10/10/2026 09:15", member: "Ravi Kumar", memberId: "CS-8403", group: "Community Chit 1L", month: "Oct 2026", amount: "₹5,000", method: "Auto-Debit", gateway: "RZP_001235", status: "SUCCESSFUL" },
  { id: "TXN-003", date: "08/10/2026 14:00", member: "Ananya Singh", memberId: "CS-8404", group: "Family Savings 50K", month: "Oct 2026", amount: "₹2,500", method: "Net Banking", gateway: "RZP_001236", status: "FAILED" },
  { id: "TXN-004", date: "05/10/2026 11:45", member: "Deepak Patel", memberId: "CS-8405", group: "Community Chit 1L", month: "Oct 2026", amount: "₹5,000", method: "UPI", gateway: "RZP_001237", status: "SUCCESSFUL" },
  { id: "TXN-005", date: "01/10/2026 16:20", member: "Sunita Rao", memberId: "CS-8406", group: "Sunrise Savings 2L", month: "Oct 2026", amount: "₹10,000", method: "NEFT", gateway: "RZP_001238", status: "PENDING" },
];

const statusStyles: Record<string, string> = {
  SUCCESSFUL: "bg-green-50 text-green-700",
  FAILED: "bg-red-50 text-red-600",
  PENDING: "bg-amber-50 text-amber-700",
  REFUNDED: "bg-blue-50 text-blue-700",
};

export default function PaymentTransactionsPage() {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const filtered = transactions.filter((t) => {
    const matchSearch = !search || t.member.toLowerCase().includes(search.toLowerCase()) || t.id.includes(search) || t.gateway.includes(search);
    const matchStatus = statusFilter === "ALL" || t.status === statusFilter;
    return matchSearch && matchStatus;
  });

  return (
    <div className="p-6 space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Payment Transactions</h2>
          <p className="text-sm text-[#64748B]">{filtered.length} transactions</p>
        </div>
        <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF]">
          <Download size={14} /> Export CSV
        </button>
      </div>

      <div className="bg-white rounded-xl border border-[#E2E8F0] p-4 shadow-sm flex items-center gap-3 flex-wrap">
        <div className="flex items-center gap-2 flex-1 min-w-60 bg-[#F7FAFC] rounded-lg px-3 py-2 border border-[#E2E8F0]">
          <Search size={15} className="text-[#64748B]" />
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search member, transaction ID, gateway ref…" className="flex-1 bg-transparent text-sm outline-none placeholder:text-[#94A3B8] text-[#041B3C]" />
        </div>
        <div className="flex items-center gap-2">
          {["ALL", "SUCCESSFUL", "FAILED", "PENDING", "REFUNDED"].map((s) => (
            <button key={s} onClick={() => setStatusFilter(s)} className={`px-3 py-1.5 rounded-full text-xs font-medium transition-colors ${statusFilter === s ? "bg-[#073763] text-white" : "bg-[#F1F3FF] text-[#64748B] hover:bg-[#E0E8FF]"}`}>
              {s === "ALL" ? "All" : s.charAt(0) + s.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-[#F7FAFC] border-b border-[#E2E8F0]">
            <tr>{["Date/Time", "Member", "Group", "Month", "Amount", "Method", "Gateway Ref", "Status", "Actions"].map(h => (
              <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">{h}</th>
            ))}</tr>
          </thead>
          <tbody className="divide-y divide-[#E2E8F0]">
            {filtered.map((t) => (
              <tr key={t.id} className="hover:bg-[#F7FAFC]">
                <td className="px-4 py-3 text-xs text-[#64748B]">{t.date}</td>
                <td className="px-4 py-3">
                  <div className="font-medium text-[#041B3C] text-xs">{t.member}</div>
                  <div className="text-[10px] text-[#64748B]">#{t.memberId}</div>
                </td>
                <td className="px-4 py-3 text-xs text-[#64748B]">{t.group}</td>
                <td className="px-4 py-3 text-xs text-[#64748B]">{t.month}</td>
                <td className="px-4 py-3 font-semibold text-[#041B3C]">{t.amount}</td>
                <td className="px-4 py-3 text-xs text-[#64748B]">{t.method}</td>
                <td className="px-4 py-3 text-xs font-mono text-[#64748B]">{t.gateway}</td>
                <td className="px-4 py-3"><span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${statusStyles[t.status]}`}>{t.status}</span></td>
                <td className="px-4 py-3">
                  <div className="flex gap-1">
                    <button className="p-1.5 rounded-lg border border-[#E2E8F0] bg-white hover:bg-[#F1F3FF] transition-colors" title="View Receipt">
                      <Receipt size={12} className="text-[#64748B]" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
