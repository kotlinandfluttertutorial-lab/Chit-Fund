import { useState } from "react";
import { UserPlus, X, AlertCircle } from "lucide-react";
import type { AdminUser, AdminRole } from "../types";

// ── Sample data ───────────────────────────────────────────────────────────────

const sampleUsers: AdminUser[] = [
  {
    id: "au-001",
    name: "Rajesh Menon",
    email: "rajesh.menon@chitsmart.in",
    role: "SUPER_ADMIN",
    lastLoginAt: "2026-10-14T09:22:00Z",
    accountStatus: "ACTIVE",
    createdAt: "2025-06-01T00:00:00Z",
  },
  {
    id: "au-002",
    name: "Kavitha Nair",
    email: "kavitha.nair@chitsmart.in",
    role: "FINANCE_MANAGER",
    lastLoginAt: "2026-10-13T16:45:00Z",
    accountStatus: "ACTIVE",
    createdAt: "2025-08-15T00:00:00Z",
  },
  {
    id: "au-003",
    name: "Suresh Patel",
    email: "suresh.patel@chitsmart.in",
    role: "SUPPORT_AGENT",
    lastLoginAt: "2026-10-14T08:10:00Z",
    accountStatus: "ACTIVE",
    createdAt: "2025-09-01T00:00:00Z",
  },
  {
    id: "au-004",
    name: "Deepa Krishnan",
    email: "deepa.krishnan@chitsmart.in",
    role: "AUDITOR",
    lastLoginAt: "2026-09-28T11:05:00Z",
    accountStatus: "INACTIVE",
    createdAt: "2025-09-20T00:00:00Z",
  },
  {
    id: "au-005",
    name: "Arun Sharma",
    email: "arun.sharma@chitsmart.in",
    role: "SUPPORT_AGENT",
    lastLoginAt: "2026-10-12T14:30:00Z",
    accountStatus: "ACTIVE",
    createdAt: "2026-01-10T00:00:00Z",
  },
];

// ── Style helpers ─────────────────────────────────────────────────────────────

const roleChipStyles: Record<AdminRole, string> = {
  SUPER_ADMIN: "bg-[#073763] text-white",
  FINANCE_MANAGER: "bg-teal-50 text-teal-700",
  SUPPORT_AGENT: "bg-blue-50 text-blue-700",
  AUDITOR: "bg-purple-50 text-purple-700",
};

const roleLabels: Record<AdminRole, string> = {
  SUPER_ADMIN: "Super Admin",
  FINANCE_MANAGER: "Finance Manager",
  SUPPORT_AGENT: "Support Agent",
  AUDITOR: "Auditor",
};

