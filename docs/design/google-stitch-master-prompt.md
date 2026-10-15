# Google Stitch Master Prompt — GenAI Chit Fund Management Platform

> **Usage:** Copy the full prompt block below and paste it into Google Stitch.  
> Generate the product in the four batches described at the end to stay within generation limits.  
> After each batch, export frames and transfer them to Figma before starting the next batch.

> **Scope note (updated 15/10/2026):** Three scope decisions have been applied to this prompt.  
> See `docs/scope-decisions.md` for full rationale.  
> 1. **Gold Saver 50K removed** — not a chit fund product; removed from all screens and mock data.  
> 2. **KYC deferred** — regulatory prerequisites not ready; KYC screens excluded from this release.  
> 3. **Bidding/Auctions deferred** — infrastructure not ready; auction and bid UI excluded from this release.

---

## COPY-READY PROMPT — PASTE INTO GOOGLE STITCH

---

Design a complete, modern Indian fintech product called **"ChitSmart"** — a GenAI-powered Chit Fund Management Platform. The product consists of two separate applications:

1. **ChitSmart Customer App** — native Android application
2. **ChitSmart Admin Dashboard** — responsive web application

This is the **current release scope**. KYC screens, auction screens, and bidding screens are intentionally excluded and will be added in future releases. Do not add them.

---

### DESIGN SYSTEM (apply to every screen)

**Brand palette**

| Token | Hex |
|---|---|
| Primary Navy | `#073763` |
| Secondary Blue | `#1769AA` |
| Teal Accent | `#14B8A6` |
| Background | `#F7FAFC` |
| Main Text | `#172B4D` |
| Secondary Text | `#64748B` |
| Success | `#22C55E` |
| Warning | `#F59E0B` |
| Error | `#EF4444` |
| Surface White | `#FFFFFF` |
| Border/Divider | `#E2E8F0` |

**Typography**
- Font family: Inter (fallback: Roboto, system sans-serif)
- Heading 1: 24 sp / Bold
- Heading 2: 20 sp / Semi-Bold
- Heading 3: 16 sp / Semi-Bold
- Body Large: 16 sp / Regular
- Body Small: 14 sp / Regular
- Caption: 12 sp / Regular
- Label/Button: 14 sp / Medium

**Spacing scale:** 4 dp base unit — use multiples: 4, 8, 12, 16, 20, 24, 32, 40, 48.

**Corner radii:** Cards 12 dp, Buttons 8 dp, Chips/Tags 20 dp, Modals 16 dp.

**Elevation/Shadow:** Subtle — cards use 2 dp elevation; modals use 8 dp.

**Accessibility:** All text/background combinations must pass WCAG AA (4.5:1 body, 3:1 large text). Touch targets minimum 48 × 48 dp.

**Realistic Indian financial data:** Use names like Priya Sharma, Ravi Kumar, Ananya Singh. Use ₹ (INR) for all monetary values. Chit values: ₹25,000 / ₹50,000 / ₹1,00,000 / ₹5,00,000. Use Indian mobile numbers (+91 98765 43210). Use Indian states, cities and pin codes. Dates in DD/MM/YYYY format. Use Hindi/English labels where bilingual UI is shown. Do NOT use "Gold Saver 50K" as a group name. Use realistic chit group names such as "Family Savings 50K", "Community Chit 1L", "Monthly Circle 25K".

**States required for every interactive component:** Default, Hover/Pressed, Loading (skeleton or spinner), Empty, Success, Error, Offline/Disabled, Validation (inline field errors).

---

### APPLICATION 1 — CHITSMART CUSTOMER ANDROID APP

**Canvas size:** 360 × 800 dp (also provide 430 × 932 dp variation for key screens).  
**Navigation pattern:** Bottom navigation bar with five tabs: **Home | My Chits | Payments | Assistant | Profile**.  
**Android design conventions:** Material Design 3, status bar 24 dp, bottom nav 56 dp, top app bar 64 dp.

> **Excluded from this release:** Auctions tab, KYC screens (1.6, 1.7). Do not include them.

---

#### SCREEN GROUP 1 — ONBOARDING AND AUTHENTICATION

