"""
API v1 router — registers all endpoint modules.
KYC, auction, and bid endpoints are excluded from this release.
See docs/scope-decisions.md for rationale.
"""

from fastapi import APIRouter

from app.api.v1.endpoints import (
    admin_ai,
    admin_chit_groups,
    admin_installments,
    admin_members,
    admin_payments,
    admin_reports,
    ai_assistant,
    auth,
    chit_groups,
    installments,
    ledger,
    members,
    payments,
    payouts,
    reports,
)

api_router = APIRouter()

# ── Customer endpoints ────────────────────────────────────────────────────────
api_router.include_router(auth.router, prefix="/auth", tags=["Auth"])
api_router.include_router(members.router, prefix="/members", tags=["Members"])
api_router.include_router(chit_groups.router, prefix="/chit-groups", tags=["Chit Groups"])
api_router.include_router(installments.router, prefix="/installments", tags=["Installments"])
api_router.include_router(payments.router, prefix="/payments", tags=["Payments"])
api_router.include_router(payouts.router, prefix="/payouts", tags=["Payouts"])
api_router.include_router(ledger.router, prefix="/ledger", tags=["Ledger"])
api_router.include_router(reports.router, prefix="/reports", tags=["Reports"])
api_router.include_router(ai_assistant.router, prefix="/ai", tags=["AI Assistant"])

# ── Admin endpoints ───────────────────────────────────────────────────────────
api_router.include_router(admin_members.router, prefix="/admin/members", tags=["Admin — Members"])
api_router.include_router(admin_chit_groups.router, prefix="/admin/chit-groups", tags=["Admin — Chit Groups"])
api_router.include_router(admin_installments.router, prefix="/admin/installments", tags=["Admin — Installments"])
api_router.include_router(admin_payments.router, prefix="/admin/payments", tags=["Admin — Payments"])
api_router.include_router(admin_reports.router, prefix="/admin/reports", tags=["Admin — Reports"])
api_router.include_router(admin_ai.router, prefix="/admin/ai", tags=["Admin — AI Tools"])

# ── Deferred (not registered in this release) ─────────────────────────────────
# kyc.*        — deferred: regulatory prerequisites not met
# auctions.*   — deferred: real-time bidding infrastructure not ready
# bids.*        — deferred: with auctions
# admin/kyc.*  — deferred: with KYC
# admin/auctions.* — deferred: with auctions
# payouts approval — deferred: with auctions (payouts above is read-only GET only)
