# MD Aesthetics Viral Forge

**A complete AI-powered viral content analysis and generation system for MD Aesthetics, built with Python FastAPI backend and Next.js frontend.**

## 🌟 Overview

MD Aesthetics Viral Forge is an advanced multi-agent system that monitors viral social media content in the medical aesthetics industry and generates superior, compliant content for MD Aesthetics. The system has been completely migrated from Google ADK/Firebase to a modern Python-based stack.

## 🏗️ Architecture

### Backend (Python FastAPI)
- **FastAPI** - Modern, fast web framework
- **Pydantic** - Data validation and agent state management
- **PostgreSQL** - Primary database (Supabase)
- **Multi-Agent System** - Specialized AI agents for different tasks

### Frontend (Next.js)
- **Next.js 14** - React framework with modern features
- **TypeScript** - Type safety and better DX
- **Tailwind CSS** - Utility-first styling
- **Radix UI** - Accessible component library

### AI Agents
1. **TrendAnalyzer** - Analyzes viral posts for hooks, CTAs, and relevance
2. **ContentCreator** - Generates MD Aesthetics-branded content
3. **ComplianceAgent** - Ensures content meets guidelines and regulations
4. **EmailDispatcher** - Sends automated digest emails

## 🚀 Quick Start

### Prerequisites
- Python 3.8+ 
- Node.js 16+
- PostgreSQL database (Supabase configured)

### Setup Development Environment
```bash
# Clone and setup
./setup-dev.sh

# Start backend (Terminal 1)
./start-backend.sh

# Start frontend (Terminal 2) 
./start-frontend.sh
```

### Access Points
- **Frontend Dashboard**: http://localhost:3000
- **API Documentation**: http://localhost:3453/docs
- **API Base URL**: http://localhost:3453/api/v1

## 📊 Features

### Viral Content Analysis
- Monitors competitor Instagram and TikTok accounts
- Calculates engagement velocity scores and virality metrics
- Categorizes content into strategic buckets:
  - Process Demystified
  - Science Explained  
  - Transformation Stories
  - Myth Busting

### Content Generation
- Creates MD Aesthetics-branded social media posts
- Focuses on key services: Duo-C-Lift, SkinTyte, Radiesse, Vivier
- Maintains professional, educational tone
- Ensures compliance (e.g., "Tox" instead of "Botox")

### Compliance & Quality
- Validates content against MD Aesthetics guidelines
- Checks for forbidden words and required elements
- Calculates brand voice alignment scores
- Auto-fixes common compliance issues

### Automation
- Daily automated competitor monitoring
- Scheduled email digests to team
- Real-time content analysis and generation
- PostgreSQL data persistence

## 🤖 API Endpoints

### Health & Status
- `GET /api/v1/health` - System health check
- `GET /api/v1/status` - Detailed system status

### Viral Content
- `GET /api/v1/viral/posts` - Get competitor posts
- `POST /api/v1/viral/analyze` - Analyze viral trends
- `POST /api/v1/viral/generate` - Generate content
- `POST /api/v1/viral/analyze-and-generate` - Complete pipeline
- `GET /api/v1/viral/insights` - Analytics and insights

### Agent Management
- `GET /api/v1/agents/` - List available agents
- `POST /api/v1/agents/trend-analyzer` - Run trend analysis
- `POST /api/v1/agents/content-creator` - Generate content
- `POST /api/v1/agents/compliance` - Check compliance
- `POST /api/v1/agents/pipeline/analyze-create-check` - Full pipeline

## 🔧 Configuration

### Environment Variables
Required environment variables in `.env`:

