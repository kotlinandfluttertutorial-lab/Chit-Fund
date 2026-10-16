"""
OpenAI function tool definitions for the ChitSmart AI assistant.

IMPORTANT CONSTRAINTS (enforced by tool definitions and tool_executor.py):
- ALL tools are READ-ONLY. No tool may write, update, or delete any data.
- Tools may only access data belonging to the authenticated member_id.
- KYC data is never returned by any tool.
- Auction / bid data is never returned by any tool.
"""

from typing import Any

# ── Tool definitions (OpenAI function-calling schema) ────────────────────────

ACCOUNT_TOOLS: list[dict[str, Any]] = [
    {
        "type": "function",
        "function": {
            "name": "get_outstanding_balance",
            "description": (
                "Returns the total pending payment amount for the authenticated member "
                "across all active chit group memberships. "
                "Use this when the member asks about their total dues, outstanding balance, "
                "or how much they owe."
            ),
            "parameters": {
                "type": "object",
                "properties": {},
                "required": [],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_next_installment_due",
            "description": (
                "Returns the nearest upcoming installment due date and amount for the "
                "authenticated member. Includes the chit group name and due date. "
                "Use this when the member asks 'when is my next payment', "
                "'what is due next', or similar."
            ),
            "parameters": {
                "type": "object",
                "properties": {},
                "required": [],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_active_memberships",
            "description": (
                "Lists all active ChitMembership records for the authenticated member. "
                "Returns group name, chit value, monthly installment amount, enrollment date, "
                "and current status. "
                "Use this when the member asks 'which chits am I in', "
                "'show my active circles', or similar."
            ),
            "parameters": {
                "type": "object",
                "properties": {},
                "required": [],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_payment_history",
            "description": (
                "Returns the last N payment transactions for the authenticated member. "
                "Each record includes amount, date, payment method, status, and receipt number. "
                "Use this when the member asks about past payments, payment history, "
                "or receipts."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "limit": {
                        "type": "integer",
                        "description": "Maximum number of recent payments to return (default 10, max 50).",
                        "default": 10,
                        "minimum": 1,
                        "maximum": 50,
                    },
                },
                "required": [],
            },
        },
    },
]
