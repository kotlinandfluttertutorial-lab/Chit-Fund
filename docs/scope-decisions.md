# Scope Decisions — ChitSmart GenAI Chit Fund Platform

**Recorded:** 15/10/2026  
**Status:** Canonical. All design documents, specifications, Jira tickets, and implementation prompts must reflect these decisions.

---

## Decision 1 — Remove Gold Saver 50K

**Decision:** Gold Saver 50K is removed from the platform entirely.

**Rationale:** Gold Saver 50K is not a chit fund product. Chit funds are rotating savings and credit associations regulated under the Chit Funds Act 1982. A gold savings scheme is a distinct financial product with different regulatory requirements, risk profiles, and operational workflows. Mixing the two products in the same platform introduces regulatory complexity and scope dilution. Gold Saver 50K is removed from all UI, data models, APIs, copy, and mock data.

**What is removed:**
- "Gold Saver 50K" group name from all Home dashboard cards, mock data, and sample data.
- "Gold Saver" as a chit group type or category anywhere in the UI.
- Any auction card, installment card, or payout card referencing Gold Saver.
- Any test fixture, seed data, or API response containing Gold Saver.

**What is NOT removed:**
- Chit groups generally. The platform continues to support chit groups by their actual names and values (e.g. "Family Savings 25K", "Community Gold 1L" where "Gold" refers to the prize amount tier, not a gold savings product).
- Any shared component or service that happens to have been illustrated with a Gold Saver example.

**Regulatory note:** If a gold savings product is added in the future, it requires a separate product risk assessment, distinct regulatory filings, and a separate product team decision.

---

## Decision 2 — Defer KYC (Know Your Customer)

**Decision:** KYC functionality is deferred to a future release. It is not part of the current implementation scope.

**Rationale:** KYC involves Aadhaar-based identity verification (regulated under UIDAI), PAN validation, and potential CKYC registry integration — each with separate legal agreements, API onboarding timelines, and compliance review cycles that are not complete. Including incomplete KYC flows creates a false sense of compliance and may expose the platform to regulatory risk. The platform will launch with basic registration (name, mobile, email) and add full KYC in a dedicated compliance release.

**What is deferred:**
- Customer app: KYC Document Upload screen (1.6), KYC Status screen (1.7).
- Customer app: Aadhaar and PAN fields on the Registration screen (1.5). Registration collects name, mobile, and email only in this release.
- Customer app: KYC status badge on the Profile screen (6.1). Profile shows account status only.
- Customer app: "Pending KYC" membership status chip.
- Customer app: KYC category in Help Center FAQ (6.5). Remove the KYC category; keep Payments, Account, Technical.
- Customer app: KYC Update notification type (2.2). Remove this notification type from the current release.
- Admin dashboard: KYC sidebar navigation item.
- Admin dashboard: KYC Pending Queue screen (C.1) and KYC Review Workspace screen (C.2).
- Admin dashboard: KYC status filter column in Member Search (B.1). Replace with "Account Status" (Active / Inactive / Suspended).
- Admin dashboard: KYC status badge and Documents tab in Member Details (B.2). The Documents tab is hidden; the KYC summary in Overview is replaced with "Account Status".
- Admin dashboard: KYC Data step in Member Create/Edit (B.3). The form collects Personal Info, Contact, Bank Accounts, Nominees only.
- Admin dashboard: "Pending KYC" KPI card on Executive Dashboard (A.1). Replace with "Pending Enrollments".
- Admin dashboard: KYC category in Reports Dashboard (J.1). Remove from current release.
- Admin dashboard: AI Document Review tool (K.2). This is exclusively a KYC feature and is deferred with KYC.
- Admin dashboard: KYC Officer role in RBAC (L.1). Defer this role; retain Super Admin, Finance Manager, Support Agent, Auditor.

**What is NOT deferred:**
- Member registration and login (mobile OTP only in this release).
- Bank account linking (penny-drop verification is a payment prerequisite, not a KYC function).
- Nominee capture (part of enrollment, not KYC).
- Document storage infrastructure. The database schema for KYC documents must be designed but the tables should be created as inactive/empty rather than omitted, so the KYC release can migrate data rather than create tables from scratch.
- Security controls and audit logging. These are not KYC-specific.

**Backend and database note (requires separate decision before production):**
- `kyc_documents` table: create the schema in migration but do not expose via API in this release.
- `kyc_status` column on the `members` table: include in the schema with default value `NOT_REQUIRED` for this release. Do not expose the field via the customer API.
- UIDAI and CKYC API integrations: do not implement in this release. No credentials, no SDK, no calls.
- Legal and regulatory verification requirement: **Full KYC compliance under Prevention of Money Laundering Act (PMLA), RBI Master Direction on KYC (2016, as updated), and Chit Funds Act 1982 must be reviewed by a qualified legal and compliance team before the KYC release goes to production.** This document does not constitute compliance advice.

---

## Decision 3 — Defer Bidding and Auction UI

**Decision:** Bidding and auction UI features are deferred to a future release. Core chit group management, installment payments, and financial records are preserved.

