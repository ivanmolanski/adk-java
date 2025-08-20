# MD Aesthetics AI Enhancement Guide

This enhanced ADK Java implementation provides Firebase integration and specialized social media scraping agents for MD Aesthetics business growth.

## 🚀 New Features Added

### 1. Firebase Integration
- **Authentication**: Firebase Auth with JWT token verification
- **Database**: Firestore integration for persistent chat sessions
- **Hosting**: Firebase Hosting configuration for web deployment
- **Security**: Custom security configuration with Spring Security

### 2. Social Media Scraping Agents
- **Instagram Agent**: Scrapes viral aesthetics content with engagement analysis
- **TikTok Agent**: Analyzes trending beauty videos and hashtag performance
- **Content Analysis**: Identifies trending treatments and successful content strategies

### 3. Automated Email Service
- **Daily Digest**: Automatic daily emails with top viral content
- **Business Insights**: Revenue optimization recommendations
- **Trending Analysis**: Hashtag and engagement metrics

### 4. Enhanced UI
- **Firebase-Powered Chat**: Real-time chat interface with Gemini 2.5 Flash
- **Agent Invocation**: Direct UI controls for scraping agents
- **Analytics Dashboard**: Visual stats for scraped content

## 🛠️ Setup Instructions

### Prerequisites
- Java 17+
- Maven 4.0+
- Firebase project with billing enabled
- Gmail/SMTP credentials for email service

### 1. Firebase Configuration

Create a Firebase project and configure:

```bash
# Set environment variables
export FIREBASE_PROJECT_ID=your-project-id
export FIREBASE_SERVICE_ACCOUNT_KEY=base64-encoded-service-account-json
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret
```

### 2. Email Service Setup

Configure email credentials:

```bash
export MAIL_HOST=smtp.gmail.com
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export EMAIL_RECIPIENT=info@mdaesthetics.ca
export EMAIL_FROM=noreply@mdaesthetics.ca
```

### 3. Build and Run

```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Start the enhanced server
./mvnw spring-boot:run -pl dev
```

The application will start on `http://localhost:8080`

## 📱 Using the Enhanced Features

### Social Media Scraping

#### Instagram Scraping
```java
InstagramScrapingTool tool = new InstagramScrapingTool();
Map<String, Object> params = Map.of(
    "hashtag", "aesthetics",
    "limit", 10
);
String results = tool.runAsync(params, toolContext).blockingGet();
```

#### TikTok Analysis
```java
TikTokScrapingTool tool = new TikTokScrapingTool();
Map<String, Object> params = Map.of(
    "keyword", "skincare",
    "limit", 15
);
String results = tool.runAsync(params, toolContext).blockingGet();
```

### Chat Interface

Access the MD Aesthetics UI at: `http://localhost:8080/md-aesthetics-ui.html`

Features:
- Firebase authentication
- Real-time chat with Gemini 2.5 Flash
- Direct agent invocation buttons
- Visual analytics dashboard

### API Endpoints

- `POST /api/chat/message` - Send chat messages
- `POST /api/chat/scrape-instagram` - Trigger Instagram scraping
- `POST /api/chat/scrape-tiktok` - Trigger TikTok analysis
- `POST /api/chat/send-digest` - Send manual email digest

## 🔧 Configuration Files

### Firebase Configuration
- `firebase.json` - Firebase hosting configuration
- `firestore.rules` - Firestore security rules
- `FirebaseConfig.java` - Spring configuration

### Email Templates
Daily digest includes:
- Top 5 viral Instagram posts
- Top 5 trending TikTok videos
- Hashtag analysis
- Business growth recommendations
- Engagement metrics

## 🎯 Business Focus Features

### Revenue Optimization
- Content strategy recommendations
- Trending treatment analysis
- Competitor engagement insights
- Optimal posting time suggestions

### MD Aesthetics Integration
- Branded UI and messaging
- Business-specific content filtering
- Revenue-focused insights
- Social media account integration

## 🚀 Firebase Hosting Deployment

```bash
# Initialize Firebase in your project
firebase init hosting

# Deploy to Firebase
firebase deploy
```

Your application will be available at: `https://your-project-id.web.app`

## 📊 Monitoring and Analytics

The application provides:
- Real-time engagement metrics
- Content performance tracking
- Email delivery status
- Chat session persistence
- Agent execution logs

## 🔒 Security Features

- JWT token verification
- Firestore security rules
- CORS configuration
- Environment-based secrets
- Role-based access control

## 💡 Customization

### Adding New Agents
Extend `BaseTool` and implement:
- `declaration()` - Function declaration
- `runAsync()` - Execution logic

### Custom Content Analysis
Modify the scraping tools to:
- Add new social platforms
- Implement specific business logic
- Integrate additional APIs
- Customize content filtering

## 📈 Performance Optimization

- Async processing for all scraping operations
- Connection pooling for external APIs
- Caching for frequent requests
- Batch processing for email generation

## 🐛 Troubleshooting

### Common Issues

1. **Firebase Auth Issues**
   - Verify service account key is properly base64 encoded
   - Check project permissions in Firebase console

2. **Email Service Issues**
   - Ensure Gmail app passwords are configured
   - Verify SMTP settings

3. **Build Issues**
   - Confirm Java 17+ is being used
   - Clear Maven cache: `./mvnw clean`

## 🤝 Contributing

When adding new features:
1. Follow existing code patterns
2. Add comprehensive tests
3. Update documentation
4. Consider Firebase integration needs

## 📄 License

Licensed under the Apache License, Version 2.0.

---

*Happy Agent Building for MD Aesthetics!* 🌟