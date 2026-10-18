# Observability — Requirements

**Epic:** CHIT-317  
**Spec path:** `.kiro/specs/observability/`  
**Steering:** `.kiro/steering/architecture.md`, `.kiro/steering/security.md`  
**Scope:** Cross-cutting — applies to backend API, admin dashboard, and Android app  
**Release:** In scope — current release

---

## Business context

ChitSmart handles real financial transactions, AI-generated responses, and OTP-based
authentication. When something breaks in production — a failed payment webhook, a
degraded AI response, a spike in OTP failures — the operations team must be able to
detect it, diagnose it, and recover within minutes, not hours. Observability is the
foundation that makes that possible.

Observability is not a post-launch add-on. Every API endpoint, payment event, AI
interaction, and authentication flow must emit structured signals from day one.

---

## Actors

| Actor | Observability concern |
|---|---|
| Backend API | Emits structured logs, metrics, and traces for every request and background event |
| Admin dashboard | Reports client-side errors and API response times to error tracking |
| Android app | Reports crashes, ANRs, and slow screen renders to crash analytics |
| Operations staff | Views dashboards, receives alerts, investigates incidents |
| Auditor | Read-only access to audit logs and operational metrics |

---

## Functional requirements

### REQ-OBS-001 — Structured logging (backend)

The backend MUST emit every log entry as a structured JSON object to stdout.

Each log record MUST contain:

| Field | Description |
|---|---|
| `timestamp` | ISO 8601 UTC (e.g. `2026-10-17T09:42:01.123Z`) |
| `level` | `DEBUG`, `INFO`, `WARNING`, `ERROR`, `CRITICAL` |
| `correlation_id` | UUID generated per request, propagated through all log lines for that request |
| `request_id` | Same as `correlation_id` on HTTP requests; task ID on background jobs |
| `path` | HTTP path (e.g. `/api/v1/payments`) |
| `method` | HTTP method |
| `status_code` | HTTP response status |
| `duration_ms` | Request processing time in milliseconds |
| `user_id` | Authenticated member or admin ID (omitted for anonymous requests) |
| `role` | Admin role claim if present |
| `module` | Source module name (e.g. `app.services.payment`) |
| `message` | Human-readable description |
| `error` | Exception type and message (ERROR/CRITICAL only) |
| `stack_trace` | Full traceback (CRITICAL only, redacted in production if it contains PII) |

The backend MUST propagate `correlation_id` in the response header `X-Correlation-ID`
so that the admin dashboard and Android app can log it for cross-system tracing.

### REQ-OBS-002 — Request/response logging middleware

The backend MUST have FastAPI middleware that automatically logs every inbound request
and its outgoing response at `INFO` level, including `correlation_id`, `path`, `method`,
`status_code`, and `duration_ms`.

Request body logging is PROHIBITED by default. It MAY be enabled at `DEBUG` level in
local development only and MUST be disabled in staging and production. Logging request
bodies in production would expose PII and financial data.

Response body logging is PROHIBITED in all environments.

### REQ-OBS-003 — Domain event logging

The backend MUST emit a structured log entry at `INFO` level for every significant
domain event. Mandatory events:

| Event | Log fields |
|---|---|
| OTP sent | `member_id` (or mobile number last-4), `provider`, `success` |
| OTP verified | `member_id`, `attempt_number`, `success` |
| OTP verification failed | `member_id`, `attempt_number`, `remaining_attempts` |
| Member registered | `member_id` |
| Login successful | `member_id`, `device_info` (OS + version only, no device ID) |
| Login failed (wrong OTP) | masked mobile, attempt count |
| Token refreshed | `member_id` |
| Payment order created | `payment_id`, `member_id`, `group_id`, `amount_paise` |
| Payment webhook received | `payment_id`, `gateway_ref`, `event_type` |
| Webhook signature valid | `payment_id`, `success: true` |
| Webhook signature invalid | `payment_id`, `success: false` — log at `WARNING` |
| Payment status updated | `payment_id`, `old_status`, `new_status` |
| Ledger entry appended | `entry_id`, `payment_id`, `debit`, `credit` |
| AI chat request | `member_id`, `message_length`, `language_detected` |
| AI tool call | `tool_name`, `member_id` — MUST NOT log tool result values |
| AI response completed | `member_id`, `tokens_used`, `model`, `duration_ms`, `cost_usd_approx` |
| AI tool call denied | `tool_name`, `reason`, `member_id` — log at `WARNING` |
| Admin action | `admin_id`, `role`, `action`, `entity_type`, `entity_id` |
| Reconciliation approved | `date`, `admin_id`, `matched_count`, `unmatched_count` |

