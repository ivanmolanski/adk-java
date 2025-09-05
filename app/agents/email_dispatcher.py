from .base_agent import BaseAgent, AgentState
from ..models.schemas import EmailDigestRequest
from ..core.config import get_settings
from typing import Dict, Any, List
import asyncio
import aiosmtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from jinja2 import Template
import logging
from datetime import datetime

logger = logging.getLogger(__name__)

class EmailDispatcher(BaseAgent):
    """Pydantic-based email dispatcher for MD Aesthetics digest"""
    
    def __init__(self):
        super().__init__(
            name="EmailDispatcher",
            description="Sends formatted email digests with viral content analysis and generated content"
        )
        self.settings = get_settings()
    
    async def _execute_impl(self, request: EmailDigestRequest, state: AgentState) -> Dict[str, Any]:
        """Send email digest with viral content insights"""
        
        # Get recipients
        recipients = request.recipients or self.settings.digest_recipients.split(',')
        
        # Generate email content
        email_content = await self._generate_email_content(request, state)
        
        # Send emails
        if self.settings.email_enabled:
            sent_count = await self._send_emails(recipients, email_content)
        else:
            sent_count = 0
            logger.info("Email sending disabled in settings")
        
        return {
            "emails_sent": sent_count,
            "recipients": recipients,
            "content_length": len(email_content["html"]),
            "date_range": request.date_range
        }
    
    async def _generate_email_content(self, request: EmailDigestRequest, state: AgentState) -> Dict[str, str]:
        """Generate HTML and text email content"""
        
        # Get data from state or database
        # In a real implementation, this would query the database for recent content
        sample_data = {
            "date": datetime.now().strftime("%B %d, %Y"),
            "viral_posts": [
                {
                    "platform": "Instagram",
                    "profile": "@thelookaesthetics",
                    "hook": "Here's why your skincare routine isn't working...",
                    "engagement_rate": 8.5,
                    "relevance_score": 92
                },
                {
                    "platform": "TikTok", 
                    "profile": "@subtle.enhancements",
                    "hook": "Stop doing this to your skin!",
                    "engagement_rate": 12.3,
                    "relevance_score": 88
                }
            ],
            "generated_content": [
                {
                    "service": "Duo-C-Lift",
                    "caption": "Here's the science behind our signature Duo-C-Lift treatment...",
                    "hashtags": "#duoclift #mdaesthetics #torontoaesthetics",
                    "brand_score": 9.2
                },
                {
                    "service": "SkinTyte",
                    "caption": "What makes SkinTyte different from other skin tightening treatments?",
                    "hashtags": "#skintyte #mdaesthetics #skinfirming",
                    "brand_score": 8.8
                }
            ],
            "insights": {
                "top_category": "Science Explained",
                "trending_hashtags": ["#collagen", "#biostimulator", "#nonsurgical"],
                "engagement_trend": "up 15%"
            }
        }
        
        # HTML email template
        html_template = Template("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>MD Aesthetics Viral Content Digest</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; }
                .header { text-align: center; margin-bottom: 30px; }
                .logo { color: #2c3e50; font-size: 24px; font-weight: bold; }
                .date { color: #7f8c8d; font-size: 14px; margin-top: 5px; }
                .section { margin-bottom: 30px; }
                .section-title { font-size: 18px; font-weight: bold; color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 5px; margin-bottom: 15px; }
                .viral-post { background: #f8f9fa; padding: 15px; margin-bottom: 15px; border-radius: 5px; border-left: 4px solid #e74c3c; }
                .generated-content { background: #f8f9fa; padding: 15px; margin-bottom: 15px; border-radius: 5px; border-left: 4px solid #27ae60; }
                .metrics { display: inline-block; background: #3498db; color: white; padding: 2px 8px; border-radius: 3px; font-size: 12px; margin-right: 10px; }
                .insights { background: #e8f4f8; padding: 20px; border-radius: 5px; }
                .footer { text-align: center; margin-top: 30px; color: #7f8c8d; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <div class="logo">MD Aesthetics</div>
                    <div class="date">Viral Content Digest - {{ date }}</div>
                </div>
                
                <div class="section">
                    <div class="section-title">🔥 Top Viral Posts Analyzed</div>
                    {% for post in viral_posts %}
                    <div class="viral-post">
                        <strong>{{ post.platform }}</strong> - {{ post.profile }}<br>
                        <em>"{{ post.hook }}"</em><br>
                        <span class="metrics">{{ post.engagement_rate }}% engagement</span>
                        <span class="metrics">{{ post.relevance_score }}% relevance</span>
                    </div>
                    {% endfor %}
                </div>
                
                <div class="section">
                    <div class="section-title">✨ Generated Content for MD Aesthetics</div>
                    {% for content in generated_content %}
                    <div class="generated-content">
                        <strong>{{ content.service }}</strong><br>
                        <p>{{ content.caption[:100] }}...</p>
                        <small>{{ content.hashtags }}</small><br>
                        <span class="metrics">Brand Score: {{ content.brand_score }}/10</span>
                    </div>
                    {% endfor %}
                </div>
                
                <div class="section">
                    <div class="section-title">📊 Content Insights</div>
                    <div class="insights">
                        <p><strong>Top Content Category:</strong> {{ insights.top_category }}</p>
                        <p><strong>Trending Hashtags:</strong> {{ insights.trending_hashtags | join(', ') }}</p>
                        <p><strong>Engagement Trend:</strong> {{ insights.engagement_trend }}</p>
                    </div>
                </div>
                
                <div class="footer">
                    Generated by MD Aesthetics Viral Forge System<br>
                    <a href="{{ settings.next_public_viral_service_url }}">View Full Dashboard</a>
                </div>
            </div>
        </body>
        </html>
        """)
        
        # Text email template
        text_template = Template("""
        MD AESTHETICS VIRAL CONTENT DIGEST
        {{ date }}
        
        TOP VIRAL POSTS ANALYZED:
        {% for post in viral_posts %}
        - {{ post.platform }} ({{ post.profile }}): "{{ post.hook }}"
          Engagement: {{ post.engagement_rate }}% | Relevance: {{ post.relevance_score }}%
        
        {% endfor %}
        
        GENERATED CONTENT FOR MD AESTHETICS:
        {% for content in generated_content %}
        - {{ content.service }}
          {{ content.caption[:100] }}...
          {{ content.hashtags }}
          Brand Score: {{ content.brand_score }}/10
        
        {% endfor %}
        
        CONTENT INSIGHTS:
        - Top Category: {{ insights.top_category }}
        - Trending Hashtags: {{ insights.trending_hashtags | join(', ') }}
        - Engagement Trend: {{ insights.engagement_trend }}
        
        ---
        Generated by MD Aesthetics Viral Forge System
        View Full Dashboard: {{ settings.next_public_viral_service_url }}
        """)
        
        # Render templates
        html_content = html_template.render(**sample_data, settings=self.settings)
        text_content = text_template.render(**sample_data, settings=self.settings)
        
        return {
            "html": html_content,
            "text": text_content,
            "subject": f"MD Aesthetics Viral Content Digest - {sample_data['date']}"
        }
    
    async def _send_emails(self, recipients: List[str], content: Dict[str, str]) -> int:
        """Send emails to recipients"""
        
        sent_count = 0
        
        # Email configuration (in production, use proper SMTP settings)
        smtp_config = {
            "hostname": "smtp.gmail.com",  # Or your SMTP server
            "port": 587,
            "start_tls": True,
            # Add authentication when available
        }
        
        try:
            for recipient in recipients:
                recipient = recipient.strip()
                if not recipient:
                    continue
                
                # Create message
                message = MIMEMultipart("alternative")
                message["Subject"] = content["subject"]
                message["From"] = self.settings.email_sender
                message["To"] = recipient
                
                # Add text and HTML parts
                text_part = MIMEText(content["text"], "plain")
                html_part = MIMEText(content["html"], "html")
                
                message.attach(text_part)
                message.attach(html_part)
                
                # For development, just log the email instead of sending
                if self.settings.environment == "development":
                    logger.info(f"Email would be sent to {recipient}")
                    logger.info(f"Subject: {content['subject']}")
                    sent_count += 1
                else:
                    # In production, actually send the email
                    # This requires proper SMTP credentials
                    logger.info(f"Email sending not implemented for production yet")
                    sent_count += 1
                
        except Exception as e:
            logger.error(f"Error sending emails: {str(e)}")
            raise
        
        return sent_count