**1.1 Splash Screen**
- Full-bleed Primary Navy background.
- Centered ChitSmart logo (teal lotus / coin motif) + wordmark in white.
- Tagline: "Your trusted chit fund partner" in Inter Light, white.
- Loading progress bar at bottom (teal).
- Version number bottom-right.

**1.2 Onboarding Carousel (3 slides)**
- Slide 1: Illustration of group savings, headline "Grow Together", body copy in English and Hindi.
- Slide 2: Illustration of making payments on time, headline "Pay Easily, Track Everything".
- Slide 3: Illustration of AI assistant, headline "AI That Understands You".
- Page dots indicator, Skip and Next CTAs, Get Started primary button on last slide.
- Note: No bidding or auction illustration. No KYC illustration.

**1.3 Mobile Number Login**
- Top app bar: ChitSmart logo, language toggle (EN | हिंदी).
- Headline: "Enter your mobile number".
- +91 country code chip + 10-digit input field with Indian number validation.
- "Send OTP" primary button (Primary Navy fill, white label).
- "New to ChitSmart? Register" link.
- Terms and privacy disclaimer in Caption style.
- Show loading state for OTP dispatch.

**1.4 OTP Verification**
- Headline: "Verify your number", sub-text showing masked number.
- 6-box OTP input row, auto-advance on digit entry.
- Countdown timer: "Resend OTP in 00:45".
- Resend link (enabled after countdown).
- Verify CTA.
- Show error state: wrong OTP (red border, inline message).

**1.5 New User Registration**
- Full name (required), email (optional), date of birth (date picker), gender selector.
- Mobile number (pre-filled from login, non-editable).
- Consent checkbox: "I agree to the Terms and Conditions".
- Register CTA.
- Note: No Aadhaar or PAN fields in this release.

---

#### SCREEN GROUP 2 — HOME DASHBOARD

**2.1 Home — Main Dashboard**
- Top greeting card: "Good morning, Priya 👋" + avatar + notification bell badge.
- Active Memberships summary card: total invested ₹1,20,000, current month installment ₹5,000 due 15/10/2026, Pay Now CTA (teal).
- Quick Actions row (icon + label, 4 items): Pay Installment | My Statement | Download Receipt | Contact Support.
- Recent Transactions list (last 3 entries): date, description, amount, status chip (Paid/Failed/Pending).
- AI Assistant promo banner: "Ask ChitSmart AI anything" with chat bubble icon.
- News/announcements carousel (2 items — e.g. "New chit groups open for October").
- Note: No "Upcoming Auction" card. No "Bid in Auction" quick action.

**2.2 Notifications Screen**
- Grouped by Today / This Week / Earlier.
- Notification types: Payment Due (amber bell), Payment Confirmed (green check), Installment Reminder (blue calendar), Payout Credited (green rupee), System Alert (gray info).
- Each row: icon, title, body snippet, relative time, unread dot.
- Mark All Read, swipe-to-dismiss.
- Note: No KYC Update notification type. No Auction Reminder notification type.

---

#### SCREEN GROUP 3 — MY CHITS

**3.1 Available Chit Groups — Browse**
- Top search bar + filter chips: All | ₹25K | ₹50K | ₹1L | ₹5L | Duration.
- Group cards in a vertical list:
  - Group name (e.g. "Family Savings 50K"), chit value (large), duration badge (e.g. "24 months"), members enrolled (e.g. "18/20"), monthly installment, next installment due date.
  - "Know More" and "Enroll Now" CTAs.
  - "Full" badge when seats are taken.
- Banner: "Refer a friend and earn ₹500".
- Note: No "next auction date" on group cards.

**3.2 Chit Group Details**
- Hero card: group name, chit value (₹50,000), monthly installment (₹2,500), duration (24 months), foreman commission (5%), members enrolled (18/20).
- Tabs: Overview | Schedule | Members | FAQs.
- Overview: summary paragraph, key stats grid (chit value, monthly installment, duration, commission, members, start date).
- Schedule tab: month-by-month installment schedule table — month #, due date, amount per member, status (Paid/Due/Upcoming). No auction calendar entries.
- Members tab: avatar list, anonymised member IDs where privacy applies, member count.
- FAQs tab: expandable Q&A about chit fund mechanics (no auction FAQs in this release).
- Sticky bottom bar: Enroll Now primary CTA.

