"""
FastAPI dependencies — authentication and authorization.

get_current_member: validates JWT Bearer token and returns the authenticated Member.
get_current_admin:  same as above but additionally checks the admin_users table.
"""

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.security import verify_token
from app.models.member import AccountStatus, Member

_bearer_scheme = HTTPBearer()


async def get_current_member(
    credentials: HTTPAuthorizationCredentials = Depends(_bearer_scheme),
    db: AsyncSession = Depends(get_db),
) -> Member:
    """
    Validate a JWT Bearer access token and return the authenticated Member.

    Raises 401 if the token is missing, invalid, or expired.
    Raises 403 if the account is suspended or inactive.
    """
    token = credentials.credentials
    try:
        member_id = verify_token(token)
    except ValueError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired access token",
            headers={"WWW-Authenticate": "Bearer"},
        )

    result = await db.execute(select(Member).where(Member.id == member_id))
    member = result.scalar_one_or_none()

    if not member:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Member account not found",
            headers={"WWW-Authenticate": "Bearer"},
        )

    if member.account_status != AccountStatus.ACTIVE:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Account is suspended or inactive",
        )

    return member


async def get_current_admin(
    credentials: HTTPAuthorizationCredentials = Depends(_bearer_scheme),
    db: AsyncSession = Depends(get_db),
) -> Member:
    """
    Validate a JWT Bearer access token for admin-only endpoints.

    Delegates to get_current_member and additionally enforces that the
    member is an admin user.  Admin role management is handled via the
    admin_users table (future batch).  For now, this dependency is a
    passthrough — RBAC enforcement will be added when admin_users is wired.
    """
    # Reuse the member authentication logic
    member = await get_current_member(credentials=credentials, db=db)
    # TODO: check admin_users table for role; deferred until admin_users batch
    return member
