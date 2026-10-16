import { Routes, Route, Navigate } from "react-router-dom";
import AdminLayout from "./components/layout/AdminLayout";
import DashboardPage from "./pages/Dashboard";
import MemberSearchPage from "./pages/MemberSearch";
import MemberDetailsPage from "./pages/MemberDetails";
import ChitGroupsListPage from "./pages/ChitGroupsList";
import InstallmentSchedulePage from "./pages/InstallmentSchedule";
import PaymentTransactionsPage from "./pages/PaymentTransactions";
import LedgerPage from "./pages/Ledger";
import ReportsPage from "./pages/Reports";
import AIToolsPage from "./pages/AITools";
import SettingsPage from "./pages/Settings";
import LoginPage from "./pages/Login";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<AdminLayout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="members" element={<MemberSearchPage />} />
        <Route path="members/:memberId" element={<MemberDetailsPage />} />
        <Route path="chit-groups" element={<ChitGroupsListPage />} />
        <Route path="installments" element={<InstallmentSchedulePage />} />
        <Route path="payments" element={<PaymentTransactionsPage />} />
        <Route path="ledger" element={<LedgerPage />} />
        <Route path="reports" element={<ReportsPage />} />
        <Route path="ai-tools" element={<AIToolsPage />} />
        <Route path="settings" element={<SettingsPage />} />
        {/* KYC and Auctions routes are DEFERRED — do not add here */}
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