### REQ-OBS-004 — Sensitive data redaction

The logging system MUST NOT emit:

- Full mobile numbers (log last 4 digits only: `XXXXXX4210`)
- Full bank account numbers (log last 4 digits only)
- OTP values
- JWT tokens or refresh tokens
- Aadhaar or PAN numbers
- Full names in error stack traces
- Payment gateway secret keys or HMAC values
- OpenAI API key
- Any value from request or response bodies

Violation of this requirement is a security incident and MUST be treated as a critical bug.

### REQ-OBS-005 — Health and readiness endpoints

The backend MUST expose:

**`GET /health`** — Liveness check. Always returns `200 OK` if the process is running.

```json
{ "status": "ok", "timestamp": "2026-10-17T09:42:01Z" }
```

**`GET /ready`** — Readiness check. Returns `200 OK` only when all dependencies are
reachable. Returns `503 Service Unavailable` if any dependency is unhealthy.

```json
{
  "status": "ready",
  "checks": {
    "database": "ok",
    "openai": "ok"
  },
  "timestamp": "2026-10-17T09:42:01Z"
}
```

Database check: execute `SELECT 1`. Return `"degraded"` on timeout or connection error.  
OpenAI check: verify the API key is configured (do NOT make a live API call on every
readiness probe — check environment variable presence only).

Both endpoints MUST be excluded from authentication middleware.  
Both endpoints MUST be excluded from request logging middleware (to avoid log noise from
container health checks polling every 5 seconds).

### REQ-OBS-006 — Application metrics (backend)

The backend MUST expose a Prometheus-compatible metrics endpoint at **`GET /metrics`**
(internal — not proxied to the internet). At minimum, the following metrics MUST be
collected:

**HTTP layer:**

| Metric | Type | Labels |
|---|---|---|
| `http_requests_total` | Counter | `method`, `path`, `status_code` |
| `http_request_duration_seconds` | Histogram | `method`, `path`, `status_code` |
| `http_requests_in_flight` | Gauge | — |

**Business domain:**

| Metric | Type | Labels |
|---|---|---|
| `otp_sent_total` | Counter | `provider`, `success` |
| `otp_verified_total` | Counter | `success` |
| `payment_orders_created_total` | Counter | `method` (UPI/NetBanking/etc.) |
| `payment_webhooks_received_total` | Counter | `event_type`, `valid_signature` |
| `payment_status_transitions_total` | Counter | `from_status`, `to_status` |
| `ledger_entries_appended_total` | Counter | `entry_type` |
| `ai_requests_total` | Counter | `language` |
| `ai_tokens_used_total` | Counter | `model`, `direction` (prompt/completion) |
| `ai_tool_calls_total` | Counter | `tool_name`, `success` |
| `ai_request_duration_seconds` | Histogram | `model` |
| `reconciliation_discrepancies_total` | Counter | — |

**Infrastructure:**

| Metric | Type | Description |
|---|---|---|
| `db_pool_size` | Gauge | SQLAlchemy pool size |
| `db_pool_checked_out` | Gauge | Active DB connections |
| `db_pool_overflow` | Gauge | Overflow connections in use |
| `db_query_duration_seconds` | Histogram | Labels: `query_type` |

Path labels MUST be normalised to remove dynamic segments:
`/api/v1/members/123` → `/api/v1/members/{member_id}` to prevent label cardinality
explosion.

`GET /metrics` MUST be excluded from authentication middleware but MUST only be
accessible from the internal network (not exposed via the public-facing load balancer).

### REQ-OBS-007 — Error tracking (backend)

The backend MUST capture all unhandled exceptions and log them at `CRITICAL` level with
the full stack trace (PII redacted).

