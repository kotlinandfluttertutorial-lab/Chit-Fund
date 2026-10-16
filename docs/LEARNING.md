# ChitSmart — Learning Documentation

## What this project is and what we built

ChitSmart is a GenAI-powered Chit Fund Management Platform for India.  
A chit fund is a rotating savings and credit association — a group of members each contributes a fixed amount every month, and one member receives the full pooled amount each cycle.

We built three applications:
- **Android customer app** — 18 screens in Kotlin/Jetpack Compose
- **Admin dashboard** — React/TypeScript web app for operations staff
- **Backend API** — Python/FastAPI with PostgreSQL

This document explains how the project was built, the decisions made, the patterns used, and what you can learn from each part.

---

## Part 1 — The AI-SDLC process

### What is AI-SDLC?

AI-SDLC (AI Software Development Lifecycle) is a structured approach to building software where an AI agent participates in every phase: requirements, design, Jira tickets, implementation, testing, and deployment documentation.

The key insight: the AI is most useful when it has clear constraints written down before coding starts. Without constraints, it generates plausible-sounding but wrong output.

### How we structured it

We created three types of documents before writing a single line of code:

**1. Scope decisions** (`docs/scope-decisions.md`)

We made three explicit decisions upfront:
- Remove Gold Saver 50K — it is not a chit fund product, wrong regulatory category
- Defer KYC — Aadhaar/PAN verification requires UIDAI API onboarding and legal compliance review not yet complete
- Defer Auctions/Bidding — real-time bid processing needs WebSocket infrastructure not yet built

**Lesson:** Writing explicit "do not build this" decisions is as important as writing "build this." Every ambiguous feature becomes a scope creep risk. Explicit deferral with documented rationale protects the project from well-intentioned additions that break timelines.

**2. Steering files** (`.kiro/steering/`)

Two always-loaded files that every Kiro session reads automatically:

- `scope.md` — what is in scope, what is deferred, what navigation tabs exist
- `architecture.md` — tech stack locked, database safety rules, API security rules

**Lesson:** Steering files encode institutional knowledge that would otherwise be repeated in every prompt. Once written, every future session inherits the constraints automatically.

**3. Skill files** (`.kiro/skills/`)

Reusable how-to guides for recurring tasks:
- `android-screen.md` — ViewModel → UiState → Composable → NavGraph pattern
- `fastapi-endpoint.md` — schema → repository → service → router → test
- `database-migration.md` — Alembic migration rules, deferred-schema policy
- `admin-dashboard-component.md` — TanStack Query → Table → Page → Tailwind
- `ai-rag-assistant.md` — RAG pipeline, OpenAI tools, streaming, guardrails

**Lesson:** Skills are the difference between an AI that reuses your project's patterns and one that invents its own. Without them, every session might produce valid code that doesn't match the rest of the codebase.

---

## Part 2 — UI design with Google Stitch and Figma

### The design-first principle

We generated UI designs before writing any code. This order matters:
1. Design in Google Stitch → Figma → MCP → Code
2. Not: Code → "looks OK" → ship

The Figma MCP (Model Context Protocol) integration lets Kiro read exact design specifications directly from a Figma file — pixel colours, font weights, spacing values, component names — and convert them to Kotlin/Compose or TypeScript/Tailwind code.

### How Google Stitch works

Google Stitch is a Google Labs AI tool that generates UI screens from natural language prompts. We wrote a detailed prompt (`docs/design/google-stitch-master-prompt.md`) specifying:
- Brand colours with hex values
- Typography scale (Inter font, exact sizes)
- Every screen and its content requirements
- Indian financial data (₹ symbols, Indian names, +91 mobile numbers)
- Scope constraints in the prompt itself ("do not generate KYC screens")

We split generation into 4 batches (A through D) to stay within Stitch's generation limits.

### Figma MCP integration

After transferring Stitch designs to Figma, we connected Kiro's Figma MCP:

