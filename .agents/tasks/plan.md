# ChitSmart — Full Implementation Plan
**Generated:** 2026-10-15  
**Covers:** CHIT-24, CHIT-39, CHIT-45, CHIT-42/83, CHIT-46–48, CHIT-50–54, CHIT-58–59, CHIT-61–64, CHIT-66–67, CHIT-69–70

---

## 1 · Current State Inventory

### Backend

| Path | State |
|---|---|
| `backend/app/api/v1/endpoints/auth.py` | **Mostly complete** — send-otp, verify-otp, register (stub), refresh, logout all present. `register` has a `TODO: inject get_current_member` placeholder. |
| `backend/app/core/deps.py` | **MISSING** — file does not exist; `get_current_member` dependency is needed by register, payments, payouts, ledger, AI, etc. |
| `backend/app/models/otp_session.py` | **MISSING** — referenced in auth.py but not yet created. |
| `backend/app/models/member_session.py` | **MISSING** — referenced in auth.py but not yet created. |
| `backend/app/models/payment.py` | **EXISTS** — `InstallmentSchedule`, `Payment`, `LedgerEntry` all fully defined. |
| `backend/app/models/chit_group.py` | **EXISTS** — `ChitGroup`, `ChitMembership` defined. |
| `backend/app/models/deferred_schema.py` | **EXISTS** — `Payout`, `Auction`, `Bid`, `KYCDocument` schemas created (deferred). |
| `backend/app/api/v1/endpoints/payments.py` | **MISSING** — imported in router.py but file doesn't exist. |
| `backend/app/api/v1/endpoints/payouts.py` | **MISSING** — imported in router.py but file doesn't exist. |
| `backend/app/api/v1/endpoints/members.py` | **MISSING** — imported in router.py but file doesn't exist. |
| `backend/app/api/v1/endpoints/chit_groups.py` | **MISSING** — imported in router.py but file doesn't exist. |
| `backend/app/api/v1/endpoints/installments.py` | **MISSING** — imported in router.py but file doesn't exist. |
| `backend/app/api/v1/endpoints/ledger.py` | **MISSING** — imported in router.py but file doesn't exist. |
| `backend/app/api/v1/endpoints/reports.py` | **MISSING** — imported in router.py but file doesn't exist. |
| `backend/app/api/v1/endpoints/ai_assistant.py` | **MISSING** — imported in router.py but file doesn't exist. |
| `backend/app/api/v1/endpoints/admin_*.py` | **ALL MISSING** — admin_members, admin_chit_groups, admin_installments, admin_payments, admin_reports, admin_ai all imported but absent. |
| `backend/app/repositories/` | **EMPTY directory** — no repo files. |
| `backend/app/schemas/` | **EMPTY directory** — no schema files. |
| `backend/app/ai/` | **EMPTY directory** — no AI files. |
| `backend/requirements.txt` | **EXISTS** — includes `openai==1.57.0`, `langchain==0.3.9`, `langchain-openai==0.2.11`, `pgvector==0.3.6`, `httpx==0.28.0`. **No razorpay SDK** — use httpx. |
| `backend/alembic/versions/001_initial_schema.py` | **EXISTS** — all tables created including `otp_sessions`, `member_sessions`, `payouts`, `policy_chunks` (with vector extension). |

### Admin Dashboard (React/TypeScript)

| Path | State |
|---|---|
| `admin/src/App.tsx` | **EXISTS** — routes for dashboard, members, chit-groups, installments, payments, ledger, reports, ai-tools, settings. **Missing routes:** chit-groups/:id, members/new, members/:id/edit, installments/collection, payments/reconciliation, payments/refunds, reports/analytics, ledger/account-statement. |
| `admin/src/components/layout/AdminLayout.tsx` | **EXISTS, FULL** — sidebar with 9 nav items, no KYC/Auctions. |
| `admin/src/pages/Dashboard.tsx` | **EXISTS, FULL** — KPI cards, charts, group performance table. |
| `admin/src/pages/MemberSearch.tsx` | **EXISTS, FULL** — search, filter, paginated table. |
| `admin/src/pages/MemberDetails.tsx` | **EXISTS, FULL** — tabs: Overview, Memberships, Payments, Activity Log. |
| `admin/src/pages/ChitGroupsList.tsx` | **EXISTS, FULL** — search, filter, table with status. |
| `admin/src/pages/PaymentTransactions.tsx` | **EXISTS, FULL** — search, filter, transaction table. |
| `admin/src/pages/InstallmentSchedule.tsx` | **STUB** — 2-line placeholder. |
| `admin/src/pages/Ledger.tsx` | **STUB** — 2-line placeholder. |
| `admin/src/pages/Reports.tsx` | **STUB** — 2-line placeholder. |
| `admin/src/pages/AITools.tsx` | Assumed stub (not read but pattern is consistent). |
| `admin/src/pages/ChitGroupEdit.tsx` | **MISSING** — new file needed. |
| `admin/src/pages/ChitGroupDetail.tsx` | **MISSING** — new file needed. |
| `admin/src/pages/MemberEdit.tsx` | **MISSING** — new file needed. |
| `admin/src/pages/CollectionAnalytics.tsx` | **MISSING** — new file needed. |
| `admin/src/pages/InstallmentCollection.tsx` | **MISSING** — new file needed. |
| `admin/src/pages/Reconciliation.tsx` | **MISSING** — new file needed. |
| `admin/src/pages/Refunds.tsx` | **MISSING** — new file needed. |
| `admin/src/pages/AccountStatement.tsx` | **MISSING** — new file needed. |
| `admin/src/types/index.ts` | **EXISTS, FULL** — all domain types: Member, ChitGroup, Payment, Payout, LedgerEntry, AdminUser, PaginatedResponse, etc. |
| `admin/src/api/client.ts` | **EXISTS** — axios client with JWT interceptor + 401 redirect. |

### Android App (Kotlin/Compose)

| Path | State |
|---|---|
| `ui/home/HomeScreen.kt` | **EXISTS, FULL** — greeting card, investment summary, quick actions, AI banner, transaction list. |
| `ui/auth/LoginScreen.kt` | **EXISTS** |
| `ui/auth/OTPScreen.kt` | **EXISTS** |
| `ui/auth/RegistrationScreen.kt` | **EXISTS** |
| `ui/chits/GroupDetailsScreen.kt` | **EXISTS, FULL** |
| `ui/chits/EnrollmentScreen.kt` | **EXISTS, FULL** |
| `ui/chits/GroupMemberViewScreen.kt` | **EXISTS, FULL** |
| `ui/payment/PaymentFlowScreen.kt` | **EXISTS, FULL** |
| `ui/payment/PaymentHistoryScreen.kt` | **EXISTS** |
| `ui/payment/PaymentsHomeScreen.kt` | **EXISTS** |
| `ui/payment/PayoutHistoryScreen.kt` | **EXISTS** |
| `ui/assistant/AIChatScreen.kt` | **EXISTS, FULL** — chat bubbles, policy chips, embedded cards, typing indicator, suggested queries. |
| `ui/profile/ProfileScreen.kt` | **EXISTS, FULL** — menu items pointing to sub-screens not yet created. |
| `ui/profile/BankAccountsScreen.kt` | **MISSING** |
| `ui/profile/PersonalInfoScreen.kt` | **MISSING** |
| `ui/profile/SecuritySettingsScreen.kt` | **MISSING** |
| `ui/profile/HelpCentreScreen.kt` | **MISSING** |
| `ui/profile/PreferencesScreen.kt` | **MISSING** |
| `ui/payment/ReceiptScreen.kt` | **MISSING** — NavGraph has a `LaunchedEffect(Unit) { navController.popBackStack() }` stub at `AppRoutes.RECEIPT`. |
| `ui/assistant/AssistantHomeScreen.kt` | **MISSING** — NavGraph routes `AppRoutes.ASSISTANT` to `AIChatScreen` directly; needs a separate home screen. |
| `ui/assistant/HumanSupportScreen.kt` | **MISSING** |
| `utils/CurrencyUtils.kt` | **MISSING** — needed by ReceiptScreen. |

