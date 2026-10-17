"""Initial schema — all tables including deferred schema.

Revision ID: 001_initial_schema
Revises:
Create Date: 2026-10-15

Deferred tables (kyc_documents, auctions, bids) are created but have no API endpoints.
See docs/scope-decisions.md for rationale.
"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects.postgresql import UUID
import uuid

revision = "001_initial_schema"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    # ── Enable extensions ─────────────────────────────────────────────────────
    op.execute("CREATE EXTENSION IF NOT EXISTS vector;")
    op.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm;")

    # ── members ───────────────────────────────────────────────────────────────
    op.create_table(
        "members",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("full_name", sa.String(255), nullable=False),
        sa.Column("mobile_number", sa.String(15), nullable=False, unique=True),
        sa.Column("email", sa.String(255), nullable=True),
        sa.Column("date_of_birth", sa.Date, nullable=True),
        sa.Column("gender", sa.String(30), nullable=True),
        sa.Column("account_status", sa.String(20), nullable=False, server_default="ACTIVE"),
        sa.Column("is_mobile_verified", sa.Boolean, nullable=False, server_default="false"),
        # KYC — schema present, NOT exposed via API in this release
        sa.Column("kyc_status", sa.String(30), nullable=False, server_default="NOT_REQUIRED"),
        sa.Column("aadhaar_number_encrypted", sa.String(512), nullable=True),
        sa.Column("pan_number_encrypted", sa.String(512), nullable=True),
        sa.Column("preferred_language", sa.String(10), nullable=False, server_default="en"),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now(), onupdate=sa.func.now()),
    )
    op.create_index("ix_members_mobile_number", "members", ["mobile_number"], unique=True)

    # ── admin_users ───────────────────────────────────────────────────────────
    op.create_table(
        "admin_users",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("name", sa.String(255), nullable=False),
        sa.Column("email", sa.String(255), nullable=False, unique=True),
        sa.Column("role", sa.String(30), nullable=False),  # SUPER_ADMIN | FINANCE_MANAGER | SUPPORT_AGENT | AUDITOR
        sa.Column("password_hash", sa.String(512), nullable=False),
        sa.Column("account_status", sa.String(20), nullable=False, server_default="ACTIVE"),
        sa.Column("last_login_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )

    # ── otp_sessions ─────────────────────────────────────────────────────────
    op.create_table(
        "otp_sessions",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("mobile_number", sa.String(15), nullable=False),
        sa.Column("otp_hash", sa.String(64), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("is_used", sa.Boolean, nullable=False, server_default="false"),
        sa.Column("attempt_count", sa.Integer, nullable=False, server_default="0"),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )
    op.create_index("ix_otp_sessions_mobile_number", "otp_sessions", ["mobile_number"])

    # ── member_sessions (refresh tokens) ─────────────────────────────────────
    op.create_table(
        "member_sessions",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("member_id", UUID(as_uuid=True), sa.ForeignKey("members.id"), nullable=False),
        sa.Column("refresh_token_hash", sa.String(64), nullable=False),
        sa.Column("device_info", sa.String(512), nullable=True),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("is_revoked", sa.Boolean, nullable=False, server_default="false"),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )
    op.create_index("ix_member_sessions_member_id", "member_sessions", ["member_id"])

    # ── chit_groups ───────────────────────────────────────────────────────────
    op.create_table(
        "chit_groups",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("name", sa.String(255), nullable=False, unique=True),
        sa.Column("chit_value", sa.Numeric(12, 2), nullable=False),
        sa.Column("monthly_installment", sa.Numeric(12, 2), nullable=False),
        sa.Column("duration_months", sa.Integer, nullable=False),
        sa.Column("max_members", sa.Integer, nullable=False),
        sa.Column("foreman_commission_pct", sa.Numeric(5, 2), nullable=False),
        sa.Column("start_date", sa.Date, nullable=False),
        sa.Column("status", sa.String(20), nullable=False, server_default="PENDING"),
        sa.Column("terms_document_url", sa.String(1024), nullable=True),
        sa.Column("description", sa.Text, nullable=True),
        # Auction schema field — present but unused in this release
        sa.Column("next_auction_date", sa.Date, nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )

    # ── chit_memberships ──────────────────────────────────────────────────────
    op.create_table(
        "chit_memberships",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("member_id", UUID(as_uuid=True), sa.ForeignKey("members.id"), nullable=False),
        sa.Column("chit_group_id", UUID(as_uuid=True), sa.ForeignKey("chit_groups.id"), nullable=False),
        sa.Column("member_number", sa.Integer, nullable=False),
        sa.Column("enrollment_date", sa.Date, nullable=False),
        sa.Column("status", sa.String(30), nullable=False, server_default="ACTIVE"),
        sa.Column("payout_received", sa.Boolean, nullable=False, server_default="false"),
        sa.Column("payout_month", sa.Integer, nullable=True),
        sa.Column("nominee_name", sa.String(255), nullable=True),
        sa.Column("nominee_relationship", sa.String(50), nullable=True),
        sa.Column("nominee_mobile", sa.String(15), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        sa.UniqueConstraint("member_id", "chit_group_id", name="uq_member_group"),
    )
    op.create_index("ix_chit_memberships_member_id", "chit_memberships", ["member_id"])
    op.create_index("ix_chit_memberships_group_id", "chit_memberships", ["chit_group_id"])

    # ── bank_accounts ─────────────────────────────────────────────────────────
    op.create_table(
        "bank_accounts",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("member_id", UUID(as_uuid=True), sa.ForeignKey("members.id"), nullable=False),
        sa.Column("bank_name", sa.String(255), nullable=False),
        sa.Column("account_number_encrypted", sa.String(512), nullable=False),
        sa.Column("ifsc_code", sa.String(11), nullable=False),
        sa.Column("account_type", sa.String(20), nullable=False, server_default="SAVINGS"),
        sa.Column("upi_vpa", sa.String(255), nullable=True),
        sa.Column("is_primary", sa.Boolean, nullable=False, server_default="false"),
        sa.Column("is_verified", sa.Boolean, nullable=False, server_default="false"),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )
    op.create_index("ix_bank_accounts_member_id", "bank_accounts", ["member_id"])

    # ── nominees ──────────────────────────────────────────────────────────────
    op.create_table(
        "nominees",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("member_id", UUID(as_uuid=True), sa.ForeignKey("members.id"), nullable=False),
        sa.Column("membership_id", UUID(as_uuid=True), sa.ForeignKey("chit_memberships.id"), nullable=True),
        sa.Column("full_name", sa.String(255), nullable=False),
        sa.Column("relationship", sa.String(50), nullable=False),
        sa.Column("mobile_number", sa.String(15), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )

    # ── installment_schedules ─────────────────────────────────────────────────
    op.create_table(
        "installment_schedules",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("chit_group_id", UUID(as_uuid=True), sa.ForeignKey("chit_groups.id"), nullable=False),
        sa.Column("month_number", sa.Integer, nullable=False),
        sa.Column("due_date", sa.DateTime(timezone=True), nullable=False),
        sa.Column("amount_per_member", sa.Numeric(12, 2), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )
    op.create_index("ix_installment_schedules_group_id", "installment_schedules", ["chit_group_id"])

    # ── payments ──────────────────────────────────────────────────────────────
    # Financial records are NEVER hard-deleted. Use status=VOIDED.
    op.create_table(
        "payments",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("member_id", UUID(as_uuid=True), sa.ForeignKey("members.id"), nullable=False),
        sa.Column("chit_group_id", UUID(as_uuid=True), sa.ForeignKey("chit_groups.id"), nullable=False),
        sa.Column("installment_schedule_id", UUID(as_uuid=True), sa.ForeignKey("installment_schedules.id"), nullable=False),
        sa.Column("amount", sa.Numeric(12, 2), nullable=False),
        sa.Column("late_fee", sa.Numeric(12, 2), nullable=False, server_default="0"),
        sa.Column("total_amount", sa.Numeric(12, 2), nullable=False),
        sa.Column("payment_method", sa.String(20), nullable=False),
        sa.Column("status", sa.String(20), nullable=False, server_default="PENDING"),
        sa.Column("gateway_reference", sa.String(255), nullable=True),
        sa.Column("bank_reference", sa.String(255), nullable=True),
        sa.Column("upi_vpa", sa.String(255), nullable=True),
        sa.Column("failure_reason", sa.String(512), nullable=True),
        sa.Column("void_reason", sa.String(512), nullable=True),
        sa.Column("receipt_number", sa.String(100), nullable=True, unique=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )
    op.create_index("ix_payments_member_id", "payments", ["member_id"])
    op.create_index("ix_payments_chit_group_id", "payments", ["chit_group_id"])
    op.create_index("ix_payments_status", "payments", ["status"])

    # ── ledger_entries ────────────────────────────────────────────────────────
    # Append-only. Never DELETE from this table.
    op.create_table(
        "ledger_entries",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("chit_group_id", UUID(as_uuid=True), sa.ForeignKey("chit_groups.id"), nullable=False),
        sa.Column("member_id", UUID(as_uuid=True), sa.ForeignKey("members.id"), nullable=True),
        sa.Column("payment_id", UUID(as_uuid=True), sa.ForeignKey("payments.id"), nullable=True),
        sa.Column("entry_type", sa.String(30), nullable=False),
        sa.Column("narration", sa.String(512), nullable=False),
        sa.Column("debit", sa.Numeric(12, 2), nullable=False, server_default="0"),
        sa.Column("credit", sa.Numeric(12, 2), nullable=False, server_default="0"),
        sa.Column("running_balance", sa.Numeric(14, 2), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )
    op.create_index("ix_ledger_entries_chit_group_id", "ledger_entries", ["chit_group_id"])

    # ── policy_chunks (AI RAG) ────────────────────────────────────────────────
    op.execute("CREATE EXTENSION IF NOT EXISTS vector;")
    op.create_table(
        "policy_chunks",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("document_name", sa.String(255), nullable=False),
        sa.Column("section_reference", sa.String(255), nullable=False),
        sa.Column("content", sa.Text, nullable=False),
        sa.Column("language", sa.String(10), nullable=False, server_default="en"),
        sa.Column("page_number", sa.Integer, nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )
    # Add vector column separately (pgvector syntax)
    op.execute("ALTER TABLE policy_chunks ADD COLUMN embedding vector(1536);")
    # IVFFlat index for cosine similarity search
    op.execute("""
        CREATE INDEX ix_policy_chunks_embedding
        ON policy_chunks
        USING ivfflat (embedding vector_cosine_ops)
        WITH (lists = 100);
    """)

    # ── payouts (read-only GET in this release) ───────────────────────────────
    op.create_table(
        "payouts",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("member_id", UUID(as_uuid=True), sa.ForeignKey("members.id"), nullable=False),
        sa.Column("chit_group_id", UUID(as_uuid=True), sa.ForeignKey("chit_groups.id"), nullable=False),
        sa.Column("month_number", sa.Integer, nullable=False),
        sa.Column("gross_amount", sa.Numeric(12, 2), nullable=False),
        sa.Column("foreman_commission", sa.Numeric(12, 2), nullable=False),
        sa.Column("net_amount", sa.Numeric(12, 2), nullable=False),
        sa.Column("bank_account_id", UUID(as_uuid=True), sa.ForeignKey("bank_accounts.id"), nullable=True),
        sa.Column("bank_reference", sa.String(255), nullable=True),
        sa.Column("status", sa.String(20), nullable=False, server_default="PENDING"),
        sa.Column("credit_date", sa.DateTime(timezone=True), nullable=True),
        sa.Column("approved_by_admin_id", UUID(as_uuid=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )
    op.create_index("ix_payouts_member_id", "payouts", ["member_id"])

    # ── DEFERRED: kyc_documents (schema only — no API) ────────────────────────
    op.create_table(
        "kyc_documents",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("member_id", UUID(as_uuid=True), sa.ForeignKey("members.id"), nullable=False),
        sa.Column("document_type", sa.String(30), nullable=False),
        sa.Column("file_url", sa.String(1024), nullable=True),
        sa.Column("status", sa.String(30), nullable=False, server_default="UPLOADED"),
        sa.Column("extracted_data", sa.Text, nullable=True),
        sa.Column("reviewer_notes", sa.Text, nullable=True),
        sa.Column("reviewed_by_admin_id", UUID(as_uuid=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )

    # ── DEFERRED: auctions (schema only — no API) ──────────────────────────────
    op.create_table(
        "auctions",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("chit_group_id", UUID(as_uuid=True), sa.ForeignKey("chit_groups.id"), nullable=False),
        sa.Column("month_number", sa.Integer, nullable=False),
        sa.Column("scheduled_start", sa.DateTime(timezone=True), nullable=False),
        sa.Column("scheduled_end", sa.DateTime(timezone=True), nullable=False),
        sa.Column("min_bid", sa.Numeric(12, 2), nullable=False),
        sa.Column("max_bid", sa.Numeric(12, 2), nullable=False),
        sa.Column("status", sa.String(20), nullable=False, server_default="SCHEDULED"),
        sa.Column("winner_member_id", UUID(as_uuid=True), nullable=True),
        sa.Column("winning_bid_amount", sa.Numeric(12, 2), nullable=True),
        sa.Column("result_approved_by", UUID(as_uuid=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )

    # ── DEFERRED: bids (schema only — no API) ─────────────────────────────────
    op.create_table(
        "bids",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, default=uuid.uuid4),
        sa.Column("auction_id", UUID(as_uuid=True), sa.ForeignKey("auctions.id"), nullable=False),
        sa.Column("member_id", UUID(as_uuid=True), sa.ForeignKey("members.id"), nullable=False),
        sa.Column("bid_amount", sa.Numeric(12, 2), nullable=False),
        sa.Column("is_winner", sa.Boolean, nullable=False, server_default="false"),
        sa.Column("is_voided", sa.Boolean, nullable=False, server_default="false"),
        sa.Column("void_reason", sa.String(512), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )
    op.create_index("ix_bids_auction_id", "bids", ["auction_id"])
    op.create_index("ix_bids_member_id", "bids", ["member_id"])


def downgrade() -> None:
    # Drop in reverse order (respecting foreign key constraints)
    op.drop_table("bids")
    op.drop_table("auctions")
    op.drop_table("kyc_documents")
    op.drop_table("payouts")
    op.drop_table("policy_chunks")
    op.drop_table("ledger_entries")
    op.drop_table("payments")
    op.drop_table("installment_schedules")
    op.drop_table("nominees")
    op.drop_table("bank_accounts")
    op.drop_table("chit_memberships")
    op.drop_table("chit_groups")
    op.drop_table("member_sessions")
    op.drop_table("otp_sessions")
    op.drop_table("admin_users")
    op.drop_table("members")