```json
// ~/.kiro/settings/mcp.json
"figma": {
  "url": "https://mcp.figma.com/mcp",
  "headers": { "X-Figma-Token": "your-token" },
  "disabled": false
}
```

With MCP connected, Kiro can call `get_design_context(nodeId)` and receive:
- The exact JSX/React code generated from the Figma frame
- Colour values as hex strings
- Font names, weights, and sizes
- Layout structure (flex, padding, margin)
- Asset URLs for images and icons

We used this to implement each Android screen with exact fidelity to the design.

**Lesson:** The MCP rate limit on the Starter plan is 20 calls per month. We hit it during the session and switched to a new Figma file (`lLqFOeduJnNpINYH7fQth7`) which had fresh quota. Plan for rate limits when building with external tool integrations.

---

## Part 3 — Android development patterns

### Architecture: MVVM + Clean Architecture

Every screen follows the same layered structure:

```
UI Layer          → Composable screen
ViewModel Layer   → StateFlow<UiState>, business calls
Domain Layer      → UseCases (encapsulate business rules)
Data Layer        → Repositories (abstract data sources)
Remote Layer      → Retrofit API service
Local Layer       → Room DAO / DataStore
```

### UiState sealed class pattern

Every screen has a sealed class representing all possible states:

```kotlin
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object OtpSent : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
```

The Composable observes this with `collectAsStateWithLifecycle()` and renders the appropriate UI for each state. This is the standard approach for one-time events (navigation, showing a snackbar) versus persistent state (loading indicators, error messages).

**Lesson:** Never put navigation logic inside a `LaunchedEffect` that re-runs on every recomposition. Use `LaunchedEffect(uiState)` with a specific key, and navigate only on `Success` state transitions.

### Compose design system

All colour tokens come from `ui/theme/Color.kt`, extracted directly from the Figma design:

```kotlin
val PrimaryNavy   = Color(0xFF073763)
val TealAccent    = Color(0xFF14B8A6)
val BackgroundLight = Color(0xFFF7FAFC)
```

These are mapped to Material 3 roles in `Theme.kt`:

```kotlin
val LightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    tertiary = TealAccent,
    background = BackgroundLight,
    ...
)
```

In screens, always use `MaterialTheme.colorScheme.primary` — never hardcode hex values. This means the entire app can be re-themed by changing `Color.kt` alone.

**Lesson:** Hardcoding colours in Composables creates maintenance debt. When the designer changes a shade, you have to search and replace across dozens of files. Token-based theming means one change propagates everywhere.

### Navigation with typed arguments

All routes are declared as constants in `AppRoutes`:

```kotlin
object AppRoutes {
    const val OTP = "otp/{mobileNumber}"
    fun otp(mobileNumber: String) = "otp/$mobileNumber"
}
```

Navigation calls always use the builder function:
```kotlin
navController.navigate(AppRoutes.otp("+91$mobile"))
```

Arguments are extracted via `navArgument`:
```kotlin
composable(
    route = AppRoutes.OTP,
    arguments = listOf(navArgument("mobileNumber") { type = NavType.StringType })
) { backStack ->
    val mobile = backStack.arguments?.getString("mobileNumber") ?: ""
    OTPScreen(mobileNumber = mobile, ...)
}
```

**Lesson:** String-based routes are brittle — a typo creates a silent runtime crash. The builder function pattern (`AppRoutes.otp(mobile)`) moves the error to compile time.

### BackHandler for blocking navigation

The payment processing screen must block back navigation:

```kotlin
BackHandler(enabled = true) {
    // intentionally empty — user cannot navigate back during processing
}
```

This is critical for payment integrity. If the user goes back while the gateway is processing, the payment may complete without the app knowing, leading to a status mismatch.

### OTP input with 6 individual boxes

The OTP screen uses a single hidden `BasicTextField` for actual input, with 6 visible decorative boxes:

