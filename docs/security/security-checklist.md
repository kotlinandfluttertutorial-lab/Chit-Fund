# ChitSmart — Security Checklist

> Review this before every production deployment. Last reviewed: 15/10/2026.

---

## Authentication and session management

| Control | Implementation | Status |
|---|---|---|
| OTP stored as SHA-256 hash | `hashlib.sha256(otp.encode()).hexdigest()` in `auth.py` | ✅ |
| OTP expires after 10 minutes | `expires_at = now + timedelta(minutes=10)` | ✅ |
| OTP one-time use | `is_used = True` after first verification | ✅ |
| OTP rate-limited | Max 3 requests per mobile per hour | ✅ |
| JWT access token short-lived | 24-hour expiry, HS256 | ✅ |
| Refresh token hashed | `hashlib.sha256(token.encode()).hexdigest()` | ✅ |
| Refresh token revocable | `is_revoked = True` in member_sessions | ✅ |
| Token stored securely on device | EncryptedSharedPreferences (Android) | ⬜ TODO: confirm implementation |
| Admin users have separate auth | Separate `admin_users` table and JWT claim | ✅ |
| Session revocation on logout | `member_sessions.is_revoked = True` | ✅ |

## Authorisation

| Control | Implementation | Status |
|---|---|---|
| Every endpoint requires auth | `Depends(get_current_member)` or `get_current_admin` | ✅ |
| Member can only access own data | `WHERE member_id = current_member.id` in all queries | ✅ (verify in stub implementations) |
| Admin RBAC enforced server-side | Role claim in JWT, checked in `get_current_admin` | ⬜ TODO: implement role checking in deps.py |
| AI tools access own data only | `member_id` passed explicitly to all tool queries | ✅ |
| AI cannot write data | No write tool in `ACCOUNT_TOOLS` list | ✅ |
| Deferred endpoints not registered | KYC, auction, bid routes commented out in router | ✅ |

## Input validation

| Control | Implementation | Status |
|---|---|---|
| Mobile number validated (E.164) | Pydantic `field_validator` with regex `^\+91[6-9]\d{9}$` | ✅ |
| OTP is 6 digits only | Validated in verify-otp endpoint | ✅ |
| No Aadhaar/PAN accepted in register | Endpoint rejects any such fields (422) | ✅ |
| SQL injection prevention | SQLAlchemy parameterised queries (ORM) | ✅ |
| Payment amount not trusted from client | Amount read from `installment_schedules` table, not request body | ✅ |
| Webhook payload not trusted without signature | HMAC-SHA256 check before any processing | ✅ |

## Financial integrity

| Control | Implementation | Status |
|---|---|---|
| Payments never hard-deleted | `status = VOIDED + void_reason` pattern | ✅ |
| Ledger entries append-only | No DELETE on ledger_entries table | ✅ |
| Running balance calculated server-side | Never from client | ✅ |
| Idempotency keys | `receipt_number = unique` prevents duplicate payment records | ⬜ TODO: add idempotency key header support |
| Webhook deduplication | `gateway_reference` unique index prevents duplicate processing | ⬜ TODO: add unique index |
| Double-entry ledger | Every payment creates both debit and credit entries | ✅ |

## Secrets management

| Control | Requirement | Status |
|---|---|---|
| SECRET_KEY | Random 32-byte hex, never in source | ✅ `.env.example` has placeholder |
| DATABASE_URL | Contains password — never commit | ✅ `.gitignore` excludes `.env` |
| OPENAI_API_KEY | Never logged, never sent to client | ✅ Server-side only |
| PAYMENT_KEY_SECRET | Never sent to client (only key_id, when a gateway is integrated) | ✅ |
| OTP never logged in production | `DEBUG=true` check before logging | ✅ |
| PII not in logs | No mobile numbers, names, or IDs in log messages | ⬜ TODO: audit all log.info calls |

## Transport security

| Control | Requirement | Status |
|---|---|---|
| HTTPS in production | All traffic TLS 1.2+ | ⬜ TODO: configure reverse proxy (nginx) |
| HTTP → HTTPS redirect | 301 redirect for all HTTP | ⬜ TODO |
| Certificate pinning (Android) | Optional for production | ⬜ TODO: evaluate |
| CORS | Configured in `main.py` with `ALLOWED_ORIGINS` env | ✅ |

## Data protection

| Control | Requirement | Status |
|---|---|---|
| Aadhaar numbers encrypted at rest | `aadhaar_number_encrypted` column (not collected in this release) | ✅ (deferred) |
| PAN numbers encrypted at rest | `pan_number_encrypted` column (not collected in this release) | ✅ (deferred) |
| Bank account numbers encrypted | `account_number_encrypted` column | ⬜ TODO: implement encryption in bank_accounts service |
| Backups encrypted | Managed DB backup with encryption at rest | ⬜ TODO: configure on production DB |

## Compliance reminders

> These are not legal advice. Qualified legal and compliance review is required before production.

| Regulation | Applicability | Action required |
|---|---|---|
| Chit Funds Act, 1982 | Governs all chit fund operations | Legal review of terms documents, foreman registration |
| State Chit Fund Rules | State-specific requirements (Tamil Nadu, Kerala, etc.) | Confirm foreman registration number per state |
| DPDPA 2023 | Digital Personal Data Protection Act | Privacy notice, consent capture, data retention policy |
| PMLA 2002 | Anti-money laundering | KYC required before large transactions (deferred) |
| RBI Payment Guidelines | UPI and payment gateway compliance | Select and onboard a payment aggregator before processing live payments |
| Income Tax | TDS on chit fund dividends | Consult CA before production |

---

## Pre-production security checklist

Run through this before going live:

- [ ] Replace all `.env` placeholders with real production values
- [ ] Rotate `SECRET_KEY` — never use the dev key in production
- [ ] Enable HTTPS on the API server
- [ ] Configure `ALLOWED_ORIGINS` to production domain only (no localhost)
- [ ] Enable PostgreSQL SSL connections (`sslmode=require` in `DATABASE_URL`)
- [ ] Set `DEBUG=false` in production `.env`
- [ ] Review all `log.info` calls — remove any that contain OTPs, mobile numbers, or account numbers
- [ ] If a payment gateway is integrated: confirm webhook secret is set and distinct from dev keys
- [ ] Run `pip audit` — check for vulnerable Python packages
- [ ] Run `npm audit` — check for vulnerable Node packages
- [ ] If a payment gateway is integrated: test the webhook handler with the gateway's test mode before going live
- [ ] Confirm `alembic upgrade head` runs successfully on the production database
- [ ] Take a database backup before first production migration
- [ ] Confirm legal review is complete for the chit fund's state registration