**3.3 Membership Enrollment**
- Summary card: group selected, monthly installment, total commitment (monthly × duration).
- Nominee details form: name, relationship, mobile.
- Mandate setup: bank account selector or "Add Bank Account", auto-debit authorisation toggle.
- E-sign agreement with scrollable terms.
- Confirm Enrollment CTA.
- Success screen: confetti animation, membership ID displayed, "View My Chit" CTA.

**3.4 My Active Memberships**
- List of enrolled groups with status chips: Active / Completed / Pending Enrollment.
- Each card: group name, chit value, installments paid (e.g. "6/24"), payout received (teal checkmark if received, "—" if not), next due date.
- Tap card → Group Member View.
- Note: No "Pending KYC" status chip.

**3.5 Group Member View**
- Header: group name, my member ID.
- Installment calendar: grid or timeline view, paid (teal), due (amber), upcoming (gray).
- "Pay Now" floating button if installment is due.
- Payout History section: read-only list — if a payout was received, show date, amount, bank reference; otherwise "No payout received yet".
- Note: No "Bid history" accordion. No "Prize won" status.

**3.6 Installment Payment — Flow**

*Step 1: Payment Summary*
- Payable details: Group name, installment month (e.g. "October 2026"), due amount ₹2,500.
- Late fee (if any): ₹50 with explanation.
- Total: ₹2,550.
- Pay CTA (teal).

*Step 2: Select Payment Method*
- Options: UPI (default, highlighted), Net Banking, Debit Card, NEFT/RTGS.
- UPI section: VPA input field + QR code toggle.
- Saved UPI IDs list with radio selection.
- "Add New UPI ID" option.
- Continue CTA.

*Step 3: Processing*
- Full-screen processing animation with teal spinner.
- "Please do not press back or close the app" advisory.
- ChitSmart logo centred.

*Step 4A: Payment Success*
- Large green animated checkmark.
- Confirmation details: amount paid, transaction ID, date/time, group name, installment month.
- Download Receipt CTA (teal outlined), View Statement CTA, Done CTA (Primary Navy, full-width).

*Step 4B: Payment Failure*
- Large red animated X.
- Failure reason (e.g. "Your bank declined this transaction").
- Retry Payment CTA (teal), Try Different Method CTA (outlined), Contact Support link.

**3.7 Payment History and Account Statement**
- Outstanding Balance card at top: ₹0 (green fill) or overdue amount with "Pay Now" CTA (red fill).
- Filter row: date range picker, group selector, status filter (All / Paid / Failed / Pending).
- Transaction list: date, installment month, amount, payment method, status chip (green/red/amber), receipt download icon.
- Download Statement button: generates PDF of selected period.

**3.8 Digital Receipt**
- Receipt card with perforated-edge top border.
- ChitSmart logo + registered office address.
- Receipt number, date, time.
- Member name, group name, installment month and year.
- Amount in numerals and words (e.g. "₹2,500 — Two Thousand Five Hundred Only").
- Payment method, bank/UPI transaction reference, gateway reference.
- "Thank you for your payment" footer.
- Download PDF CTA, Share via WhatsApp CTA, Share via Email CTA.

---

#### SCREEN GROUP 4 — PAYMENTS (TAB)

This tab replaces the former Auctions tab. It provides a consolidated payment centre.

**4.1 Payments Home**
- Active installment dues card: list of groups with outstanding amounts + Pay CTA per group.
- Payment History shortcut: "View all transactions" link.
- Payout History shortcut: "View received payouts" link.
- Quick filters: Due Today | Overdue | Paid This Month.

**4.2 Payout History (read-only)**
- Header: "Payouts Received".
- List of payouts: group name, payout date, amount credited, bank reference, status chip (Credited / Pending / Processing).
- Empty state: "No payouts received yet. Active members may receive a payout when a chit cycle completes."
- Download payout certificate CTA per row (where issued).

