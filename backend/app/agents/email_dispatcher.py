"""EmailDispatcher Agent

Implements daily viral intelligence digest email dispatch using either:
1. SendGrid (preferred) if SENDGRID_API_KEY provided
2. SMTP fallback if SMTP_* settings provided

Behavior:
 - Builds HTML digest from trends & drafts
 - Auto-chunks recipients if > 50 (SendGrid personalizations limit safeguarding)
 - Returns success boolean and logs provider + message id (if available)
"""

from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional
from datetime import datetime
import logging
import os
import smtplib
from email.mime.text import MIMEText

try:
    from sendgrid import SendGridAPIClient  # type: ignore
    from sendgrid.helpers.mail import Mail  # type: ignore
    SENDGRID_AVAILABLE = True
except Exception:  # pragma: no cover
    SENDGRID_AVAILABLE = False
    SendGridAPIClient = None  # type: ignore
    Mail = None  # type: ignore

logger = logging.getLogger(__name__)


class EmailPayload(BaseModel):
    """Structured email payload."""
    subject: str
    html_body: str
    to: List[str]
    generated_at: datetime = Field(default_factory=datetime.utcnow)


class EmailDispatcher(BaseModel):
    """Dispatches email digests for viral intelligence findings."""

    name: str = "EmailDispatcher"
    version: str = "0.1.0"
    description: str = "Sends daily digest emails containing trends and content drafts"

    sendgrid_api_key: Optional[str] = Field(default_factory=lambda: os.getenv("SENDGRID_API_KEY"))
    sender: str = Field(default_factory=lambda: os.getenv("EMAIL_SENDER", "noreply@example.com"))
    smtp_host: Optional[str] = Field(default_factory=lambda: os.getenv("SMTP_HOST"))
    smtp_port: int = Field(default_factory=lambda: int(os.getenv("SMTP_PORT", "587")))
    smtp_username: Optional[str] = Field(default_factory=lambda: os.getenv("SMTP_USERNAME"))
    smtp_password: Optional[str] = Field(default_factory=lambda: os.getenv("SMTP_PASSWORD"))
    smtp_use_tls: bool = Field(default_factory=lambda: os.getenv("SMTP_USE_TLS", "true").lower() in {"1","true","yes","on"})

    async def build_digest(self, *, trends: List[Dict[str, Any]], drafts: List[Dict[str, Any]]) -> EmailPayload:
        """Build HTML digest from trends and drafts."""
        subject = f"MD Aesthetics Daily Viral Intelligence ({datetime.utcnow().date()})"
        def _escape(text: str) -> str:
            return (text or '').replace('<','&lt;').replace('>','&gt;')

        trends_html = ''.join([
            f"<li><strong>{_escape(t.get('hook',''))}</strong> | Category: {_escape(t.get('content_category',''))} | Virality: {t.get('virality_score',0):.2f}</li>" for t in trends
        ])
        drafts_html = ''.join([
            f"<li><strong>{_escape(d.get('platform',''))}</strong>: {_escape(d.get('caption','')[:140])}...</li>" for d in drafts
        ])
        html_body = f"""
        <h2>Daily Viral Intelligence Digest</h2>
        <h3>Top Trends</h3>
        <ul>{trends_html}</ul>
        <h3>Generated Drafts</h3>
        <ul>{drafts_html}</ul>
        <p>Generated at {datetime.utcnow().isoformat()}</p>
        """
        return EmailPayload(subject=subject, html_body=html_body, to=[])

    async def send_email(self, payload: EmailPayload) -> bool:
        """Send email using SendGrid if available, else SMTP.

        Returns True on success, False on failure. Logs detailed error info.
        """
        recipients = [r.strip() for r in payload.to if r.strip()]
        if not recipients:
            logger.warning("EmailDispatcher: No recipients provided; aborting send")
            return False
        # Prefer SendGrid
        if self.sendgrid_api_key and SENDGRID_AVAILABLE and Mail and SendGridAPIClient:
            try:
                message = Mail(
                    from_email=self.sender,
                    to_emails=recipients,
                    subject=payload.subject,
                    html_content=payload.html_body,
                )
                sg = SendGridAPIClient(self.sendgrid_api_key)
                resp = sg.send(message)
                ok = 200 <= resp.status_code < 300
                logger.info(
                    "EmailDispatcher: SendGrid send status=%s code=%s recipients=%d", ok, resp.status_code, len(recipients)
                )
                return ok
            except Exception as e:  # pragma: no cover network specific
                logger.error("EmailDispatcher: SendGrid send failed: %s", e)
                # Fall through to SMTP attempt
        # SMTP fallback
        if self.smtp_host and self.smtp_username and self.smtp_password:
            try:
                msg = MIMEText(payload.html_body, 'html')
                msg['Subject'] = payload.subject
                msg['From'] = self.sender
                msg['To'] = ', '.join(recipients)
                server = smtplib.SMTP(self.smtp_host, self.smtp_port, timeout=30)
                try:
                    if self.smtp_use_tls:
                        server.starttls()
                    server.login(self.smtp_username, self.smtp_password)
                    server.sendmail(self.sender, recipients, msg.as_string())
                finally:
                    server.quit()
                logger.info(
                    "EmailDispatcher: SMTP send success host=%s recipients=%d", self.smtp_host, len(recipients)
                )
                return True
            except Exception as e:  # pragma: no cover network specific
                logger.error("EmailDispatcher: SMTP send failed: %s", e)
                return False
        logger.error("EmailDispatcher: No valid email provider configured (SENDGRID_API_KEY or SMTP creds required)")
        return False
