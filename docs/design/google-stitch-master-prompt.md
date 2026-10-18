# Google Stitch Master Prompt — GenAI Chit Fund Management Platform

> **Purpose:** This file is the mandatory Phase 0 deliverable for the ChitSmart AI-SDLC.
> It contains the complete, copy-ready prompt to paste into Google Stitch.
> Google Stitch generation is an **external user action** — copy the prompt block below
> and paste it into [Google Stitch](https://stitch.withgoogle.com/).
> After generation, transfer designs to Figma per `docs/design/stitch-to-figma-handoff.md`,
> then proceed with Figma MCP inspection, specifications, Jira epics, and implementation.

> **Design scope note:** This prompt covers the **full product vision**, including screens
> that are deferred in the current software release (KYC, auctions). Design them now so
> that the visual system is complete and implementation can adopt them when the features
> ship. The `docs/scope-decisions.md` file governs what is built, not what is designed.

> **Do not claim Stitch designs have been generated** unless you have actually completed
> the external generation step. Update `docs/design/design-traceability.csv` with real
> frame IDs and Figma URLs after transfer.

---

## PHASE 0 — EXECUTION BOUNDARY

| Step | Owner | Status |
|---|---|---|
| 1. Kiro generates this file | Kiro | ✅ Complete |
| 2. User copies prompt into Google Stitch and generates designs | User | ⬜ Pending |
| 3. User exports frames and transfers to Figma | User | ⬜ Pending |
| 4. Kiro uses Figma MCP to inspect frames and extract tokens | Kiro | ⬜ Pending |
| 5. Kiro proceeds with specifications, Jira epics, and implementation | Kiro | ⬜ Pending |

---

## COPY-READY PROMPT — PASTE INTO GOOGLE STITCH

---

Design a complete, modern Indian fintech product called **"ChitSmart"** — a GenAI-powered
Chit Fund Management Platform. The product consists of two separate applications:

1. **ChitSmart Customer App** — native Android application
2. **ChitSmart Admin Dashboard** — responsive web application

Generate the full product including all features: authentication, chit group management,
installments, payments, auctions and bidding, KYC, ledger, payouts, reports, and an AI
assistant. Use realistic Indian financial data throughout. Follow the design system,
screen specifications, and generation strategy defined below exactly.

---

### DESIGN SYSTEM (apply to every screen in both applications)

**Brand palette**

| Token | Hex | Usage |
|---|---|---|
| Primary Navy | `#073763` | Primary actions, headers, key UI elements |
| Secondary Blue | `#1769AA` | Secondary actions, links, hover states |
| Teal Accent | `#14B8A6` | Success, CTAs, highlights, AI elements |
| Background | `#F7FAFC` | Page and screen backgrounds |
| Surface White | `#FFFFFF` | Cards, modals, inputs |
| Main Text | `#172B4D` | Headings, body copy |
| Secondary Text | `#64748B` | Labels, captions, placeholders |
| Border / Divider | `#E2E8F0` | Borders, separators |
| Success Green | `#22C55E` | Success states, paid status |
| Warning Amber | `#F59E0B` | Due, overdue, pending states |
| Error Red | `#EF4444` | Failure states, validation errors |

**Typography**

- Font family: Inter (fallback: Roboto, system sans-serif)
- Display / H1: 28 sp Bold — screen titles on Android; page headings on web
- H2: 22 sp Semi-Bold
- H3: 18 sp Semi-Bold
- H4: 16 sp Semi-Bold
- Body Large: 16 sp Regular — primary body copy
- Body Small: 14 sp Regular — secondary body copy, table cells
- Caption: 12 sp Regular — metadata, timestamps, footnotes
- Label / Button: 14 sp Medium — all interactive controls

**Spacing scale:** 4 dp / px base unit. Use multiples: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64.

**Corner radii:** Cards 12 dp, Buttons 8 dp, Input fields 8 dp, Chips / tags 20 dp,
Modals and bottom sheets 16 dp top corners, Avatars 50 % (circle).

**Elevation:** Cards 2 dp shadow, Sticky headers 4 dp, Modals / sheets 8 dp, Floating
action buttons 6 dp.

**Accessibility:** All text/background combinations must pass WCAG AA (4.5 : 1 for body
text, 3 : 1 for large text and UI components). Minimum touch target 48 × 48 dp on Android.

**Realistic Indian financial data — mandatory rules**
- Use these names: Priya Sharma, Ravi Kumar, Ananya Singh, Deepak Patel, Meena Nair,
  Suresh Iyengar, Kavitha Reddy, Arjun Mehta.
- Use ₹ (INR) for all monetary values. Example amounts: ₹25,000 / ₹50,000 / ₹1,00,000 /
  ₹5,00,000.
- Group names: "Family Savings 50K", "Community Chit 1L", "Monthly Circle 25K",
  "Silver Savers 25K", "Premium Circle 5L". Do NOT use "Gold Saver 50K".
- Indian mobile numbers: +91 98765 43210, +91 87654 32109.
- Indian states and cities: Mumbai, Bengaluru, Chennai, Hyderabad, Pune.
- Dates in DD/MM/YYYY format.
- Bank names: SBI, HDFC Bank, ICICI Bank, Axis Bank.
- Use IFSC codes matching realistic bank branches (e.g. HDFC0001234).
- Show Hindi/English bilingual labels on screens that demonstrate multilingual capability.

**States required for every interactive component and screen**
- Default (data loaded and available)
- Hover and Pressed (web: hover; Android: ripple / pressed)
- Loading (skeleton screens for lists and tables; spinner for actions)
- Empty (friendly illustration + helpful message)
- Success (confirmation card or toast)
- Error (inline validation on fields; full-screen for critical failures)
- Offline (banner or screen indicating no connectivity)
- Validation (inline field-level error messages with red border and error icon)

---

### APPLICATION 1 — CHITSMART CUSTOMER ANDROID APP

**Canvas size:** 360 × 800 dp primary. Provide a 430 × 932 dp variant for screens 2.1,
3.6 (Payment), and 5.2 (AI Chat) to demonstrate larger-phone responsiveness.

**Navigation:** Bottom navigation bar (56 dp) with five tabs:
**Home | My Chits | Payments | Assistant | Profile**

Note: Auctions tab is visible in this design. It is deferred in the current software
release but must be designed now.

**Android conventions:** Material Design 3 components. Status bar 24 dp. Top app bar 64 dp.
Use Material 3 colour roles (Primary, Secondary, Tertiary, Surface, Error). Avoid custom
components where a Material 3 component exists.

---

#### SCREEN GROUP 1 — ONBOARDING AND AUTHENTICATION

**1.1 Splash Screen**
- Full-bleed Primary Navy (#073763) background.
- Centred ChitSmart logo: a teal lotus / coin motif with "ChitSmart" wordmark in white.
- Tagline below wordmark: "Your trusted chit fund partner" in Inter Light, white, 16 sp.
- Teal horizontal progress bar at bottom (indeterminate loading).
- Version number "v1.0.0" in caption style, bottom-right, white 50 % opacity.

**1.2 Onboarding Carousel (3 slides)**
- Background: white with a soft teal geometric pattern at top 40 %.
- Slide 1: Illustration — diverse group of people around a table with coins and growth
  chart. Headline: "Grow Together" (H2 Navy). Body: "Join a chit group with people you
  trust and save consistently." Hindi translation below in caption style.
- Slide 2: Illustration — phone with payment success animation and calendar checkmarks.
  Headline: "Pay Easily, Track Everything". Body: "Make payments in seconds and know
  exactly where every rupee goes."
- Slide 3: Illustration — conversational AI interface with chat bubbles and policy
  document references. Headline: "AI That Understands You". Body: "Ask questions in
  Hindi or English. Get answers backed by policy, not guesses."
- Navigation: page dots (3), Skip (top right, text button), Next CTA (teal button).
- Last slide: "Get Started" primary button replaces Next. "Already a member? Sign in" link.

**1.3 Mobile Number Login**
- Top app bar: ChitSmart logo left, language toggle right (EN | हिंदी).
- Headline: "Enter your mobile number" (H2 Navy).
- Sub-text: "We'll send you a one-time verification code."
- Input: "+91" country code chip (non-editable) + 10-digit number field. Keyboard type:
  numeric. Validate format on blur.
- "Send OTP" primary button (Primary Navy fill, white label, disabled until 10 digits
  entered, shows spinner during dispatch).
- "New to ChitSmart? Register" body link below button.
- Terms disclaimer (Caption): "By continuing, you agree to our Terms of Service and
  Privacy Policy."
- Show error state: "Invalid mobile number — please check and try again."

**1.4 OTP Verification**
- Headline: "Verify your number". Sub-text: "OTP sent to +91 987XX XX210".
- OTP input: 6 separate single-digit boxes in a row. Auto-advance on entry. Paste-support.
- Countdown: "Resend OTP in 00:45" (grey, disabled). After 0: "Resend OTP" (teal, enabled).
- "Verify" primary CTA (disabled until 6 digits entered).
- Error state: wrong OTP — red border on all boxes, inline message "Incorrect OTP. 2
  attempts remaining."
- Success transition: brief teal checkmark animation, then navigate to next screen.

**1.5 New User Registration**
- Headline: "Create your account".
- Step indicator: Step 1 of 1 (single-step for this release).
- Form fields:
  - Full Name (required, text input, validate non-empty and no numbers)
  - Email Address (optional, email input, validate format if entered)
  - Date of Birth (required, date picker, must be 18+ years)
  - Gender (required, segmented control: Male | Female | Other | Prefer not to say)
  - Mobile Number (pre-filled from login screen, non-editable, show lock icon)
- Consent section:
  - Checkbox: "I agree to the Terms and Conditions and Privacy Policy" (required)
- "Create Account" primary CTA (disabled until required fields are valid and consent
  checked).
- Note: No Aadhaar or PAN fields. KYC document upload is deferred to a future release.

**1.6 KYC Document Upload** *(design now; deferred in current release)*
- Headline: "Complete your KYC".
- Sub-text: "Required by law. Your documents are encrypted and stored securely."
- Document types: Aadhaar Card, PAN Card.
- Each document row: document type name, upload status chip (Not Uploaded / Uploaded /
  Verified / Rejected), upload button (opens camera or file picker), preview thumbnail
  once uploaded.
- Aadhaar: front and back both required.
- PAN: single document.
- Rejection reason shown in red below the row if status is Rejected.
- "Submit for Verification" CTA (enabled when all required documents uploaded).
- Skip for Now link (where legally permitted — show with disclaimer).

**1.7 KYC Status** *(design now; deferred in current release)*
- Status card: large status icon (hourglass = Pending, checkmark = Verified, X = Rejected).
- Status label: "KYC Pending Review" / "KYC Verified" / "KYC Action Required".
- Explanation paragraph for each state.
- Estimated review time (Pending): "1–2 business days".
- Rejection detail (Rejected): reason text + "Re-upload Documents" CTA.
- What happens next: 3-step explainer (Submit → Review → Verified → Full Access).
- Contact Support link.

---

#### SCREEN GROUP 2 — HOME DASHBOARD

**2.1 Home — Main Dashboard**
- Greeting header card: "Good morning, Priya 👋", member avatar, notification bell with
  badge count.
- Active Memberships summary card (Primary Navy fill, white text): total invested
  ₹1,20,000, active groups count 2, current month installment ₹5,000 due 15/10/2026,
  "Pay Now" teal CTA.
- Quick Actions row (4 items, icon + label, horizontally scrollable if needed):
  Pay Installment | My Statement | Download Receipt | Contact Support.
- Recent Transactions (last 3 items): date, description, amount, status chip
  (Paid = green / Failed = red / Pending = amber).
- AI Assistant promo banner: gradient teal-to-navy, "Ask ChitSmart AI anything →",
  chat bubble icon.
- Announcements carousel (2 items): "New groups open for November" / "Festival offer:
  zero late fees for October."
- Upcoming Auctions teaser (1 card, teal accent): "Family Savings 50K — Auction on
  20/10/2026." View Details link. Note: visible in design; deferred in current release.

**2.2 Notifications Screen**
- Top app bar: "Notifications", Mark All Read CTA (text button).
- Grouped sections: Today | This Week | Earlier.
- Notification row: icon, title (Body Small Bold), body snippet (Caption), relative time.
  Unread dot (teal, 8 dp) on left.
- Notification types and icons:
  - Payment Due: amber bell
  - Payment Confirmed: green checkmark circle
  - Installment Reminder: blue calendar
  - Payout Credited: green rupee coin
  - Auction Reminder: teal hammer *(deferred in release; include in design)*
  - KYC Update: amber shield *(deferred in release; include in design)*
  - System Alert: grey info circle
- Swipe left to dismiss individual notification.
- Empty state: "You're all caught up" illustration.

---

#### SCREEN GROUP 3 — MY CHITS

**3.1 Available Chit Groups — Browse**
- Top bar: search input + filter icon.
- Filter chips (horizontally scrollable): All | ₹25K | ₹50K | ₹1L | ₹5L | Duration 12M |
  Duration 24M.
- Group cards (vertical list):
  - Group name (H3 Navy), chit value (H2 Teal), duration badge pill, members enrolled
    "18/20" with progress bar, monthly installment, next installment date.
  - "Know More" (outlined) and "Enroll Now" (filled teal) CTA buttons.
  - "Full" overlay badge (amber) when seats exhausted.
  - "Next Auction: 20/10/2026" line (teal, small icon) *(deferred in release; include
    in design)*.
- Referral banner: "Refer a friend and earn ₹500" — teal background, share icon.

**3.2 Chit Group Details**
- Hero section (Primary Navy): group name (H2 white), chit value (Display white), teal
  accent bar.
- Key stats row: Monthly Installment | Duration | Members | Commission.
- Tabs: Overview | Schedule | Members | Auctions | FAQs.
- Overview tab: full description, key stats grid, terms document download link.
- Schedule tab: month-by-month table — month #, due date, amount per member, status chip.
  Below the installment table, show an auction calendar section *(deferred in release;
  design the section placeholder)*.
- Members tab: avatar grid (anonymised member IDs), enrolled count vs capacity.
- Auctions tab: auction schedule list — auction month, estimated prize pool, type
  (Open/Reserve price), status *(deferred in release; design the empty state)*.
- FAQs tab: expandable accordion Q&A.
- Sticky bottom bar: "Enroll Now" primary CTA.

**3.3 Membership Enrollment**
- Step indicator: 3 steps — Details | Mandate | Confirm.
- Step 1 Details: group summary card, nominee form (name, relationship, mobile).
- Step 2 Mandate: bank account selector, auto-debit authorisation toggle, NACH mandate
  explanation, "Add Bank Account" link.
- Step 3 Confirm: full summary card (group, monthly installment, total commitment,
  nominee, mandate account), e-sign terms scrollable area, Confirm Enrollment CTA.
- Success screen: confetti animation, membership ID displayed, "View My Chit" CTA.

**3.4 My Active Memberships**
- Header: "My Chits".
- Status filter chips: All | Active | Completed | Pending Enrollment.
- Membership cards (vertical list):
  - Group name, chit value, progress bar "6/24 installments paid".
  - Next due date + amount.
  - Payout status: "Payout received ✓" (teal) or "—".
  - KYC status badge: "KYC Required" (amber) or "KYC Verified" (green) *(design;
    deferred in release)*.
  - Status chip: Active / Completed / Pending Enrollment / Suspended.

**3.5 Group Member View**
- Header: group name, my member ID.
- Installment calendar: grid (5 columns × N rows). Paid = teal fill, Due = amber fill,
  Upcoming = grey outline. Tap cell → see installment detail sheet.
- "Pay Now" floating action button (teal) when installment is due.
- Payout History section (read-only): date, amount, bank reference, status. Empty state
  if no payout yet.
- Auction History section: list of auctions I participated in, bid submitted, result
  *(deferred in release; design the section)*.

**3.6 Installment Payment — Complete Flow**

*Step 1: Payment Summary*
- Group name, installment month (October 2026), due amount ₹2,500.
- Late fee row (amber, conditional): ₹50 + "Late fee applies after due date" tooltip.
- Total: ₹2,550 (H2 Navy).
- "Pay ₹2,550" primary CTA.

*Step 2: Select Payment Method*
- Section header: "Choose payment method".
- UPI row (default selected, teal radio): saved UPI IDs list with radio buttons.
  "Add New UPI ID" option. QR code toggle.
- Net Banking row: bank search dropdown.
- Debit Card row: card number, expiry, CVV fields (masked).
- NEFT / RTGS row: bank details display, reference instructions.
- "Continue" primary CTA.

*Step 3: Processing*
- Full-screen navy background. Centred: ChitSmart logo, teal circular spinner,
  "Processing your payment…" body text.
- "Please do not close the app or press back" advisory in caption style.

*Step 4A: Payment Success*
- Large animated green checkmark (Lottie animation).
- "Payment Successful!" H2 navy.
- Detail card: Amount ₹2,550, Transaction ID CS-TXN-20261015-001, Date 15/10/2026
  11:42 AM, Group "Family Savings 50K", Installment Month October 2026.
- CTAs: "Download Receipt" (teal outlined), "View Statement" (text), "Done" (Primary
  Navy, full-width).

*Step 4B: Payment Failure*
- Large animated red X.
- "Payment Failed" H2 error red.
- Reason card: "Your bank declined this transaction. Error code: BANK_DECLINED_01".
- CTAs: "Retry Payment" (teal), "Try Different Method" (outlined), "Contact Support"
  (text link).

**3.7 Payment History and Account Statement**
- Outstanding Balance card: ₹0 (teal fill) or overdue amount (red fill with "Pay Now").
- Filter bar: date range picker | group dropdown | status chips (All/Paid/Failed/Pending).
- Transaction list: date, group name, installment month, amount, method icon, status chip,
  receipt download icon.
- "Download Statement (PDF)" button at bottom.

**3.8 Digital Receipt**
- Perforated card border at top (dashed line with scissors icon).
- ChitSmart logo + "Registered under the Chit Funds Act, 1982" footer.
- Receipt #: CS-RCP-20261015-001, Date 15/10/2026.
- Member: Priya Sharma | Member ID: CS-MBR-001.
- Group: Family Savings 50K | Installment: October 2026.
- Amount: ₹2,500 — "Two Thousand Five Hundred Only".
- Payment method: UPI | UPI Ref: 202610151142001 | Gateway Ref: GW-20261015-XYZ.
- "Thank you for your payment" footer in teal italic.
- CTAs: "Download PDF" (Primary Navy), "Share via WhatsApp" (green), "Share via Email"
  (blue outlined).

---

#### SCREEN GROUP 4 — PAYMENTS TAB

**4.1 Payments Home**
- Section: "Outstanding Installments" — list of groups with due amounts + "Pay" CTA
  per row. Empty state if nothing is due.
- Section: "Payment History" shortcut card — last 3 transactions preview + "View All" →.
- Section: "Payout History" shortcut card — last received payout + "View All" →.
- Quick filters: Due Today | Overdue | Paid This Month.

**4.2 Payout History (read-only)**
- Header: "Payouts Received".
- Payout cards: group name, payout month, gross amount, commission deducted, net amount
  credited, bank account (last 4 digits), transfer date, bank reference, status chip
  (Credited = green / Processing = amber / Pending = grey).
- Empty state: illustration + "No payouts received yet. Payouts are credited after a
  chit cycle completes."
- "Download Payout Certificate" link per payout row (where issued).

---

#### SCREEN GROUP 5 — AUCTIONS TAB *(design now; deferred in current release)*

**5.1 Auctions Home**
- Header: "Auctions".
- Upcoming Auctions section: cards with group name, auction date, estimated prize pool,
  type (Open / Reserve price), "My Eligibility" chip (Eligible = teal / Not Eligible =
  grey), "View Details" CTA.
- Past Auctions section: cards with result, my bid amount, whether I won.
- Empty state (no upcoming auctions): "No auctions scheduled yet. Check back closer to
  your chit cycle date."

**5.2 Auction Details**
- Group name and auction date (H2, navy).
- Auction type, prize pool estimate, current highest bid (masked for privacy), time
  remaining countdown.
- Rules accordion: prize pool calculation, bidding window, eligibility, result criteria.
- My Status card: Eligible / Not Eligible with reason.
- "Submit Bid" primary CTA (teal, enabled only when eligible and window open).
- Note: No live bid feed or WebSocket UI — show "Results announced after bidding closes."

**5.3 Bid Submission**
- Group name and auction context.
- Bid amount input (numeric, with ₹ prefix, min/max validation and guidelines shown).
- Bidding rules summary.
- Confirmation checkbox: "I confirm this bid is final and cannot be withdrawn."
- "Submit Bid" CTA.
- Success state: bid submitted confirmation card with bid reference number.
- Error states: below minimum, above maximum, already bid, window closed.

**5.4 Bid History**
- My bids list per group: auction month, bid amount, submitted time, status (Submitted /
  Won / Not Won / Cancelled).
- Won badge (teal star) for successful bids.

**5.5 Auction Results**
- Result card: "You won the auction!" (teal confetti) or "Better luck next time."
- For winner: prize pool breakdown (gross payout, commission, net credited), bank account
  to receive funds, expected credit date.
- For non-winner: highest winning bid amount (anonymised), number of bids submitted.

**5.6 Prize Payout Status**
- Payout status tracker (horizontal stepper): Auction Won → Payout Approved → Funds
  Transferred → Credited.
- Active step highlighted teal, completed steps green, pending steps grey.
- Estimated date for each step.
- "Download Payout Certificate" CTA (available once Credited).

---

#### SCREEN GROUP 6 — AI ASSISTANT

**6.1 AI Assistant Home**
- Header: "ChitSmart AI" (H2 navy), teal robot / assistant avatar (40 dp).
- Language selector pills: English | हिंदी | தமிழ் | తెలుగు | ಕನ್ನಡ | മലയാളം.
- Suggested questions (3 tappable cards with teal left border):
  - "What is my outstanding balance?"
  - "When is my next installment due?"
  - "Explain how a chit fund works."
- Recent conversations list (3 items): last message snippet, timestamp.
- Input bar at bottom: text field ("Ask me anything…"), microphone icon, send button (teal).

**6.2 AI Chat Interface**
- Chat thread: user messages (Primary Navy bubble, right-aligned), AI responses (white
  card, teal left border, left-aligned, robot avatar top-left).
- AI typing indicator: 3 animated dots in teal.
- Policy citation chips on AI responses: "Policy: Chit Funds Act §4" (teal chip, tappable
  → opens policy viewer).
- Quick reply chips after AI responses (contextual, e.g. "Show my balance" / "Pay now"
  / "Talk to support").
- Per-message actions (long-press menu): Copy | Share | 👍 Helpful | 👎 Not helpful.
- Keyboard pushes input bar up; messages scroll.

**6.3 Multilingual AI Interaction**
- Same conversation thread shown switching between English and Hindi mid-conversation.
- Hindi messages in Devanagari script in the user bubble.
- AI response in matching language (Hindi / Devanagari).
- Language auto-detect badge on AI message: "Responding in Hindi" (teal tag).
- Hinglish example: user "Mera next payment kab hai?", AI responds in Hinglish.

**6.4 Approved Policy Reference Viewer**
- Modal (bottom sheet, 80 % height).
- Title: document name (e.g. "Chit Funds Act, 1982"), section reference "§ 4.2".
- Excerpt text with highlighted relevant passage (teal background highlight).
- "View Full Document" text button.
- Share and Bookmark icons.
- Dismiss handle at top.

**6.5 Human Support Escalation**
- "Talk to a Support Agent" CTA at bottom of AI chat (outlined, with headphones icon).
- Transition screen: teal animated loader, "Connecting you to an agent…".
- Support chat: agent avatar with "Support Agent" badge, online indicator (green dot).
- Toggle: "Share this AI conversation with agent" (on by default).
- Chat input identical to AI chat.

---

#### SCREEN GROUP 7 — PROFILE AND SETTINGS

**7.1 Profile Screen**
- Profile header: avatar (80 dp circle, photo or navy initials), full name (H2),
  member ID (caption), account status badge (Active = teal / Inactive = grey /
  Suspended = red).
- KYC status badge row: "KYC Verified ✓" (green chip) or "KYC Required" (amber chip
  with right-arrow) *(design; deferred in release)*.
- Profile menu list sections:
  - Account: Personal Info | Bank Accounts | Nominees | Documents *(design; deferred)*.
  - Security: Security Settings.
  - Preferences: Language & Notifications.
  - Support: Help Center.
  - Legal: About ChitSmart | Terms & Privacy.
  - Logout (red text).

**7.2 Personal Information Edit**
- Editable: full name, email, alternate mobile.
- Non-editable (lock icon + "Cannot be changed" tooltip): primary mobile, date of birth,
  member ID.
- "Save Changes" primary CTA.
- Unsaved changes dialog on back navigation.

**7.3 Bank Accounts**
- Linked accounts list: bank logo, name, account type, masked "•••• 4321", linked UPI
  IDs list. Primary account teal star badge.
- "Add Bank Account" outlined CTA → penny-drop flow (enter account number + IFSC code →
  initiate ₹1 verification → confirm).
- Remove account (trash icon) → confirmation dialog "Remove this bank account?".

**7.4 Security Settings**
- Change MPIN: current MPIN → new MPIN (6 digits) → confirm new MPIN.
- Biometric toggle: "Use fingerprint or face unlock" (switch).
- Active Sessions: device name, OS, last active, "Revoke" text button.
- Login History: last 10 entries — device, date/time, IP city, status (Success/Failed).

**7.5 Help Center**
- Search bar: "Search for help…" with type-ahead suggestions.
- Categories: Payments | Account | Technical | Auctions | KYC.
- FAQ accordion per category (3–5 questions each).
- "Still need help?" card: "Chat with AI" (teal) | "Talk to Support" (outlined).

**7.6 Language and Notification Preferences**
- Language selector: list of supported languages with flag, native script name, English
  name. Active language has teal checkmark.
- Notification preferences grid: rows = notification type (Payment Due, Confirmed,
  Reminder, Payout, Auction, System), columns = channel (Push, SMS, Email). Each cell:
  toggle switch.
- Statement format preference: PDF | Excel segmented control.

---

### APPLICATION 2 — CHITSMART ADMIN DASHBOARD (WEB)

**Canvas size:** 1440 × 900 px (primary). Also design 1280 × 800 px and 768 × 1024 px
variants for Batch D.

**Layout:** Fixed left sidebar 240 px + fixed top header bar 64 px + scrollable main
content area. Sidebar collapses to 64 px (icons only) at ≤ 1280 px.

**Sidebar navigation sections (with icons):**
Dashboard | Members | Chit Groups | Installments | Payments | Ledger | Reports |
AI Tools | Settings

**Note:** KYC and Auctions sidebar items must be designed even though they are deferred
in the current release. Include them as separate sidebar items with a "Coming Soon" chip
until activated.

**Top header bar:** ChitSmart logo (left), breadcrumb trail (centre), search (right),
notification bell, admin avatar + name + role badge.

**Admin role badge colours:**
- Super Admin: Primary Navy
- Finance Manager: Secondary Blue
- Support Agent: Teal
- Auditor: Grey
- KYC Officer: Amber *(design; deferred in release)*

---

#### SCREEN GROUP A — EXECUTIVE DASHBOARD

**A.1 Main Executive Dashboard**
- KPI cards row (6 cards, 3 per row on 1440 px):
  - Total Active Members (count, +3 delta this month)
  - Total Chit Value Under Management (₹ sum, trend arrow)
  - Collections This Month (₹ sum, % of target)
  - Pending Enrollments (count)
  - Overdue Installments (count, red badge)
  - Pending KYC Reviews (count, amber badge) *(design; deferred in release)*
- Collection Analytics chart: stacked bar — collected (teal) vs overdue (amber) vs
  outstanding (grey), monthly for last 12 months. X-axis: month labels. Y-axis: ₹.
- Group Performance table: group name, chit value, members, collection rate %, next
  due date, status chip. Sortable columns. 10 rows.
- Recent Activity feed (right column, 320 px): event icon, description, timestamp,
  admin name. Last 20 events.
- Quick Actions row: + New Group | Send Bulk Reminder | Export Report |
  Schedule Auction *(deferred)* | Review KYC Queue *(deferred)*.

**A.2 Collection Analytics Deep-Dive**
- Page header: "Collection Analytics". Date range picker, group multi-select, status filter.
- Line chart: daily collection trend (last 30 days). Teal line, amber line for target.
- Donut chart (right): payment method split — UPI / Net Banking / Debit Card / NEFT.
  Legend shows ₹ and % for each segment.
- Heatmap calendar: collection performance by day (greener = higher collection %).
  X = day of week, Y = week number.
- Detailed table: member name, group, due amount, paid amount, status chip, last payment
  date, action "Send Reminder". Sortable. Paginated (50 per page). Export CSV/PDF.

---

#### SCREEN GROUP B — MEMBER MANAGEMENT

**B.1 Member Search and List**
- Page header: "Members". + Add Member CTA (teal).
- Search bar (full-width): name, mobile, or member ID with type-ahead.
- Filter panel (collapsible, left): account status, enrolled groups, join date range,
  state/city, KYC status *(design; deferred)*.
- Data table: avatar, name, mobile, member ID, account status chip, enrolled groups
  count, join date, last payment date, KYC status chip *(deferred)*, actions
  (View | Edit | Suspend).
- Pagination: 50 per page. Column sort. Column visibility toggle.
- Bulk actions (checkbox selection): Export selected | Send reminder | Deactivate.

**B.2 Member Details**
- Top header: avatar (64 px), name (H2), member ID, account status badge, KYC status
  badge *(deferred)*, action menu (Edit | Suspend | Reset MPIN | Export | Send Message).
- Tabs: Overview | Memberships | Payments | KYC *(deferred)* | Activity Log.
- Overview tab: personal info card (name, mobile, email, DoB, gender, join date),
  bank accounts card, nominees card, account status card.
- Memberships tab: table — group name, chit value, installments paid/total, payout
  received (Y/N), enrollment date, status chip, "View Group" link.
- Payments tab: transaction table with date range filter, group filter, status filter.
  Export CSV.
- KYC tab *(deferred)*: document status list, reviewer notes field, approve/reject
  buttons — design with "Coming Soon" overlay.
- Activity Log tab: timestamped events — login, payment, enrollment, profile change,
  document upload.

**B.3 Member Create / Edit**
- Multi-step form (horizontal step bar): 1 Personal Info → 2 Contact → 3 Bank Accounts
  → 4 Nominees → 5 KYC Data *(deferred, greyed out)* → 6 Review.
- Each step: card form with inline validation. Previous / Save Draft / Next buttons.
- Step 6 Review: read-only summary cards for all sections. Submit button.
- Confirmation modal: "Create member account for Priya Sharma? This will send a welcome
  SMS to +91 98765 43210."

---

#### SCREEN GROUP C — CHIT GROUP MANAGEMENT

**C.1 Chit Groups List**
- Page header: "Chit Groups". + Create New Group CTA (teal).
- Search bar. Filter panel: status (Active/Pending/Completed/Closed), chit value range,
  start date range.
- Data table: group name, chit value, monthly installment, duration, members
  enrolled/total, start date, status chip, creation date, actions (View | Edit | Archive).
- Status chip colours: Active = teal, Pending = amber, Completed = green, Closed = grey.

**C.2 Create / Edit Chit Group**
- Form layout: two-column (form left, live preview right at 1440 px).
- Form sections:
  - Group Details: name (text), chit value (₹ input), duration (months, number),
    max members (number), foreman commission % (slider 0–10 %).
  - Installment Configuration: monthly installment (auto-calculated = chit value ÷
    members, shown read-only with explanation), first installment due date (date picker),
    payment window days (number, before/after).
  - Auction Rules: auction type (Open / Reserve price), bidding window hours, minimum
    bid (₹), reserve price if applicable *(include but mark as Coming Soon)*.
  - Terms Document: PDF upload dropzone.
- Right preview panel: auto-generated installment schedule table (month, due date,
  per-member amount, total). Updates live as form values change.
- Save Draft / Publish CTA. Confirmation modal for Publish.

**C.3 Chit Group Detail View**
- Header: group name (H2), status badge, chit value, key stats bar (members, duration,
  commission, start date, end date).
- Tabs: Overview | Members | Installments | Auctions *(deferred)* | Financials.
- Overview tab: configuration details card, terms document link, admin notes.
- Members tab: enrolled members table with avatar, name, member ID, enrollment date,
  installments paid, payout status, status chip.
- Installments tab: full schedule table — month #, due date, expected total, collected
  total, collection rate %, variance ₹, status chip.
- Auctions tab *(deferred)*: auction schedule with status. Design with Coming Soon overlay.
- Financials tab: summary cards (total collected, total disbursed, commission earned,
  net balance). Running totals bar chart by month.

---

#### SCREEN GROUP D — INSTALLMENT MANAGEMENT

**D.1 Installment Schedule**
- Page header: "Installments". Group selector dropdown.
- Schedule table: month #, due date, per-member amount, total expected ₹, total
  collected ₹, collection rate %, variance ₹, status chip.
- Inline date edit: click due date → date picker popover → confirm. Change logged in
  audit trail.
- Send Reminder CTA (teal, per row): shows confirmation "Send reminder to all members
  with outstanding balance for this month?".

**D.2 Installment Collection View**
- Group selector + month picker.
- Collection progress bar: "18/20 members paid — ₹45,000 of ₹50,000 (90%)". Teal fill.
- Member collection table: name, member ID, due amount, paid amount, payment date,
  method, late fee, receipt link, status chip.
- Bulk actions row (checkbox select): Mark as Paid (offline) | Send Individual Reminder |
  Export.
- "Mark as Paid (Cash/Offline)" modal: confirm member, amount, date, reference note.
- "Download Collection Summary PDF" button.

---

#### SCREEN GROUP E — PAYMENT TRANSACTIONS AND RECONCILIATION

**E.1 Payment Transactions**
- Page header: "Payments". Date range, group, method, status filters.
- Master table: date/time, member name, member ID, group, installment month, amount,
  method icon, gateway reference, status chip (Successful = green / Failed = red /
  Pending = amber / Refunded = blue), receipt link.
- Row actions: View Details (opens slide-in panel), Download Receipt, Initiate Refund.
- Export CSV button.

**E.2 Payment Detail Slide-In Panel**
- Panel slides in from right (480 px wide).
- All transaction fields. Member snapshot card (name, member ID, group, outstanding
  balance). Timeline: payment initiated → gateway sent → bank response → status updated.
- Action buttons: Download Receipt | Initiate Refund | Flag for Review.
- Close (×) at top right.

**E.3 Payment Reconciliation**
- Page header: "Reconciliation". Date selector (single day, calendar picker).
- Three-column summary: Expected Collections | Gateway Settlements | Bank Credits.
  Each column: total ₹, count, delta vs expected.
- Summary KPI cards: Total Expected, Total Received, Unmatched Count, Discrepancy ₹.
- Reconciliation status badge: Balanced (green) / Discrepancy (amber) / Unreviewed (grey).
- Unmatched transactions table: transaction ID, member, amount, reason (gateway vs bank
  mismatch), actions (Match Manually | Investigate | Escalate).
- "Approve Reconciliation" CTA (locks the day's record). Confirmation modal.

**E.4 Refunds and Disputes**
- Page header: "Refunds & Disputes". + Log New Dispute CTA.
- Open disputes table: member name, group, amount, reason, date raised, assigned admin,
  status chip (Open / In Review / Resolved / Escalated).
- Dispute detail slide-in: communication thread, file attachments, refund approval form
  (amount, reason, payment method for refund).
- "Approve Refund" (teal) / "Reject Dispute" (red) — both require mandatory reason entry
  and show confirmation modal. Rejection reason is sent to member.

---

#### SCREEN GROUP F — AUCTIONS MANAGEMENT *(design now; deferred in current release)*

**F.1 Auctions List (Admin)**
- Sidebar item: Auctions (with Coming Soon chip in current release).
- Auctions table: group name, auction month, type, status (Scheduled / Open / Closed /
  Results Published), bids received count, winning bid, winner member ID, actions.
- + Schedule Auction CTA.

**F.2 Schedule Auction**
- Form: group selector, auction month, type (Open / Reserve), bidding window start/end,
  min bid, reserve price (if type = Reserve).
- Preview: participating members count, eligible bidders count, estimated prize pool.
- Publish Auction CTA with confirmation modal.

**F.3 Auction Monitoring**
- Live bids table (auto-refresh): member ID (anonymised), bid amount, submitted time.
- Current highest bid (teal banner).
- Bidding window countdown.
- Close Bidding CTA (enabled after window closes).

**F.4 Auction Results and Finalization**
- Results summary: winner member ID, winning bid, prize pool breakdown (gross, commission,
  net payout).
- Finalize Results CTA → triggers payout workflow. Confirmation modal.
- Results notification preview: "Notify winner and all participants? Preview →".

---

#### SCREEN GROUP G — KYC MANAGEMENT *(design now; deferred in current release)*

**G.1 KYC Sidebar Item and Pending Queue**
- Sidebar item: KYC (with Coming Soon chip in current release).
- KYC Pending Queue table: member name, member ID, submitted date, document types
  submitted, queue age (days), assigned reviewer, status chip (Pending / In Review /
  Verified / Rejected).
- Filters: status, document type, submission date.
- Assign to Me CTA per row.

**G.2 KYC Review Workspace**
- Split view: document viewer (left 60 %) + review form (right 40 %).
- Document viewer: image with zoom + rotate. Tabs for each document (Aadhaar Front /
  Aadhaar Back / PAN). Extracted data overlay (name, number, DoB).
- Review form: member details summary, extracted data fields for confirmation, any
  discrepancy flag checkboxes, reviewer notes text area.
- "Approve KYC" (teal) / "Reject with Reason" (red) CTAs. Rejection requires reason
  from a predefined list + free text.

---

#### SCREEN GROUP H — PAYOUT RECORDS (ADMIN)

**H.1 Payout Records**
- Page header: "Payouts".
- Payout approval workflow note: "Payout approval workflow coming in next release. Records
  shown are read-only." (amber info banner).
- Table: member name, group, cycle month, gross payout, commission deducted, net payout,
  bank account (masked), transfer date, bank reference, status chip.
- Filters: date range, group, status.
- Row actions: View Details, Download Payout Certificate.
- Export CSV / PDF.

---

#### SCREEN GROUP I — LEDGER AND ACCOUNTS

**I.1 Ledger View**
- Page header: "Ledger". Group selector + date range.
- Opening balance card (top).
- Double-entry ledger table: date, narration (description), debit ₹, credit ₹, running
  balance ₹. Debit rows: light red row background. Credit rows: light green row background.
- Closing balance card (bottom).
- Download PDF / Export Excel buttons.

**I.2 Account Statement — Member Level**
- Member search (type-ahead).
- Statement period: month picker or custom date range.
- Opening balance card.
- Statement table: date, description, debit, credit, closing balance.
- "Send to Member" CTA (email + WhatsApp) with preview modal showing statement PDF.
- Download PDF.

---

#### SCREEN GROUP J — REPORTS AND EXPORTS

**J.1 Reports Dashboard**
- Report category cards (2 × 2 grid): Collections | Member Activity | Payouts | AI Usage.
- Each card: icon, report name, description, last generated date/time, "Run Report" CTA
  (teal), "Schedule" CTA (outlined).

**J.2 Report Builder**
- Report type selector (tabs or dropdown).
- Filters panel: date range, group selector, member selector, status filter.
- Column selector: toggle per column with drag-to-reorder.
- Preview table: first 20 rows, column headers, sort arrows.
- Export row: PDF | Excel | CSV buttons.
- Schedule panel: frequency (daily/weekly/monthly), next run date, recipient email list.

**J.3 Audit Log**
- Page header: "Audit Log". Non-deletable badge (grey lock icon + "Read only").
- Table: timestamp, admin user (name + role chip), action type (colour-coded chip),
  entity type, entity ID, IP / city.
- Expandable row: before/after state diff in JSON viewer (collapsible).
- Filters: date range, admin user, action type (filter multi-select).
- Export only (no delete).

---

#### SCREEN GROUP K — AI TOOLS

**K.1 AI Collection Summary**
- Page header: "AI Tools — Collection Summary".
- "Generate Summary" CTA (teal). Shows spinner while generating.
- Output card: narrative text, confidence score badge (e.g. "High confidence — based on
  verified transaction data"). Teal left border.
- Supporting stats cards: total collected, overdue count, trend sentence.
- Actions: Copy Text | Export PDF | Add to Report.
- Disclaimer: "AI-generated summary. Verify against source reports before distribution."

**K.2 Natural-Language Reporting**
- Page header: "AI Tools — Natural-Language Reporting".
- Text input (large, multi-line): placeholder "e.g. Show members who missed 3 or more
  consecutive installments."
- Submit CTA.
- Generated SQL (read-only code block, collapsible, labelled "Query used — for
  transparency").
- Results table with export.
- "Save as Custom Report" CTA (opens naming dialog).
- Disclaimer: "Results are based on current database records. Validate before operational
  use."

---

#### SCREEN GROUP L — SETTINGS AND PERMISSIONS

**L.1 Role-Based Access Control**
- Page header: "Settings — Roles & Permissions".
- Roles table (row = role, column = module). Roles: Super Admin | Finance Manager |
  Support Agent | Auditor | KYC Officer *(deferred; design with Coming Soon)*.
- Modules: Dashboard | Members | Chit Groups | Installments | Payments | Ledger |
  Reports | AI Tools | KYC *(deferred)* | Auctions *(deferred)* | Settings.
- Each cell: toggle dropdown (Read / Write / None).
- Super Admin row: all cells locked to Read+Write, non-editable.
- Save Changes CTA. Confirmation modal.

**L.2 Admin User Management**
- Admin users table: avatar, name, email, role chip, last login date/time, status chip
  (Active/Inactive/Locked).
- + Invite Admin CTA → email invite modal (email, role selector, optional message).
- Row actions: Edit Role | Deactivate | Reset Password | View Activity.

---

### GENERATION STRATEGY — FOUR BATCHES

Generate in this exact order. Complete each batch before starting the next.
Apply the design system, colour tokens, component styles, and spacing consistently
across all batches.

**Batch A — Core Customer Authentication and Payment Flows**
- 1.3 Mobile Number Login
- 1.4 OTP Verification
- 2.1 Home Dashboard
- 3.4 My Active Memberships
- 3.6 Installment Payment — all steps (Summary, Select Method, Processing,
  Success, Failure)
- 4.1 Payments Home
- 6.2 AI Chat Interface
- 6.1 AI Assistant Home

**Batch B — Remaining Customer Screens**
- 1.1 Splash Screen
- 1.2 Onboarding Carousel (3 slides)
- 1.5 New User Registration
- 1.6 KYC Document Upload *(deferred in release; design now)*
- 1.7 KYC Status *(deferred in release; design now)*
- 2.2 Notifications
- 3.1 Browse Chit Groups
- 3.2 Chit Group Details
- 3.3 Membership Enrollment (3 steps)
- 3.5 Group Member View
- 3.7 Payment History and Account Statement
- 3.8 Digital Receipt
- 4.2 Payout History
- 5.1 Auctions Home *(deferred in release; design now)*
- 5.2 Auction Details *(deferred in release; design now)*
- 5.3 Bid Submission *(deferred in release; design now)*
- 5.4 Bid History *(deferred in release; design now)*
- 5.5 Auction Results *(deferred in release; design now)*
- 5.6 Prize Payout Status *(deferred in release; design now)*
- 6.3 Multilingual AI Interaction
- 6.4 Policy Reference Viewer
- 6.5 Human Support Escalation
- 7.1–7.6 All Profile and Settings screens

**Batch C — Admin Dashboard Core Operations**
- A.1 Executive Dashboard
- A.2 Collection Analytics Deep-Dive
- B.1 Member Search and List
- B.2 Member Details
- C.1 Chit Groups List
- C.2 Create / Edit Chit Group
- D.1 Installment Schedule
- D.2 Installment Collection View
- E.1 Payment Transactions
- E.3 Payment Reconciliation
- K.1 AI Collection Summary
- K.2 Natural-Language Reporting

**Batch D — Remaining Admin Screens, Shared Components, and Responsive Variants**
- B.3 Member Create / Edit (multi-step form)
- C.3 Chit Group Detail View
- E.2 Payment Detail Slide-In Panel
- E.4 Refunds and Disputes
- F.1–F.4 Auctions Management *(deferred; design now)*
- G.1–G.2 KYC Management *(deferred; design now)*
- H.1 Payout Records
- I.1 Ledger View
- I.2 Member Account Statement
- J.1 Reports Dashboard
- J.2 Report Builder
- J.3 Audit Log
- L.1 Role-Based Access Control
- L.2 Admin User Management
- Shared components: skeleton loaders, empty states, confirmation dialogs, toast
  notifications, error pages (404 / 500 / Offline), form validation patterns.
- Responsive variants: 1280 × 800 px and 768 × 1024 px for all key admin screens.

---

*End of Google Stitch copy-ready prompt.*
*After generating all four batches, export frames to Figma. Follow the transfer
procedure in `docs/design/stitch-to-figma-handoff.md` and record each frame's
Figma URL in `docs/design/design-traceability.csv`.*

---

## POST-GENERATION CHECKLIST

After the user completes Stitch generation and Figma transfer, Kiro will:

- [ ] Connect to Figma MCP and inspect the target file
- [ ] Extract design tokens (colours, typography, spacing, component patterns)
- [ ] Map each Figma frame to a screen specification and implementation component
- [ ] Record frame names and URLs in `docs/design/design-traceability.csv`
- [ ] Identify missing designs and document them as pending
- [ ] Generate product specifications for every business module
- [ ] Generate the full Jira backlog (epics, stories, tasks — minimum 60 issues)
- [ ] Generate one Kiro implementation prompt per Jira issue
- [ ] Create steering files and skill files
- [ ] Proceed with Android and admin implementation milestone by milestone
- [ ] Before every push: run `.\check-before-push.ps1` (Windows) or `bash check-before-push.sh` (macOS/Linux) — see `docs/deployment/check-before-push.md`
- [ ] Verify the correct build variant (local Docker / staging / production) is targeted for each environment — see `docs/deployment/build-variants.md`
- [ ] Run automated tests and CI/CD verification at each milestone — see `docs/deployment/cicd.md`
- [ ] Produce a production-readiness report with evidence, not claims

---

## DEVELOPMENT WORKFLOW REFERENCE

These three documents govern how code moves from a developer's machine into production.
Every team member must read them before writing or pushing code.

### Pre-push quality gate — `docs/deployment/check-before-push.md`

`check-before-push.ps1` (Windows) and `check-before-push.sh` (bash) run five checks
before you push to GitHub:

| Section | What it checks |
|---|---|
| 0 — Git status | Warns about uncommitted changes that won't be in the push |
| 1 — Admin dashboard | ESLint, TypeScript, Vitest tests, Vite production build |
| 2 — Backend | Ruff lint + format, Mypy type check, pytest (DB tests need `DATABASE_URL`) |
| 3 — Android | Kotlin compile, Android Lint, ktlint check + format, unit tests |
| 4 — Security | Blocks `.env`, `*.pem`, `*.key`, `*.jks` committed to git |
| 5 — npm audit | Fails on critical/high vulnerabilities in admin dependencies |

```powershell
# Run from repo root (Windows)
.\check-before-push.ps1
```

```bash
# Run from repo root (macOS / Linux / WSL)
bash check-before-push.sh
```

To run backend DB tests locally, set `DATABASE_URL` first:

```powershell
$env:DATABASE_URL = "postgresql+asyncpg://chitsmart:password@localhost:5432/chitsmart_dev"
.\check-before-push.ps1
```

Full reference including fix shortcuts for every failure type:
**`docs/deployment/check-before-push.md`**

---

### Build variants — `docs/deployment/build-variants.md`

Every application layer has four environments with different build configurations:

#### Android

| Variant | Gradle task | `API_BASE_URL` | Signing |
|---|---|---|---|
| Local emulator (debug) | `assembleDebug` | `http://127.0.0.1:8000/api/v1` | Debug keystore |
| Local device (debug) | `assembleDebug` | `http://<LAN-IP>:8000/api/v1` | Debug keystore |
| CI / release | `bundleRelease` | Injected via GitHub secret | Production keystore |
| Production | `bundleRelease` | Production API URL | Production keystore |

#### Admin dashboard (React / Vite)

`VITE_API_BASE_URL` is baked into the bundle at **build time** — not read at runtime.

| Variant | Command | `VITE_API_BASE_URL` |
|---|---|---|
| Local dev | `npm run dev` | `http://localhost:8000/api/v1` (proxied by Vite) |
| Local Docker | `npm run build` inside container | `http://api:8000/api/v1` |
| Staging | `npm run build` (CI) | `https://api.staging.chitsmart.in/api/v1` |
| Production | `npm run build` (CI) | `https://api.chitsmart.in/api/v1` |

#### Backend (Python / FastAPI)

| Variant | How to run | Database |
|---|---|---|
| Local dev | `uvicorn app.main:app --reload` | Docker `db` container on `localhost:5432` |
| Local Docker | `docker-compose up` | Docker `db` container on hostname `db` |
| CI test | GitHub Actions | Ephemeral Postgres 16 service container |
| Staging / Production | Docker image from GHCR | Managed PostgreSQL (with pgvector) |

#### Local Docker full stack

```powershell
# Start database only (recommended during active development)
docker-compose up -d db

# Start database + API together
docker-compose up -d

# Force rebuild after Dockerfile or dependency changes
docker-compose up --build -d

# Apply migrations inside the running container
docker exec chitsmart_api alembic upgrade head

# Tail logs
docker-compose logs -f api
```

Health check after startup:

```powershell
Invoke-RestMethod http://localhost:8000/health
# Expected: {"status":"ok"}
```

Full reference including cloud hosting options and all required GitHub secrets:
**`docs/deployment/build-variants.md`**

---

### CI/CD reference — `docs/deployment/cicd.md`

Five GitHub Actions workflows run automatically on push and PRs:

| Workflow file | Trigger | Jobs |
|---|---|---|
| `ci.yml` | Push + PR → `main`/`develop` | admin, backend, android (path-filtered, parallel) |
| `admin.yml` | `admin/**` changes | quality → build → deploy (staging or production) |
| `backend.yml` | `backend/**` changes | lint → test → docker (GHCR push on `main`/`develop`) |
| `android.yml` | `app/**` + Gradle changes | lint → unit-test → build-debug → build-release (`main` only) |
| `pr-gate.yml` | PR → `main`/`develop` | Aggregated required status check |

#### Branch → job matrix

| | PR to `develop` | Push to `develop` | Push to `main` |
|---|---|---|---|
| Admin lint/test/build | ✅ | ✅ | ✅ |
| Admin deploy | ✅ staging preview | ✅ staging | ✅ production |
| Backend lint/test | ✅ | ✅ | ✅ |
| Backend Docker image | — | ✅ `develop` tag | ✅ `latest` tag |
| Android lint/unit | ✅ | ✅ | ✅ |
| Android debug APK | ✅ | ✅ | ✅ |
| Android signed AAB | — | — | ✅ |

#### Required GitHub secrets

Repository-level (all environments):

```
KEYSTORE_BASE64      base64-encoded Android signing keystore
KEYSTORE_PASSWORD    keystore password
KEY_ALIAS            key alias
KEY_PASSWORD         key password
```

Environment `staging` and `production`:

```
VITE_API_BASE_URL    API base URL baked into the admin build
DEPLOY_HOST          server hostname for rsync deploy
DEPLOY_USER          SSH user
DEPLOY_PATH          remote directory path
SSH_PRIVATE_KEY      SSH private key (no passphrase)
```

#### Pre-push vs CI

The pre-push script catches obvious failures **locally before consuming CI minutes**.
GitHub Actions is the **authoritative gate** for branch protection.

| | Pre-push script | GitHub Actions |
|---|---|---|
| Runs | On developer's machine before `git push` | In a clean Ubuntu VM after push |
| Database | Optional (warn if `DATABASE_URL` not set) | Always available (service container) |
| Docker build | Not run | On `main`/`develop` push |
| Android keystore signing | Not run | On `main` push only |

Full reference including debugging failed jobs, caching strategy, and how to add new checks:
**`docs/deployment/cicd.md`**

---

## RELATED DOCUMENTS

| Document | Purpose |
|---|---|
| `docs/design/stitch-to-figma-handoff.md` | How to transfer Stitch designs to Figma |
| `docs/design/figma-mcp-implementation.md` | Figma MCP connection and implementation readiness |
| `docs/design/design-traceability.csv` | Frame-to-requirement traceability (populate with real IDs after transfer) |
| `docs/scope-decisions.md` | Authoritative list of in-scope and deferred features for current release |
| `docs/deployment/check-before-push.md` | Pre-push quality gate — all sections, fix shortcuts, skip rules |
| `docs/deployment/build-variants.md` | Build configurations for local dev, local Docker, staging, production |
| `docs/deployment/cicd.md` | GitHub Actions workflows — every job, trigger, secret, cache, and branch matrix |
| `docs/deployment/local-setup.md` | First-time developer setup walkthrough |
