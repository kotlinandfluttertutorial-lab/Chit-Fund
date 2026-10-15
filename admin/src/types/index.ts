// ── Shared domain types ────────────────────────────────────────────────────

export type UUID = string;

export type AccountStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED";

export type MembershipStatus =
  | "PENDING_ENROLLMENT"
  | "ACTIVE"
  | "COMPLETED"
  | "SUSPENDED";

export type ChitGroupStatus = "PENDING" | "ACTIVE" | "COMPLETED" | "CLOSED";

export type PaymentStatus =
  | "PENDING"
  | "PROCESSING"
  | "SUCCESSFUL"
  | "FAILED"
  | "REFUNDED"
  | "VOIDED";

export type PaymentMethod =
  | "UPI"
  | "NET_BANKING"
  | "DEBIT_CARD"
  | "NEFT"
  | "RTGS"
  | "CASH";

export type PayoutStatus =
  | "PENDING"
  | "APPROVED"
  | "PROCESSING"
  | "CREDITED"
  | "FAILED";

// ── Member ────────────────────────────────────────────────────────────────

export interface Member {
  id: UUID;
  fullName: string;
  mobileNumber: string;
  email?: string;
  dateOfBirth?: string; // ISO date
  gender?: "MALE" | "FEMALE" | "OTHER" | "PREFER_NOT_TO_SAY";
  accountStatus: AccountStatus;
  isMobileVerified: boolean;
  preferredLanguage: string;
  enrolledGroupsCount: number;
  lastPaymentDate?: string;
  joinDate: string;
  createdAt: string;
}

// ── Chit Group ─────────────────────────────────────────────────────────────

export interface ChitGroup {
  id: UUID;
  name: string;
  chitValue: number;
  monthlyInstallment: number;
  durationMonths: number;
  maxMembers: number;
  enrolledMembers: number;
  foremanCommissionPct: number;
  startDate: string;
  status: ChitGroupStatus;
  termsDocumentUrl?: string;
  description?: string;
  createdAt: string;
}

// ── Installment ────────────────────────────────────────────────────────────

export interface InstallmentSchedule {
  id: UUID;
  chitGroupId: UUID;
  chitGroupName: string;
  monthNumber: number;
  dueDate: string;
  amountPerMember: number;
  totalExpected: number;
  totalCollected: number;
  collectionRatePct: number;
  status: "UPCOMING" | "DUE" | "PARTIAL" | "COLLECTED" | "OVERDUE";
}

// ── Payment ────────────────────────────────────────────────────────────────

export interface Payment {
  id: UUID;
  memberId: UUID;
  memberName: string;
  chitGroupId: UUID;
  chitGroupName: string;
  installmentMonth: number;
  amount: number;
  lateFee: number;
  totalAmount: number;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;
  gatewayReference?: string;
  bankReference?: string;
  receiptNumber?: string;
  failureReason?: string;
  createdAt: string;
}

// ── Payout (read-only in this release) ────────────────────────────────────

export interface Payout {
  id: UUID;
  memberId: UUID;
  memberName: string;
  chitGroupId: UUID;
  chitGroupName: string;
  monthNumber: number;
  grossAmount: number;
  foremanCommission: number;
  netAmount: number;
  bankReference?: string;
  status: PayoutStatus;
  creditDate?: string;
  createdAt: string;
}

// ── Ledger ─────────────────────────────────────────────────────────────────

export interface LedgerEntry {
  id: UUID;
  chitGroupId: UUID;
  memberId?: UUID;
  memberName?: string;
  entryType:
    | "INSTALLMENT"
    | "LATE_FEE"
    | "PAYOUT"
    | "FOREMAN_COMMISSION"
    | "REFUND"
    | "ADJUSTMENT";
  narration: string;
  debit: number;
  credit: number;
  runningBalance: number;
  createdAt: string;
}

// ── Admin user ─────────────────────────────────────────────────────────────

export type AdminRole =
  | "SUPER_ADMIN"
  | "FINANCE_MANAGER"
  | "SUPPORT_AGENT"
  | "AUDITOR";

export interface AdminUser {
  id: UUID;
  name: string;
  email: string;
  role: AdminRole;
  lastLoginAt?: string;
  accountStatus: AccountStatus;
  createdAt: string;
}

// ── Pagination ─────────────────────────────────────────────────────────────

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

// ── API error ──────────────────────────────────────────────────────────────

export interface ApiError {
  detail: string;
  code?: string;
}