The backend MUST have a FastAPI exception handler that:
- Catches all unhandled `Exception` instances
- Logs at `CRITICAL` with `correlation_id`, `path`, `method`, `error`, `stack_trace`
- Returns a sanitised `500` response to the client (no internal details in the response)

For production deployments, a third-party error tracking service (e.g. Sentry) MAY be
integrated. If integrated, it MUST use a DSN stored as an environment variable and MUST
be configured to scrub PII from payloads before transmission.

### REQ-OBS-008 — Slow query detection (database)

The backend MUST log any database query that takes longer than **500 ms** at `WARNING`
level, including the normalised query template (no bound parameter values) and duration.

The backend MUST log any database query that takes longer than **2000 ms** at `ERROR`
level.

### REQ-OBS-009 — AI cost and quality tracking

The backend MUST log the following for every OpenAI API call:

- Model name
- Prompt token count
- Completion token count
- Total token count
- Estimated cost in USD (calculated from current OpenAI pricing — not fetched live,
  stored as a config constant)
- Latency in milliseconds (time from API call to first token received, and to full
  response)
- Whether a tool was called and which tool
- Whether the response was truncated (`finish_reason`)

These fields MUST be stored in a `ai_usage_log` table in addition to being logged, so
that the Admin → AI Tools → AI Usage report can display monthly cost summaries.

The backend MUST alert (log at `ERROR`) when a single AI request exceeds:
- 8,000 prompt tokens
- 3 seconds to first token
- $0.10 estimated cost per single request

### REQ-OBS-010 — Correlation ID propagation (cross-service)

Every HTTP request from the Android app MUST include a client-generated
`X-Correlation-ID` header (UUID v4).

The backend MUST:
1. Read `X-Correlation-ID` from the request header if present.
2. Generate a new UUID if the header is absent.
3. Include the `correlation_id` in every log line for that request.
4. Return the `correlation_id` in the `X-Correlation-ID` response header.

The admin dashboard MUST log the `X-Correlation-ID` from every API error response to
the browser console and to error tracking so that a support agent can correlate a user
complaint with a backend log entry.

### REQ-OBS-011 — Admin dashboard error tracking

The admin dashboard MUST integrate a client-side error tracker (e.g. Sentry browser
SDK) that captures:

- Unhandled JavaScript exceptions
- Unhandled promise rejections
- React error boundary catches
- API requests that return 4xx or 5xx, including the `X-Correlation-ID` header value

The DSN and environment name MUST be injected via `VITE_SENTRY_DSN` and
`VITE_SENTRY_ENVIRONMENT` at build time.

If `VITE_SENTRY_DSN` is not set, the integration MUST be silently disabled (no errors,
no console warnings). This allows local development to run without a Sentry account.

### REQ-OBS-012 — Android crash reporting

The Android app MUST integrate Firebase Crashlytics (or an equivalent crash reporting
SDK) that captures:

- Native crashes
- Uncaught Kotlin exceptions
- ANR (Application Not Responding) events
- Non-fatal exceptions logged via `recordException()`

The app MUST log the `correlation_id` of the last API request as a Crashlytics key
`last_correlation_id` so crashes can be correlated with backend logs.

Crashlytics MUST be disabled for debug builds (`BuildConfig.DEBUG == true`) to avoid
polluting the production error feed during development.

### REQ-OBS-013 — Operational alerts

The following conditions MUST trigger an alert to the operations team (via a configured
notification channel — email, PagerDuty, Slack webhook, or equivalent):

| Condition | Severity | Threshold |
|---|---|---|
| API error rate (`5xx / total`) | Critical | > 5 % over 5 minutes |
| API p95 latency | Warning | > 2,000 ms over 5 minutes |
| Database connection pool exhausted | Critical | `db_pool_overflow > 0` for 2 minutes |
| Payment webhook signature failures | Warning | > 3 in 1 hour |
| OTP send failures | Warning | > 5 in 10 minutes |
| AI request error rate | Warning | > 10 % over 15 minutes |
| AI estimated cost | Warning | > $5 in a single hour |
| Health check failure (`/ready` → non-200) | Critical | Any occurrence |
| Disk usage (database volume) | Warning | > 80 % |
| Reconciliation discrepancy unresolved | Warning | Open > 24 hours |