---

## 2 · Dependency Map

```
CHIT-24 (auth models + deps.py)
  └─► CHIT-39 (payments — needs get_current_member)
        └─► CHIT-45 (AI — needs get_current_member + ledger model)

All admin screens (CHIT-58, 59, 61–64, 66–67, 69–70)
  └─► Independent of backend (static/mock data; wired to API later)
  └─► App.tsx routing must be updated for each new page

Android screens (CHIT-50–54, CHIT-42, CHIT-46–48)
  └─► Independent of backend (UI-only with mock data for now)
  └─► NavGraph.kt must be updated for each new screen
  └─► ProfileScreen.kt menu items need real route navigation
```

**Critical blocking chain:** `deps.py` → `get_current_member` → `register` endpoint fix → `payments.py` → `ai_assistant.py`

**Router bomb:** `backend/app/api/v1/router.py` imports 15 endpoint modules that are ALL missing. The app will fail to start until every imported module exists with at least a `router = APIRouter()`. Create stub routers for all missing endpoints as part of CHIT-24 before any other backend work.

---

## 3 · Conflicts & Watch-outs

1. **`OtpSession` naming inconsistency** — `auth.py` queries `OtpSession.mobile_number` but the Alembic migration column is also `mobile_number`. The `member.py` relationship references `"OTPSession"` (capital P) but `otp_session.py` model should use `__tablename__ = "otp_sessions"` with class name `OtpSession`. Fix: use `OtpSession` consistently everywhere; update `member.py` relationship to `"OtpSession"`.

2. **Missing `__init__.py` files** — `backend/app/repositories/`, `backend/app/schemas/`, `backend/app/ai/` directories exist but are empty. Every new file needs its directory to be importable; add `__init__.py` to each.

3. **`razorpay` not in requirements.txt** — Use `httpx` (already present) to call Razorpay REST API directly. Do not add the razorpay SDK.

4. **`policy_chunks` vector column** — `pgvector` extension and `vector(1536)` column are created by the migration. The `pgvector` Python package is in requirements. Use `sqlalchemy` with raw SQL for vector similarity until a pgvector SQLAlchemy type is confirmed compatible.

5. **Admin `App.tsx` only has 11 routes** — New pages (ChitGroupEdit, ChitGroupDetail, MemberEdit, CollectionAnalytics, InstallmentCollection, Reconciliation, Refunds, AccountStatement) require new `<Route>` entries; update App.tsx with each ticket.

6. **Android NavGraph stub for RECEIPT** — `AppRoutes.RECEIPT` currently pops back immediately. Must be replaced with `ReceiptScreen` composable call in CHIT-42.

7. **Android `AssistantHomeScreen`** — NavGraph routes `AppRoutes.ASSISTANT` to `AIChatScreen`. After CHIT-46, the ASSISTANT route should point to `AssistantHomeScreen`, which then navigates to `AIChatScreen`.

8. **`MemberSession.is_revoked` vs `revoked_at`** — The ticket spec says `revoked_at` (timestamp) but `auth.py` already uses `is_revoked` (boolean) and the Alembic migration uses `is_revoked`. Keep `is_revoked` to match what's already built.

9. **Double-entry ledger balance** — `LedgerEntry.running_balance` must be computed at write time. The payment service must query the current max running_balance for the group before inserting a new entry.

---

## 4 · Implementation Order

Items are ordered by dependency. Each item leaves the codebase in a buildable state.

---

### BATCH 1 — Backend Foundation (CHIT-24 prerequisite)

---

- [ ] **1. Create missing model files: `OtpSession` and `MemberSession`**

  `auth.py` already imports these; they must exist before the app starts.

  - `backend/app/models/otp_session.py` — `OtpSession` class with fields:
    `id` (UUID PK), `mobile_number` (String 15, indexed), `otp_hash` (String 64),
    `expires_at` (DateTime tz), `is_used` (Boolean default False),
    `attempt_count` (Integer default 0), `created_at` (DateTime tz).
    Relationship: `member` back-populates `"otp_sessions"` using a FK to `members.id` (nullable — OTP can exist before member record).
    **Note:** the Alembic migration has no `member_id` FK on `otp_sessions`. Keep it nullable/absent; auth.py doesn't use the relationship.
  
  - `backend/app/models/member_session.py` — `MemberSession` class with fields:
    `id` (UUID PK), `member_id` (UUID FK → members.id, indexed),
    `refresh_token_hash` (String 64, unique), `device_info` (String 512, nullable),
    `ip_address` (String 45, nullable — not in migration but add as nullable),
    `expires_at` (DateTime tz), `is_revoked` (Boolean default False), `created_at` (DateTime tz).
    Relationship: `member` → `Member`.
  
  - Fix `backend/app/models/member.py` relationship: change `"OTPSession"` → `"OtpSession"`.
  
  **Files:** `backend/app/models/otp_session.py`, `backend/app/models/member_session.py`, `backend/app/models/member.py`
  
  **Verify:** `cd backend && python -c "from app.models.otp_session import OtpSession; from app.models.member_session import MemberSession; print('OK')"` — prints OK.

---

- [ ] **2. Create stub routers for all endpoint modules missing from router.py**

  The router imports 15 modules. Every missing import crashes the app at startup. Create minimal stub files (just `router = APIRouter()`) for: `members`, `chit_groups`, `installments`, `ledger`, `reports`, `ai_assistant`, `admin_members`, `admin_chit_groups`, `admin_installments`, `admin_payments`, `admin_reports`, `admin_ai`. The `payments` and `payouts` stubs will be fleshed out in CHIT-39.

  **Files:**
  - `backend/app/api/v1/endpoints/members.py`
  - `backend/app/api/v1/endpoints/chit_groups.py`
  - `backend/app/api/v1/endpoints/installments.py`
  - `backend/app/api/v1/endpoints/ledger.py`
  - `backend/app/api/v1/endpoints/reports.py`
  - `backend/app/api/v1/endpoints/ai_assistant.py`
  - `backend/app/api/v1/endpoints/admin_members.py`
  - `backend/app/api/v1/endpoints/admin_chit_groups.py`
  - `backend/app/api/v1/endpoints/admin_installments.py`
  - `backend/app/api/v1/endpoints/admin_payments.py`
  - `backend/app/api/v1/endpoints/admin_reports.py`
  - `backend/app/api/v1/endpoints/admin_ai.py`
  - `backend/app/api/v1/endpoints/payments.py` (stub only; fleshed out in CHIT-39)
  - `backend/app/api/v1/endpoints/payouts.py` (stub only; fleshed out in CHIT-39)
  
  Each stub:
  ```python
  from fastapi import APIRouter
  router = APIRouter()
  ```
  
  **Files:** 14 endpoint stubs listed above  
  **Verify:** `cd backend && python -c "from app.api.v1.router import api_router; print('router OK')"` — prints router OK.

---

