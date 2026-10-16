import { useNavigate } from "react-router-dom";
import { ShieldCheck, Users, ChevronRight, Server } from "lucide-react";

// ── Settings hub page ─────────────────────────────────────────────────────────

const settingsCategories = [
  {
    id: "rbac",
    icon: ShieldCheck,
    title: "Role-Based Access Control",
    description: "Manage module permissions for each admin role. Set read/write/none access per module.",
    path: "/settings/rbac",
    accent: "#073763",
  },
  {
    id: "users",
    icon: Users,
    title: "Admin Users",
    description: "Invite, manage roles, and deactivate admin accounts. Reset passwords and view activity.",
    path: "/settings/users",
    accent: "#14B8A6",
  },
];

export default function SettingsPage() {
  const navigate = useNavigate();

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-semibold text-[#041B3C]">Settings</h2>
        <p className="text-sm text-[#64748B]">Manage admin roles, permissions, and system configuration.</p>
      </div>

      {/* Settings category cards */}
      <div className="grid grid-cols-2 gap-5">
        {settingsCategories.map((cat) => (
          <div
            key={cat.id}
            className="bg-white rounded-xl border border-[#E2E8F0] p-5 shadow-sm flex flex-col gap-4"
          >
            <div className="flex items-start gap-3">
              <div
                className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
                style={{ background: `${cat.accent}15` }}
              >
                <cat.icon size={20} style={{ color: cat.accent }} />
              </div>
              <div className="flex-1">
                <h3 className="font-semibold text-[#041B3C]">{cat.title}</h3>
                <p className="text-xs text-[#64748B] mt-1 leading-relaxed">{cat.description}</p>
              </div>
            </div>
            <div className="pt-1 border-t border-[#E2E8F0]">
              <button
                onClick={() => navigate(cat.path)}
                className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors"
              >
                Manage
                <ChevronRight size={14} />
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* System info card */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] p-5 shadow-sm">
        <div className="flex items-center gap-2 mb-4">
          <Server size={16} className="text-[#073763]" />
          <h3 className="font-semibold text-[#041B3C]">System Information</h3>
        </div>
        <div className="grid grid-cols-3 gap-6">
          <div>
            <p className="text-xs text-[#64748B] uppercase tracking-wide font-semibold mb-1">App Version</p>
            <p className="text-sm font-medium text-[#041B3C]">v0.1.0</p>
          </div>
          <div>
            <p className="text-xs text-[#64748B] uppercase tracking-wide font-semibold mb-1">Environment</p>
            <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-green-50 text-green-700">
              Production
            </span>
          </div>
          <div>
            <p className="text-xs text-[#64748B] uppercase tracking-wide font-semibold mb-1">Last Deploy</p>
            <p className="text-sm font-medium text-[#041B3C]">14 Oct 2026</p>
          </div>
        </div>
      </div>
    </div>
  );
}