Alert configuration MUST be stored as infrastructure code (e.g. Prometheus alerting
rules, Grafana alert rules, or equivalent). Alert thresholds MUST be configurable via
environment variables — not hardcoded.

### REQ-OBS-014 — Audit trail completeness

Every admin action that creates, modifies, or changes the status of a financial record
MUST be written to the `audit_log` table before the response is returned to the caller.
The write MUST occur in the same database transaction as the business operation. If the
audit write fails, the entire transaction MUST be rolled back — a business operation
MUST NOT succeed without a corresponding audit record.

Audit log records MUST be immutable. The application MUST NOT provide any endpoint that
deletes or modifies audit log entries.

Audit log entries MUST contain: `admin_id`, `action`, `entity_type`, `entity_id`,
`before_state` (JSON snapshot), `after_state` (JSON snapshot), `ip_address`,
`timestamp`.

The `before_state` and `after_state` MUST NOT contain raw secrets, tokens, or full
bank account numbers.

### REQ-OBS-015 — Log retention and export

Logs MUST be retained for a minimum of 90 days in the operational log store.  
Audit logs MUST be retained for a minimum of 7 years to satisfy Chit Funds Act
record-keeping requirements.  
Metrics data MUST be retained for a minimum of 13 months (to enable year-over-year
comparison).

Logs and metrics MUST be exportable by authorised personnel (Super Admin and Auditor
roles) via the Admin → Reports → Audit Log screen.

---

## Non-functional requirements

| Ref | Requirement |
|---|---|
| REQ-OBS-NFR-01 | Logging middleware MUST add no more than 2 ms to p99 request latency. |
| REQ-OBS-NFR-02 | The `/health` endpoint MUST respond in under 50 ms. |
| REQ-OBS-NFR-03 | The `/ready` endpoint MUST respond in under 200 ms (includes one `SELECT 1`). |
| REQ-OBS-NFR-04 | The `/metrics` endpoint MUST respond in under 100 ms. |
| REQ-OBS-NFR-05 | Log writes MUST be asynchronous and MUST NOT block request processing. |
| REQ-OBS-NFR-06 | Prometheus metrics MUST use correct types — never use a Gauge where a Counter is appropriate. |
| REQ-OBS-NFR-07 | Metric label cardinality MUST remain bounded — no user IDs, payment IDs, or other high-cardinality values in labels. |
| REQ-OBS-NFR-08 | Structured logs MUST be valid JSON — a malformed log line MUST NOT crash the application. |
| REQ-OBS-NFR-09 | All observability configuration (DSNs, log level, retention) MUST be injectable via environment variables. |
| REQ-OBS-NFR-10 | Observability components MUST degrade gracefully — if the metrics endpoint fails, requests MUST still be served. |

---

## Acceptance criteria

### AC-OBS-001 — Structured log format

**Given** any HTTP request is made to the backend  
**When** the request completes  
**Then** exactly one JSON log line is emitted to stdout containing `timestamp`, `level`,
`correlation_id`, `path`, `method`, `status_code`, and `duration_ms`  
**And** the log line is valid JSON parseable by `json.loads()`  
**And** no mobile number, token, or OTP value appears in the log line

### AC-OBS-002 — Correlation ID round-trip

**Given** the Android app sends a request with header `X-Correlation-ID: abc-123`  
**When** the backend processes the request  
**Then** every log line for that request contains `"correlation_id": "abc-123"`  
**And** the response includes header `X-Correlation-ID: abc-123`

### AC-OBS-003 — Health endpoint

**Given** the backend is running and the database is reachable  
**When** `GET /health` is called  
**Then** the response is `200 OK` with body `{"status": "ok", ...}`  
**And** the endpoint does not require an `Authorization` header

### AC-OBS-004 — Readiness endpoint degradation

**Given** the backend is running but the database is unreachable  
**When** `GET /ready` is called  
**Then** the response is `503 Service Unavailable`  
**And** the body contains `{"checks": {"database": "degraded"}, ...}`

### AC-OBS-005 — Metrics endpoint

