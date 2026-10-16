"""
SMS service abstraction — Twilio / MSG91 / AWS SNS / console (DEBUG).
"""
import logging

from app.core.config import settings

logger = logging.getLogger(__name__)


async def send_sms_otp(mobile_number: str, otp: str) -> None:
    """
    Send OTP via SMS. In DEBUG mode, log to console instead.
    Provider is selected from settings.SMS_PROVIDER.
    """
    if settings.DEBUG:
        logger.info("=" * 50)
        logger.info(f"[DEV OTP] {mobile_number} → {otp}")
        logger.info("=" * 50)
        return

    provider = settings.SMS_PROVIDER.lower()

    if provider == "twilio":
        await _send_twilio(mobile_number, otp)
    elif provider == "msg91":
        await _send_msg91(mobile_number, otp)
    else:
        logger.warning(f"Unknown SMS provider: {provider}. OTP not sent.")


async def _send_twilio(mobile_number: str, otp: str) -> None:
    """Send OTP via Twilio SMS API."""
    # Install: pip install twilio
    # from twilio.rest import Client
    # client = Client(settings.SMS_API_KEY, settings.SMS_API_SECRET)
    # client.messages.create(
    #     body=f"Your ChitSmart OTP is: {otp}. Valid for {settings.OTP_EXPIRE_MINUTES} minutes. Do not share.",
    #     from_=settings.SMS_SENDER_ID,
    #     to=mobile_number,
    # )
    logger.info(f"[Twilio stub] OTP {otp} to {mobile_number}")


async def _send_msg91(mobile_number: str, otp: str) -> None:
    """Send OTP via MSG91 API (popular in India)."""
    import httpx
    # MSG91 flow-based OTP
    # https://docs.msg91.com/reference/send-otp
    logger.info(f"[MSG91 stub] OTP {otp} to {mobile_number}")