---

#### SCREEN GROUP 5 — AI ASSISTANT

**5.1 AI Assistant Home**
- Header: "ChitSmart AI" with teal robot/assistant avatar.
- Language selector: English | हिंदी | தமிழ் | తెలుగు | ಕನ್ನಡ | മലയാളം.
- Suggested questions (3 cards):
  - "What is my outstanding balance?"
  - "When is my next installment due?"
  - "Explain how a chit fund works."
- Recent conversations list.
- Text input + microphone icon + Send button.
- Note: No auction-related suggested questions.

**5.2 AI Chat Interface**
- Chat bubbles: user messages (Primary Navy fill, right-aligned), AI responses (white card with teal left border, left-aligned).
- AI typing indicator (three animated dots).
- Cited policy references shown as teal chip tags: e.g. "Policy: RBI Chit Fund Regulation §4.2".
- Multi-turn conversation context maintained.
- Quick reply chips after AI responses (contextual suggestions).
- Per-message actions: Copy, Share, Helpful / Not Helpful thumbs.

**5.3 Multilingual AI Interaction**
- Show same conversation in Hindi (Devanagari script).
- Language auto-detect toggle.
- Mixed-language (Hinglish) response example.

**5.4 Approved Policy Reference Viewer**
- When user taps a policy chip, open a modal with:
  - Document title, section reference.
  - Relevant excerpt (highlighted in teal).
  - "View Full Document" link.
  - Share and Bookmark actions.

**5.5 Human Support Escalation**
- "Talk to a Support Agent" CTA at bottom of AI chat.
- Transition screen: "Connecting you to an agent…" with animated teal loader.
- Support chat screen: agent avatar, online indicator, conversation thread.
- Option: "Share this AI conversation with agent" toggle.

---

#### SCREEN GROUP 6 — PROFILE AND SETTINGS

**6.1 Profile Screen**
- Avatar circle (photo or initials), name, member ID, account status badge (Active / Inactive).
- Profile menu sections: Personal Info | Bank Accounts | Nominees | Security | Language | Help | About | Logout.
- Note: No KYC status badge. No Documents section in this release.

**6.2 Personal Information Edit**
- Editable fields: full name, email, alternate mobile.
- Non-editable (locked icon with tooltip): primary mobile, date of birth, member ID.
- Save Changes CTA.

**6.3 Bank Accounts**
- List of linked accounts: bank name, account type (Savings/Current), masked account number, linked UPI IDs.
- Primary account indicator (teal star badge).
- Add Bank Account CTA → penny-drop verification flow (enter account number + IFSC → verify ₹1 transfer).
- Remove account with confirmation dialog.

**6.4 Security Settings**
- Change MPIN (current → new → confirm).
- Biometric authentication toggle (fingerprint / face unlock).
- Active Sessions list: device name, last active, Revoke button.
- Login History: last 10 logins with device, date, status.

**6.5 Help Center**
- Search bar with suggested queries.
- Categories: Payments | Account | Technical.
- FAQ accordion per category.
- "Still need help?" CTA → AI Assistant or Human Support.
- Note: No KYC category. No Auctions category in this release.

**6.6 Language and Preferences**
- Language selector: flag + native script name + English name.
- Notification preferences: toggles per category (Payment Due, Payment Confirmed, Installment Reminder, System Alerts) × channel (SMS, Push, Email).
- Statement format preference: PDF / Excel.

---

### APPLICATION 2 — CHITSMART ADMIN DASHBOARD (WEB)

**Canvas size:** 1440 × 900 px desktop primary; also 1280 × 800 px and 768 × 1024 px tablet.  
**Layout:** Fixed left sidebar (240 px) + top header bar (64 px) + main content area.  
**Navigation:** Sidebar sections: Dashboard | Members | Chit Groups | Installments | Payments | Ledger | Reports | AI Tools | Settings.

> **Excluded from this release:** KYC sidebar item, Auctions sidebar item, Payouts approval workflow. Do not include them.

---

#### SCREEN GROUP A — EXECUTIVE DASHBOARD