- [ ] **3. Create `backend/app/core/deps.py` — `get_current_member` dependency (CHIT-24)**

  This is the central auth dependency. Every protected endpoint uses it.

  ```python
  from fastapi import Depends, HTTPException, status
  from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
  from sqlalchemy.ext.asyncio import AsyncSession
  from sqlalchemy import select
  from app.core.database import get_db
  from app.core.security import verify_token
  from app.models.member import Member, AccountStatus

  bearer_scheme = HTTPBearer()

  async def get_current_member(
      credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme),
      db: AsyncSession = Depends(get_db),
  ) -> Member:
      token = credentials.credentials
      try:
          member_id = verify_token(token)
      except ValueError:
          raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token")
      
      result = await db.execute(select(Member).where(Member.id == member_id))
      member = result.scalar_one_or_none()
      if not member:
          raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Member not found")
      if member.account_status != AccountStatus.ACTIVE:
          raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Account suspended")
      return member
  ```

  **Files:** `backend/app/core/deps.py`  
  **Verify:** `cd backend && python -c "from app.core.deps import get_current_member; print('deps OK')"` — prints deps OK.

---

- [ ] **4. Fix `register` endpoint in auth.py to use `get_current_member` (CHIT-24)**

  Replace the stub `current_member=Depends(lambda: None)` with real dependency and implement the handler. Only update fields: `full_name`, `email`, `date_of_birth` (parsed to `date`), `gender` (validated against `Gender` enum). No Aadhaar/PAN fields.

  Pattern to follow: `verify_otp` in the same file — same async DB session usage.

  The endpoint should:
  1. `Depends(get_current_member)` from `app.core.deps`
  2. Update member fields from `RegisterRequest`
  3. Return `AuthTokenResponse(is_new_user=False, access_token=create_access_token(str(member.id)), refresh_token="")` — no new refresh token issued on register.

  **Files:** `backend/app/api/v1/endpoints/auth.py`  
  **Verify:** `cd backend && python -c "from app.api.v1.endpoints.auth import router; print('auth OK')"` — prints auth OK.

---

- [ ] **5. Create `__init__.py` files for empty backend packages**

  Three directories (`repositories/`, `schemas/`, `ai/`) need `__init__.py` so Python treats them as packages before files are added.

  **Files:**
  - `backend/app/repositories/__init__.py` (empty)
  - `backend/app/schemas/__init__.py` (empty)
  - `backend/app/ai/__init__.py` (empty)

  **Verify:** `cd backend && python -c "import app.repositories; import app.schemas; import app.ai; print('packages OK')"` — prints packages OK.

---

### BATCH 2 — Payments API (CHIT-39)

---

- [ ] **6. Create payment Pydantic schemas (`backend/app/schemas/payment.py`)**

  These are needed before the repository and service layers.

  ```python
  # PaymentCreateRequest: installment_schedule_id, payment_method, upi_vpa (optional)
  # PaymentResponse: id, member_id, chit_group_id, installment_schedule_id,
  #   amount, late_fee, total_amount, payment_method, status, gateway_reference,
  #   receipt_number, created_at
  # ReceiptResponse: receipt_number, member_name, group_name, month_number,
  #   amount, late_fee, total_amount, payment_method, status, paid_at, qr_code_url
  # PaymentListResponse: PaginatedResponse[PaymentResponse]
  # PayoutResponse: id, member_id, group_name, month_number, gross_amount,
  #   foreman_commission, net_amount, status, credit_date
  # PayoutListResponse: PaginatedResponse[PayoutResponse]
  # WebhookPayload: razorpay_order_id, razorpay_payment_id, razorpay_signature
  ```

  Use Pydantic v2 `model_config = ConfigDict(from_attributes=True)` (not the old `class Config: orm_mode`). Pattern: base models with `_Create`, `_Response` suffixes following existing `auth.py` inline schemas.

  **Files:** `backend/app/schemas/payment.py`  
  **Verify:** `cd backend && python -c "from app.schemas.payment import PaymentCreateRequest, PaymentResponse; print('schemas OK')"` — prints schemas OK.

---

- [ ] **7. Create `PaymentRepository` and `InstallmentRepository` (`backend/app/repositories/`)**

  ```python
  # backend/app/repositories/payment_repo.py
  class PaymentRepository:
      def __init__(self, db: AsyncSession): ...
      async def create(self, member_id, schedule_id, amount, late_fee, method) -> Payment
      async def get_by_id(self, payment_id: UUID) -> Payment | None
      async def list_by_member(self, member_id, group_id=None, status=None,
                                from_date=None, to_date=None, page=1, size=20)
          -> tuple[list[Payment], int]
      async def update_status(self, payment_id, status, gateway_ref=None,
                               failure_reason=None) -> Payment
      async def get_running_balance(self, chit_group_id) -> Decimal
      async def create_ledger_entry(self, ...) -> LedgerEntry
  ```

  ```python
  # backend/app/repositories/installment_repo.py
  class InstallmentRepository:
      async def get_by_id(self, schedule_id) -> InstallmentSchedule | None
      async def list_by_group(self, group_id) -> list[InstallmentSchedule]
      async def get_current_due(self, group_id) -> InstallmentSchedule | None
  ```

  All methods use `AsyncSession` via constructor injection. No global session. Use `sqlalchemy.select` with `await db.execute(...)`. Follow the pattern of `auth.py` inline DB queries.

  **Files:** `backend/app/repositories/payment_repo.py`, `backend/app/repositories/installment_repo.py`  
  **Verify:** `cd backend && python -c "from app.repositories.payment_repo import PaymentRepository; print('repo OK')"` — prints repo OK.

---

- [ ] **8. Create `PaymentService` (`backend/app/services/payment_service.py`)**

  Business logic layer between endpoint and repository.

  ```python
  class PaymentService:
      async def create_razorpay_order(self, member, schedule, method) -> dict:
          # Use httpx to POST https://api.razorpay.com/v1/orders
          # Auth: HTTPBasicAuth(settings.PAYMENT_KEY_ID, settings.PAYMENT_KEY_SECRET)
          # Body: {"amount": total_paise, "currency": "INR", "receipt": f"rcpt_{payment_id}"}
          # Return: {"order_id": ..., "payment_id": ..., "amount": ..., "currency": ...}
      
      async def verify_webhook(self, payload: str, signature: str) -> bool:
          # HMAC-SHA256: key=PAYMENT_KEY_SECRET, msg=payload
          # Compare with signature using hmac.compare_digest
      
      async def handle_webhook(self, db, order_id, payment_id, signature) -> None:
          # 1. verify_webhook
          # 2. Fetch payment by gateway_reference (order_id)
          # 3. Update status to SUCCESSFUL
          # 4. Generate receipt_number: f"CS-{year}{month}-{seq:06d}"
          # 5. Write LedgerEntry (INSTALLMENT debit on member, credit on group)
          #    running_balance = get_running_balance(group_id) + credit
      
      async def get_receipt(self, payment_id, member) -> ReceiptResponse:
          # Fetch payment, validate ownership, return ReceiptResponse
  ```

  **Files:** `backend/app/services/payment_service.py`  
  **Verify:** `cd backend && python -c "from app.services.payment_service import PaymentService; print('service OK')"` — prints service OK.

---

- [ ] **9. Implement `payments.py` and `payouts.py` endpoints (CHIT-39)**

  Replace the stubs created in step 2 with full implementations.

  **`payments.py`:**
  ```
  POST /payments          → create_razorpay_order (requires auth)
  POST /payments/webhook  → handle_webhook (NO auth — Razorpay server-to-server)
  GET  /payments          → list_by_member with filters (requires auth)
  GET  /payments/{id}/receipt → get_receipt (requires auth)
  ```

  **`payouts.py`:**
  ```
  GET /payouts → paginated list from Payout model filtered by current member (read-only)
  ```

  All endpoints that require auth: `Depends(get_current_member)` from `app.core.deps`.
  Webhook endpoint: verify Razorpay signature before processing; return 400 on failure.
  Pagination: query params `page: int = 1, size: int = 20` (max 100).
  Filter params on GET /payments: `group_id`, `status`, `from_date`, `to_date`.

  **Files:** `backend/app/api/v1/endpoints/payments.py`, `backend/app/api/v1/endpoints/payouts.py`  
  **Verify:** `cd backend && python -c "from app.api.v1.endpoints.payments import router; from app.api.v1.endpoints.payouts import router as pr; print('payments OK')"` — prints payments OK.