```bash
# Application
ENVIRONMENT=development
DEBUG=true
SERVER_PORT=3453

# Database (Supabase PostgreSQL)
POSTGRES_USER="postgres"
POSTGRES_PASSWORD="your-password"
POSTGRES_DATABASE="postgres"
POSTGRES_HOST="your-supabase-host"
POSTGRES_PORT=6543

# APIs
OPENROUTER_API_KEY=your-openrouter-key
APIFY_TOKEN=your-apify-token
GOOGLE_CSE_KEY=your-google-cse-key
GOOGLE_CSE_CX=your-google-cse-cx

# Email
EMAIL_ENABLED=true
DIGEST_RECIPIENTS=christine.carrer@hotmail.com,dalkeith@golden.net
EMAIL_SENDER=noreply@mdaesthetics.ca

# Frontend URLs
NEXT_PUBLIC_API_BASE_URL=http://localhost:3453/api/v1
```

## 🧪 Testing

### Test Backend Agents
```bash
source venv/bin/activate

# Test TrendAnalyzer
python3 -c "
import asyncio
from app.agents.trend_analyzer import TrendAnalyzerAgent
from app.models.schemas import TrendAnalysisRequest, CompetitorPostCreate, Platform

async def test():
    agent = TrendAnalyzerAgent()
    post = CompetitorPostCreate(
        platform=Platform.INSTAGRAM,
        profile_url='https://instagram.com/test',
        post_url='https://instagram.com/p/test123',
        caption='Transform your skin! #skincare #aesthetics',
        hashtags=['#skincare', '#aesthetics'],
        likes=150, comments=25, shares=5, views=800
    )
    result = await agent.execute(TrendAnalysisRequest(post_data=post))
    print(f'Success: {result[\"success\"]}')
    print(f'Virality Score: {result[\"data\"][\"virality_score\"]}')

asyncio.run(test())
"
```

### Test API Endpoints
```bash
# Health check
curl http://localhost:3453/api/v1/health

# List agents
curl http://localhost:3453/api/v1/agents/
```

## 📱 Frontend Components

### Dashboard
- Real-time viral content insights
- Generated content queue
- Performance metrics
- Quick actions

### Research Center  
- Competitor post analysis
- Trend identification
- Content categorization
- Export capabilities

### Command Center
- Manual content generation
- Agent pipeline controls
- Batch operations
- System monitoring

### Studio
- Content review and approval
- Compliance checking
- Publishing workflows
- Analytics tracking

## 🔄 Migration from Google ADK

This system has been completely migrated from the original Google ADK/Firebase implementation:

### ✅ Completed
- ✅ Removed all Google Cloud/Firebase dependencies
- ✅ Migrated from Java Spring Boot to Python FastAPI
- ✅ Replaced Google ADK agents with Pydantic-based agents  
- ✅ Switched from Firestore to PostgreSQL (Supabase)
- ✅ Updated frontend API integration
- ✅ Maintained all core functionality
- ✅ Enhanced type safety with Pydantic validation

### 🏗️ Architecture Changes
- **Before**: Java ADK + Google Cloud + Firebase
- **After**: Python FastAPI + Pydantic + PostgreSQL + Supabase

## 🛠️ Development

### Project Structure
```
├── app/                     # Python backend
│   ├── agents/             # AI agents (Pydantic-based)
│   ├── api/                # FastAPI routes
│   ├── core/               # Configuration and database
│   ├── models/             # Pydantic schemas
│   └── services/           # Business logic
├── app/                    # Next.js frontend
│   ├── components/         # React components
│   ├── hooks/              # Custom hooks
│   └── pages/              # Page components
├── requirements.txt        # Python dependencies
├── package.json           # Node.js dependencies
└── main.py                # FastAPI application entry
```

### Key Technologies
- **FastAPI** - High-performance Python web framework
- **Pydantic** - Data validation and settings management
- **SQLAlchemy** - Database ORM with async support
- **PostgreSQL** - Robust relational database
- **Next.js** - React framework with TypeScript
- **Tailwind CSS** - Utility-first CSS framework

## 📝 License

Copyright 2025 MD Aesthetics. All rights reserved.

## 🤝 Support

For technical support or questions about the Viral Forge system, contact the development team.

---

**Built with ❤️ for MD Aesthetics - Transforming viral intelligence into business results.**