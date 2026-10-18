# Implementation Plan — Fix all mypy type errors (zero-error target)

**Goal:** `cd backend && mypy app --ignore-missing-imports --no-error-summary` exits 0 with no output.
**Constraint:** Type annotations + None guards only. No runtime behavior change. No dropped tables/columns. No KYC/auction/bid endpoints exposed. Financial records never deleted.

---

## Decisions (grounded in code read during exploration)

### D1 — SQLAlchemy typing pattern: `Mapped[...] + mapped_column()` (NOT the plugin, NOT `Column[Enum]`)

`requirements.txt` pins **SQLAlchemy 2.0.36**, so the idiomatic 2.0 pattern is `Mapped[T] = mapped_column(...)`.

Why this over the alternatives:
- **Why not the SQLAlchemy mypy plugin (`sqlalchemy.ext.mypy.plugin`)?** `pyproject.toml` has a `[tool.mypy]` block (python_version 3.12, ignore_missing_imports, strict=false, exclude alembic) but no plugin. Adding the plugin would fix `Column[...]` typing without code change, but the task framing explicitly asks for the `Mapped[]` migration for SA 2.0+, and a plugin can surface unexpected new model errors. The `Mapped[]` migration fixes the errors at the source in a way that is verifiable per-file.
- **Why not `Column[EnumType]` annotations + casts/`# type: ignore`?** `Column(Enum(X))` columns are the only ones flagged `[var-annotated]` (SA can't infer the type parameter from an `Enum()` instance; `Column(String/Integer/Boolean/...)` infers fine). Annotating them as `Column[PaymentMethod]` fixes `[var-annotated]` but leaves the attribute typed `Column[PaymentMethod]`, so every downstream `payment.payment_method` passed to a `PaymentMethod` parameter still fails `[arg-type]`, and every `payment.status = status` still fails `[assignment]` — forcing `cast(...)` / `# type: ignore` across `payment_repo.py`, `payment_service.py`, `payments.py`. `Mapped[]` makes each attribute its plain Python type (`UUID`, `PaymentStatus`, `bool`, …), so all `[var-annotated]`, `[assignment]`, and `[arg-type]` errors disappear at the source with one consistent pattern.

Runtime/DDL safety: `mapped_column(...)` constructs an equivalent `Column` object — the runtime table metadata and the existing Alembic migrations are unchanged (migrations are static files; model declaration style does not regenerate them). The `DeclarativeBase`-provided kwargs `__init__` still accepts the existing `Member(mobile_number=..., full_name=...)` / `Payment(member_id=..., ...)` constructor calls. **No migration files are touched.**

**Rule for the implementer:** derive the `Mapped` Optional-ness from the original `nullable=` — `Mapped[Optional[X]]` for `nullable=True`, `Mapped[X]` for `nullable=False` — and keep every other column argument (type, `ForeignKey`, `default`, `index`, `unique`, `onupdate`) identical. SA 2.0 errors at mapper-config time if `mapped_column(nullable=...)` contradicts the `Mapped` Optional-ness, so match them exactly. After migrating, remove now-unused `Column` imports (ruff F401) and add `from sqlalchemy.orm import Mapped, mapped_column`.

Relationships (`relationship(...)`) are left untyped — they produce no mypy errors and are not part of this fix.

### D2 — None guards (union-attr errors)

Two `Optional[...]` results are accessed without a guard:
- `payment_service.process_webhook` reloads `payment = await self.payment_repo.get_by_id(payment.id)` (returns `Optional[Payment]`) then reads `payment.chit_group_id/total_amount/member_id/id`. Fix: insert a None guard after the reload (raise `HTTPException(500)`). This branch is unreachable (the payment was just updated successfully), so it changes a potential `AttributeError→500` into `HTTPException→500` — no user-visible behavior change.
- `auth.verify_firebase_token` does `member = ...scalar_one_or_none()` then branches on `if is_new_user:`. mypy cannot carry the `member is None` narrowing through the `is_new_user` variable, so `member` stays `Member | None` after the block. Fix: branch on `if member is None:` instead (identical control flow since `is_new_user = member is None`; keep `is_new_user` for the return value). After the block mypy narrows `member` to `Member`.

`get_receipt` already guards `if payment is None: raise HTTPException(...)` and `if payment.receipt_number is None: raise ...` — no change needed there.

### D3 — OpenAI `tools` overload mismatch (chat_service.py:123)

`ACCOUNT_TOOLS` is typed `list[dict[str, Any]]` (in `app/ai/tools.py`); the SDK `client.chat.completions.create(..., tools=...)` expects `Iterable[ChatCompletionToolParam]`, so the overload fails. `ChatCompletionToolParam` is importable from `openai.types.chat` (verified: openai 1.57.0). Fix: at the single call site, `cast(list[ChatCompletionToolParam], ACCOUNT_TOOLS)`. Lowest-risk, localized, and explicitly endorsed by the task. (Annotating `ACCOUNT_TOOLS` itself as `list[ChatCompletionToolParam]` in `tools.py` risks TypedDict-conformance churn on the `parameters` sub-dict; the cast avoids that.) `tool_choice="auto"` is already a valid `Literal["auto"]` and is not the cause of the overload failure.

### D4 — No behavior-changing fix is required

Every error is fixable by annotation / None-guard / refactor. The one "behavior" item — adding `SMS_PROVIDER` to `Settings` — actually fixes a latent runtime `AttributeError` (the code already reads `settings.SMS_PROVIDER`); with the default `"firebase"`, non-DEBUG calls log "Unknown SMS provider: firebase. OTP not sent." instead of crashing. Nothing here needs user approval.

---

## Ordered steps

- [ ] 1. Add the missing `SMS_PROVIDER` setting to `Settings`.
      `app/services/sms_service.py` reads `settings.SMS_PROVIDER` but `app/core/config.py` does not define it (the only `settings.SMS_*`/`OTP_*` reference in real code; the rest in `sms_service.py` are inside comments).
      In `app/core/config.py`, inside the `Settings` class (near the Firebase block), add: `SMS_PROVIDER: str = "firebase"` (default aligns with the app's Firebase-based OTP; in DEBUG mode `send_sms_otp` returns before reaching the provider switch, so this only affects the non-DEBUG warning path).
      Files: `app/core/config.py`
      Verify: `cd backend && python -m py_compile app/core/config.py` (and full mypy in step 12).

- [ ] 2. Migrate the shared mixins in `app/models/base.py` to `Mapped[] + mapped_column()` (dependency for steps 3–8).
      Add `from sqlalchemy.orm import Mapped, mapped_column`; keep `Column` import only if still used (it won't be — remove it; ruff F401). Convert:
      - `UUIDMixin.id` → `id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)`
      - `TimestampMixin.created_at` → `created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc), nullable=False)`
      - `TimestampMixin.updated_at` → `updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc), nullable=False)`
      Keep `import uuid`, `from datetime import datetime, timezone`, `DateTime`, `UUID` imports.
      Files: `app/models/base.py`
      Verify: `cd backend && python -c "import app.models.base"` then `mypy app --ignore-missing-imports --no-error-summary` (full run in step 12).

- [ ] 3. Migrate the five error-producing model files to `Mapped[] + mapped_column()` (same pattern). For every column, map the SA type to its Python type: `UUID(as_uuid=True)→uuid.UUID`, `String→str`, `Integer→int`, `Numeric→decimal.Decimal`, `Boolean→bool`, `DateTime→datetime`, `Date→datetime.date`, `Enum(X)→X`; use `Mapped[Optional[T]]` where the original had `nullable=True`, else `Mapped[T]`. Keep all `ForeignKey`/`default`/`index`/`unique`/`onupdate` args identical. Add `from sqlalchemy.orm import Mapped, mapped_column`, plus `import uuid`, `from datetime import date, datetime`, `from decimal import Decimal`, `from typing import Optional` as needed; drop the now-unused `Column` import (ruff F401).

      **`app/models/member.py`** (fixes `[var-annotated]` on `gender`, `account_status`, `kyc_status`; fixes `auth.py` `[assignment]` on `is_mobile_verified = True`):
      - `full_name: Mapped[str]`, `mobile_number: Mapped[str]`, `email: Mapped[Optional[str]]`, `date_of_birth: Mapped[Optional[date]]`, `gender: Mapped[Optional[Gender]] = mapped_column(Enum(Gender), nullable=True)`, `account_status: Mapped[AccountStatus] = mapped_column(Enum(AccountStatus), default=AccountStatus.ACTIVE, nullable=False)`, `is_mobile_verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)`, `kyc_status: Mapped[KYCStatus] = mapped_column(Enum(KYCStatus), default=KYCStatus.NOT_REQUIRED, nullable=False)`, `aadhaar_number_encrypted: Mapped[Optional[str]]`, `pan_number_encrypted: Mapped[Optional[str]]`, `preferred_language: Mapped[str]`.
      - Leave the four `relationship(...)` lines untouched.

      **`app/models/chit_group.py`** (fixes `[var-annotated]` on `ChitGroup.status`, `ChitMembership.status`):
      - `ChitGroup`: `name: Mapped[str]`, `chit_value: Mapped[Decimal]`, `monthly_installment: Mapped[Decimal]`, `duration_months: Mapped[int]`, `max_members: Mapped[int]`, `foreman_commission_pct: Mapped[Decimal]`, `start_date: Mapped[date]`, `status: Mapped[ChitGroupStatus] = mapped_column(Enum(ChitGroupStatus), default=ChitGroupStatus.PENDING, nullable=False)`, `terms_document_url: Mapped[Optional[str]]`, `description: Mapped[Optional[str]]`, `next_auction_date: Mapped[Optional[date]]`.
      - `ChitMembership`: `member_id: Mapped[uuid.UUID]`, `chit_group_id: Mapped[uuid.UUID]`, `member_number: Mapped[int]`, `enrollment_date: Mapped[date]`, `status: Mapped[MembershipStatus] = mapped_column(Enum(MembershipStatus), default=MembershipStatus.ACTIVE, nullable=False)`, `payout_received: Mapped[bool]`, `payout_month: Mapped[Optional[int]]`.

      **`app/models/payment.py`** (fixes `[var-annotated]` on `payment_method`, `status`, `entry_type`; fixes all `payment_service.py`/`payments.py`/`payment_repo.py` `[arg-type]`/`[assignment]` Column errors at source). Add `import uuid`, `from datetime import datetime`, `from decimal import Decimal`, `from typing import Optional`:
      - `InstallmentSchedule`: `chit_group_id: Mapped[uuid.UUID]`, `month_number: Mapped[int]`, `due_date: Mapped[datetime]`, `amount_per_member: Mapped[Decimal]`.
      - `Payment`: `member_id: Mapped[uuid.UUID]`, `chit_group_id: Mapped[uuid.UUID]`, `installment_schedule_id: Mapped[uuid.UUID]`, `amount: Mapped[Decimal]`, `late_fee: Mapped[Decimal]`, `total_amount: Mapped[Decimal]`, `payment_method: Mapped[PaymentMethod] = mapped_column(Enum(PaymentMethod), nullable=False)`, `status: Mapped[PaymentStatus] = mapped_column(Enum(PaymentStatus), default=PaymentStatus.PENDING, nullable=False)`, `gateway_reference: Mapped[Optional[str]]`, `bank_reference: Mapped[Optional[str]]`, `upi_vpa: Mapped[Optional[str]]`, `failure_reason: Mapped[Optional[str]]`, `void_reason: Mapped[Optional[str]]`, `receipt_number: Mapped[Optional[str]] = mapped_column(String(100), unique=True, nullable=True)`.
      - `LedgerEntry`: `chit_group_id: Mapped[uuid.UUID]`, `member_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=True)`, `payment_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), ForeignKey("payments.id"), nullable=True)`, `entry_type: Mapped[LedgerEntryType] = mapped_column(Enum(LedgerEntryType), nullable=False)`, `narration: Mapped[str]`, `debit: Mapped[Decimal]`, `credit: Mapped[Decimal]`, `running_balance: Mapped[Decimal]`.

      **`app/models/deferred_schema.py`** (fixes `[var-annotated]` on `document_type`, `status`×3). Add `import uuid`, `from datetime import datetime`, `from decimal import Decimal`, `from typing import Optional`:
      - `KYCDocument`: `member_id: Mapped[uuid.UUID]`, `document_type: Mapped[KYCDocumentType] = mapped_column(Enum(KYCDocumentType), nullable=False)`, `file_url: Mapped[Optional[str]]`, `status: Mapped[KYCDocumentStatus] = mapped_column(Enum(KYCDocumentStatus), default=KYCDocumentStatus.UPLOADED, nullable=False)`, `extracted_data: Mapped[Optional[str]]`, `reviewer_notes: Mapped[Optional[str]]`, `reviewed_by_admin_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), nullable=True)`.
      - `Auction`: `chit_group_id: Mapped[uuid.UUID]`, `month_number: Mapped[int]`, `scheduled_start: Mapped[datetime]`, `scheduled_end: Mapped[datetime]`, `min_bid: Mapped[Decimal]`, `max_bid: Mapped[Decimal]`, `status: Mapped[AuctionStatus] = mapped_column(Enum(AuctionStatus), default=AuctionStatus.SCHEDULED, nullable=False)`, `winner_member_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), nullable=True)`, `winning_bid_amount: Mapped[Optional[Decimal]]`, `result_approved_by: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), nullable=True)`.
      - `Bid`: `auction_id: Mapped[uuid.UUID]`, `member_id: Mapped[uuid.UUID]`, `bid_amount: Mapped[Decimal]`, `is_winner: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)`, `is_voided: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)`, `void_reason: Mapped[Optional[str]]`.
      - `Payout`: `member_id: Mapped[uuid.UUID]`, `chit_group_id: Mapped[uuid.UUID]`, `auction_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), ForeignKey("auctions.id"), nullable=True)`, `month_number: Mapped[int]`, `gross_amount: Mapped[Decimal]`, `foreman_commission: Mapped[Decimal]`, `net_amount: Mapped[Decimal]`, `bank_account_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), ForeignKey("bank_accounts.id"), nullable=True)`, `bank_reference: Mapped[Optional[str]]`, `status: Mapped[PayoutStatus] = mapped_column(Enum(PayoutStatus), default=PayoutStatus.PENDING, nullable=False)`, `credit_date: Mapped[Optional[datetime]]`, `approved_by_admin_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), nullable=True)`.

      **`app/models/member_session.py`** (fixes `auth.py:269` `[assignment]` on `session.is_revoked = True`). It does **not** use the mixins (defines its own `id`/`created_at`), so migrate it directly. Add `from typing import Any, Optional`, `from sqlalchemy.orm import Mapped, mapped_column`; drop unused `Column` import:
      - `id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)`, `member_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=False, index=True)`, `refresh_token_hash: Mapped[str] = mapped_column(String(64), nullable=False, unique=True)`, `device_info: Mapped[Optional[Any]] = mapped_column(JSONB, nullable=True)`, `ip_address: Mapped[Optional[str]]`, `expires_at: Mapped[datetime]`, `is_revoked: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)`, `created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc), nullable=False)`. Leave `member = relationship(...)` untouched.

      Files: `app/models/member.py`, `app/models/chit_group.py`, `app/models/payment.py`, `app/models/deferred_schema.py`, `app/models/member_session.py`
      Verify: `cd backend && python -c "import app.models.payment, app.models.member, app.models.chit_group, app.models.deferred_schema, app.models.member_session"` (catches SA mapper-config errors from Optional/nullable mismatches); then full mypy in step 12.

- [ ] 4. Migrate the two remaining (error-free) model files to `Mapped[] + mapped_column()` for a fully consistent model layer. No current mypy errors, so this is for consistency only — verify it introduces none. Same rules as step 3; drop unused `Column` import.
      **`app/models/otp_session.py`**: `id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)`, `mobile_number: Mapped[str]`, `otp_hash: Mapped[str]`, `expires_at: Mapped[datetime]`, `is_used: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)`, `attempt_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)`, `created_at: Mapped[datetime]`. Add `from typing import Optional`, `from sqlalchemy.orm import Mapped, mapped_column`.
      **`app/models/policy_document.py`**: `id: Mapped[uuid.UUID]`, `document_name: Mapped[str]`, `section_reference: Mapped[str]`, `content: Mapped[str]`, `language: Mapped[str]`, `page_number: Mapped[Optional[int]]`, `created_at: Mapped[datetime]`. Keep the `__repr__` (works with `Mapped[uuid.UUID]`/`Mapped[str]`). Do **not** add the `embedding` column (it is intentionally not mapped; added via raw ALTER in migration 001). Add `from typing import Optional`, `from sqlalchemy.orm import Mapped, mapped_column`.
      Files: `app/models/otp_session.py`, `app/models/policy_document.py`
      Verify: `cd backend && python -c "import app.models.otp_session, app.models.policy_document"`; full mypy in step 12.

- [ ] 5. Add the None guard after the payment reload in `app/services/payment_service.py` (fixes `[union-attr]` + `[arg-type]` at the reload block: `payment.chit_group_id`, `payment.total_amount`, `payment.member_id`, `payment.id` — original errors ~163,164,167,168,169,173).
      Immediately after the line `payment = await self.payment_repo.get_by_id(payment.id)` (the "Reload updated payment" line in `process_webhook`), insert:
      ```python
      if payment is None:
          raise HTTPException(
              status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
              detail="Payment not found after status update",
          )
      ```
      Do not change anything else in `process_webhook`. (The earlier `payment.id` passed to `update_status`/`get_by_id` and the `create`/`_create_internal_order` calls in `create_payment_intent` are already correct once step 3 makes the model attributes `UUID`; `get_receipt` already has its own None guards.)
      Files: `app/services/payment_service.py`
      Verify: `cd backend && python -m py_compile app/services/payment_service.py`; full mypy in step 12.

- [ ] 6. Narrow `member` in `app/api/v1/endpoints/auth.py` `verify_firebase_token` (fixes `[union-attr]` on `member.is_mobile_verified`/`member.id` — original errors ~153,157,162; the `[assignment]` on `is_mobile_verified`/`is_revoked` is already fixed by step 3's `Mapped[bool]`).
      Change the branch condition from `if is_new_user:` to `if member is None:` (keep `is_new_user = member is None` and its use in the return value). Identical control flow; this lets mypy narrow `member` to `Member` after the if/else so `member.id` (JWT + `MemberSession(member_id=member.id)`) and the `member.is_mobile_verified = True` in the `else` branch type-check. No other edits in `auth.py`.
      Files: `app/api/v1/endpoints/auth.py`
      Verify: `cd backend && python -m py_compile app/api/v1/endpoints/auth.py`; full mypy in step 12.

- [ ] 7. Fix the OpenAI `tools` overload in `app/ai/chat_service.py` (fixes `[call-overload]` at line 123).
      Add imports: `from typing import cast` (extend the existing `from typing import AsyncGenerator` line) and `from openai.types.chat import ChatCompletionToolParam`. At the first `client.chat.completions.create(...)` call (the one with `tools=ACCOUNT_TOOLS`), change `tools=ACCOUNT_TOOLS,` to `tools=cast(list[ChatCompletionToolParam], ACCOUNT_TOOLS),`. Leave the second (tool-results) `create` call and everything else unchanged.
      Files: `app/ai/chat_service.py`
      Verify: `cd backend && python -m py_compile app/ai/chat_service.py`; full mypy in step 12.

- [ ] 8. Lint cleanup + final verification.
      a. Remove unused imports introduced by the migration: `cd backend && ruff check . --fix` (drops now-unused `Column` in migrated model files; F401), then `cd backend && ruff format .` (wraps any `mapped_column(...)` lines over 120 chars), then `cd backend && ruff check .` and `cd backend && ruff format --check .` — both must pass (they are part of `check-before-push.sh`).
      b. mypy (source of truth, matches CI): install if missing (`pip install mypy`), then `cd backend && mypy app --ignore-missing-imports --no-error-summary` — must exit 0 with no output. If a residual error appears, fix per the per-file guidance above (most likely a `Mapped` Optional/`nullable=` mismatch caught by SA mapper config, or an unused import).
      c. Import smoke test (catches SA mapper-config failures that mypy alone won't): `cd backend && python -c "import app.models; import app.services.payment_service; import app.api.v1.endpoints.auth; import app.api.v1.endpoints.payments; import app.ai.chat_service"`.
      d. Tests (annotation-only changes, must still pass; requires PostgreSQL + `DATABASE_URL`): `cd backend && pytest tests/ -v --asyncio-mode=auto -x`. If no DB is available locally, this is run in CI — note that the changes are type-annotation/guard only and do not alter runtime logic, so existing tests are expected to pass unchanged.
      Files: all modified files (no further edits unless a verification failure requires one).
      Verify: steps a–d all green; the defining success criterion is step (b) = mypy exits 0 with no output.

---

## Error-to-fix cross-reference (for the implementer to check off against the CI output)

| Original error | Root cause | Fixed by step |
|---|---|---|
| `sms_service.py:23` Settings has no `SMS_PROVIDER` | missing setting | 1 |
| `payment.py:65,66,85` `[var-annotated]` enum cols | `Column(Enum(X))` uninferrable | 3 |
| `member.py:46,49,58` `[var-annotated]` enum cols | same | 3 |
| `deferred_schema.py:45,47,75,122` `[var-annotated]` | same | 3 |
| `chit_group.py:42,62` `[var-annotated]` | same | 3 |
| `payment_repo.py:117–125` `[assignment]` (status/refs to `Column`) | model attrs typed `Column[T]` | 3 (attrs become `T`) |
| `payment_service.py:68,71,72,73,153,160,191` & `payments.py:137` `[arg-type]` `Column[UUID]`→`UUID` | model attrs typed `Column[UUID]` | 3 (attrs become `UUID`) |
| `payment_service.py:163,164,167,168,169,173` `[union-attr]` + reload `[arg-type]` | reloaded `payment` is `Optional[Payment]` | 5 (None guard; attrs `UUID`/`Decimal` from step 3) |
| `payment_service.py:214–224` `[arg-type]` to `ReceiptResponse` | model attrs typed `Column[...]` | 3 (attrs become plain types; existing `receipt_number is None` guard narrows) |
| `auth.py:153,157,162` `[union-attr]` | `member` stays `Member \| None` after block | 6 (branch on `if member is None:`) |
| `auth.py:269` `[assignment]` bool→`Column[bool]` | `is_revoked`/`is_mobile_verified` typed `Column[bool]` | 3 (`Mapped[bool]`) |
| `chat_service.py:123` `[call-overload]` | `tools` is `list[dict[str, Any]]` | 7 (cast to `list[ChatCompletionToolParam]`) |

## Notes / things NOT to do
- Do not edit anything under `alembic/` (mypy excludes it; migrations are unchanged).
- Do not add KYC/auction/bid endpoints; `deferred_schema.py` migration is schema-only as before.
- Do not add the `embedding` column to `PolicyChunk`.
- Do not change the OpenAI tool definitions in `app/ai/tools.py` (the cast lives at the call site).
- Do not delete financial records / change void logic — this plan touches no such code.
- If mypy is unavailable locally, install it (`pip install mypy`); `python -m py_compile` is only a syntax fallback, not a type check.
