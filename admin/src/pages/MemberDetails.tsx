import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Mail, Phone, Calendar, Edit, Lock, Download } from "lucide-react";

const tabs = ["Overview", "Memberships", "Payments", "Activity Log"];

const sampleMember = {
  id: "member-1",
  fullName: "Priya Sharma",
  mobileNumber: "+91 98765 43210",
  email: "priya.sharma@example.com",
  dateOfBirth: "15/06/1992",
  gender: "Female",
  accountStatus: "ACTIVE",
  joinDate: "01/03/2026",
  preferredLanguage: "English",
  enrolledGroupsCount: 3,
  memberships: [
    { group: "Family Savings 50K", value: "₹50,000", paid: "6/24", payout: "—", enrolled: "01/03/2026", status: "Active" },
    { group: "Community Chit 1L", value: "₹1,00,000", paid: "14/20", payout: "₹82,500", enrolled: "01/04/2025", status: "Active" },
    { group: "Monthly Circle 25K", value: "₹25,000", paid: "20/20", payout: "₹23,750", enrolled: "01/01/2025", status: "Completed" },
  ],
  recentPayments: [
    { date: "12/10/2026", group: "Family Savings 50K", month: "Oct 2026", amount: "₹2,500", method: "UPI", status: "SUCCESSFUL" },
    { date: "10/09/2026", group: "Community Chit 1L", month: "Sep 2026", amount: "₹5,000", method: "Auto-Debit", status: "SUCCESSFUL" },
    { date: "15/09/2026", group: "Family Savings 50K", month: "Sep 2026", amount: "₹2,500", method: "UPI", status: "FAILED" },
  ],
  activityLog: [
    { time: "12/10/2026 10:32", action: "Payment received ₹2,500", entity: "Family Savings 50K" },
    { time: "10/09/2026 09:15", action: "Auto-debit payment ₹5,000", entity: "Community Chit 1L" },
    { time: "01/03/2026 14:00", action: "Account created", entity: "System" },
  ],
  bankAccounts: [{ bank: "HDFC Bank", type: "Savings", masked: "•••• 4092", primary: true }],
  nominees: [{ name: "Amit Sharma", relationship: "Spouse", mobile: "+91 98765 00001" }],
};

