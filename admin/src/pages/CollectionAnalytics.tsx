import { useState, useMemo } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import { ArrowUpDown, Download, FileText } from "lucide-react";

// ── Mock data ──────────────────────────────────────────────────────────────

const trendData = [
  { date: "01 Oct", collected: 85000 },
  { date: "03 Oct", collected: 42000 },
  { date: "05 Oct", collected: 125000 },
  { date: "07 Oct", collected: 67000 },
  { date: "09 Oct", collected: 98000 },
  { date: "11 Oct", collected: 155000 },
  { date: "13 Oct", collected: 88000 },
  { date: "15 Oct", collected: 210000 },
  { date: "17 Oct", collected: 73000 },
  { date: "19 Oct", collected: 190000 },
  { date: "21 Oct", collected: 110000 },
  { date: "23 Oct", collected: 145000 },
  { date: "25 Oct", collected: 92000 },
  { date: "27 Oct", collected: 178000 },
  { date: "29 Oct", collected: 63000 },
];

const paymentMethodData = [
  { name: "UPI", value: 52, color: "#14B8A6" },
  { name: "Net Banking", value: 23, color: "#073763" },
  { name: "Debit Card", value: 15, color: "#1769AA" },
  { name: "NEFT / RTGS", value: 10, color: "#F59E0B" },
];

const GROUPS = ["All Groups", "Family Savings 50K", "Community Chit 1L", "Monthly Circle 25K", "Sunrise Savings 2L"];

interface CollectionRow {
  id: string;
  name: string;
  group: string;
  due: number;
  paid: number;
  status: "PAID" | "PARTIAL" | "OVERDUE" | "PENDING";
}

const mockRows: CollectionRow[] = [
  { id: "1", name: "Priya Sharma", group: "Family Savings 50K", due: 2500, paid: 2500, status: "PAID" },
  { id: "2", name: "Ravi Kumar", group: "Community Chit 1L", due: 5000, paid: 5000, status: "PAID" },
  { id: "3", name: "Ananya Singh", group: "Family Savings 50K", due: 2500, paid: 1500, status: "PARTIAL" },
  { id: "4", name: "Suresh Patel", group: "Sunrise Savings 2L", due: 10000, paid: 0, status: "OVERDUE" },
  { id: "5", name: "Meena Iyer", group: "Community Chit 1L", due: 5000, paid: 5000, status: "PAID" },
  { id: "6", name: "Arjun Nair", group: "Monthly Circle 25K", due: 1250, paid: 0, status: "PENDING" },
  { id: "7", name: "Kavitha Reddy", group: "Family Savings 50K", due: 2500, paid: 2500, status: "PAID" },
  { id: "8", name: "Vikram Mehta", group: "Sunrise Savings 2L", due: 10000, paid: 7500, status: "PARTIAL" },
  { id: "9", name: "Sunita Rao", group: "Community Chit 1L", due: 5000, paid: 0, status: "OVERDUE" },
  { id: "10", name: "Deepak Joshi", group: "Monthly Circle 25K", due: 1250, paid: 1250, status: "PAID" },
];

// ── Status chip ────────────────────────────────────────────────────────────

const statusStyles: Record<string, string> = {
  PAID: "bg-green-50 text-green-700 border-green-200",
  PARTIAL: "bg-amber-50 text-amber-700 border-amber-200",
  OVERDUE: "bg-red-50 text-red-700 border-red-200",
  PENDING: "bg-[#F1F3FF] text-[#64748B] border-[#E2E8F0]",
};