---

### BATCH 3 — Admin React Pages (CHIT-61, 58, 59, 62, 64, 66, 63, 67, 69, 70)

All admin pages follow the same pattern as `MemberDetails.tsx` and `Dashboard.tsx`:
- Color tokens inline (no CSS variables): `#073763` navy, `#14B8A6` teal, `#041B3C` dark text, `#64748B` secondary text, `#E2E8F0` border, `#F7FAFC` table row bg, `#F1F3FF` active tab/badge bg.
- No external state management — local `useState` with sample data.
- Recharts for charts (already in `package.json`: `recharts ^2.13.3`).
- `react-hook-form` + `zod` for forms (already in `package.json`).
- Every new page needs a `<Route>` added to `admin/src/App.tsx`.

---

- [ ] **10. Create `ChitGroupEdit.tsx` and wire route (CHIT-61)**

  Multi-section form. Three sections: Group Details, Installment Config, Preview Panel.

  **Group Details fields (all required):**
  - `name`: text input, min 3 chars
  - `chit_value`: number (₹), min 10000
  - `duration_months`: number 6–60
  - `max_members`: number 5–50
  - `foreman_commission_pct`: number 0–5 (step 0.25)

  **Installment Config fields:**
  - `monthly_installment`: auto-calculated display = `chit_value / duration_months`, read-only
  - `first_due_date`: `<input type="date">` wrapped in a styled div
  - `payment_window_days`: number 1–10

  **Preview Panel (right column, 1/3 width):**
  - Auto-generated schedule table: 5 rows showing Month #, Due Date, Amount. Add "…and N more rows" for groups > 5.
  - Refreshes live as form values change via `watch()` from `react-hook-form`.

  **CTAs:**
  - Save as Draft button → `console.log("Draft saved")` + toast-style div.
  - Publish button → `AlertDialog` (using a simple `<dialog>` or inline confirm div) asking for confirmation.

  **NO Auction Rules section.**

  **App.tsx changes:** Add `<Route path="chit-groups/new" element={<ChitGroupEditPage />} />` and `<Route path="chit-groups/:id/edit" element={<ChitGroupEditPage />} />` inside the `/` layout route.

  **Files:** `admin/src/pages/ChitGroupEdit.tsx`, `admin/src/App.tsx`  
  **Verify:** `cd admin && npm run type-check` — no TypeScript errors.

---

- [ ] **11. Create `MemberEdit.tsx` — 4-step wizard (CHIT-58)**

  Step indicator bar at top (4 pills: 1 Personal → 2 Contact → 3 Bank Accounts → 4 Review).

  **Step 1 — Personal Info:**
  - `full_name` (required, min 2), `date_of_birth` (date input), `gender` (select: Male/Female/Other/Prefer not to say)
  - NO Aadhaar/PAN fields.

  **Step 2 — Contact:**
  - `email` (optional, validated), `alternate_mobile` (optional, Indian format)
  - `preferred_language` (select: en/hi/ta/te/kn/ml)

  **Step 3 — Bank Accounts:**
  - Read-only list of existing bank accounts + "Add Bank Account" button (shows inline form: bank_name, account_number, IFSC, account_type select, UPI VPA optional).
  - Mark Primary radio button per account.

  **Step 4 — Review:**
  - Read-only summary of all filled fields in two columns.
  - "Save as Draft" and "Submit" CTA buttons.

  Use `react-hook-form` with `zod` resolver. Each step validates its own fields before allowing Next. `useFormContext` is not needed — pass values between steps using a single top-level `useForm` and conditional rendering.

  **App.tsx changes:** Add `<Route path="members/new" element={<MemberEditPage />} />` and `<Route path="members/:memberId/edit" element={<MemberEditPage />} />`.

  **Files:** `admin/src/pages/MemberEdit.tsx`, `admin/src/App.tsx`  
  **Verify:** `cd admin && npm run type-check` — no TypeScript errors.

---

- [ ] **12. Create `CollectionAnalytics.tsx` (CHIT-59)**

  **Filters bar (top):** Date range (two `<input type="date">` fields), Group multi-select (checkboxes in a dropdown).

  **Charts row (below filters, 2/3 + 1/3 split):**
  - Left: `LineChart` (Recharts) — X: dates, Y: ₹ collected. `CartesianGrid`, `Tooltip`, `XAxis`, `YAxis`, `Legend`. Color: `#14B8A6`.
  - Right: `PieChart` (Recharts) with `Pie` + `Cell` for 4 payment methods. Colors: UPI `#14B8A6`, Net Banking `#073763`, Debit Card `#1769AA`, NEFT/RTGS `#F59E0B`.

  **Collection table (below charts):**
  Columns: Name | Group | Due Amount | Paid Amount | Status — all sortable via `useState(sortKey)` + `useState(sortDir)` toggled by clicking column headers (show `↑↓` icon from lucide `ArrowUpDown`).

  **Export buttons:** "Export CSV" and "Export PDF" as styled buttons (no actual download logic — `console.log` is acceptable for now).

  **App.tsx changes:** Add `<Route path="reports/analytics" element={<CollectionAnalyticsPage />} />`.

  **Files:** `admin/src/pages/CollectionAnalytics.tsx`, `admin/src/App.tsx`  
  **Verify:** `cd admin && npm run type-check` — no TypeScript errors.

---

- [ ] **13. Replace `InstallmentSchedule.tsx` stub (CHIT-62)**

  **Group selector:** `<select>` dropdown populated from sample groups array. Changing selection re-filters the schedule table.

  **Schedule table columns:** Month # | Due Date | Amount/Member | Total Expected | Total Collected | Collection Rate % | Variance | Status chip.

  - Status chips: `COLLECTED` (green), `PARTIAL` (amber), `OVERDUE` (red), `UPCOMING` (gray).
  - Collection rate: progress bar (same pattern as Dashboard.tsx group performance table).
  - Inline due date edit: clicking a Due Date cell replaces it with `<input type="date">` and shows Save/Cancel icons.

  **Send Reminder CTA:** Button above table — "Send Reminder to Overdue Members" → shows a small confirmation div below the button.

  **Files:** `admin/src/pages/InstallmentSchedule.tsx`  
  **Verify:** `cd admin && npm run type-check` — no TypeScript errors.

---

- [ ] **14. Create `InstallmentCollection.tsx` (CHIT-64)**

  **Filters bar:** Month picker (`<input type="month">`) + Group selector (`<select>`).

  **Progress banner:** Full-width card:
  ```
  October 2026 — Family Savings 50K
  [████████░░░░░░] 14/20 members paid — ₹35,000 of ₹50,000 collected (70%)
  ```
  Use a `<div>` progress bar (same teal `#14B8A6` fill pattern from Dashboard).

  **Member collection table:** Name | Due Amount | Paid Amount | Method | Receipt | Status chip.
  - Receipt column: small link button showing receipt number.
  - Status: `PAID` (green), `PENDING` (amber), `OVERDUE` (red), `OFFLINE_MARKED` (blue).

  **Bulk actions toolbar (above table, appears when rows selected):**
  - Checkbox in header row toggles all rows.
  - Toolbar shows: "Mark as Paid (Offline)" | "Send Reminder" | "Export" — as styled buttons.
  - "Mark as Paid" shows an inline confirmation modal div.

  **App.tsx changes:** Add `<Route path="installments/collection" element={<InstallmentCollectionPage />} />`.

  **Files:** `admin/src/pages/InstallmentCollection.tsx`, `admin/src/App.tsx`  
  **Verify:** `cd admin && npm run type-check` — no TypeScript errors.

