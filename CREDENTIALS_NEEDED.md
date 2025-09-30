# Required Credentials & Keys

This document lists all the API keys and credentials needed to run the Viral Forge system.

## Status: ⚠️ NEEDS ATTENTION

The following credentials need to be provided by the user to enable full system functionality.

---

## 1. GitHub Models API (REQUIRED for AI Features) 🔴

**Current Status**: Invalid/Placeholder token  
**Impact**: `/chat` endpoint returns 401 Unauthorized  
**Scopes Needed**: `models` (GitHub Models API access)

### How to Generate:

1. Go to https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Give it a descriptive name (e.g., "MD Aesthetics Viral Forge")
4. Select the `models` scope
5. Generate and copy the token

### Where to Add:

Update `.env` file:
```bash
GITHUB_TOKEN=ghp_your_actual_token_here
```

### Testing:

```bash
# Start the backend
cd backend
GITHUB_TOKEN=your_token python -m uvicorn main:app --port 3453

# Test the endpoint
curl -X POST http://localhost:3453/viral-service/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"messages": [{"role": "user", "content": "Hello"}]}'
```

---

## 2. Apify API Token (REQUIRED for Scraping) 🔴

**Current Status**: Placeholder `apify_api_token_here`  
**Impact**: Social media scraping won't work  
**Used For**: Instagram and TikTok post scraping

### How to Generate:

1. Sign up at https://apify.com
2. Go to Settings → API & Integrations
3. Copy your API token

### Where to Add:

Update `.env` file:
```bash
APIFY_TOKEN=your_apify_token_here
```

---

## 3. SendGrid API Key (OPTIONAL for Email) 🟡

**Current Status**: Placeholder `SG.xxxxxx`  
**Impact**: Daily digest emails won't be sent  
**Used For**: Automated email reports

### How to Generate:

1. Sign up at https://sendgrid.com
2. Go to Settings → API Keys
3. Create API Key with "Mail Send" permission

### Where to Add:

Update `.env` file:
```bash
SENDGRID_API_KEY=SG.your_actual_key_here
EMAIL_FROM=your_email@mdaesthetics.ca
```

---

## 4. Supabase Credentials (CONFIGURED) ✅

**Current Status**: Already configured in `.env`  
**Keys Present**:
- SUPABASE_URL ✓
- SUPABASE_ANON_KEY ✓
- SUPABASE_SERVICE_ROLE_KEY ✓
- SUPABASE_JWT_SECRET ✓

No action needed.

---

## 5. Database URLs (CONFIGURED) ✅

**Current Status**: Already configured in `.env`  
**URLs Present**:
- POSTGRES_URL ✓
- POSTGRES_PRISMA_URL ✓
- POSTGRES_URL_NON_POOLING ✓
- DB_URL (SQLite fallback) ✓

No action needed.

---

## Quick Setup Checklist

Before running the system in production:

- [ ] Generate GitHub Personal Access Token with `models` scope
- [ ] Add GitHub token to `.env` file
- [ ] Sign up for Apify and get API token
- [ ] Add Apify token to `.env` file
- [ ] (Optional) Get SendGrid API key for email functionality
- [ ] (Optional) Add SendGrid key to `.env` file
- [ ] Run `./validate-system.sh` to verify configuration
- [ ] Start backend: `cd backend && python -m uvicorn main:app --port 3453`
- [ ] Test health endpoint: `curl http://localhost:3453/viral-service/api/v1/health`
- [ ] Test AI chat: `curl -X POST http://localhost:3453/viral-service/api/v1/chat -H "Content-Type: application/json" -d '{"messages": [{"role": "user", "content": "Test"}]}'`

---

## Security Notes

⚠️ **NEVER commit the `.env` file to git**

The `.env` file is already in `.gitignore` to prevent accidental commits.

✅ **Rotate credentials regularly**

Change API tokens periodically for security.

✅ **Use environment-specific credentials**

Use different credentials for development, staging, and production.

---

## Support

If you have questions about credentials setup:

1. Check `OPTIMIZATION_REPORT.md` for technical details
2. Run `./validate-system.sh` to diagnose issues
3. Review logs in `backend.log` for errors

## Testing Without Credentials

For testing purposes without full credentials:

- ✅ Health endpoint works without credentials
- ✅ Agent configurations can be inspected
- ✅ Java agents can be tested independently
- ❌ AI chat requires GitHub token
- ❌ Scraping requires Apify token
- ❌ Email requires SendGrid key