const formatLastLogin = (iso?: string) => {
  if (!iso) return "Never";
  return new Date(iso).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

// ── Page ──────────────────────────────────────────────────────────────────────

export default function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUser[]>(sampleUsers);
  const [inviteOpen, setInviteOpen] = useState(false);
  const [deactivateTarget, setDeactivateTarget] = useState<AdminUser | null>(null);

  // Invite form state
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteRole, setInviteRole] = useState<AdminRole>("SUPPORT_AGENT");

  const handleInvite = () => {
    setInviteOpen(false);
    setInviteEmail("");
    setInviteRole("SUPPORT_AGENT");
  };

  const handleDeactivate = (user: AdminUser) => {
    setUsers((prev) =>
      prev.map((u) =>
        u.id === user.id ? { ...u, accountStatus: "INACTIVE" } : u
      )
    );
    setDeactivateTarget(null);
  };

  return (
    <div className="p-6 space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#041B3C]">Admin Users</h2>
          <p className="text-sm text-[#64748B]">{users.length} admin accounts</p>
        </div>
        <button
          onClick={() => setInviteOpen(true)}
          className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors"
        >
          <UserPlus size={14} />
          Invite Admin
        </button>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-[#F7FAFC] border-b border-[#E2E8F0]">
            <tr>
              {["Admin User", "Role", "Last Login", "Status", "Actions"].map((h) => (
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
            {users.map((user) => (
              <tr key={user.id} className="hover:bg-[#F7FAFC] transition-colors">
                {/* Name + email */}
                <td className="px-5 py-3.5">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-[#073763] flex items-center justify-center shrink-0">
                      <span className="text-white text-xs font-semibold">
                        {user.name.charAt(0)}
                      </span>
                    </div>
                    <div>
                      <div className="font-medium text-[#041B3C]">{user.name}</div>
                      <div className="text-xs text-[#64748B]">{user.email}</div>
                    </div>
                  </div>
                </td>

                {/* Role */}
                <td className="px-5 py-3.5">
                  <span
                    className={`px-2.5 py-1 rounded-full text-xs font-semibold ${roleChipStyles[user.role]}`}
                  >
                    {roleLabels[user.role]}
                  </span>
                </td>

                {/* Last login */}
                <td className="px-5 py-3.5 text-xs text-[#64748B]">
                  {formatLastLogin(user.lastLoginAt)}
                </td>

                {/* Status */}
                <td className="px-5 py-3.5">
                  <span
                    className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                      user.accountStatus === "ACTIVE"
                        ? "bg-green-50 text-green-700"
                        : "bg-slate-100 text-slate-500"
                    }`}
                  >
                    {user.accountStatus === "ACTIVE" ? "Active" : "Inactive"}
                  </span>
                </td>

                {/* Actions */}
                <td className="px-5 py-3.5">
                  <div className="flex items-center gap-3 text-xs font-medium">
                    <button className="text-[#073763] hover:underline">Edit Role</button>
                    <button
                      onClick={() => setDeactivateTarget(user)}
                      disabled={user.accountStatus === "INACTIVE"}
                      className="text-[#64748B] hover:underline disabled:opacity-40 disabled:cursor-not-allowed"
                    >
                      Deactivate
                    </button>
                    <button className="text-[#64748B] hover:underline">Reset Password</button>
                    <button className="text-[#64748B] hover:underline">View Activity</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* ── Invite Admin Modal ─────────────────────────────────────────── */}
      {inviteOpen && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
            <div className="flex items-center justify-between mb-5">
              <h3 className="text-lg font-semibold text-[#041B3C]">Invite Admin</h3>
              <button
                onClick={() => setInviteOpen(false)}
                className="p-1 rounded-lg hover:bg-[#F7FAFC] transition-colors"
              >
                <X size={18} className="text-[#64748B]" />
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-[#041B3C] mb-1.5">
                  Email address
                </label>
                <input
                  type="email"
                  value={inviteEmail}
                  onChange={(e) => setInviteEmail(e.target.value)}
                  placeholder="admin@example.com"
                  className="w-full px-3 py-2 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] placeholder:text-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-[#041B3C] mb-1.5">
                  Role
                </label>
                <select
                  value={inviteRole}
                  onChange={(e) => setInviteRole(e.target.value as AdminRole)}
                  className="w-full px-3 py-2 border border-[#E2E8F0] rounded-lg text-sm text-[#041B3C] focus:outline-none focus:ring-2 focus:ring-[#073763]/20 focus:border-[#073763] transition-colors bg-white"
                >
                  <option value="SUPER_ADMIN">Super Admin</option>
                  <option value="FINANCE_MANAGER">Finance Manager</option>
                  <option value="SUPPORT_AGENT">Support Agent</option>
                  <option value="AUDITOR">Auditor</option>
                </select>
              </div>
            </div>

            <div className="flex gap-2 mt-6">
              <button
                onClick={() => setInviteOpen(false)}
                className="flex-1 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleInvite}
                disabled={!inviteEmail}
                className="flex-1 px-3 py-2 rounded-lg bg-[#073763] text-white text-sm font-medium hover:bg-[#1769AA] transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
              >
                Send Invite
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Deactivate Confirmation Dialog ────────────────────────────── */}
      {deactivateTarget && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-sm shadow-xl">
            <div className="flex items-start gap-3 mb-4">
              <div className="w-9 h-9 rounded-full bg-amber-50 flex items-center justify-center shrink-0">
                <AlertCircle size={18} className="text-amber-600" />
              </div>
              <div>
                <h3 className="text-base font-semibold text-[#041B3C]">Deactivate Admin</h3>
                <p className="text-sm text-[#64748B] mt-1">
                  Are you sure you want to deactivate{" "}
                  <span className="font-medium text-[#041B3C]">{deactivateTarget.name}</span>?
                  They will no longer be able to log in.
                </p>
              </div>
            </div>
            <div className="flex gap-2 mt-5">
              <button
                onClick={() => setDeactivateTarget(null)}
                className="flex-1 px-3 py-2 rounded-lg border border-[#E2E8F0] bg-white text-sm font-medium text-[#041B3C] hover:bg-[#F1F3FF] transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDeactivate(deactivateTarget)}
                className="flex-1 px-3 py-2 rounded-lg bg-red-600 text-white text-sm font-medium hover:bg-red-700 transition-colors"
              >
                Deactivate
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
