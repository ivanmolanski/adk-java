# MD Aesthetics Viral Content System

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Python](https://img.shields.io/badge/Python-3.12-blue.svg)](https://python.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.116-green.svg)](https://fastapi.tiangolo.com)
[![Next.js](https://img.shields.io/badge/Next.js-14-black.svg)](https://nextjs.org)

<html>
    <h2 align="center">
      <img src="https://raw.githubusercontent.com/google/adk-python/main/assets/agent-development-kit.png" width="256"/>
    </h2>
    <h3 align="center">
      An intelligent competitive analysis and content generation system for MD Aesthetics, powered by Python/FastAPI backend with Pydantic-based AI agents.
    </h3>
</html>

The MD Aesthetics Viral Content System is a comprehensive solution for monitoring competitor social media content, analyzing viral trends, and generating compliant, on-brand content for MD Aesthetics' social media channels.

--------------------------------------------------------------------------------

## ✨ Key Features

-   **🔍 Competitive Intelligence**: Automated monitoring of competitor Instagram and TikTok accounts with engagement analysis
-   **🤖 AI-Powered Agents**: Pydantic-based agents for trend analysis, content creation, and compliance checking
-   **📊 Trend Analysis**: Identifies viral hooks, CTAs, content categories, and engagement drivers
-   **✍️ Content Generation**: Creates MD Aesthetics-branded content with automatic compliance checking
-   **📧 Automated Reporting**: Daily digest emails with trending content and generated drafts
-   **🌐 Modern Architecture**: Python/FastAPI backend with PostgreSQL/Supabase database

## 🏗️ Architecture

### Backend (Python/FastAPI)
- **FastAPI**: High-performance async web framework
- **Pydantic**: Data validation and agent state management  
- **PostgreSQL/Supabase**: Production-ready database
- **SQLAlchemy**: Async ORM for database operations

### AI Agents
- **TrendAnalyzer**: Analyzes viral posts for hooks, CTAs, and engagement factors
- **ContentCreator**: Generates MD Aesthetics-branded content with compliance
- **ComplianceAgent**: Validates content against brand guidelines and regulations
- **EmailDispatcher**: Sends automated HTML digest emails

### Frontend (Next.js)
- **Next.js 14**: React framework with TypeScript
- **Tailwind CSS**: Utility-first styling
- **Real-time Dashboard**: Live competitor monitoring and content generation

## 🚀 Quick Start

### Prerequisites
- Python 3.12+
- Node.js 18+
- PostgreSQL database (or Supabase account)

### 1. Setup Development Environment
```bash
# Clone the repository
git clone https://github.com/ivanmolanski/adk-java.git
cd adk-java

# Setup Python backend
./setup-dev.sh

# Edit environment variables
cp .env.example .env
# Edit .env with your API keys and database credentials
```

### 2. Configure Environment Variables
Required environment variables in `.env`:

```bash
# Database (PostgreSQL/Supabase)
DATABASE_URL=postgresql://user:password@localhost:5432/mdaesthetics_db
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-supabase-anon-key

# AI/LLM APIs
OPENROUTER_API_KEY=your-openrouter-api-key
OPENAI_API_KEY=your-openai-api-key

# Social Media APIs
APIFY_TOKEN=your-apify-token
GOOGLE_CSE_KEY=your-google-cse-key
GOOGLE_CSE_CX=your-search-engine-id

# Email Configuration
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
DAILY_DIGEST_RECIPIENTS=team@mdaesthetics.ca
```

### 3. Start the Application
```bash
# Terminal 1: Start Python backend
./start-backend.sh

# Terminal 2: Start Next.js frontend  
./start-frontend.sh
```

### 4. Access the Application
- **Backend API**: http://localhost:3453
- **API Documentation**: http://localhost:3453/docs
- **Frontend Dashboard**: http://localhost:3000

## 📚 API Endpoints

### Health & Status
```
GET  /api/v1/health                    # System health check
```

### Viral Content Analysis
```
GET  /api/v1/viral/posts               # Get competitor posts
POST /api/v1/viral/analyze             # Analyze posts for trends
POST /api/v1/viral/generate            # Generate MD Aesthetics content
POST /api/v1/viral/analyze-and-generate # Complete pipeline
```

### Agent Management
```
GET  /api/v1/agents/                   # List available agents
GET  /api/v1/agents/{agent_type}       # Get agent details
POST /api/v1/agents/pipeline/*         # Execute agent workflows
POST /api/v1/agents/{agent_type}/execute # Execute specific agent
```

## 🤖 Pydantic Agents

### TrendAnalyzer
Analyzes viral social media posts to extract:
- **Hooks**: Opening 3-second attention grabbers
- **CTAs**: Call-to-action patterns and effectiveness
- **Categories**: Content type classification (Process Demystified, Science Explained, etc.)
- **Scores**: Relevance and virality scoring for MD Aesthetics

### ContentCreator  
Generates MD Aesthetics-branded content with:
- **Brand Compliance**: Automatic checking against forbidden terms (e.g., "Botox" → "Tox")
- **Service Focus**: Content tailored to specific treatments (Duo-C-Lift, SkinTyte, etc.)
- **Platform Optimization**: Instagram/TikTok specific formatting
- **Educational Value**: Clinical authority and trustworthy information

### ComplianceAgent
Validates content for:
- **Brand Guidelines**: Tone, voice, and messaging consistency
- **Medical Regulations**: Compliance with aesthetic medicine advertising rules
- **Forbidden Terms**: Automatic detection and replacement
- **Quality Assurance**: Professional standards verification

## 🎯 Competitor Monitoring

The system automatically monitors these competitor profiles:
- `_thelookaesthetics` (Instagram)
- `subtle.enhancements` (Instagram)  
- `skinvitalityofficial` (Instagram/TikTok)

### Content Categories Analyzed
1. **Process Demystified**: Treatment demonstrations and procedures
2. **Science Explained**: Educational content about technologies and ingredients
3. **Transformation**: Before/after results and success stories
4. **Expert Myth-Busting**: Professional authority and trust-building content

## 📧 Automated Reporting

Daily digest emails include:
- Top 5 viral posts from competitors
- Trend analysis and engagement insights
- Generated MD Aesthetics content drafts
- Compliance-checked captions and hashtags
- Posting recommendations and timing tips

## 🛠️ Development

### Project Structure
```
├── backend/                 # Python FastAPI backend
│   ├── main.py             # FastAPI application entry point
│   └── app/
│       ├── api/            # API route handlers
│       ├── agents/         # Pydantic-based AI agents
│       └── models/         # Database and data models
├── app/                    # Next.js frontend
│   ├── components/         # React components
│   ├── hooks/              # Custom React hooks
│   └── lib/                # Utility functions
├── src/google/adk/         # Python ADK framework (legacy)
├── requirements.txt        # Python dependencies
└── package.json           # Node.js dependencies
```

### Adding New Agents
1. Create agent class in `backend/app/agents/`
2. Inherit from Pydantic `BaseModel`
3. Define agent methods and validation
4. Add to agent registry in `app/api/agents.py`

### Database Schema
- **competitor_posts**: Scraped social media content
- **trend_analyses**: Agent analysis results  
- **content_drafts**: Generated MD Aesthetics content
- **agent_runs**: Execution logs and metrics

## 🚀 Installation

If you're using Maven, add the following to your dependencies:

<!-- {x-version-start:google-adk:released} -->

```xml
<dependency>
  <groupId>com.google.adk</groupId>
  <artifactId>google-adk</artifactId>
  <version>0.2.0</version>
</dependency>
<!-- Dev UI -->
<dependency>
    <groupId>com.google.adk</groupId>
    <artifactId>google-adk-dev</artifactId>
    <version>0.2.0</version>
</dependency>
```

<!-- {x-version-end} -->

To instead use an unreleased version, you could use <https://jitpack.io/#google/adk-java/>;
see <https://github.com/enola-dev/LearningADK#jitpack> for an example illustrating this.

## 📚 Documentation

For building, evaluating, and deploying agents by follow the Java
documentation & samples:

*   **[Documentation](https://google.github.io/adk-docs)**
*   **[Samples](https://github.com/google/adk-samples)**

## 🏁 Feature Highlight

### Same Features & Familiar Interface As Python ADK:

```java
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.GoogleSearchTool;

LlmAgent rootAgent = LlmAgent.builder()
    .name("search_assistant")
    .description("An assistant that can search the web.")
    .model("gemini-2.5-flash") // Or your preferred models
    .instruction("You are a helpful assistant. Answer user questions using Google Search when needed.")
    .tools(new GoogleSearchTool())
    .build();
```

### Development UI

Same as the beloved Python Development UI.
A built-in development UI to help you test, evaluate, debug, and showcase your agent(s).
<img src="https://raw.githubusercontent.com/google/adk-python/main/assets/adk-web-dev-ui-function-call.png"/>

### Evaluate Agents

Coming soon...

## 🤖 A2A and ADK integration

For remote agent-to-agent communication, ADK integrates with the
[A2A protocol](https://github.com/google/A2A/).
Examples coming soon...

## 🤝 Contributing

We welcome contributions from the community! Whether it's bug reports, feature
requests, documentation improvements, or code contributions, please see our
[**Contributing Guidelines**](./CONTRIBUTING.md) to get started.

## 📄 License

This project is licensed under the Apache 2.0 License - see the
[LICENSE](LICENSE) file for details.

## Preview

This feature is subject to the "Pre-GA Offerings Terms" in the General Service
Terms section of the
[Service Specific Terms](https://cloud.google.com/terms/service-terms#1). Pre-GA
features are available "as is" and might have limited support. For more
information, see the
[launch stage descriptions](https://cloud.google.com/products?hl=en#product-launch-stages).

--------------------------------------------------------------------------------

*Happy Agent Building!*