function StatusChip({ status }: { status: string }) {
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${statusStyles[status] ?? statusStyles.PENDING}`}>
      {status}
    </span>
  );
}

function formatINR(v: number) {
  return `₹${v.toLocaleString("en-IN")}`;
}

// ── Custom pie label ───────────────────────────────────────────────────────

interface PieLabelProps {
  name: string;
  percent?: number;
}

function PieLabel({ name, percent = 0 }: PieLabelProps) {
  return `${name} ${(percent * 100).toFixed(0)}%`;
}

// ── Component ──────────────────────────────────────────────────────────────

type SortKey = "due" | "paid" | "outstanding" | "name";
type SortDir = "asc" | "desc";

export default function CollectionAnalyticsPage() {
  const [fromDate, setFromDate] = useState("2026-10-01");
  const [toDate, setToDate] = useState("2026-10-31");
  const [selectedGroups, setSelectedGroups] = useState<string[]>([]);
  const [groupDropOpen, setGroupDropOpen] = useState(false);
  const [sortKey, setSortKey] = useState<SortKey>("name");
  const [sortDir, setSortDir] = useState<SortDir>("asc");

  function toggleGroup(g: string) {
    setSelectedGroups((prev) =>
      prev.includes(g) ? prev.filter((x) => x !== g) : [...prev, g]
    );
  }

  function handleSort(key: SortKey) {
    if (sortKey === key) {
      setSortDir((d) => (d === "asc" ? "desc" : "asc"));
    } else {
      setSortKey(key);
      setSortDir("asc");
    }
  }

  const filteredRows = useMemo(() => {
    let rows = [...mockRows];
    if (selectedGroups.length > 0) {
      rows = rows.filter((r) => selectedGroups.includes(r.group));
    }
    rows.sort((a, b) => {
      let av: number | string = 0;
      let bv: number | string = 0;
      if (sortKey === "name") { av = a.name; bv = b.name; }
      if (sortKey === "due") { av = a.due; bv = b.due; }
      if (sortKey === "paid") { av = a.paid; bv = b.paid; }
      if (sortKey === "outstanding") { av = a.due - a.paid; bv = b.due - b.paid; }
      if (typeof av === "string") return sortDir === "asc" ? av.localeCompare(bv as string) : (bv as string).localeCompare(av);
      return sortDir === "asc" ? (av as number) - (bv as number) : (bv as number) - (av as number);
    });
    return rows;
  }, [selectedGroups, sortKey, sortDir]);

  function exportCSV() {
    const header = "Name,Group,Due Amount,Paid Amount,Outstanding,Status";
    const rows = filteredRows.map((r) =>
      `${r.name},${r.group},${r.due},${r.paid},${r.due - r.paid},${r.status}`
    );
    const csv = [header, ...rows].join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "collection-analytics.csv";
    a.click();
    URL.revokeObjectURL(url);
  }

  function exportPDF() {
    console.log("Generating PDF...");
    // stub — would integrate jsPDF or server-side PDF here
  }

  const SortBtn = ({ col }: { col: SortKey }) => (
    <button onClick={() => handleSort(col)} className="ml-1 inline-flex">
      <ArrowUpDown size={12} className={sortKey === col ? "text-[#14B8A6]" : "text-[#64748B]"} />
    </button>
  );

  return (
    <div className="p-6 space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Collection Analytics</h2>
          <p className="text-sm text-[#64748B]">Deep-dive into payment collection trends</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={exportCSV}
            className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
          >
            <Download size={14} />
            Export CSV
          </button>
          <button
            onClick={exportPDF}
            className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
          >
            <FileText size={14} />
            Export PDF
          </button>
        </div>
      </div>

      {/* Filter bar */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-4 flex items-center gap-4 flex-wrap">
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

        {/* Group multi-select */}
        <div className="relative">
          <button
            onClick={() => setGroupDropOpen((o) => !o)}
            className="px-3 py-1.5 rounded-lg border border-[#E2E8F0] text-sm text-[#172B4D] bg-white flex items-center gap-2 min-w-[160px]"
          >
            {selectedGroups.length === 0 ? "All Groups" : `${selectedGroups.length} group(s)`}
            <span className="ml-auto text-[#64748B]">▾</span>
          </button>
          {groupDropOpen && (
            <div className="absolute top-full mt-1 left-0 bg-white rounded-xl border border-[#E2E8F0] shadow-lg z-10 min-w-[200px] py-2">
              {GROUPS.slice(1).map((g) => (
                <label key={g} className="flex items-center gap-2 px-4 py-2 text-sm text-[#172B4D] hover:bg-[#F7FAFC] cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedGroups.includes(g)}
                    onChange={() => toggleGroup(g)}
                    className="accent-[#14B8A6]"
                  />
                  {g}
                </label>
              ))}
              {selectedGroups.length > 0 && (
                <button
                  onClick={() => setSelectedGroups([])}
                  className="w-full text-left px-4 py-2 text-xs text-red-500 hover:bg-red-50 border-t border-[#E2E8F0]"
                >
                  Clear all
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-3 gap-5">
        {/* Line chart — 2/3 */}
        <div className="col-span-2 bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5">
          <h3 className="font-semibold text-[#041B3C] mb-1">Daily Collection Trend</h3>
          <p className="text-xs text-[#64748B] mb-4">
            {fromDate} → {toDate}
          </p>
          <ResponsiveContainer width="100%" height={240}>
            <LineChart data={trendData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#F1F3FF" />
              <XAxis dataKey="date" tick={{ fontSize: 11, fill: "#64748B" }} axisLine={false} tickLine={false} />
              <YAxis
                tickFormatter={(v: number) => `₹${(v / 1000).toFixed(0)}K`}
                tick={{ fontSize: 11, fill: "#64748B" }}
                axisLine={false}
                tickLine={false}
              />
              <Tooltip
                formatter={(v: number) => [formatINR(v), "Collected"]}
                contentStyle={{ borderRadius: 8, border: "1px solid #E2E8F0", fontSize: 12 }}
              />
              <Legend wrapperStyle={{ fontSize: 12 }} />
              <Line
                type="monotone"
                dataKey="collected"
                name="Collected"
                stroke="#14B8A6"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Donut chart — 1/3 */}
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5">
          <h3 className="font-semibold text-[#041B3C] mb-1">Payment Methods</h3>
          <p className="text-xs text-[#64748B] mb-2">This period</p>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={paymentMethodData}
                cx="50%"
                cy="50%"
                innerRadius={55}
                outerRadius={80}
                paddingAngle={3}
                dataKey="value"
                label={PieLabel}
                labelLine={false}
              >
                {paymentMethodData.map((entry) => (
                  <Cell key={entry.name} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip formatter={(v: number) => [`${v}%`]} />
            </PieChart>
          </ResponsiveContainer>
          <div className="space-y-1.5 mt-1">
            {paymentMethodData.map((m) => (
              <div key={m.name} className="flex items-center justify-between text-xs">
                <span className="flex items-center gap-1.5">
                  <span className="w-2.5 h-2.5 rounded-full inline-block" style={{ background: m.color }} />
                  {m.name}
                </span>
                <span className="font-semibold text-[#041B3C]">{m.value}%</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Collection table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-[#E2E8F0] flex items-center justify-between">
          <h3 className="font-semibold text-[#041B3C]">Member Collections</h3>
          <span className="text-xs text-[#64748B]">{filteredRows.length} members</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-[#F7FAFC]">
              <tr>
                <th className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">
                  Member Name <SortBtn col="name" />
                </th>
                <th className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">Group</th>
                <th className="px-5 py-3 text-right text-xs font-semibold text-[#64748B] uppercase tracking-wide">
                  Due Amount <SortBtn col="due" />
                </th>
                <th className="px-5 py-3 text-right text-xs font-semibold text-[#64748B] uppercase tracking-wide">
                  Paid Amount <SortBtn col="paid" />
                </th>
                <th className="px-5 py-3 text-right text-xs font-semibold text-[#64748B] uppercase tracking-wide">
                  Outstanding <SortBtn col="outstanding" />
                </th>
                <th className="px-5 py-3 text-center text-xs font-semibold text-[#64748B] uppercase tracking-wide">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {filteredRows.map((row) => {
                const outstanding = row.due - row.paid;
                return (
                  <tr key={row.id} className="hover:bg-[#F7FAFC] transition-colors">
                    <td className="px-5 py-3.5 font-medium text-[#041B3C]">{row.name}</td>
                    <td className="px-5 py-3.5 text-[#64748B]">{row.group}</td>
                    <td className="px-5 py-3.5 text-right text-[#041B3C]">{formatINR(row.due)}</td>
                    <td className="px-5 py-3.5 text-right text-green-600 font-medium">{formatINR(row.paid)}</td>
                    <td className={`px-5 py-3.5 text-right font-medium ${outstanding > 0 ? "text-red-500" : "text-[#64748B]"}`}>
                      {formatINR(outstanding)}
                    </td>
                    <td className="px-5 py-3.5 text-center">
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
