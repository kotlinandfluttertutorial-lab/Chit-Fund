import { TrendingUp, TrendingDown, Users, Landmark, IndianRupee, AlertCircle, Plus, Send, Download } from "lucide-react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";

// ── KPI Cards ─────────────────────────────────────────────────────────────────

const kpis = [
  { label: "Total Active Members", value: "1,247", delta: "+12%", up: true, icon: Users, accent: "#073763" },
  { label: "Chit Value Under Management", value: "₹3.2 Cr", delta: "+8%", up: true, icon: Landmark, accent: "#1769AA" },
  { label: "Collections This Month", value: "₹12.4 L", delta: "-3%", up: false, icon: IndianRupee, accent: "#14B8A6" },
  { label: "Pending Enrollments", value: "23", delta: "+5", up: true, icon: Users, accent: "#F59E0B" },
  { label: "Overdue Installments", value: "47", delta: "+12", up: false, icon: AlertCircle, accent: "#EF4444" },
];

// ── Chart data ────────────────────────────────────────────────────────────────

const collectionData = [
  { month: "Nov '25", collected: 980000, overdue: 45000 },
  { month: "Dec '25", collected: 1050000, overdue: 32000 },
  { month: "Jan '26", collected: 1120000, overdue: 28000 },
  { month: "Feb '26", collected: 1080000, overdue: 55000 },
  { month: "Mar '26", collected: 1200000, overdue: 40000 },
  { month: "Apr '26", collected: 1180000, overdue: 38000 },
  { month: "May '26", collected: 1240000, overdue: 30000 },
  { month: "Jun '26", collected: 1190000, overdue: 42000 },
  { month: "Jul '26", collected: 1280000, overdue: 25000 },
  { month: "Aug '26", collected: 1350000, overdue: 20000 },
  { month: "Sep '26", collected: 1310000, overdue: 35000 },
  { month: "Oct '26", collected: 1240000, overdue: 58000 },
];

// ── Group performance table ───────────────────────────────────────────────────

const groups = [
  { name: "Family Savings 50K", value: "₹50,000", members: "18/20", rate: 90, next: "15 Oct 2026", status: "Active" },
  { name: "Community Chit 1L", value: "₹1,00,000", members: "17/17", rate: 100, next: "20 Oct 2026", status: "Active" },
  { name: "Monthly Circle 25K", value: "₹25,000", members: "20/20", rate: 100, next: "—", status: "Completed" },
  { name: "Sunrise Savings 2L", value: "₹2,00,000", members: "8/15", rate: 85, next: "25 Oct 2026", status: "Active" },
];

// ── Recent activity ───────────────────────────────────────────────────────────

const activity = [
  { type: "payment", text: "₹2,500 received from Priya Sharma — Family Savings 50K", time: "10 mins ago" },
  { type: "enrollment", text: "Ravi Kumar enrolled in Community Chit 1L", time: "45 mins ago" },
  { type: "reminder", text: "Bulk reminder sent to 18 members for October installment", time: "2 hrs ago" },
  { type: "payment", text: "₹5,000 received from Ananya Singh — Community Chit 1L", time: "3 hrs ago" },
  { type: "group", text: "New group 'Sunrise Savings 2L' created and published", time: "Yesterday" },
];

const activityColors: Record<string, string> = {
  payment: "bg-green-100 text-green-700",
  enrollment: "bg-blue-100 text-blue-700",
  reminder: "bg-amber-100 text-amber-700",
  group: "bg-purple-100 text-purple-700",
};

const formatAmount = (value: number) =>
  value >= 100000 ? `₹${(value / 100000).toFixed(1)}L` : `₹${(value / 1000).toFixed(0)}K`;

// ── Dashboard page ────────────────────────────────────────────────────────────

