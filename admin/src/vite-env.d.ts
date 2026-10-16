/// <reference types="vite/client" />

// lucide-react ships its own types since v0.263 — this fallback covers
// older installs that lack bundled declarations
declare module "lucide-react" {
  import type { ComponentType, SVGAttributes } from "react";
  export interface LucideProps extends SVGAttributes<SVGElement> {
    size?: number | string;
    strokeWidth?: number | string;
    absoluteStrokeWidth?: boolean;
  }
  export type LucideIcon = ComponentType<LucideProps>;
  // Named icon exports
  export const LayoutDashboard: LucideIcon;
  export const Users: LucideIcon;
  export const Landmark: LucideIcon;
  export const Calendar: LucideIcon;
  export const CreditCard: LucideIcon;
  export const BookOpen: LucideIcon;
  export const BarChart3: LucideIcon;
  export const Bot: LucideIcon;
  export const Settings: LucideIcon;
  export const Bell: LucideIcon;
  export const ChevronRight: LucideIcon;
  export const ChevronLeft: LucideIcon;
  export const LogOut: LucideIcon;
  export const ArrowLeft: LucideIcon;
  export const ArrowUpDown: LucideIcon;
  export const Plus: LucideIcon;
  export const Send: LucideIcon;
  export const Download: LucideIcon;
  export const Search: LucideIcon;
  export const Filter: LucideIcon;
  export const UserPlus: LucideIcon;
  export const TrendingUp: LucideIcon;
  export const TrendingDown: LucideIcon;
  export const IndianRupee: LucideIcon;
  export const AlertCircle: LucideIcon;
  export const Sparkles: LucideIcon;
  export const Database: LucideIcon;
  export const Mail: LucideIcon;
  export const Phone: LucideIcon;
  export const Edit: LucideIcon;
  export const Lock: LucideIcon;
  export const Receipt: LucideIcon;
}
