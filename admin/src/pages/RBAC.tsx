import { useState } from "react";
import { Shield, Plus } from "lucide-react";

// ── Types ─────────────────────────────────────────────────────────────────────

type Permission = "None" | "Read" | "Write";

type ModulePermissions = {
  Dashboard: Permission;
  Members: Permission;
  "Chit Groups": Permission;
  Installments: Permission;
  Payments: Permission;
  Ledger: Permission;
  Reports: Permission;
  "AI Tools": Permission;
  Settings: Permission;
};

const MODULES: (keyof ModulePermissions)[] = [
  "Dashboard",
  "Members",
  "Chit Groups",
  "Installments",
  "Payments",
  "Ledger",
  "Reports",
  "AI Tools",
  "Settings",
];

// ── Role definitions ──────────────────────────────────────────────────────────

const rolesMeta = [
  {
    id: "SUPER_ADMIN",
    name: "Super Admin",
    description: "Full unrestricted access to all modules and settings.",
    memberCount: 2,
    locked: true,
  },
  {
    id: "FINANCE_MANAGER",
    name: "Finance Manager",
    description: "Manages payments, ledger entries, and financial reports.",
    memberCount: 4,
    locked: false,
  },
  {
    id: "SUPPORT_AGENT",
    name: "Support Agent",
    description: "Handles member queries, enrollment, and payment support.",
    memberCount: 8,
    locked: false,
  },
  {
    id: "AUDITOR",
    name: "Auditor",
    description: "Read-only access to all financial and operational data.",
    memberCount: 2,
    locked: false,
  },
];

const defaultPermissions: Record<string, ModulePermissions> = {
  SUPER_ADMIN: {
    Dashboard: "Write",
    Members: "Write",
    "Chit Groups": "Write",
    Installments: "Write",
    Payments: "Write",
    Ledger: "Write",
    Reports: "Write",
    "AI Tools": "Write",
    Settings: "Write",
  },
  FINANCE_MANAGER: {
    Dashboard: "Read",
    Members: "Read",
    "Chit Groups": "Read",
    Installments: "Read",
    Payments: "Write",
    Ledger: "Write",
    Reports: "Write",
    "AI Tools": "Read",
    Settings: "None",
  },
  SUPPORT_AGENT: {
    Dashboard: "Read",
    Members: "Write",
    "Chit Groups": "Read",
    Installments: "Read",
    Payments: "Read",
    Ledger: "None",
    Reports: "None",
    "AI Tools": "None",
    Settings: "None",
  },
  AUDITOR: {
    Dashboard: "Read",
    Members: "Read",
    "Chit Groups": "Read",
    Installments: "Read",
    Payments: "Read",
    Ledger: "Read",
    Reports: "Read",
    "AI Tools": "Read",
    Settings: "None",
  },
};

const permissionColors: Record<Permission, string> = {
  Write: "bg-green-50 text-green-700",
  Read: "bg-blue-50 text-blue-700",
  None: "bg-slate-100 text-slate-500",
};

// ── Page ──────────────────────────────────────────────────────────────────────