export default function DashboardPage() {
  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Good morning 👋</h2>
          <p className="text-sm text-[#64748B]">Here's what's happening with ChitSmart today.</p>
        </div>
        <div className="flex gap-2">
          <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
            <Send size={14} />
            Send Reminder
          </button>
          <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
            <Download size={14} />
            Export Report
          </button>
          <button className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors">
            <Plus size={14} />
            New Group
          </button>
        </div>
      </div>

      {/* KPI cards — 5 items, NO Pending KYC, NO Upcoming Auctions */}
      <div className="grid grid-cols-5 gap-4">
        {kpis.map((kpi) => (
          <div key={kpi.label} className="bg-white rounded-xl border border-[#E2E8F0] p-4 shadow-sm">
            <div className="flex items-start justify-between mb-3">
              <div
                className="w-9 h-9 rounded-lg flex items-center justify-center"
                style={{ background: `${kpi.accent}18` }}
              >
                <kpi.icon size={16} style={{ color: kpi.accent }} />
              </div>
              <span className={`flex items-center gap-1 text-xs font-semibold ${kpi.up ? "text-green-600" : "text-red-500"}`}>
                {kpi.up ? <TrendingUp size={12} /> : <TrendingDown size={12} />}
                {kpi.delta}
              </span>
            </div>
            <div className="text-2xl font-bold text-[#041B3C] leading-none mb-1">{kpi.value}</div>
            <div className="text-xs text-[#64748B] leading-tight">{kpi.label}</div>
          </div>
        ))}
      </div>

      {/* Charts + Activity */}
      <div className="grid grid-cols-3 gap-6">
        {/* Collection Analytics chart */}
        <div className="col-span-2 bg-white rounded-xl border border-[#E2E8F0] p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="font-semibold text-[#041B3C]">Collection Analytics</h3>
              <p className="text-xs text-[#64748B]">Last 12 months</p>
            </div>
            <div className="flex gap-4 text-xs">
              <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded-sm bg-[#14B8A6] inline-block" />Collected</span>
              <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded-sm bg-[#F59E0B] inline-block" />Overdue</span>
            </div>
          </div>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={collectionData} barSize={10} barGap={2}>
              <CartesianGrid strokeDasharray="3 3" stroke="#F1F3FF" />
              <XAxis dataKey="month" tick={{ fontSize: 11, fill: "#64748B" }} axisLine={false} tickLine={false} />
              <YAxis tickFormatter={formatAmount} tick={{ fontSize: 11, fill: "#64748B" }} axisLine={false} tickLine={false} />
              <Tooltip
                formatter={(v: number) => [formatAmount(v)]}
                contentStyle={{ borderRadius: 8, border: "1px solid #E2E8F0", fontSize: 12 }}
              />
              <Bar dataKey="collected" fill="#14B8A6" radius={[3, 3, 0, 0]} />
              <Bar dataKey="overdue" fill="#F59E0B" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Recent activity */}
        <div className="bg-white rounded-xl border border-[#E2E8F0] p-5 shadow-sm">
          <h3 className="font-semibold text-[#041B3C] mb-4">Recent Activity</h3>
          <div className="space-y-3">
            {activity.map((item, i) => (
              <div key={i} className="flex gap-3">
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium shrink-0 mt-0.5 ${activityColors[item.type]}`}>
                  {item.type}
                </span>
                <div>
                  <p className="text-xs text-[#172B4D] leading-tight">{item.text}</p>
                  <p className="text-[11px] text-[#64748B] mt-0.5">{item.time}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Group Performance table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-[#E2E8F0] flex items-center justify-between">
          <h3 className="font-semibold text-[#041B3C]">Group Performance</h3>
          <span className="text-xs text-[#64748B]">{groups.length} groups</span>
        </div>
        <table className="w-full text-sm">
          <thead className="bg-[#F7FAFC]">
            <tr>
              {["Group Name", "Chit Value", "Members", "Collection Rate", "Next Installment", "Status"].map((h) => (
                <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[#E2E8F0]">
            {groups.map((g) => (
              <tr key={g.name} className="hover:bg-[#F7FAFC] transition-colors">
                <td className="px-5 py-3.5 font-medium text-[#041B3C]">{g.name}</td>
                <td className="px-5 py-3.5 text-[#64748B]">{g.value}</td>
                <td className="px-5 py-3.5 text-[#64748B]">{g.members}</td>
                <td className="px-5 py-3.5">
                  <div className="flex items-center gap-2">
                    <div className="w-20 h-1.5 bg-[#E2E8F0] rounded-full overflow-hidden">
                      <div
                        className="h-full rounded-full bg-[#14B8A6]"
                        style={{ width: `${g.rate}%` }}
                      />
                    </div>
                    <span className="text-xs font-medium text-[#041B3C]">{g.rate}%</span>
                  </div>
                </td>
                <td className="px-5 py-3.5 text-[#64748B] text-xs">{g.next}</td>
                <td className="px-5 py-3.5">
                  <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                    g.status === "Active"
                      ? "bg-green-50 text-green-700"
                      : "bg-[#F1F3FF] text-[#64748B]"
                  }`}>
                    {g.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
