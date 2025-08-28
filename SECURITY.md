# Security Setup Guide

## 🔒 Important: API Key Security

This repository implements secure secret management practices to protect sensitive API keys and credentials.

### ⚠️ Never commit sensitive credentials to version control!

## Local Development Setup

1. **Copy the template file:**
   ```bash
   cp .env.example .env
   ```

2. **Fill in your actual credentials:**
   - Edit `.env` with your real API keys
   - The `.env` file is excluded from git by `.gitignore`
   - Never commit the `.env` file with real credentials

3. **Required credentials:**
   - **Gemini API Key**: Get from [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
   - **Firebase Config**: Get from [Firebase Console](https://console.firebase.google.com/) → Project Settings
   - **Google CSE**: Get from [Google Custom Search](https://developers.google.com/custom-search/v1/introduction)
   - **Apify Token**: Get from [Apify Console](https://console.apify.com/account#/integrations)
   - **TikTok/Instagram**: Get from respective developer portals

## Production Deployment

**Production uses Google Cloud Secret Manager** - never use plain text credentials in production!

### 1. Store secrets in Google Secret Manager:

```bash
# Store each secret individually
gcloud secrets create GEMINI_API_KEY --data-file=<(echo -n "your-actual-api-key")
gcloud secrets create FIREBASE_API_KEY --data-file=<(echo -n "your-firebase-key")
gcloud secrets create GOOGLE_CSE_KEY --data-file=<(echo -n "your-cse-key")
gcloud secrets create APIFY_TOKEN --data-file=<(echo -n "your-apify-token")
gcloud secrets create TIKTOK_CLIENT_SECRET --data-file=<(echo -n "your-tiktok-secret")
gcloud secrets create INSTAGRAM_CLIENT_SECRET --data-file=<(echo -n "your-instagram-secret")
gcloud secrets create SERVICE_ACCOUNT_JSON --data-file=service-account.json
```

### 2. Grant access to your service account:

```bash
# Replace SERVICE_ACCOUNT_EMAIL with your actual service account
gcloud secrets add-iam-policy-binding GEMINI_API_KEY \
    --member="serviceAccount:SERVICE_ACCOUNT_EMAIL" \
    --role="roles/secretmanager.secretAccessor"

# Repeat for all secrets...
```

### 3. Deploy with production profile:

```bash
# The SecretManagerConfig will automatically load secrets in production
SPRING_PROFILES_ACTIVE=production java -jar viral-service.jar
```

## Security Best Practices

- ✅ **DO**: Use `.env` for local development (excluded from git)
- ✅ **DO**: Use Google Secret Manager for production
- ✅ **DO**: Rotate keys immediately if exposed
- ✅ **DO**: Use least-privilege IAM permissions

- ❌ **DON'T**: Commit `.env` files to version control
- ❌ **DON'T**: Hardcode credentials in source code
- ❌ **DON'T**: Share credentials via email/slack
- ❌ **DON'T**: Use development credentials in production

## If Credentials Are Compromised

1. **Immediate action**: Rotate/regenerate all exposed keys
2. **Audit**: Check Google Cloud Console for suspicious activity
3. **Update**: Store new keys in Secret Manager
4. **Monitor**: Enable audit logging for future tracking

## Emergency Contact

If you detect a security incident, immediately:
1. Rotate compromised credentials
2. Review GCP billing for unexpected usage
3. Check logs for suspicious access patterns