```kotlin
// Hidden real input
BasicTextField(
    value = otp,
    modifier = Modifier.size(1.dp).focusRequester(focusRequester),
    ...
)

// Visual boxes rendered separately
Row {
    for (i in 0..5) {
        val char = otp.getOrNull(i)
        Box(/* filled/empty/active styling based on i and otp.length */)
    }
}
```

This pattern gives full control over the visual design while letting the system keyboard handle actual input.

---

## Part 4 — Backend API patterns

### FastAPI async everywhere

All database operations are async using SQLAlchemy 2.0:

```python
async def get_member(db: AsyncSession, member_id: UUID) -> Member | None:
    result = await db.execute(
        select(Member).where(Member.id == member_id)
    )
    return result.scalar_one_or_none()
```

Never use synchronous SQLAlchemy in a FastAPI async context — it blocks the event loop and eliminates the concurrency benefit.

### Dependency injection for database sessions

```python
async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async with AsyncSessionLocal() as session:
        try:
            yield session
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()
```

Used in every endpoint:
```python
@router.post("/send-otp")
async def send_otp(request: SendOtpRequest, db: AsyncSession = Depends(get_db)):
    ...
```

FastAPI handles cleanup automatically when the request ends.

### OTP security: hash, not plaintext

OTPs are never stored in plaintext:

```python
otp = str(secrets.randbelow(900000) + 100000)
otp_hash = hashlib.sha256(otp.encode()).hexdigest()
# Store otp_hash in DB, send otp via SMS
```

On verification:
```python
provided_hash = hashlib.sha256(request.otp.encode()).hexdigest()
if provided_hash != otp_session.otp_hash:
    raise HTTPException(401, "Incorrect OTP")
```

**Lesson:** If your OTP table is leaked, hashed OTPs cannot be reversed. With plaintext OTPs, an attacker with database access can authenticate as any user.

### Deferred schema: tables without endpoints

A key architectural decision: create database tables for deferred features (KYC, auctions, bids) but register zero API endpoints for them.

```python
# alembic/versions/001_initial_schema.py
# DEFERRED: auctions (schema only — no API)
op.create_table("auctions", ...)
op.create_table("bids", ...)
op.create_table("kyc_documents", ...)
```

```python
# app/api/v1/router.py
# Deferred — not registered:
# kyc.*        — KYC regulatory prerequisites not met
# auctions.*   — real-time bidding infrastructure not ready
```

**Why this matters:** If we skip creating the tables now, the "KYC release" six months later requires a migration that adds columns to existing member records. That migration runs against production data with existing rows — risky. By creating the schema now (empty), the KYC release only needs to add API endpoints and UI, with no schema migration risk.

### Financial records: never DELETE

The ledger and payments tables are append-only. We enforce this at the code level:

```python
# Bad — never do this
await db.delete(payment)

# Correct
payment.status = PaymentStatus.VOIDED
payment.void_reason = "Customer requested cancellation"
await db.commit()
```

A reversal is a new ledger entry, not an edit to an existing one:

```python
# Create reversal entry instead of editing the original
reversal = LedgerEntry(
    entry_type=LedgerEntryType.ADJUSTMENT,
    narration=f"Reversal of entry {original_id}",
    debit=original.credit,  # debit reverses the original credit
    credit=0,
    running_balance=new_balance,
)
db.add(reversal)
```

**Lesson:** Financial auditors need to see every transaction that ever happened, including corrections. Soft-delete or voiding preserves the audit trail. Hard-delete destroys evidence and may be illegal under the Chit Funds Act, 1982.

---

## Part 5 — Admin dashboard patterns

### Component architecture

Every admin page follows the same structure:

```
Page Component
  └── useQuery hook (TanStack Query)
  └── Filter bar component
  └── Data table component (TanStack Table)
  └── Detail panel / modal component
```

### TanStack Query for server state

