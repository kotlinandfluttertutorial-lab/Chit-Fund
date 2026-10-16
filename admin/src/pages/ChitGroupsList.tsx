import { useState } from "react";
import { useNavigate as _useNavigate } from "react-router-dom";
import { Plus, Search, Filter } from "lucide-react";

const groups = [
  { id: "1", name: "Family Savings 50K", value: "₹50,000", installment: "₹2,500", duration: "24M", members: "18/20", start: "01/03/2026", status: "ACTIVE" },
  { id: "2", name: "Community Chit 1L", value: "₹1,00,000", installment: "₹5,000", duration: "20M", members: "17/17", start: "01/04/2025", status: "ACTIVE" },
  { id: "3", name: "Monthly Circle 25K", value: "₹25,000", installment: "₹1,250", duration: "24M", members: "20/20", start: "01/01/2025", status: "COMPLETED" },
  { id: "4", name: "Sunrise Savings 2L", value: "₹2,00,000", installment: "₹10,000", duration: "20M", members: "8/15", start: "01/10/2026", status: "PENDING" },
];

const statusStyles: Record<string, string> = {
  ACTIVE: "bg-green-50 text-green-700",
  PENDING: "bg-amber-50 text-amber-700",
  COMPLETED: "bg-blue-50 text-blue-700",
  CLOSED: "bg-slate-100 text-slate-500",
};

export default function ChitGroupsListPage() {
  const [search, setSearch] = useState("");
  const filtered = groups.filter((g) => g.name.toLowerCase().includes(search.toLowerCase()));

  return (
    <div className="p-6 space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Chit Groups</h2>
          <p className="text-sm text-[#64748B]">{filtered.length} groups</p>
        </div>
        <button className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors">
          <Plus size={14} /> Create New Group
        </button>
      </div>

      {/* Search */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] p-4 shadow-sm flex items-center gap-3">
        <div className="flex items-center gap-2 flex-1 bg-[#F7FAFC] rounded-lg px-3 py-2 border border-[#E2E8F0]">
          <Search size={15} className="text-[#64748B]" />
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search group name…" className="flex-1 bg-transparent text-sm outline-none placeholder:text-[#94A3B8] text-[#041B3C]" />
        </div>
        <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#64748B] hover:bg-[#F1F3FF] transition-colors">
          <Filter size={14} /> Filters
        </button>
      </div>

      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-[#F7FAFC] border-b border-[#E2E8F0]">
            <tr>{["Group Name", "Chit Value", "Monthly", "Duration", "Members", "Start Date", "Status", "Actions"].map(h => (
              <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">{h}</th>
            ))}</tr>
          </thead>
          <tbody className="divide-y divide-[#E2E8F0]">
            {filtered.map((g) => (
              <tr key={g.id} className="hover:bg-[#F7FAFC] transition-colors cursor-pointer" onClick={() => {}}>
                <td className="px-5 py-3.5 font-medium text-[#041B3C]">{g.name}</td>
                <td className="px-5 py-3.5 text-[#64748B]">{g.value}</td>
                <td className="px-5 py-3.5 text-[#64748B]">{g.installment}</td>
                <td className="px-5 py-3.5 text-[#64748B]">{g.duration}</td>
                <td className="px-5 py-3.5">
                  <div className="flex items-center gap-2">
                    <div className="w-16 h-1.5 bg-[#E2E8F0] rounded-full overflow-hidden">
                      <div className="h-full bg-[#14B8A6] rounded-full" style={{ width: `${parseInt(g.members) / parseInt(g.members.split("/")[1]) * 100}%` }} />
                    </div>
                    <span className="text-xs text-[#64748B]">{g.members}</span>
                  </div>
                </td>
                <td className="px-5 py-3.5 text-xs text-[#64748B]">{g.start}</td>
                <td className="px-5 py-3.5"><span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${statusStyles[g.status]}`}>{g.status}</span></td>
                <td className="px-5 py-3.5" onClick={(e) => e.stopPropagation()}>
                  <div className="flex gap-2">
                    <button className="px-2.5 py-1 text-xs font-medium text-[#073763] border border-[#073763] rounded-lg hover:bg-[#F1F3FF]">View</button>
                    <button className="px-2.5 py-1 text-xs font-medium text-[#64748B] border border-[#E2E8F0] rounded-lg hover:bg-[#F7FAFC]">Edit</button>
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