---

- [ ] **15. Create `Reconciliation.tsx` (CHIT-66)**

  **Day selector:** `<input type="date">` that re-renders the reconciliation view.

  **Summary cards row (4 cards):**
  - Total Expected (blue), Total Received (teal), Unmatched Count (amber), Discrepancy Amount (red if > 0, else green).

  **3-column comparison grid:**
  | Expected Collections | Gateway Settlements | Bank Credits |
  Each column is a card with a mini table: time, ref, amount. Rows in "Gateway" and "Bank" columns with no match in "Expected" are highlighted in amber.

  **Unmatched transactions list (below grid):**
  Each row: ref number | amount | source | timestamp | Action buttons: "Manual Match" (blue), "Investigate" (amber), "Escalate" (red).

  **Reconciliation status badge:** Top-right of page — `BALANCED` (green), `DISCREPANCY` (red), `UNREVIEWED` (gray). Calculated from discrepancy amount.

  **Approve Reconciliation CTA:** Disabled when status is DISCREPANCY. Enabled when BALANCED. Shows confirmation dialog before approval.

  **App.tsx changes:** Add `<Route path="payments/reconciliation" element={<ReconciliationPage />} />`.

  **Files:** `admin/src/pages/Reconciliation.tsx`, `admin/src/App.tsx`  
  **Verify:** `cd admin && npm run type-check` — no TypeScript errors.

---

- [ ] **16. Create `ChitGroupDetail.tsx` (CHIT-63)**

  **Header (full width):**
  - Group name, status badge, chit value, key stats row: Duration | Members | Commission | Start Date.
  - Edit button (→ `/chit-groups/:id/edit`) and status change dropdown.

  **4 tabs: Overview | Members | Installments | Financials**

  - **Overview:** description text, key parameters grid (same card style as MemberDetails.tsx overview grid).
  - **Members:** table with member number, name, enrollment date, status, payout received (yes/no), payout month.
  - **Installments:** mini version of InstallmentSchedule table filtered to this group.
  - **Financials:** 4 summary cards: Total Collected | Total Disbursed | Foreman Commission Earned | Balance. Below: recent ledger entries list.

  **NO Auctions tab.**

  **App.tsx changes:** Add `<Route path="chit-groups/:groupId" element={<ChitGroupDetailPage />} />`.  
  Update `ChitGroupsList.tsx` "View" button: `navigate(\`/chit-groups/${g.id}\`)`.

  **Files:** `admin/src/pages/ChitGroupDetail.tsx`, `admin/src/App.tsx`, `admin/src/pages/ChitGroupsList.tsx`  
  **Verify:** `cd admin && npm run type-check` — no TypeScript errors.

---

- [ ] **17. Replace `Refunds.tsx` stub (CHIT-67)**

  **Open disputes table:**
  Columns: Member | Amount | Reason | Date | Assigned Admin | Status chip.
  Status chips: `OPEN` (amber), `UNDER_REVIEW` (blue), `RESOLVED` (green), `REJECTED` (red).

  **"+ Log New Dispute" CTA:** Opens an inline form panel (slide-in from right using CSS transition + `useState(showPanel)`):
  - Fields: member search (type-ahead input), amount, reason (select: Wrong Amount / Duplicate Payment / Gateway Error / Other), description.

  **Dispute detail panel (same slide-in pattern):**
  Clicking a table row opens the detail panel showing:
  - Communication thread (list of timestamped messages).
  - File attachments section (mock: show uploaded file names).
  - Refund approval form: amount (pre-filled), reason text area.
  - "Approve Refund" (teal) and "Reject Dispute" (red) CTA buttons with mandatory reason validation.

  **Files:** `admin/src/pages/Refunds.tsx`, `admin/src/App.tsx`  
  Add `<Route path="payments/refunds" element={<RefundsPage />} />`.  
  **Verify:** `cd admin && npm run type-check` — no TypeScript errors.

---

- [ ] **18. Replace `Ledger.tsx` stub (CHIT-69)**

  **Filters:** Group selector + date range (from/to date inputs). Apply button re-filters.

  **Summary cards (top):**
  - Opening Balance card (left) and Closing Balance card (right), both with teal accent.

  **Ledger table:**
  Columns: Date | Narration | Entry Type chip | Debit | Credit | Running Balance.
  - Debit column: red text if > 0.
  - Credit column: green text if > 0.
  - Running Balance: bold navy.
  - Entry type chips: INSTALLMENT (teal), PAYOUT (blue), LATE_FEE (amber), FOREMAN_COMMISSION (purple), REFUND (red), ADJUSTMENT (gray).

  **Export buttons:** "Download PDF" and "Download Excel" styled buttons (no actual logic).

  **Files:** `admin/src/pages/Ledger.tsx`  
  **Verify:** `cd admin && npm run type-check` — no TypeScript errors.

---

- [ ] **19. Create `AccountStatement.tsx` (CHIT-70)**

  **Member search:** Type-ahead `<input>` with a dropdown list of matching members (filter sampleMembers by name/mobile). Selecting sets `selectedMember` state.

  **Period selector:** Toggle between "Month Picker" (`<input type="month">`) and "Custom Range" (two date inputs).

  **Opening balance card:** Shows previous period's closing balance in a teal accent card.

  **Statement table:**
  Columns: Date | Description | Debit | Credit | Closing Balance.
  Same styling as Ledger table (red debit, green credit, bold balance).

  **Actions:**
  - "Send to Member" button — shows a confirmation toast div.
  - "Download PDF" button.

  **App.tsx changes:** Add `<Route path="ledger/account-statement" element={<AccountStatementPage />} />`.

  **Files:** `admin/src/pages/AccountStatement.tsx`, `admin/src/App.tsx`  
  **Verify:** `cd admin && npm run type-check` — no TypeScript errors.

---

### BATCH 4 — Android Profile + Payment Screens (CHIT-50, 54, 51, 53, 52, 42/83)

All Android screens follow the same patterns as `HomeScreen.kt`, `ProfileScreen.kt`, and `AIChatScreen.kt`:
- Private color constants at top of file (same values as established tokens).
- `@Composable` functions with `@Preview` at bottom.
- `Scaffold` with `TopAppBar` + optional `bottomBar`.
- `LazyColumn` for scrollable content.
- `MaterialTheme.colorScheme` is **not** used — inline `Color(0xFF...)` values match Figma tokens.

---

- [ ] **20. Create `BankAccountsScreen.kt` (CHIT-50)**

  **Account list:** `LazyColumn` of account cards. Each card:
  - Bank logo placeholder (Box with bank initial), bank name, account type, masked number (`•••• 4092`).
  - UPI IDs listed as sub-rows.
  - Primary badge: teal star icon (`Icons.Outlined.Star`) + "Primary" text in teal pill.
  - Remove button: trash icon → shows `AlertDialog` with confirmation.

  **"Add Bank Account" FAB:** `FloatingActionButton` at bottom-right with `+` icon. Taps show a `ModalBottomSheet` (Compose Material3) with:
  - Fields: Bank Name (text), Account Number (numeric, masked), IFSC (uppercase), Account Type (radio: Savings/Current), UPI VPA (optional).
  - "Verify via Penny Drop" CTA — shows `LinearProgressIndicator` for 2s then "Verification successful ✓" text.

  **NavGraph update:** Add `BANK_ACCOUNTS = "bank_accounts"` to `AppRoutes`, add `composable(AppRoutes.BANK_ACCOUNTS)` block, update `ProfileScreen.kt` "Bank Accounts" `onTap` to `navController.navigate(AppRoutes.BANK_ACCOUNTS)`.

  **Files:** `app/src/main/java/com/chitsmart/ui/profile/BankAccountsScreen.kt`, `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt`  
  **Verify:** Open in Android Studio; no Kotlin compilation errors. `./gradlew :app:compileDebugKotlin` passes.