```typescript
export function useMembers(params: MembersParams = {}) {
  return useQuery<PaginatedResponse<Member>>({
    queryKey: ["members", params],  // cache key includes filters
    queryFn: async () => {
      const { data } = await apiClient.get("/admin/members", { params });
      return data;
    },
    staleTime: 30_000,  // 30 seconds before refetch
  });
}
```

**Why TanStack Query instead of useState + useEffect:**
- Automatic caching and background refetch
- Shared cache — two components using the same query key share the same data
- Loading/error states built in
- Optimistic updates for mutations
- Request deduplication

### Recharts for data visualisation

The dashboard collection chart:

```tsx
<ResponsiveContainer width="100%" height={200}>
  <BarChart data={collectionData} barSize={10}>
    <CartesianGrid strokeDasharray="3 3" stroke="#F1F3FF" />
    <XAxis dataKey="month" tick={{ fontSize: 11, fill: "#64748B" }} />
    <YAxis tickFormatter={(v) => `₹${(v/100000).toFixed(1)}L`} />
    <Tooltip formatter={(v: number) => [`₹${(v/1000).toFixed(0)}K`]} />
    <Bar dataKey="collected" fill="#14B8A6" radius={[3, 3, 0, 0]} />
    <Bar dataKey="overdue" fill="#F59E0B" radius={[3, 3, 0, 0]} />
  </BarChart>
</ResponsiveContainer>
```

**Lesson:** Always use `ResponsiveContainer` as the outer wrapper — it makes the chart fill its parent div without fixed pixel dimensions.

### Sidebar navigation with active state detection

```tsx
const isActive = location.pathname === path ||
  (path !== "/dashboard" && location.pathname.startsWith(path));
```

The `/dashboard` route gets exact matching because `/dashboard` is a prefix of nothing else. All other routes use `startsWith` so that `/members/member-1` still highlights the Members nav item.

---

## Part 6 — Jira and project management

### Issue hierarchy

```
Epic (17 epics)
  └── Story (66 stories)
       └── Each story has:
            - Description with implementation details
            - Labels (android / backend / admin / ai / devops)
            - Priority (Highest / High / Medium / Low)
            - Parent epic link
            - Kiro prompt path reference
```

### Status workflow

We used three statuses throughout:

| Status | Meaning |
|---|---|
| **To Do** | Not started |
| **In Progress** | Design done or partially implemented |
| **Done** | Code written, tracked in this session |

In a real team, "Done" would require passing tests, code review, and deployment. Here it means the implementation exists in the repository.

### Kiro prompts as implementation specs

Every Jira ticket has a corresponding file in `docs/jira/kiro-prompts/`. These are not simple task descriptions — they are complete implementation briefs containing:

1. The Jira ticket reference
2. The Figma frame and node ID
3. The target file path
4. Detailed UI requirements (screen by screen)
5. API contracts (request/response shapes)
6. Acceptance criteria as checkboxes
7. Scope guards ("do NOT include KYC fields")

Example from `KAN-255-mobile-login-screen.md`:
```markdown
### LoginScreen.kt
- Language toggle EN | हिंदी (top-right)
- "+91" prefix chip (Surface variant background, non-editable)
- `BasicTextField` — `KeyboardType.Number`, max 10 chars
- Validate: exactly 10 digits, first digit 6–9
- "Send OTP" `Button` — full width, PrimaryNavy fill
  - Disabled when input < 10 digits
  - Shows `CircularProgressIndicator` when loading
```

**Lesson:** Kiro prompts are the bridge between Jira (what to build) and code (how to build it). A prompt that says "implement the login screen" produces mediocre results. A prompt with exact field names, validation rules, colour tokens, and Figma node IDs produces production-quality code.

---

## Part 7 — Design traceability

The design traceability CSV (`docs/design/design-traceability.csv`) links every screen to:

- Requirement ID
- Figma frame name and node ID
- Jira ticket
- Spec path
- Kiro prompt path
- Implementation file path
- Test path
- Status

