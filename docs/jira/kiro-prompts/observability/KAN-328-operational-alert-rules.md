# Kiro Prompt — KAN-328: Operational Alert Rules

**Ticket:** KAN-328  
**Epic:** KAN-317 — Observability  
**Priority:** High  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-013  
**Prerequisites:** KAN-318 (logging), KAN-320 (Prometheus metrics)  
**Files:**
- `infra/alerting/prometheus-rules.yml` (new)
- `infra/alerting/README.md` (new)
- `backend/app/core/config.py` (alert threshold env vars)

---

## Objective

Define all operational alert rules as code (Prometheus alerting rules YAML) covering
every threshold from REQ-OBS-013. All thresholds must be configurable via environment
variables — not hardcoded. Include a README explaining how to load these rules into
Prometheus / Grafana Cloud / an equivalent.

## Repository inspection

Before writing any code:
1. Check if `infra/` directory exists. If not, create it with `alerting/` subdirectory.
2. Read `backend/app/core/config.py` — identify where to add alert threshold constants.
3. Read `infra/docker/` or `docker-compose.yml` — check if Prometheus is already
   configured locally. If not, add a note in the README.

## `infra/alerting/prometheus-rules.yml`

```yaml
groups:
  - name: chitsmart_api
    rules:

      # ── API error rate ────────────────────────────────────────────────────
      - alert: HighAPIErrorRate
        expr: |
          (
            sum(rate(http_requests_total{status_code=~"5.."}[5m]))
            /
            sum(rate(http_requests_total[5m]))
          ) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "API 5xx error rate above 5%"
          description: >
            {{ $value | humanizePercentage }} of requests are returning 5xx.
            Investigate backend logs with the latest correlation IDs.

      # ── API p95 latency ───────────────────────────────────────────────────
      - alert: HighAPILatency
        expr: |
          histogram_quantile(0.95,
            sum by (le) (rate(http_request_duration_seconds_bucket[5m]))
          ) > 2.0
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API p95 latency above 2 seconds"
          description: "p95 latency is {{ $value | humanizeDuration }}."

      # ── Database pool overflow ────────────────────────────────────────────
      - alert: DatabasePoolOverflow
        expr: db_pool_overflow > 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool in overflow"
          description: "{{ $value }} overflow connections active for > 2 minutes."

      # ── Payment webhook signature failures ───────────────────────────────
      - alert: WebhookSignatureFailures
        expr: |
          increase(payment_webhooks_received_total{valid_signature="false"}[1h]) > 3
        for: 0m
        labels:
          severity: warning
        annotations:
          summary: "Multiple payment webhook signature failures"
          description: "{{ $value }} invalid signatures in the last hour. Possible replay attack."

      # ── OTP send failures ────────────────────────────────────────────────
      - alert: OTPSendFailures
        expr: |
          increase(otp_sent_total{success="false"}[10m]) > 5
        for: 0m
        labels:
          severity: warning
        annotations:
          summary: "High OTP send failure rate"
          description: "{{ $value }} OTP send failures in the last 10 minutes."

      # ── AI request error rate ─────────────────────────────────────────────
      - alert: AIRequestHighErrorRate
        expr: |
          (
            sum(rate(ai_tool_calls_total{success="false"}[15m]))
            /
            sum(rate(ai_requests_total[15m]))
          ) > 0.10
        for: 15m
        labels:
          severity: warning
        annotations:
          summary: "AI assistant error rate above 10%"
          description: "{{ $value | humanizePercentage }} of AI requests are failing."

      # ── Health check failure ──────────────────────────────────────────────
      - alert: HealthCheckFailing
        expr: up{job="chitsmart-api"} == 0
        for: 0m
        labels:
          severity: critical
        annotations:
          summary: "ChitSmart API health check failing"
          description: "The /ready endpoint is returning non-200. Immediate investigation required."

      # ── Reconciliation discrepancy unresolved ─────────────────────────────
      - alert: ReconciliationDiscrepancyUnresolved
        expr: |
          reconciliation_discrepancies_total > 0
        for: 24h
        labels:
          severity: warning
        annotations:
          summary: "Payment reconciliation discrepancy unresolved for 24 hours"
          description: "{{ $value }} discrepancy(ies) open. Finance team action required."

  - name: chitsmart_ai_cost
    rules:

      # ── AI hourly cost ────────────────────────────────────────────────────
      # Note: This rule requires a recording rule for cost.
      # Approximate using token totals × pricing constants.
      - alert: AIHourlyCostHigh
        expr: |
          (
            increase(ai_tokens_used_total{direction="prompt"}[1h]) * 0.000005
            + increase(ai_tokens_used_total{direction="completion"}[1h]) * 0.000015
          ) > 5.0
        for: 0m
        labels:
          severity: warning
        annotations:
          summary: "AI estimated cost exceeds $5 in one hour"
          description: "Estimated AI cost is ${{ $value | humanize }} this hour."
```

## Alert threshold env vars — `backend/app/core/config.py`

Document (as comments) where these thresholds come from, so operators know to
adjust the Prometheus rules when they change the env vars:

```python
# Observability alert thresholds — also mirrored in infra/alerting/prometheus-rules.yml
ALERT_ERROR_RATE_THRESHOLD: float = float(os.getenv("ALERT_ERROR_RATE_THRESHOLD", "0.05"))
ALERT_LATENCY_P95_MS: int = int(os.getenv("ALERT_LATENCY_P95_MS", "2000"))
ALERT_WEBHOOK_FAILURES_PER_HOUR: int = int(os.getenv("ALERT_WEBHOOK_FAILURES_PER_HOUR", "3"))
ALERT_OTP_FAILURES_PER_10MIN: int = int(os.getenv("ALERT_OTP_FAILURES_PER_10MIN", "5"))
ALERT_AI_ERROR_RATE_THRESHOLD: float = float(os.getenv("ALERT_AI_ERROR_RATE_THRESHOLD", "0.10"))
ALERT_AI_HOURLY_COST_USD: float = float(os.getenv("ALERT_AI_HOURLY_COST_USD", "5.0"))
ALERT_AI_SINGLE_REQUEST_COST_USD: float = float(os.getenv("ALERT_AI_SINGLE_REQUEST_COST_USD", "0.10"))
```

## `infra/alerting/README.md`

Document:
- How to load `prometheus-rules.yml` into a self-hosted Prometheus
- How to import as a Grafana Cloud managed alerts rule group
- How to set up a notification channel (email, Slack webhook, PagerDuty)
- How to test an alert fires correctly using `amtool` or Grafana's alert testing UI
- Which thresholds correspond to which environment variables

## Acceptance criteria

- [ ] `prometheus-rules.yml` is valid YAML and passes `promtool check rules`
- [ ] All 9 alert rules from REQ-OBS-013 are present
- [ ] Each alert has `severity` label and human-readable `annotations.description`
- [ ] `infra/alerting/README.md` explains how to load and test the rules
- [ ] Alert threshold env vars are documented in `config.py` with comments
- [ ] `ruff check` passes on `config.py` changes

## Definition of done

- `promtool check rules infra/alerting/prometheus-rules.yml` passes (if `promtool` is available; otherwise YAML validation passes)
- README reviewed by at least one team member
- All 9 rules present and correctly reference the metrics from KAN-320