**Given** the backend is running  
**When** `GET /metrics` is called  
**Then** the response body contains `http_requests_total`, `http_request_duration_seconds`,
`otp_sent_total`, `payment_orders_created_total`, and `ai_requests_total` in
Prometheus text format

### AC-OBS-006 — Payment event logging

**Given** a payment webhook is received with an invalid signature  
**When** the backend processes it  
**Then** a `WARNING` log line is emitted containing `"event": "webhook_signature_invalid"`
and the `payment_id`  
**And** the gateway reference does NOT appear in a `CRITICAL` log (it should appear
only in `WARNING` or `INFO`)

### AC-OBS-007 — AI cost logging

**Given** an AI chat request is processed  
**When** the OpenAI response is received  
**Then** a log line is emitted containing `tokens_used`, `model`, `duration_ms`, and
`cost_usd_approx`  
**And** a row is inserted into the `ai_usage_log` table

### AC-OBS-008 — Sensitive data absent from logs

**Given** a member's OTP verification attempt is logged  
**When** the log output is inspected  
**Then** no full mobile number, OTP value, JWT token, or bank account number appears  
**And** the mobile number is represented as the last 4 digits only (e.g. `****4210`)

### AC-OBS-009 — Slow query warning

**Given** a database query takes longer than 500 ms  
**When** the query completes  
**Then** a `WARNING` log line is emitted containing `duration_ms` and the normalised
query template  
**And** no bound parameter values (which may contain PII) appear in the log line

### AC-OBS-010 — Audit write atomicity

**Given** an admin performs an action that modifies a financial record  
**When** the database write succeeds  
**Then** an audit log row exists in the same transaction  
**And** if the audit log write fails, the business operation is rolled back  
**And** no orphaned business record without an audit entry exists

---

## Security and privacy requirements

- Logs MUST be treated as sensitive data — access to the log store MUST require
  authentication and role-based authorisation.
- Log data MUST NOT be written to the Android device's external storage. Android logs
  visible via `adb logcat` in debug builds MUST NOT appear in release builds.
- The `/metrics` endpoint MUST NOT be exposed on the public-facing port or via the
  API gateway. It MUST only be reachable from internal infrastructure (VPC, private
  subnet, or equivalent).
- Sentry and Crashlytics data transmission MUST be covered by the platform's privacy
  policy and data processing agreement. PII scrubbing MUST be configured before any
  data is transmitted.
- Log access by operations staff MUST itself be logged (access to audit logs is audited).

---

## Dependencies and assumptions

- Backend is deployed in an environment that supports stdout log aggregation
  (Docker + a log driver, or a managed cloud service that collects container stdout).
- A Prometheus-compatible scrape endpoint is reachable from whatever metrics collection
  system the team operates (Grafana Cloud, self-hosted Prometheus, Datadog agent, etc.).
- Firebase project is configured for the Android app (required for Crashlytics).
- A Sentry project (or equivalent) is configured for the admin dashboard.
- Alert delivery channel (email, Slack, PagerDuty) is agreed with operations team before
  production launch.
- `ai_usage_log` table must be added to the database schema via an Alembic migration.

---

## Out of scope (this release)

- Distributed tracing (OpenTelemetry spans, Jaeger, Tempo) — planned for when the
  architecture grows beyond a single service.
- Real-time streaming dashboards with WebSocket push — deferred with auctions.
- Automated anomaly detection using ML — future release.
- Log-based billing alerts directly from cloud provider — operational configuration
  outside this spec.

---

## Related documents

| Document | Relevance |
|---|---|
| `.kiro/steering/architecture.md` | Architectural constraints — modular monolith, no unnecessary services |
| `.kiro/steering/security.md` | Security constraints governing PII handling and log redaction |
| `.kiro/steering/financial-integrity.md` | Audit trail atomicity requirements |
| `docs/architecture/system-overview.md` | System topology — where observability signals flow |
| `docs/deployment/cicd.md` | CI/CD pipeline — where health checks are exercised |
| `docs/deployment/build-variants.md` | Environment-specific observability configuration |
| `docs/jira/epics.md` | Parent epic: CHIT-317 — Observability |