This traceability matrix answers the question "if the designer changes the payment success screen, which files do I need to update?" in seconds.

---

## Part 8 — Scope decisions as a governance tool

The `docs/scope-decisions.md` file was referenced in:
- Every steering file
- Every Kiro prompt
- Every Jira ticket description
- The Google Stitch master prompt
- The design traceability CSV

When an AI session tried to add auction functionality (which slipped through from an earlier version of the onboarding carousel), the steering file caught it:

```markdown
## What is DEFERRED — do not implement
### Bidding and Auctions (infrastructure not ready)
- Customer app Auctions tab and all auction screens
- Bid submission, live auction monitoring, bid history
```

**Lesson:** Document scope decisions in a single canonical file and reference it everywhere. The more places a constraint is referenced, the harder it is to accidentally violate.

---

## Part 9 — What the deferred features look like in code

KYC and auctions are deferred but their database schemas exist. This is deliberate:

```python
# members table — kyc_status column exists but defaults to NOT_REQUIRED
sa.Column("kyc_status", sa.String(30), nullable=False, server_default="NOT_REQUIRED")

# kyc_documents table — exists, no API endpoints
op.create_table("kyc_documents", ...)

# auctions table — exists, no API endpoints  
op.create_table("auctions", ...)
```

In the Android app, navigation routes for deferred features are commented out in `NavGraph.kt`:

```kotlin
// KYC routes deferred — do not add here until KYC release
// Auction routes deferred — do not add here until auctions release
```

In the router:
```python
# app/api/v1/router.py
# Deferred (not registered in this release):
# kyc.*        — deferred: regulatory prerequisites not met
# auctions.*   — deferred: real-time bidding infrastructure not ready
```

When the KYC release is approved, the implementation work is:
1. Add KYC API endpoints (the schema is already there)
2. Add KYC screens to the Android app
3. Wire the KYC routes in NavGraph
4. Add the KYC sidebar item in AdminLayout
5. Update scope-decisions.md

No database migration is needed. No data migration is needed. The feature can be shipped with minimal risk.

---

## Part 10 — AI safety in the GenAI assistant

The AI assistant has hard guardrails that appear in two places:

**In the backend system prompt (`ai/prompts.py`):**
```python
RULES:
1. You may ONLY read data. Never write, update, approve, or initiate transactions.
2. You may ONLY access data for member ID: {member_id}.
3. Cite every policy statement with document name and section.
4. Do NOT answer questions about KYC verification.
5. Do NOT answer questions about auctions or bidding.
```

**In the tool definitions (`ai/tools.py`):**
All tools are read-only GET operations. There is no tool that writes to the database, moves money, approves payouts, or modifies memberships.

**Why this matters:** Large language models can be manipulated by prompt injection — a user or malicious document could instruct the AI to take actions beyond its intended scope. By designing the tool set to be read-only at the API level, even a successful prompt injection cannot cause financial harm. The guardrail is architectural, not just instructional.

---

## Part 11 — Lessons learned from this build

### 1. Scope before code

We spent Phase 0 on design, Phase 0 on scope decisions, and Phase 0 on SDLC artifacts before writing a single line of application code. This felt slow. It was not. Every hour spent on scope documentation saved multiple hours of rework.

### 2. Figma MCP is a force multiplier

Without Figma MCP, a developer would spend hours translating a design into colour values, spacing numbers, and component names. With MCP, that translation takes seconds. The quality difference is also significant — human translation introduces errors; automated extraction does not.

### 3. Design tokens eliminate bikeshedding

Once the colour tokens were in `Color.kt` and `tailwind.config.ts`, nobody needed to debate colours. The designer's decision was encoded once and propagated everywhere automatically.

### 4. Financial integrity is non-negotiable

The double-entry ledger, the append-only payment records, and the idempotency requirement for webhooks are not nice-to-haves. They are legal requirements under the Chit Funds Act, 1982. Building them correctly from day one is far cheaper than retrofitting them after an audit.