**A.1 Main Executive Dashboard**
- KPI cards row (5 cards):
  - Total Active Members
  - Total Chit Value Under Management
  - Collections This Month
  - Pending Enrollments
  - Overdue Installments
- Each KPI card: metric name, large value, delta % from last month, trend sparkline.
- Collection Analytics chart: stacked bar — collected vs overdue, monthly for last 12 months.
- Group Performance table: group name, chit value, members enrolled, collection rate %, next installment due date, status chip.
- Recent Activity feed: last 20 events with type icons.
- Quick Actions: + New Group | Send Reminder | Export Report.
- Note: No "Pending KYC" KPI. No "Upcoming Auctions" KPI. No "Schedule Auction" quick action.

**A.2 Collection Analytics Deep-Dive**
- Date range picker, group multi-select filter, status filter.
- Line chart: daily collection trend.
- Donut chart: payment method breakdown (UPI / Net Banking / Debit Card / NEFT).
- Heatmap: collection performance by day of week × week of month.
- Table: member-level collection status (name, group, due amount, paid amount, status, last payment date) — sortable.
- Export CSV / Export PDF buttons.

---

#### SCREEN GROUP B — MEMBER MANAGEMENT

**B.1 Member Search and List**
- Global search bar with type-ahead (name, mobile, member ID).
- Filter panel: account status (Active/Inactive/Suspended), enrolled groups, join date range, state/city.
- Members data table: avatar, name, mobile, member ID, account status chip, enrolled groups count, join date, last payment date, actions (View | Edit | Suspend).
- Pagination (50 per page), column sort, column visibility toggle.
- Export selected / all as CSV.
- Note: No KYC status filter column.

**B.2 Member Details**
- Top header: avatar, name, member ID, account status badge (Active/Suspended), action menu (Edit | Suspend | Reset MPIN | Export).
- Tabs: Overview | Memberships | Payments | Activity Log.
- Overview tab: personal info card, bank accounts card, nominees card, account status card.
- Memberships tab: table of enrolled groups — group name, chit value, installments paid/total, payout received, enrollment date, status.
- Payments tab: full transaction history table with date range filter, group filter, status filter.
- Activity Log tab: timestamped event list — login, payment, enrollment, profile changes.
- Note: No KYC status badge. No Documents tab. No Auctions tab.

**B.3 Member Create / Edit**
- Multi-step form: Personal Info → Contact → Bank Accounts → Nominees → Review.
- Each step has inline validation.
- Save as Draft / Submit CTA.
- Note: No KYC Data step.

---

#### SCREEN GROUP C — CHIT GROUP MANAGEMENT

**C.1 Chit Groups List**
- Table: group name, chit value, monthly installment, duration (months), members enrolled/total, start date, status chip (Active / Pending / Completed / Closed), creation date, actions (View | Edit | Archive).
- + Create New Group CTA (teal).
- Filter panel: status, chit value range, start date range.
- Search by group name.

**C.2 Create / Edit Chit Group**
- Form sections:
  - Group Details: name, chit value, duration (months), max members, foreman commission %.
  - Installment Configuration: monthly installment amount (auto-calculated from chit value / members), first installment due date, payment window (days before/after due date).
  - Terms Document: upload PDF of group terms.
- Preview panel: auto-generated installment schedule table showing all months.
- Save Draft / Publish CTA.
- Confirmation dialog for Publish: "This will make the group visible to members. Publish?"
- Note: No Auction Rules section.

**C.3 Chit Group Detail View**
- Header: group name, status badge, chit value, key stats (members, duration, commission).
- Tabs: Overview | Members | Installments | Financials.
- Overview tab: full configuration details, terms document link, creation date.
- Members tab: enrolled members list — name, member ID, enrollment date, installments paid, payout status, status chip.
- Installments tab: full schedule table — all months, due dates, collected totals, variance.
- Financials tab: total collected, total disbursed (payouts), foreman commission earned, outstanding balance.
- Note: No Auctions tab.

---

#### SCREEN GROUP D — INSTALLMENT MANAGEMENT

