import { useState } from "react";
import { Bot, Sparkles, Send, Database, TrendingUp } from "lucide-react";

export default function AIToolsPage() {
  const [nlQuery, setNlQuery] = useState("");
  const [nlResult, setNlResult] = useState<{ sql: string; rows: string[][] } | null>(null);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [summary, setSummary] = useState<string | null>(null);

  const handleGenerateSummary = () => {
    setSummaryLoading(true);
    setTimeout(() => {
      setSummary(
        "In October 2026, ChitSmart collected ₹12.4 Lakhs across 3 active chit groups — up 8% from September. " +
        "Family Savings 50K achieved 90% collection rate (18/20 members paid). " +
        "Community Chit 1L reached 100% collection. " +
        "47 installments remain overdue, up from 35 last month. " +
        "Recommended action: Send targeted reminders to 47 overdue members before the 15th."
      );
      setSummaryLoading(false);
    }, 2000);
  };

  const handleNLQuery = () => {
    if (!nlQuery.trim()) return;
    setNlResult({
      sql: `SELECT m.full_name, cm.group_name, COUNT(*) as missed_months\nFROM payments p\nJOIN members m ON m.id = p.member_id\nJOIN chit_memberships cm ON cm.id = p.membership_id\nWHERE p.status = 'FAILED'\nGROUP BY m.full_name, cm.group_name\nHAVING COUNT(*) >= 3\nORDER BY missed_months DESC;`,
      rows: [
        ["Ananya Singh", "Family Savings 50K", "3"],
        ["Rajesh Kumar", "Community Chit 1L", "3"],
      ],
    });
  };

  return (
    <div className="p-6 space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-[#041B3C]">AI Tools</h2>
        <p className="text-sm text-[#64748B]">AI-powered insights over authorised datasets. Read-only — no data modifications.</p>
      </div>

      <div className="grid grid-cols-2 gap-6">
        {/* AI Collection Summary */}
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5 space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#F1F3FF] flex items-center justify-center">
              <TrendingUp size={18} className="text-[#073763]" />
            </div>
            <div>
              <h3 className="font-semibold text-[#041B3C]">AI Collection Summary</h3>
              <p className="text-xs text-[#64748B]">Natural-language analysis of this month's collections</p>
            </div>
          </div>

          <button
            onClick={handleGenerateSummary}
            disabled={summaryLoading}
            className="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] disabled:opacity-60 transition-colors w-full justify-center"
          >
            <Sparkles size={14} />
            {summaryLoading ? "Generating…" : "Generate Summary"}
          </button>

          {summary && (
            <div className="bg-[#F1F3FF] rounded-lg p-4 space-y-3">
              <div className="flex items-center gap-2">
                <Bot size={14} className="text-[#073763]" />
                <span className="text-xs font-semibold text-[#073763]">ChitSmart AI · October 2026</span>
                <span className="ml-auto text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full font-medium">High confidence</span>
              </div>
              <p className="text-sm text-[#172B4D] leading-relaxed">{summary}</p>
              <div className="flex gap-2 pt-1">
                <button className="text-xs text-[#073763] border border-[#073763] px-3 py-1.5 rounded-lg hover:bg-[#F1F3FF] font-medium">Copy</button>
                <button className="text-xs text-[#073763] border border-[#073763] px-3 py-1.5 rounded-lg hover:bg-[#F1F3FF] font-medium">Export</button>
                <button className="text-xs text-[#073763] border border-[#073763] px-3 py-1.5 rounded-lg hover:bg-[#F1F3FF] font-medium">Add to Report</button>
              </div>
            </div>
          )}
        </div>

        {/* Natural-language reporting */}
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5 space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#F1F3FF] flex items-center justify-center">
              <Database size={18} className="text-[#073763]" />
            </div>
            <div>
              <h3 className="font-semibold text-[#041B3C]">Natural-Language Reporting</h3>
              <p className="text-xs text-[#64748B]">Ask questions in plain English, get SQL-backed results</p>
            </div>
          </div>

          <div className="flex gap-2">
            <input
              value={nlQuery}
              onChange={(e) => setNlQuery(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleNLQuery()}
              placeholder="e.g. Show members who missed 3+ consecutive installments"
              className="flex-1 text-sm border border-[#E2E8F0] rounded-lg px-3 py-2.5 outline-none focus:border-[#073763] bg-[#F7FAFC] text-[#041B3C] placeholder:text-[#94A3B8]"
            />
            <button
              onClick={handleNLQuery}
              className="p-2.5 bg-[#073763] text-white rounded-lg hover:bg-[#1769AA] transition-colors"
            >
              <Send size={14} />
            </button>
          </div>

          {nlResult && (
            <div className="space-y-3">
              <div>
                <div className="text-xs font-semibold text-[#64748B] mb-1.5 flex items-center gap-1.5">
                  <Database size={11} /> Query used — for transparency (read-only)
                </div>
                <pre className="text-xs bg-[#F7FAFC] border border-[#E2E8F0] rounded-lg p-3 overflow-x-auto text-[#172B4D] font-mono">
                  {nlResult.sql}
                </pre>
              </div>
              <div>
                <div className="text-xs font-semibold text-[#64748B] mb-1.5">{nlResult.rows.length} results</div>
                <table className="w-full text-xs border border-[#E2E8F0] rounded-lg overflow-hidden">
                  <thead className="bg-[#F7FAFC]">
                    <tr>{["Member Name", "Group", "Missed Months"].map(h => (
                      <th key={h} className="px-3 py-2 text-left font-semibold text-[#64748B]">{h}</th>
                    ))}</tr>
                  </thead>
                  <tbody className="divide-y divide-[#E2E8F0]">
                    {nlResult.rows.map((row, i) => (
                      <tr key={i} className="hover:bg-[#F7FAFC]">
                        {row.map((cell, j) => <td key={j} className="px-3 py-2 text-[#041B3C]">{cell}</td>)}
                      </tr>
                    ))}
                  </tbody>
                </table>
                <button className="mt-2 text-xs text-[#073763] font-medium hover:underline">Save as Custom Report →</button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