---

- [ ] **21. Create `PersonalInfoScreen.kt` (CHIT-54)**

  **Editable fields** (use `OutlinedTextField` from Material3):
  - Full name, email address, alternate mobile number.
  - Inline validation: name min 2 chars, email format, mobile `^\+91[6-9]\d{9}$`.

  **Locked fields** (non-editable `OutlinedTextField` with `enabled = false` and a lock icon trailing):
  - Primary mobile, date of birth, Member ID.
  - `Icons.Outlined.Lock` trailing icon. On tap: show `Snackbar` "This field cannot be changed. Contact support."

  **"Save Changes" CTA:** `Button` at bottom. Disabled until any editable field has changed from initial value. Shows `CircularProgressIndicator` for 1.5s then "Saved successfully" Snackbar.

  **NavGraph update:** Add `PERSONAL_INFO = "personal_info"` to `AppRoutes`, wire composable, update ProfileScreen navigation.

  **Files:** `app/src/main/java/com/chitsmart/ui/profile/PersonalInfoScreen.kt`, `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt`  
  **Verify:** `./gradlew :app:compileDebugKotlin` passes.

---

- [ ] **22. Create `SecuritySettingsScreen.kt` (CHIT-51)**

  **Section 1 — Change MPIN:**
  Three `OutlinedTextField` fields rendered as OTP-style 6-digit inputs (KeyboardType.NumberPassword, visualTransformation = PasswordVisualTransformation):
  - Current MPIN, New MPIN, Confirm New MPIN.
  - Validation: new ≠ current; confirm == new.
  - "Update MPIN" button shows progress then success.

  **Section 2 — Biometric:**
  `Switch` toggle for biometric login. When turned on, call `BiometricPrompt` — use `androidx.biometric` (already in deps). On success: show "Biometric enabled" Snackbar.

  **Section 3 — Active Sessions:**
  `LazyColumn` of session cards: device info (e.g. "Android · OnePlus Nord · Mumbai"), created_at date, "Revoke" button (red outlined). Revoking removes the card from the list with a confirmation dialog.

  **Section 4 — Login History:**
  List of last 10 login events: date, device, location (mock "Mumbai, IN"). Read-only, no actions.

  **NavGraph update:** Add `SECURITY_SETTINGS = "security_settings"` and wire.

  **Files:** `app/src/main/java/com/chitsmart/ui/profile/SecuritySettingsScreen.kt`, `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt`  
  **Verify:** `./gradlew :app:compileDebugKotlin` passes.

---

- [ ] **23. Create `HelpCentreScreen.kt` (CHIT-53)**

  **Search bar:** `OutlinedTextField` with search icon. Filters the FAQ list reactively.

  **Category chips row:** 3 `FilterChip` items: "Payments", "Account", "Technical". **NO KYC, NO Auctions.** Tapping a chip filters FAQs to that category (or "All" if chip is deselected).

  **FAQ accordion:** `LazyColumn` of expandable items. Each item:
  - Question text row with `Icons.Outlined.ExpandMore` / `Icons.Outlined.ExpandLess` toggle.
  - Answer text animates in with `AnimatedVisibility(visible = expanded)`.
  - Divider between items.

  At least 6 sample FAQs across 3 categories (payments, account, technical).

  **"Still need help?" card at bottom:**
  - "Ask AI Assistant" button (teal) → `onAiAssistant()` callback.
  - "Human Support" button (outlined) → `onHumanSupport()` callback.

  **NavGraph update:** Add `HELP_CENTRE = "help_centre"` and wire. Callbacks passed from NavGraph: AI → `navigate(AppRoutes.AI_CHAT)`, Human → `navigate(AppRoutes.HUMAN_SUPPORT)`.

  **Files:** `app/src/main/java/com/chitsmart/ui/profile/HelpCentreScreen.kt`, `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt`  
  **Verify:** `./gradlew :app:compileDebugKotlin` passes.

---

- [ ] **24. Create `PreferencesScreen.kt` (CHIT-52)**

  **Section 1 — Language Selector:**
  6 `RadioButton` items in a `Column` card:
  - English (EN), हिंदी (HI), தமிழ் (TA), తెలుగు (TE), ಕನ್ನಡ (KN), മലയാളം (ML).
  - Each row: radio button + language name (native script) + language code badge.
  - Selecting stores choice in `remember { mutableStateOf("en") }`.

  **Section 2 — Notification Preferences:**
  Rows with `Switch` toggles:
  - Payment reminders (SMS), Payment reminders (Push), Payment success/failure (Push), Monthly statement (Email), Group updates (Push).

  **Section 3 — Statement Format:**
  `RadioButton` group: PDF, Excel. Default: PDF.

  **"Save Preferences" button** at bottom. Shows brief Snackbar confirmation.

  **NavGraph update:** Add `PREFERENCES = "preferences"` and wire.

  **Files:** `app/src/main/java/com/chitsmart/ui/profile/PreferencesScreen.kt`, `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt`  
  **Verify:** `./gradlew :app:compileDebugKotlin` passes.

---

- [ ] **25. Create `ReceiptScreen.kt` + `CurrencyUtils.kt` (CHIT-42/83)**

  **`utils/CurrencyUtils.kt`:**
  ```kotlin
  object CurrencyUtils {
      fun amountToWords(amount: Long): String { ... }
      // Handles units: Rupees, Thousands, Lakhs, Crores
      // e.g. 2550 → "Two Thousand Five Hundred Fifty Rupees Only"
  }
  ```

  **`ReceiptScreen.kt`:**
  - Perforated top border: `Canvas` with a dashed path drawn horizontally at top of the receipt card. Use `drawLine` with `PathEffect.dashPathEffect(floatArrayOf(8f, 6f))`.
  - Receipt card contents:
    - ChitSmart logo row, "Official Payment Receipt" heading.
    - Receipt number (`CS-202610-000001`), date, payment method badge.
    - Divider.
    - Group name, Month # / Total, Member name, Member ID.
    - Divider.
    - Amount row: Base Amount + Late Fee = **Total**.
    - Amount in words: `CurrencyUtils.amountToWords(totalPaise)`.
    - "PAID" stamp box (green border, green text, rotated via `graphicsLayer { rotationZ = -15f }`).
  - Actions bar at bottom:
    - "Download PDF" (calls `onDownloadPdf()` callback).
    - "Share via WhatsApp" (calls `onShareWhatsApp()` callback).
    - "Share via Email" (calls `onShareEmail()` callback).

  **NavGraph update:** Replace the `LaunchedEffect(Unit) { popBackStack() }` stub at `AppRoutes.RECEIPT` with `ReceiptScreen(...)` composable. Import `ReceiptScreen`.

  **Files:**
  - `app/src/main/java/com/chitsmart/utils/CurrencyUtils.kt`
  - `app/src/main/java/com/chitsmart/ui/payment/ReceiptScreen.kt`
  - `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt`  
  **Verify:** `./gradlew :app:compileDebugKotlin` passes.

---

### BATCH 5 — Android AI Screens (CHIT-46, 47, 48)

---