**D.1 Installment Schedule**
- Group selector dropdown.
- Schedule table: month #, due date, amount per member, total expected, total collected, collection rate %, variance, status chip (Upcoming/Due/Partial/Collected/Overdue).
- Edit due dates inline (with confirmation dialog).
- Send Reminder CTA: triggers bulk push + SMS to all members with outstanding balance.

**D.2 Installment Collection View**
- Selected month picker + group selector.
- Collection progress bar at top: "18/20 members paid — ₹45,000 of ₹50,000 collected".
- Member collection table: name, member ID, due amount, paid amount, payment date, method, late fee, receipt link, status chip.
- Bulk actions: Mark as Paid (offline cash), Send Individual Reminder, Export.
- Download collection summary PDF.

---

#### SCREEN GROUP E — PAYMENT TRANSACTIONS AND RECONCILIATION

**E.1 Payment Transactions**
- Master transaction table: date/time, member name, member ID, group name, installment month, amount, payment method, gateway reference, status chip, receipt link.
- Filters: date range, group, payment method, status (All / Successful / Failed / Pending / Refunded).
- Export CSV.
- Row actions: View Details (slide-in panel), Download Receipt, Initiate Refund.

**E.2 Payment Detail Panel (slide-in)**
- Full transaction details: all fields from table plus bank reference, gateway response code, processing time.
- Member snapshot: name, member ID, group, current outstanding balance.
- Action buttons: Download Receipt, Initiate Refund, Flag for Review.

