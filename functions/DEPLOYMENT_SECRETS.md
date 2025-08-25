# Firebase Functions Secret Management for Production Deployment

To securely manage all sensitive values for your Cloud Functions deployment, use the Firebase CLI to set secrets. This ensures that API keys and credentials are not hardcoded or checked into source control.

## Required Secrets

Set all of the following as Firebase secrets for production:

- GOOGLE_CSE_KEY
- GOOGLE_CSE_CX
- FIREBASE_STORAGE_BUCKET
- FIREBASE_PROJECT_ID
- FIREBASE_AUTH_DOMAIN
- FIREBASE_API_KEY
- FIREBASE_APP_ID
- FIREBASE_MESSAGING_SENDER_ID
- FIREBASE_MEASUREMENT_ID
- SERVICE_ACCOUNT_JSON (the contents of service-account.json)


## Commands to Set Secrets

Run these commands in your project root (replace `<value>` with your actual secret values):

```sh
firebase functions:secrets:set GOOGLE_CSE_KEY --project contentforge-ai-ygy25 --data <your-google-cse-key>
firebase functions:secrets:set GOOGLE_CSE_CX --project contentforge-ai-ygy25 --data <your-google-cse-cx>
firebase functions:secrets:set FIREBASE_STORAGE_BUCKET --project contentforge-ai-ygy25 --data <your-firebase-storage-bucket>
firebase functions:secrets:set FIREBASE_PROJECT_ID --project contentforge-ai-ygy25 --data <your-firebase-project-id>
firebase functions:secrets:set FIREBASE_AUTH_DOMAIN --project contentforge-ai-ygy25 --data <your-firebase-auth-domain>
firebase functions:secrets:set FIREBASE_API_KEY --project contentforge-ai-ygy25 --data <your-firebase-api-key>
firebase functions:secrets:set FIREBASE_APP_ID --project contentforge-ai-ygy25 --data <your-firebase-app-id>
firebase functions:secrets:set FIREBASE_MESSAGING_SENDER_ID --project contentforge-ai-ygy25 --data <your-firebase-messaging-sender-id>
firebase functions:secrets:set FIREBASE_MEASUREMENT_ID --project contentforge-ai-ygy25 --data <your-firebase-measurement-id>
firebase functions:secrets:set SERVICE_ACCOUNT_JSON --project contentforge-ai-ygy25 --data-file service-account.json
```

## Usage in Cloud Functions

In your function code, access these secrets using:

```js
process.env.GOOGLE_CSE_KEY
process.env.GOOGLE_CSE_CX
// ...etc
```

For SERVICE_ACCOUNT_JSON, you may need to parse the JSON string:

```js
const serviceAccount = JSON.parse(process.env.SERVICE_ACCOUNT_JSON);
```

## Notes
- Never commit secrets or service-account.json to source control.
- Always use the Firebase CLI for secret management in production.
- For local development, use the .env file as before.

---

**This file documents the required steps for secure, production-grade secret management for your deployment pipeline.**
