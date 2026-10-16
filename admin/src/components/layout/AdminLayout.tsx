import { Outlet, NavLink, useLocation } from "react-router-dom";
import {
  LayoutDashboard,
  Users,
  Landmark,
  Calendar,
  CreditCard,
  BookOpen,
  BarChart3,
  Bot,
  Settings,
  Bell,
  ChevronRight,
  LogOut,
} from "lucide-react";

// Current release sidebar nav — NO KYC, NO Auctions
const navItems = [
  { path: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { path: "/members", label: "Members", icon: Users },
  { path: "/chit-groups", label: "Chit Groups", icon: Landmark },
  { path: "/installments", label: "Installments", icon: Calendar },
  { path: "/payments", label: "Payments", icon: CreditCard },
  { path: "/ledger", label: "Ledger", icon: BookOpen },
  { path: "/reports", label: "Reports", icon: BarChart3 },
  { path: "/ai-tools", label: "AI Tools", icon: Bot },
  { path: "/settings", label: "Settings", icon: Settings },
];

export default function AdminLayout() {
  const location = useLocation();

  return (
    <div className="flex h-screen bg-[#F7FAFC] overflow-hidden">
      {/* ── Sidebar ─────────────────────────────────────────────────── */}
      <aside className="w-60 bg-[#073763] flex flex-col shrink-0 shadow-xl">
        {/* Logo */}
        <div className="h-16 flex items-center px-5 border-b border-white/10">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-[#14B8A6] flex items-center justify-center">
              <span className="text-white font-bold text-sm">CS</span>
            </div>
            <div>
              <div className="text-white font-semibold text-sm leading-none">ChitSmart</div>
              <div className="text-blue-200 text-xs opacity-70">Admin</div>
            </div>
          </div>
        </div>

        {/* Nav items */}
        <nav className="flex-1 py-4 overflow-y-auto">
          {navItems.map(({ path, label, icon: Icon }) => {
            const isActive = location.pathname === path ||
              (path !== "/dashboard" && location.pathname.startsWith(path));
            return (
              <NavLink
                key={path}
                to={path}
                className={`flex items-center gap-3 mx-2 px-3 py-2.5 rounded-lg mb-0.5 text-sm font-medium transition-all ${
                  isActive
                    ? "bg-white/15 text-white"
                    : "text-blue-200 hover:bg-white/8 hover:text-white"
                }`}
              >
                <Icon size={17} />
                <span>{label}</span>
                {isActive && <ChevronRight size={14} className="ml-auto opacity-60" />}
              </NavLink>
            );
          })}
        </nav>

        {/* Bottom: admin user */}
        <div className="p-4 border-t border-white/10">
          <div className="flex items-center gap-3 px-2">
            <div className="w-8 h-8 rounded-full bg-[#14B8A6] flex items-center justify-center shrink-0">
              <span className="text-white font-semibold text-sm">A</span>
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-white text-sm font-medium truncate">Admin User</div>
              <div className="text-blue-200 text-xs opacity-70 truncate">Super Admin</div>
            </div>
            <button className="text-blue-200 hover:text-white transition-colors">
              <LogOut size={15} />
            </button>
          </div>
        </div>
      </aside>

      {/* ── Main content ─────────────────────────────────────────────── */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top header */}
        <header className="h-16 bg-white border-b border-[#E2E8F0] flex items-center justify-between px-6 shrink-0 shadow-sm">
          <div>
            <PageTitle pathname={location.pathname} />
          </div>
          <div className="flex items-center gap-3">
            <button className="relative p-2 rounded-full hover:bg-[#F1F3FF] transition-colors">
              <Bell size={18} className="text-[#64748B]" />
              <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
            </button>
            <div className="text-sm text-[#64748B]">
              {new Date().toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })}
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function PageTitle({ pathname }: { pathname: string }) {
  const titles: Record<string, string> = {
    "/dashboard": "Executive Dashboard",
    "/members": "Members",
    "/chit-groups": "Chit Groups",
    "/installments": "Installments",
    "/payments": "Payments",
    "/ledger": "Ledger",
    "/reports": "Reports",
    "/ai-tools": "AI Tools",
    "/settings": "Settings",
  };
  const key = Object.keys(titles).find((k) => pathname === k || (k !== "/dashboard" && pathname.startsWith(k)));
  return <h1 className="text-lg font-semibold text-[#041B3C]">{key ? titles[key] : "ChitSmart Admin"}</h1>;
}