- [ ] **26. Create `AssistantHomeScreen.kt` (CHIT-46)**

  New screen; NavGraph `AppRoutes.ASSISTANT` will point here instead of `AIChatScreen`.

  **Language selector chips row:**
  `LazyRow` of 6 `FilterChip` items: `EN`, `हिंदी`, `தமிழ்`, `తెలుగు`, `ಕನ್ನಡ`, `മലയാളം`.
  Only one can be selected at a time. Selected chip: navy background + white text. Deselected: `SurfaceBlue` bg.

  **3 Suggested question cards:**
  Full-width cards with a question mark icon. Questions:
  1. "When is my next installment due?" 
  2. "How does foreman commission work?"
  3. "What happens if I miss a payment?"
  **NO auction question.** Tapping any card navigates to `AIChatScreen` with the question pre-populated (pass as nav argument).

  **Recent conversations list:**
  `LazyColumn` with 3–4 sample conversation rows: AI icon, first message snippet, date chip. Tapping → `AIChatScreen`.

  **NavGraph update:** Change `AppRoutes.ASSISTANT` composable from `AIChatScreen` to `AssistantHomeScreen`. Add `onStartChat = { navController.navigate(AppRoutes.AI_CHAT) }` callback.

  **Files:** `app/src/main/java/com/chitsmart/ui/assistant/AssistantHomeScreen.kt`, `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt`  
  **Verify:** `./gradlew :app:compileDebugKotlin` passes.

---

- [ ] **27. Add `PolicyReferenceBottomSheet` to `AIChatScreen.kt` (CHIT-47)**

  Add a `ModalBottomSheet` composable inside `AIChatScreen`. Triggered by `onPolicyChipTap(chip)` callback (already wired in the existing `AIMessage` composable).

  **Bottom sheet content:**
  - Document title text (large, navy).
  - Section reference badge (`§4 · Foreman Commission`).
  - Highlighted excerpt: excerpt text in a teal-bordered `Box` with slightly tinted background.
  - Three action buttons at bottom:
    - "View Full Document" → `onViewDocument()` callback (stub).
    - "Share" → `onShare()` callback (stub).
    - "Bookmark" → toggles a bookmark icon and shows Snackbar "Saved to bookmarks".

  **State:** `var showPolicySheet by remember { mutableStateOf(false) }` and `var selectedChip by remember { mutableStateOf<PolicyChip?>(null) }` at the `AIChatScreen` scope. Pass `onPolicyChipTap = { chip -> selectedChip = chip; showPolicySheet = true }` to `AIMessage`.

  **Files:** `app/src/main/java/com/chitsmart/ui/assistant/AIChatScreen.kt`  
  **Verify:** `./gradlew :app:compileDebugKotlin` passes.

---

- [ ] **28. Create `HumanSupportScreen.kt` (CHIT-48)**

  **State machine:** `val state by remember { mutableStateOf("CONNECTING") }` — transitions automatically.

  **CONNECTING state (0–2s):**
  `CircularProgressIndicator` (teal) + "Connecting you to a support agent…" text. Use `LaunchedEffect(Unit) { delay(2000); state = "UNAVAILABLE" }`.

  **UNAVAILABLE state:**
  - Icon: `Icons.Outlined.SupportAgent` (large, teal).
  - Heading: "Support agents currently unavailable".
  - Sub-text: "Email us at support@chitsmart.in — we'll respond within 4 hours."
  - "Copy Email" button: copies `support@chitsmart.in` to clipboard.
  - "Share AI Conversation" toggle (`Switch`): when enabled, shows transcript preview card with message count.
  - "Back to AI Assistant" button → `onBack()` callback.

  **NavGraph update:** Add `HUMAN_SUPPORT = "human_support"` to `AppRoutes`, add composable block. Update `AIChatScreen.kt` `onHumanSupport` callback in NavGraph to `navigate(AppRoutes.HUMAN_SUPPORT)`. Update `HelpCentreScreen.kt` human support callback similarly.

  **Files:** `app/src/main/java/com/chitsmart/ui/assistant/HumanSupportScreen.kt`, `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt`  
  **Verify:** `./gradlew :app:compileDebugKotlin` passes.

---

### BATCH 6 — AI Backend (CHIT-45)

---

- [ ] **29. Create AI module: `embeddings.py`, `prompts.py`, `tools.py` (CHIT-45)**

  **`backend/app/ai/embeddings.py`:**
  ```python
  from openai import AsyncOpenAI
  client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)

  async def get_embedding(text: str) -> list[float]:
      response = await client.embeddings.create(
          model=settings.OPENAI_EMBEDDING_MODEL,  # "text-embedding-3-small"
          input=text,
      )
      return response.data[0].embedding
  ```

  **`backend/app/ai/prompts.py`:**
  System prompt string (stored as a constant `SYSTEM_PROMPT`):
  - Role: ChitSmart AI assistant for chit fund members.
  - Scope: ONLY answer from approved policy documents (cited as `[DocumentName §Section]`) and the member's own account data via tools.
  - Hard prohibitions: No auction guidance, no KYC advice, no financial advice beyond chit fund products, never write data or initiate transactions.
  - Multilingual: respond in the language the member uses (detected from input). Support EN, HI, TA, TE, KN, ML.
  - If asked about unavailable features: "This feature is not available yet. Please contact support."

  **`backend/app/ai/tools.py`:**
  Read-only OpenAI function-calling tools (no writes). Define as a `list[dict]` of OpenAI tool specs:
  ```python
  ACCOUNT_TOOLS = [
      {
          "type": "function",
          "function": {
              "name": "get_member_balance",
              "description": "Get the member's current outstanding balance across all active chit groups.",
              "parameters": {"type": "object", "properties": {}, "required": []},
          },
      },
      {
          "type": "function",
          "function": {
              "name": "get_next_installment",
              "description": "Get the member's next upcoming installment due date and amount.",
              "parameters": {"type": "object", "properties": {}, "required": []},
          },
      },
      {
          "type": "function",
          "function": {
              "name": "get_memberships",
              "description": "List all active and completed chit group memberships for the member.",
              "parameters": {"type": "object", "properties": {}, "required": []},
          },
      },
      {
          "type": "function",
          "function": {
              "name": "get_payment_history",
              "description": "Get the member's recent payment history (last 10).",
              "parameters": {
                  "type": "object",
                  "properties": {
                      "limit": {"type": "integer", "default": 10}
                  },
                  "required": [],
              },
          },
      },
  ]
  ```

  Also implement `async def execute_tool(tool_name: str, member: Member, db: AsyncSession) -> str` that dispatches to DB queries (SQLAlchemy selects on `ChitMembership`, `Payment`, `InstallmentSchedule`).

  **Files:** `backend/app/ai/embeddings.py`, `backend/app/ai/prompts.py`, `backend/app/ai/tools.py`  
  **Verify:** `cd backend && python -c "from app.ai.embeddings import get_embedding; from app.ai.prompts import SYSTEM_PROMPT; from app.ai.tools import ACCOUNT_TOOLS; print('ai modules OK')"` — prints ai modules OK.

---

- [ ] **30. Create `rag_pipeline.py` (CHIT-45)**

  Retrieves relevant policy chunks from PostgreSQL using pgvector cosine similarity.

  ```python
  async def retrieve_relevant_chunks(
      query: str, db: AsyncSession, top_k: int = 5
  ) -> list[dict]:
      query_embedding = await get_embedding(query)
      # Use raw SQL for pgvector — sqlalchemy doesn't have native vector type yet
      sql = text("""
          SELECT document_name, section_reference, content, language
          FROM policy_chunks
          ORDER BY embedding <=> CAST(:embedding AS vector)
          LIMIT :top_k
      """)
      result = await db.execute(sql, {"embedding": str(query_embedding), "top_k": top_k})
      return [{"document": r.document_name, "section": r.section_reference, "content": r.content}
              for r in result.fetchall()]
  ```

  **Files:** `backend/app/ai/rag_pipeline.py`  
  **Verify:** `cd backend && python -c "from app.ai.rag_pipeline import retrieve_relevant_chunks; print('rag OK')"` — prints rag OK.