export default function RBACPage() {
  const [permissions, setPermissions] = useState<Record<string, ModulePermissions>>(
    defaultPermissions
  );
  const [saved, setSaved] = useState<Record<string, boolean>>({});

  const handleChange = (
    roleId: string,
    module: keyof ModulePermissions,
    value: Permission
  ) => {
    setPermissions((prev) => ({
      ...prev,
      [roleId]: { ...prev[roleId], [module]: value },
    }));
    setSaved((prev) => ({ ...prev, [roleId]: false }));
  };

  const handleSave = (roleId: string) => {
    setSaved((prev) => ({ ...prev, [roleId]: true }));
  };

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Role-Based Access Control</h2>
          <p className="text-sm text-[#64748B]">Manage module permissions for each admin role.</p>
        </div>
        <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors">
          <Plus size={14} />
          New Role
        </button>
      </div>

      {/* Roles summary table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-[#E2E8F0] flex items-center gap-2">
          <Shield size={16} className="text-[#073763]" />
          <h3 className="font-semibold text-[#041B3C]">System Roles</h3>
        </div>
        <table className="w-full text-sm">
          <thead className="bg-[#F7FAFC] border-b border-[#E2E8F0]">
            <tr>
              {["Role Name", "Description", "Members", "Action"].map((h) => (
                <th
                  key={h}
                  className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide"
                >
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[#E2E8F0]">
            {rolesMeta.map((role) => (
              <tr key={role.id} className="hover:bg-[#F7FAFC] transition-colors">
                <td className="px-5 py-3.5">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-[#041B3C]">{role.name}</span>
                    {role.locked && (
                      <span className="px-2 py-0.5 rounded-full text-xs bg-[#073763] text-white font-semibold">
                        System
                      </span>
                    )}
                  </div>
                </td>
                <td className="px-5 py-3.5 text-[#64748B] text-xs">{role.description}</td>
                <td className="px-5 py-3.5">
                  <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-[#F1F3FF] text-[#073763]">
                    {role.memberCount} users
                  </span>
                </td>
                <td className="px-5 py-3.5">
                  <button
                    disabled={role.locked}
                    className="px-2.5 py-1 text-xs font-medium text-[#073763] border border-[#073763] rounded-lg hover:bg-[#F1F3FF] transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    Edit
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Permission Matrix */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-[#E2E8F0]">
          <h3 className="font-semibold text-[#041B3C]">Permission Matrix</h3>
          <p className="text-xs text-[#64748B] mt-0.5">
            Super Admin permissions are fixed and cannot be changed.
          </p>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm min-w-[900px]">
            <thead className="bg-[#F7FAFC] border-b border-[#E2E8F0]">
              <tr>
                <th className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide sticky left-0 bg-[#F7FAFC] z-10 min-w-[160px]">
                  Role
                </th>
                {MODULES.map((mod) => (
                  <th
                    key={mod}
                    className="px-3 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide whitespace-nowrap"
                  >
                    {mod}
                  </th>
                ))}
                <th className="px-5 py-3 text-left text-xs font-semibold text-[#64748B] uppercase tracking-wide">
                  Action
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {rolesMeta.map((role) => (
                <tr key={role.id} className="hover:bg-[#F7FAFC] transition-colors">
                  <td className="px-5 py-3.5 sticky left-0 bg-white z-10 font-medium text-[#041B3C] border-r border-[#E2E8F0]">
                    {role.name}
                  </td>
                  {MODULES.map((mod) => (
                    <td key={mod} className="px-3 py-3.5">
                      {role.locked ? (
                        <select
                          disabled
                          value="Write"
                          className="text-xs border border-[#E2E8F0] rounded-lg px-2 py-1 bg-[#F7FAFC] text-[#64748B] opacity-50 cursor-not-allowed"
                        >
                          <option value="Write">Write</option>
                        </select>
                      ) : (
                        <select
                          value={permissions[role.id][mod]}
                          onChange={(e) =>
                            handleChange(role.id, mod, e.target.value as Permission)
                          }
                          className="text-xs border border-[#E2E8F0] rounded-lg px-2 py-1 bg-white text-[#041B3C] focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
                        >
                          <option value="None">None</option>
                          <option value="Read">Read</option>
                          <option value="Write">Write</option>
                        </select>
                      )}
                    </td>
                  ))}
                  <td className="px-5 py-3.5">
                    {role.locked ? (
                      <span className="text-xs text-[#64748B]">—</span>
                    ) : (
                      <button
                        onClick={() => handleSave(role.id)}
                        className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                          saved[role.id]
                            ? "bg-green-50 text-green-700 border border-green-200"
                            : "bg-[#073763] text-white hover:bg-[#1769AA]"
                        }`}
                      >
                        {saved[role.id] ? "Saved ✓" : "Save Changes"}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Legend */}
        <div className="px-5 py-3 border-t border-[#E2E8F0] bg-[#F7FAFC] flex items-center gap-4">
          <span className="text-xs text-[#64748B] font-medium">Legend:</span>
          {(["Write", "Read", "None"] as Permission[]).map((p) => (
            <span
              key={p}
              className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${permissionColors[p]}`}
            >
              {p}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}