### 5. Deferred does not mean ignored

Every deferred feature (KYC, auctions) has:
- A documented decision with rationale
- A database schema waiting for it
- Jira tickets marked as deferred
- Kiro prompts written and waiting
- A clear path to implementation

"Deferred" means "not now, but planned." "Not implemented" means it was forgotten.

### 6. Jira tickets need more than a title

A Jira ticket titled "Login screen" is useless. A ticket with a Figma frame reference, the target file path, the validation rules, and acceptance criteria checkboxes is a complete specification. The difference in implementation quality is proportional to the difference in ticket quality.

### 7. Rate limits are a project risk

The Figma MCP Starter plan allows 20 tool calls per month. We exhausted them during the renaming session. Plan rate limits into your project timeline like you plan any other technical constraint. For a production project, upgrade to Pro before the project starts.

---

## Part 12 — File inventory

### Android (`app/src/main/java/com/chitsmart/`)

| File | Screen / Purpose |
|---|---|
| `MainActivity.kt` | Single-activity host |
| `ChitSmartApplication.kt` | Hilt application entry point |
| `ui/splash/SplashScreen.kt` | Splash with animated progress bar |
| `ui/onboarding/OnboardingScreen.kt` | 3-slide onboarding carousel |
| `ui/auth/LoginScreen.kt` | Mobile number login |
| `ui/auth/LoginViewModel.kt` | Auth state management |
| `ui/auth/OTPScreen.kt` | 6-box OTP with animated cursor |
| `ui/auth/RegistrationScreen.kt` | New user registration (no KYC fields) |
| `ui/home/HomeScreen.kt` | Home dashboard |
| `ui/notifications/NotificationsScreen.kt` | Grouped notifications |
| `ui/chits/BrowseGroupsScreen.kt` | Browse chit groups with filters |
| `ui/chits/GroupDetailsScreen.kt` | Group details with 4-tab layout |
| `ui/chits/ActiveMembershipsScreen.kt` | Active memberships list |
| `ui/chits/EnrollmentScreen.kt` | 4-step enrollment flow |
| `ui/chits/GroupMemberViewScreen.kt` | Calendar + payout history |
| `ui/payment/PaymentsHomeScreen.kt` | Payments tab (replaced Auctions) |
| `ui/payment/PaymentFlowScreen.kt` | 5-step payment state machine |
| `ui/payment/PaymentHistoryScreen.kt` | Transaction history |
| `ui/payment/PayoutHistoryScreen.kt` | Read-only payout history |
| `ui/assistant/AIChatScreen.kt` | AI chat with policy citations |
| `ui/profile/ProfileScreen.kt` | Profile with 3 menu sections |
| `ui/navigation/NavGraph.kt` | All routes + typed arguments |
| `ui/theme/Color.kt` | Brand colour tokens |
| `ui/theme/Type.kt` | Inter typography scale |
| `ui/theme/Theme.kt` | Material 3 colour scheme |

### Backend (`backend/`)

| File | Purpose |
|---|---|
| `app/main.py` | FastAPI app + CORS + router |
| `app/core/config.py` | Pydantic settings from .env |
| `app/core/database.py` | Async session factory |
| `app/core/security.py` | JWT + token hashing |
| `app/models/base.py` | SQLAlchemy base + mixins |
| `app/models/member.py` | Member model (kyc_status=NOT_REQUIRED default) |
| `app/models/chit_group.py` | ChitGroup + ChitMembership |
| `app/models/payment.py` | Payment + LedgerEntry (append-only) |
| `app/models/deferred_schema.py` | KYCDocument, Auction, Bid, Payout |
| `app/api/v1/router.py` | All endpoint registration (deferred ones commented) |
| `app/api/v1/endpoints/auth.py` | send-otp, verify-otp, register, refresh, logout |
| `app/services/sms_service.py` | SMS abstraction (console in DEBUG) |
| `alembic/versions/001_initial_schema.py` | All 16 tables + deferred schema |
| `alembic/env.py` | Async migration runner |