export default function MemberDetailsPage() {
  useParams(); // memberId used for future API call
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState(0);
  const m = sampleMember;

  return (
    <div className="p-6 space-y-5">
      {/* Back + header */}
      <div className="flex items-center gap-4">
        <button onClick={() => navigate("/members")} className="p-2 rounded-lg border border-[#E2E8F0] bg-white hover:bg-[#F1F3FF] transition-colors">
          <ArrowLeft size={16} className="text-[#64748B]" />
        </button>
        <div className="flex-1">
          <h2 className="text-xl font-semibold text-[#041B3C]">{m.fullName}</h2>
          <p className="text-sm text-[#64748B]">#{m.id.slice(-6).toUpperCase()} · {m.enrolledGroupsCount} active groups</p>
        </div>
        <div className="flex gap-2">
          <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#064748B] hover:bg-[#F1F3FF] transition-colors">
            <Lock size={14} className="text-[#64748B]" /> Reset MPIN
          </button>
          <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium hover:bg-[#F1F3FF] transition-colors">
            <Download size={14} className="text-[#64748B]" /> Export
          </button>
          <button className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors">
            <Edit size={14} /> Edit
          </button>
          <span className={`self-center px-3 py-1.5 rounded-full text-xs font-semibold ${m.accountStatus === "ACTIVE" ? "bg-green-50 text-green-700" : "bg-red-50 text-red-600"}`}>
            {m.accountStatus}
          </span>
        </div>
      </div>

      {/* Tabs — NO KYC, NO Documents, NO Auctions */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="flex border-b border-[#E2E8F0] px-2 pt-2">
          {tabs.map((t, i) => (
            <button key={t} onClick={() => setActiveTab(i)}
              className={`px-4 py-2.5 text-sm font-medium rounded-t-lg transition-colors mr-0.5 ${activeTab === i ? "bg-[#F1F3FF] text-[#073763] border-b-2 border-[#073763]" : "text-[#64748B] hover:text-[#041B3C]"}`}
            >{t}</button>
          ))}
        </div>

        <div className="p-5">
          {activeTab === 0 && (
            <div className="grid grid-cols-3 gap-5">
              {/* Personal info */}
              <div className="col-span-2 space-y-4">
                <h3 className="text-sm font-semibold text-[#041B3C] uppercase tracking-wide">Personal Information</h3>
                <div className="grid grid-cols-2 gap-4">
                  {[
                    { label: "Full Name", value: m.fullName, icon: null },
                    { label: "Mobile", value: m.mobileNumber, icon: Phone },
                    { label: "Email", value: m.email, icon: Mail },
                    { label: "Date of Birth", value: m.dateOfBirth, icon: Calendar },
                    { label: "Gender", value: m.gender, icon: null },
                    { label: "Language", value: m.preferredLanguage, icon: null },
                    { label: "Member Since", value: m.joinDate, icon: null },
                  ].map(({ label, value, icon: Icon }) => (
                    <div key={label} className="bg-[#F7FAFC] rounded-lg p-3">
                      <div className="text-xs text-[#64748B] mb-1">{label}</div>
                      <div className="flex items-center gap-2 text-sm font-medium text-[#041B3C]">
                        {Icon && <Icon size={13} className="text-[#64748B]" />}{value}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
              <div className="space-y-4">
                {/* Bank accounts */}
                <h3 className="text-sm font-semibold text-[#041B3C] uppercase tracking-wide">Bank Accounts</h3>
                {m.bankAccounts.map((acc) => (
                  <div key={acc.masked} className="bg-[#F1F3FF] rounded-lg p-3">
                    <div className="flex items-center justify-between">
                      <div className="text-sm font-medium text-[#041B3C]">{acc.bank}</div>
                      {acc.primary && <span className="text-xs bg-[#073763] text-white px-2 py-0.5 rounded-full">Primary</span>}
                    </div>
                    <div className="text-xs text-[#64748B] mt-1">{acc.type} · {acc.masked}</div>
                  </div>
                ))}
                {/* Nominees */}
                <h3 className="text-sm font-semibold text-[#041B3C] uppercase tracking-wide mt-4">Nominees</h3>
                {m.nominees.map((n) => (
                  <div key={n.name} className="bg-[#F7FAFC] rounded-lg p-3">
                    <div className="text-sm font-medium text-[#041B3C]">{n.name}</div>
                    <div className="text-xs text-[#64748B]">{n.relationship} · {n.mobile}</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 1 && (
            <table className="w-full text-sm">
              <thead><tr className="border-b border-[#E2E8F0]">
                {["Group", "Chit Value", "Installments Paid", "Payout Received", "Enrolled", "Status"].map(h => (
                  <th key={h} className="pb-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">{h}</th>
                ))}
              </tr></thead>
              <tbody className="divide-y divide-[#E2E8F0]">
                {m.memberships.map((mem) => (
                  <tr key={mem.group} className="hover:bg-[#F7FAFC]">
                    <td className="py-3 font-medium text-[#041B3C]">{mem.group}</td>
                    <td className="py-3 text-[#64748B]">{mem.value}</td>
                    <td className="py-3 text-[#64748B]">{mem.paid}</td>
                    <td className="py-3 font-medium text-[#14B8A6]">{mem.payout}</td>
                    <td className="py-3 text-xs text-[#64748B]">{mem.enrolled}</td>
                    <td className="py-3"><span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${mem.status === "Active" ? "bg-green-50 text-green-700" : "bg-[#F1F3FF] text-[#64748B]"}`}>{mem.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {activeTab === 2 && (
            <table className="w-full text-sm">
              <thead><tr className="border-b border-[#E2E8F0]">
                {["Date", "Group", "Month", "Amount", "Method", "Status"].map(h => (
                  <th key={h} className="pb-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">{h}</th>
                ))}
              </tr></thead>
              <tbody className="divide-y divide-[#E2E8F0]">
                {m.recentPayments.map((p, i) => (
                  <tr key={i} className="hover:bg-[#F7FAFC]">
                    <td className="py-3 text-xs text-[#64748B]">{p.date}</td>
                    <td className="py-3 text-[#041B3C]">{p.group}</td>
                    <td className="py-3 text-xs text-[#64748B]">{p.month}</td>
                    <td className="py-3 font-semibold text-[#041B3C]">{p.amount}</td>
                    <td className="py-3 text-xs text-[#64748B]">{p.method}</td>
                    <td className="py-3"><span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${p.status === "SUCCESSFUL" ? "bg-green-50 text-green-700" : "bg-red-50 text-red-600"}`}>{p.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {activeTab === 3 && (
            <div className="space-y-3">
              {m.activityLog.map((log, i) => (
                <div key={i} className="flex gap-4 items-start py-2 border-b border-[#E2E8F0] last:border-0">
                  <span className="text-xs text-[#64748B] w-36 shrink-0">{log.time}</span>
                  <span className="text-sm text-[#041B3C] flex-1">{log.action}</span>
                  <span className="text-xs text-[#64748B]">{log.entity}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