**Rationale:** Auctions in chit funds involve real-time competitive bidding, prize disbursement calculations, and dispute resolution workflows. These require robust backend event processing, WebSocket infrastructure, and regulatory clarity on electronic bid submission. Launching without these being production-ready would create broken user journeys. The platform launches with chit group enrollment, installment payment, and financial reporting — the core value proposition — and adds auction functionality in a dedicated release.

**What is deferred:**
- Customer app: Entire "Auctions" bottom navigation tab. The five tabs become: **Home | My Chits | Payments | Assistant | Profile**.
- Customer app: Upcoming Auctions screen (4.1).
- Customer app: Auction Details screen (4.2) — **including** the countdown timer, bid history, and "Place Bid" CTA. The auction date may be shown as informational text on Group Details, but no interactive auction elements.
- Customer app: Bid Submission screen (4.3).
- Customer app: Auction Live Monitoring screen (4.4).
- Customer app: Auction Results screen (4.5).
- Customer app: Prize Payout Status screen (4.6) as a separate screen. Payout status may be shown as a read-only field within Group Member View.
- Customer app Home dashboard (2.1): Remove "Upcoming Auction" card and "Bid in Auction" quick action. Replace quick actions with: Pay Installment | My Statement | Download Receipt | Contact Support.
- Customer app: "Bid history" accordion on Group Member View (3.5). Replace with "Payout History" (read-only record of any prizes received).
- Customer app: "Prize won" field on Active Memberships card (3.4). Replace with "Payout received" (read-only, historical).
- Customer app Group Details (3.2): Remove "Schedule tab auction calendar" entries. Keep installment schedule. Remove auction-specific FAQs.
- Customer app: AI Assistant suggested question "When is my next auction?" (5.1). Replace with "When is my next installment due?".
- Customer app: Notification type "Auction Reminder" (2.2). Remove from current release.
- Admin dashboard: Auctions sidebar navigation item and all auction screens (F.1–F.4).
- Admin dashboard: "Schedule Auction" quick action on Executive Dashboard (A.1). Remove.
- Admin dashboard: "Upcoming Auctions This Week" KPI card (A.1). Replace with "Upcoming Installment Due Dates".
- Admin dashboard: "Next auction" column in Group Performance table (A.1). Remove column.
- Admin dashboard: Auctions tab in Member Details (B.2). Remove tab.
- Admin dashboard: "Auction Rules" section in Create/Edit Chit Group form (D.2). Remove section. Group creation covers name, value, duration, members, commission, and installment configuration only.
- Admin dashboard: Auctions tab in Chit Group Detail View (D.3). Remove tab.
- Admin dashboard: Payout Management screens (I.1–I.2). Payouts in this release are admin-initiated offline; the approval workflow UI is deferred. A read-only payout records table may be included in the Payments section.
- Admin dashboard: Auctions category in Reports Dashboard (J.1). Remove from current release.
- Admin dashboard: Payouts category in Reports Dashboard (J.1). Keep as a read-only report of historical payout records only — no approval workflow.

**What is NOT deferred (preserved):**
- Chit group creation, configuration, and management.
- Installment schedules (dates, amounts, members).
- Installment payment flows (all 4 steps — Summary, Method, Processing, Success/Failure).
- Payment history and account statements.
- Digital receipts.
- Ledger and double-entry accounting records.
- Payment reconciliation.
- Refunds and disputes.
- Financial reports and exports.
- Payout records as read-only historical data (amounts, dates, bank references).

**Backend and database note (requires separate decision before production):**
- `auctions` table: create schema in migration, do not expose via API in this release.
- `bids` table: create schema in migration, do not expose via API in this release.
- `payouts` table: include in this release as read-only records. The approval workflow API endpoints are deferred.
- WebSocket infrastructure for live auction: do not implement in this release.
- Any existing financial records linked to past auctions or payouts must not be deleted. They are historical records.

---

## Summary Table

| Feature Area | Decision | Release |
|---|---|---|
| Gold Saver 50K product | **Removed** — not a chit fund product | Never |
| KYC document upload and review | **Deferred** — regulatory prerequisites not ready | Future compliance release |
| Aadhaar / PAN fields in registration | **Deferred** | Future compliance release |
| Admin KYC review workflow | **Deferred** | Future compliance release |
| AI Document Review (KYC) | **Deferred** with KYC | Future compliance release |
| Auction UI — customer bidding | **Deferred** — infrastructure not ready | Future auctions release |
| Auction UI — live monitoring | **Deferred** | Future auctions release |
| Auction management — admin | **Deferred** | Future auctions release |
| Payout approval workflow | **Deferred** | Future auctions release |
| Core chit group management | **In scope** | Current release |
| Installment payment flow | **In scope** | Current release |
| Payment history and statements | **In scope** | Current release |
| Ledger, reconciliation, receipts | **In scope** | Current release |
| GenAI assistant | **In scope** | Current release |
| Member registration and login | **In scope** (mobile OTP only) | Current release |
| Admin dashboard — finance ops | **In scope** | Current release |
| Reports and exports | **In scope** (collections, members, AI usage) | Current release |
| Role-based access control | **In scope** (4 roles, KYC Officer deferred) | Current release |