### Admin dashboard (`admin/src/`)

| File | Purpose |
|---|---|
| `main.tsx` | React + TanStack Query + Router entry |
| `App.tsx` | Route definitions (no KYC/Auction routes) |
| `index.css` | Tailwind + scrollbar styling |
| `components/layout/AdminLayout.tsx` | Sidebar + top header |
| `pages/Dashboard.tsx` | 5 KPI cards + Recharts bar chart |
| `pages/MemberSearch.tsx` | Paginated table + filters |
| `pages/MemberDetails.tsx` | 4-tab member view |
| `pages/ChitGroupsList.tsx` | Groups table + search |
| `pages/PaymentTransactions.tsx` | Transaction table + filters |
| `pages/AITools.tsx` | AI summary + NL reporting |
| `api/client.ts` | Axios + JWT interceptor |
| `types/index.ts` | All TypeScript domain types |

### Documentation (`docs/`)

| File | Purpose |
|---|---|
| `scope-decisions.md` | Gold Saver removed, KYC deferred, Auctions deferred |
| `RUNNING.md` | How to run all three applications |
| `LEARNING.md` | This file |
| `design/google-stitch-master-prompt.md` | Complete Stitch prompt (Batches A–D) |
| `design/stitch-to-figma-handoff.md` | Transfer methods A/B/C |
| `design/figma-mcp-implementation.md` | MCP setup + screen-to-code mapping |
| `design/design-traceability.csv` | 68 rows linking requirements → Figma → code → tests |
| `jira/epics.md` | 17 epics with CHIT keys |
| `jira/csv/jira-import.csv` | 80-row importable Jira CSV |
| `jira/kiro-prompts/` | 39 per-ticket implementation briefs |

### Kiro configuration (`.kiro/`)

| File | Purpose |
|---|---|
| `steering/scope.md` | Current release scope — loaded in every session |
| `steering/architecture.md` | Tech stack locked — loaded in every session |
| `skills/android-screen.md` | Android screen implementation pattern |
| `skills/fastapi-endpoint.md` | FastAPI endpoint pattern |
| `skills/database-migration.md` | Alembic migration rules |
| `skills/admin-dashboard-component.md` | Admin dashboard component pattern |
| `skills/ai-rag-assistant.md` | RAG pipeline + OpenAI tools |

---

## Part 13 — What to build next

These are the prioritised remaining tasks, in recommended order:

**P0 — Must complete before beta launch**
1. `CHIT-81` Android build configuration (font files, signing)
2. `CHIT-39` Payment API — Razorpay integration (webhook + idempotency)
3. `CHIT-16` Backend: remaining endpoints (members, chit groups, installments)
4. `CHIT-17` CI/CD pipeline — GitHub Actions for all three apps
5. `CHIT-79` PostgreSQL production setup with managed cloud DB

**P1 — Required for operations**
6. `CHIT-9` Admin installment management (schedule + collection view)
7. `CHIT-10` Admin payment operations (reconciliation + refunds)
8. `CHIT-11` Admin ledger view
9. `CHIT-13` Admin reports with PDF export
10. `CHIT-45` AI backend — RAG pipeline with pgvector + policy documents

**P2 — Enhances quality**
11. `CHIT-15` Admin RBAC — 4 roles with permission matrix
12. Push notifications via FCM
13. Android unit tests (ViewModel + repository layer)
14. Backend integration tests (auth + payments)
15. Admin E2E tests (Playwright)

**Future releases (deferred)**
16. KYC — UIDAI API, PAN verification, admin review workflow
17. Auctions — WebSocket real-time bidding, result finalization
18. Payout approval workflow — maker-checker with admin authorization

---

*This document was generated from the actual implementation session. All file paths, code samples, and decisions are accurate as of the date of implementation.*
