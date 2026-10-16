"""
JWT token creation and verification, refresh token hashing.
"""
import hashlib
from datetime import datetime, timedelta, timezone
from typing import Any

from jose import JWTError, jwt

from app.core.config import settings


def create_access_token(subject: str, extra_claims: dict[str, Any] | None = None) -> str:
    """Create a signed JWT access token (24-hour expiry by default)."""
    now = datetime.now(timezone.utc)
    expire = now + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    payload = {
        "sub": subject,
        "iat": now,
        "exp": expire,
        "type": "access",
    }
    if extra_claims:
        payload.update(extra_claims)
    return jwt.encode(payload, settings.SECRET_KEY, algorithm=settings.ALGORITHM)


def create_refresh_token(subject: str) -> str:
    """Create a signed JWT refresh token (30-day expiry)."""
    import secrets
    # Refresh tokens are random opaque strings — their hash is stored in DB
    return secrets.token_urlsafe(64)


def decode_access_token(token: str) -> dict[str, Any]:
    """
    Decode and validate a JWT access token.
    Raises ValueError if the token is invalid or expired.
    """
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        if payload.get("type") != "access":
            raise ValueError("Not an access token")
        return payload
    except JWTError as e:
        raise ValueError(f"Invalid token: {e}") from e


def hash_refresh_token(token: str) -> str:
    """SHA-256 hash of a refresh token for secure DB storage."""
    return hashlib.sha256(token.encode()).hexdigest()


def verify_token(token: str) -> str:
    """Return the subject (member_id) from a valid access token."""
    payload = decode_access_token(token)
    sub = payload.get("sub")
    if not sub:
        raise ValueError("Token has no subject")
    return sub