---

- [ ] **31. Create `chat_service.py` and wire `ai_assistant.py` endpoint (CHIT-45)**

  **`backend/app/ai/chat_service.py`:**
  ```python
  async def stream_chat(
      message: str,
      history: list[dict],
      member: Member,
      db: AsyncSession,
  ) -> AsyncGenerator[str, None]:
      # 1. Retrieve RAG context chunks
      chunks = await retrieve_relevant_chunks(message, db)
      # 2. Build messages: [system, ...history, user_msg]
      #    Inject RAG chunks into system prompt as context
      # 3. Call OpenAI chat.completions.create(stream=True, tools=ACCOUNT_TOOLS)
      # 4. If tool_call in response, execute_tool and re-submit
      # 5. Stream each delta.content chunk as SSE: f"data: {json.dumps({'text': chunk})}\n\n"
  ```

  **`backend/app/api/v1/endpoints/ai_assistant.py`** (replace stub):
  ```
  POST /ai/chat — StreamingResponse of SSE events
  GET  /ai/chat/history — last N conversations (stub returning [] for now)
  ```

  Request body: `{"message": str, "history": [...], "language": str}`.
  Response: `StreamingResponse(stream_chat(...), media_type="text/event-stream")`.
  Auth: `Depends(get_current_member)`.

  **Files:** `backend/app/ai/chat_service.py`, `backend/app/api/v1/endpoints/ai_assistant.py`  
  **Verify:** `cd backend && python -c "from app.ai.chat_service import stream_chat; from app.api.v1.endpoints.ai_assistant import router; print('ai endpoint OK')"` — prints ai endpoint OK.

---

## 5 · Patterns Reference

### Backend patterns (follow exactly)

- **Async DB queries:** `result = await db.execute(select(Model).where(...))` then `result.scalar_one_or_none()` or `result.scalars().all()`.
- **Pydantic v2 ORM mode:** `model_config = ConfigDict(from_attributes=True)` (NOT `class Config: orm_mode = True`).
- **UUID comparison:** `Member.id == uuid.UUID(member_id_str)` — cast string to UUID when comparing.
- **Import guard:** All model imports that could cause circular imports go inside endpoint function body, not at module level (pattern from `auth.py`).
- **Financial records:** Never `db.delete(...)` — set `status = VOIDED` with `void_reason`.
- **Error responses:** `raise HTTPException(status_code=..., detail="...")` — no custom error classes.

### Admin React patterns (follow exactly)

- **Color values (inline, not CSS variables):**
  - Navy primary: `#073763` / `#041B3C` (text)
  - Teal accent: `#14B8A6`
  - Secondary text: `#64748B`
  - Border: `#E2E8F0`
  - Table row hover bg: `#F7FAFC`
  - Active tab/badge: `#F1F3FF`
- **Status chip pattern:** `<span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-green-50 text-green-700">ACTIVE</span>`.
- **Table pattern:** `<table className="w-full text-sm">` with `<thead className="bg-[#F7FAFC]">` and `<tbody className="divide-y divide-[#E2E8F0]">`.
- **Form validation:** `const schema = z.object({...})` + `useForm({ resolver: zodResolver(schema) })`.
- **All pages are default exports named `<Name>Page`.**
- **App.tsx imports:** Add import at top + Route inside layout Route.

### Android Compose patterns (follow exactly)

- **Color tokens:** Private `val` constants at top of file (no import from Color.kt — inline per Figma).
- **Screen structure:** `Scaffold(topBar = {...}, bottomBar = {...}) { padding -> LazyColumn(...) }`.
- **Dialogs:** `AlertDialog` for confirmations, `ModalBottomSheet` for panels.
- **Navigation:** Callbacks as lambda parameters; no navigation from within composables.
- **Preview:** `@Preview(showBackground = true, widthDp = 390, heightDp = 860)` at bottom.
- **No ViewModel in screens yet** — screens accept data as parameters with defaults.

---

## 6 · Build & Verify Commands

| Layer | Command | Expected |
|---|---|---|
| Backend syntax check | `cd backend && python -c "from app.main import app; print('OK')"` | `OK` printed |
| Backend tests | `cd backend && pytest tests/ -v` | All tests pass |
| Admin type check | `cd admin && npm run type-check` | 0 errors |
| Admin tests | `cd admin && npm test` | All tests pass |
| Admin full check | `cd admin && npm run check` | lint + types + tests + build pass |
| Android compile | `./gradlew :app:compileDebugKotlin` | BUILD SUCCESSFUL |

---

## 7 · Summary Table

| Ticket | Layer | Files | Status |
|---|---|---|---|
| CHIT-24 | Backend | `models/otp_session.py`, `models/member_session.py`, `core/deps.py`, `endpoints/auth.py` | Steps 1, 3, 4 |
| CHIT-39 | Backend | `schemas/payment.py`, `repositories/payment_repo.py`, `repositories/installment_repo.py`, `services/payment_service.py`, `endpoints/payments.py`, `endpoints/payouts.py` | Steps 6–9 |
| CHIT-45 | Backend | `ai/embeddings.py`, `ai/prompts.py`, `ai/tools.py`, `ai/rag_pipeline.py`, `ai/chat_service.py`, `endpoints/ai_assistant.py` | Steps 29–31 |
| CHIT-42/83 | Android | `ui/payment/ReceiptScreen.kt`, `utils/CurrencyUtils.kt`, `NavGraph.kt` | Step 25 |
| CHIT-46 | Android | `ui/assistant/AssistantHomeScreen.kt`, `NavGraph.kt` | Step 26 |
| CHIT-47 | Android | `ui/assistant/AIChatScreen.kt` (add bottom sheet) | Step 27 |
| CHIT-48 | Android | `ui/assistant/HumanSupportScreen.kt`, `NavGraph.kt` | Step 28 |
| CHIT-50 | Android | `ui/profile/BankAccountsScreen.kt`, `NavGraph.kt` | Step 20 |
| CHIT-51 | Android | `ui/profile/SecuritySettingsScreen.kt`, `NavGraph.kt` | Step 22 |
| CHIT-52 | Android | `ui/profile/PreferencesScreen.kt`, `NavGraph.kt` | Step 24 |
| CHIT-53 | Android | `ui/profile/HelpCentreScreen.kt`, `NavGraph.kt` | Step 23 |
| CHIT-54 | Android | `ui/profile/PersonalInfoScreen.kt`, `NavGraph.kt` | Step 21 |
| CHIT-58 | Admin | `pages/MemberEdit.tsx`, `App.tsx` | Step 11 |
| CHIT-59 | Admin | `pages/CollectionAnalytics.tsx`, `App.tsx` | Step 12 |
| CHIT-61 | Admin | `pages/ChitGroupEdit.tsx`, `App.tsx` | Step 10 |
| CHIT-62 | Admin | `pages/InstallmentSchedule.tsx` (replace stub) | Step 13 |
| CHIT-63 | Admin | `pages/ChitGroupDetail.tsx`, `App.tsx`, `ChitGroupsList.tsx` | Step 16 |
| CHIT-64 | Admin | `pages/InstallmentCollection.tsx`, `App.tsx` | Step 14 |
| CHIT-66 | Admin | `pages/Reconciliation.tsx`, `App.tsx` | Step 15 |
| CHIT-67 | Admin | `pages/Refunds.tsx`, `App.tsx` | Step 17 |
| CHIT-69 | Admin | `pages/Ledger.tsx` (replace stub) | Step 18 |
| CHIT-70 | Admin | `pages/AccountStatement.tsx`, `App.tsx` | Step 19 |
| Infra | Backend | 14 stub endpoint files, 3 `__init__.py` | Steps 2, 5 |
