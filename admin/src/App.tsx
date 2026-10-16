import { Routes, Route, Navigate } from "react-router-dom";
import AdminLayout from "./components/layout/AdminLayout";
import DashboardPage from "./pages/Dashboard";
import MemberSearchPage from "./pages/MemberSearch";
import MemberDetailsPage from "./pages/MemberDetails";
import MemberEditPage from "./pages/MemberEdit";
import ChitGroupsListPage from "./pages/ChitGroupsList";
import ChitGroupDetailPage from "./pages/ChitGroupDetail";
import ChitGroupEditPage from "./pages/ChitGroupEdit";
import InstallmentSchedulePage from "./pages/InstallmentSchedule";
import InstallmentCollectionPage from "./pages/InstallmentCollection";
import PaymentTransactionsPage from "./pages/PaymentTransactions";
import ReconciliationPage from "./pages/Reconciliation";
import RefundsPage from "./pages/Refunds";
import LedgerPage from "./pages/Ledger";
import AccountStatementPage from "./pages/AccountStatement";
import CollectionAnalyticsPage from "./pages/CollectionAnalytics";
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

        {/* Members */}
        <Route path="members" element={<MemberSearchPage />} />
        <Route path="members/new" element={<MemberEditPage />} />
        <Route path="members/:memberId" element={<MemberDetailsPage />} />
        <Route path="members/:memberId/edit" element={<MemberEditPage />} />

        {/* Chit Groups */}
        <Route path="chit-groups" element={<ChitGroupsListPage />} />
        <Route path="chit-groups/new" element={<ChitGroupEditPage />} />
        <Route path="chit-groups/:id" element={<ChitGroupDetailPage />} />
        <Route path="chit-groups/:id/edit" element={<ChitGroupEditPage />} />

        {/* Installments */}
        <Route path="installments" element={<InstallmentSchedulePage />} />
        <Route path="installments/collection" element={<InstallmentCollectionPage />} />

        {/* Payments */}
        <Route path="payments" element={<PaymentTransactionsPage />} />
        <Route path="payments/reconciliation" element={<ReconciliationPage />} />
        <Route path="payments/refunds" element={<RefundsPage />} />

        {/* Ledger */}
        <Route path="ledger" element={<LedgerPage />} />
        <Route path="ledger/account-statements" element={<AccountStatementPage />} />

        {/* Reports */}
        <Route path="reports" element={<ReportsPage />} />
        <Route path="reports/analytics" element={<CollectionAnalyticsPage />} />

        {/* Other */}
        <Route path="ai-tools" element={<AIToolsPage />} />
        <Route path="settings" element={<SettingsPage />} />

        {/* KYC and Auctions routes are DEFERRED — do not add here */}
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