**E.3 Payment Reconciliation**
- Date selector (single day).
- Three-column comparison: Expected Collections | Gateway Settlements | Bank Credits.
- Summary cards: Total Expected, Total Received, Unmatched Count, Discrepancy Amount.
- Unmatched transactions list: each row has manual match / investigate / escalate actions.
- Reconciliation status badge: Balanced (green) / Discrepancy (amber) / Unreviewed (gray).
- Approve Reconciliation CTA (locks the day's reconciliation record).

**E.4 Refunds and Disputes**
- Open disputes table: member name, group, amount, reason, date raised, assigned admin, status chip (Open / In Review / Resolved / Escalated).
- + Log New Dispute CTA.
- Dispute detail panel: communication thread, file attachments, refund approval form.
- Approve Refund CTA (teal) / Reject Dispute CTA (red) — both require mandatory reason entry.

---

#### SCREEN GROUP F — PAYOUT RECORDS (READ-ONLY)

> Note: Payout approval workflow is deferred. This screen shows historical payout records only.

**F.1 Payout Records**
- Table: member name, group name, cycle month, gross payout, foreman commission deducted, net payout, bank account (masked), transfer date, bank reference, status chip (Credited / Processing / Pending / Failed).
- Filters: date range, group, status.
- Export CSV / PDF.
- Row action: View Details, Download Payout Certificate.

---

#### SCREEN GROUP G — LEDGER AND ACCOUNTS

**G.1 Ledger View**
- Group selector + date range.
- Double-entry ledger table: date, narration, debit (₹), credit (₹), running balance (₹).
- Opening balance card at top.
- Closing balance card at bottom.
- Download as PDF / Excel.

**G.2 Account Statement — Member Level**
- Member search (type-ahead).
- Statement period selector: month picker or custom date range.
- Opening balance card.
- Statement table: date, description (e.g. "Installment — October 2026"), debit, credit, closing balance.
- Send to Member CTA: sends PDF by email or WhatsApp.
- Download PDF.

---

#### SCREEN GROUP H — REPORTS AND EXPORTS

**H.1 Reports Dashboard**
- Report category cards: Collections | Member Activity | Payouts | AI Usage.
- Each card: report name, description, last generated date, Run Report CTA, Schedule Report CTA.

**H.2 Report Builder**
- Report type selector (Collections, Member Activity, Payouts, AI Usage).
- Filter panel: date range, group selector, member selector, status filter.
- Column selector (toggle on/off per column).
- Preview table (first 20 rows).
- Export: PDF, Excel, CSV.
- Schedule: frequency (daily / weekly / monthly), recipient email list.

**H.3 Audit Log**
- Chronological event log: timestamp, admin user (name + role), action type, entity type, entity ID, IP address, before/after diff (collapsed by default, expandable).
- Filters: date range, admin user, action type.
- Non-deletable. Export only.

---

#### SCREEN GROUP I — AI TOOLS

**I.1 AI Collection Summary**
- "Generate Summary" button.
- Output card: natural-language narrative of this month's collections — total collected, top-performing groups, members with overdue installments (anonymised count), trend vs prior month.
- Confidence/quality score badge.
- Copy, Export, Add to Report actions.

**I.2 Natural-Language Reporting**
- Text input with placeholder: "e.g. Show me all members who missed installments for 3 or more consecutive months."
- Submit button.
- AI-generated SQL query display (read-only, labelled "Query used — for transparency").
- Result table with export option.
- Save as Custom Report CTA.

---

#### SCREEN GROUP J — SETTINGS AND PERMISSIONS

**J.1 Role-Based Access Control**
- Roles table: Super Admin | Finance Manager | Support Agent | Auditor (read-only).
- Permission matrix: rows = roles, columns = modules (Dashboard, Members, Chit Groups, Installments, Payments, Ledger, Reports, AI Tools, Settings), cells = Read / Write / None toggles.
- Edit Role CTA, + New Role CTA.
- Note: No KYC Officer role in this release.

**J.2 Admin User Management**
- Admin users table: name, email, role, last login date/time, account status (Active/Inactive).
- Invite Admin CTA (sends email invite).
- Row actions: Edit Role, Deactivate, Reset Password, View Activity.

---

### GENERATION STRATEGY — FOUR BATCHES

Generate in the following order. Complete one batch fully before starting the next. Ensure consistent navigation, colour tokens, component styles and spacing across all batches.

> Reminder: Do NOT generate KYC screens, auction screens, or bidding screens. They are excluded from this release.

#### Batch A — Core Customer Flows (start here)
- 1.3 Mobile Number Login
- 1.4 OTP Verification
- 2.1 Home Dashboard
- 3.4 My Active Memberships
- 3.6 Installment Payment (all 4 steps: Summary, Select Method, Processing, Success, Failure)
- 4.1 Payments Home
- 5.2 AI Chat Interface
- 5.1 AI Assistant Home

#### Batch B — Remaining Customer Screens
- 1.1 Splash Screen
- 1.2 Onboarding Carousel
- 1.5 New User Registration
- 2.2 Notifications
- 3.1 Browse Chit Groups
- 3.2 Chit Group Details
- 3.3 Membership Enrollment
- 3.5 Group Member View
- 3.7 Payment History and Account Statement
- 3.8 Digital Receipt
- 4.2 Payout History
- 5.3 Multilingual AI Interaction
- 5.4 Policy Reference Viewer
- 5.5 Human Support Escalation
- 6.1–6.6 All Profile and Settings screens

#### Batch C — Admin Dashboard Core
- A.1 Executive Dashboard
- A.2 Collection Analytics
- B.1 Member Search and List
- B.2 Member Details
- C.1 Chit Groups List
- C.2 Create / Edit Chit Group
- D.1 Installment Schedule
- E.1 Payment Transactions
- E.3 Payment Reconciliation
- I.1 AI Collection Summary
- I.2 Natural-Language Reporting

#### Batch D — Remaining Admin + Shared Components
- B.3 Member Create / Edit
- C.3 Chit Group Detail View
- D.2 Installment Collection View
- E.2 Payment Detail Panel
- E.4 Refunds and Disputes
- F.1 Payout Records
- G.1 Ledger View
- G.2 Member Account Statement
- H.1 Reports Dashboard
- H.2 Report Builder
- H.3 Audit Log
- J.1 Role-Based Access Control
- J.2 Admin User Management
- Shared: empty states, skeleton loaders, confirmation dialogs, toast notifications, error pages (404, 500, offline), responsive breakpoint variations (1280 px and 768 px) for key admin screens.

---

*End of Google Stitch prompt. Paste the section above the dashed line into Google Stitch. After generation, export all frames and transfer to Figma per `docs/design/stitch-to-figma-handoff.md`.*
