#!/bin/bash

# Firebase Secrets Setup Script for Viral Forge System
# Run this script after logging into gcloud to set up all required secrets for production deployment

set -e

PROJECT_ID="contentforge-ai-ygy25"
echo "Setting up Firebase Secrets for project: $PROJECT_ID"

# Ensure we're using the correct project
gcloud config set project $PROJECT_ID

# Read secrets from .env file if it exists
if [ -f ".env" ]; then
    echo "Reading configuration from .env file..."
    source .env
else
    echo "No .env file found. Please create one with required values."
    exit 1
fi

# Function to create or update a secret
create_or_update_secret() {
    local SECRET_NAME=$1
    local SECRET_VALUE=$2
    
    if gcloud secrets describe $SECRET_NAME --project=$PROJECT_ID >/dev/null 2>&1; then
        echo "Updating existing secret: $SECRET_NAME"
        echo -n "$SECRET_VALUE" | gcloud secrets versions add $SECRET_NAME --data-file=-
    else
        echo "Creating new secret: $SECRET_NAME"
        echo -n "$SECRET_VALUE" | gcloud secrets create $SECRET_NAME --data-file=-
    fi
}

# Set up all required secrets
echo "Setting up secrets..."

# Firebase configuration secrets
if [ -n "$FIREBASE_API_KEY" ]; then
    create_or_update_secret "FIREBASE_API_KEY" "$FIREBASE_API_KEY"
fi

if [ -n "$FIREBASE_AUTH_DOMAIN" ]; then
    create_or_update_secret "FIREBASE_AUTH_DOMAIN" "$FIREBASE_AUTH_DOMAIN"
fi

if [ -n "$FIREBASE_PROJECT_ID" ]; then
    create_or_update_secret "FIREBASE_PROJECT_ID" "$FIREBASE_PROJECT_ID"
fi

if [ -n "$FIREBASE_STORAGE_BUCKET" ]; then
    create_or_update_secret "FIREBASE_STORAGE_BUCKET" "$FIREBASE_STORAGE_BUCKET"
fi

if [ -n "$FIREBASE_MESSAGING_SENDER_ID" ]; then
    create_or_update_secret "FIREBASE_MESSAGING_SENDER_ID" "$FIREBASE_MESSAGING_SENDER_ID"
fi

if [ -n "$FIREBASE_APP_ID" ]; then
    create_or_update_secret "FIREBASE_APP_ID" "$FIREBASE_APP_ID"
fi

# GenAI API Key (already exists but ensure it's up to date)
if [ -n "$GEMINI_API_KEY" ]; then
    create_or_update_secret "GEMINI_API_KEY" "$GEMINI_API_KEY"
fi

# Service Account JSON (if service-account.json file exists)
if [ -f "service-account.json" ]; then
    echo "Setting up SERVICE_ACCOUNT_JSON secret from service-account.json file"
    create_or_update_secret "SERVICE_ACCOUNT_JSON" "$(cat service-account.json)"
fi

# Google Custom Search (optional - for enhanced scraping)
if [ -n "$GOOGLE_CSE_KEY" ]; then
    create_or_update_secret "GOOGLE_CSE_KEY" "$GOOGLE_CSE_KEY"
fi

if [ -n "$GOOGLE_CSE_CX" ]; then
    create_or_update_secret "GOOGLE_CSE_CX" "$GOOGLE_CSE_CX"
fi

# Social Media API credentials (optional)
if [ -n "$TIKTOK_CLIENT_ID" ]; then
    create_or_update_secret "TIKTOK_CLIENT_ID" "$TIKTOK_CLIENT_ID"
fi

if [ -n "$TIKTOK_CLIENT_SECRET" ]; then
    create_or_update_secret "TIKTOK_CLIENT_SECRET" "$TIKTOK_CLIENT_SECRET"
fi

if [ -n "$INSTAGRAM_CLIENT_ID" ]; then
    create_or_update_secret "INSTAGRAM_CLIENT_ID" "$INSTAGRAM_CLIENT_ID"
fi

if [ -n "$INSTAGRAM_CLIENT_SECRET" ]; then
    create_or_update_secret "INSTAGRAM_CLIENT_SECRET" "$INSTAGRAM_CLIENT_SECRET"
fi

# Gmail API credentials for email dispatch
if [ -n "$GMAIL_SERVICE_ACCOUNT_KEY" ]; then
    create_or_update_secret "GMAIL_SERVICE_ACCOUNT_KEY" "$GMAIL_SERVICE_ACCOUNT_KEY"
fi

echo "✅ Firebase secrets setup complete!"
echo ""
echo "Secrets created/updated:"
gcloud secrets list --filter="name:projects/$PROJECT_ID/secrets/" --format="table(name.basename():label='SECRET_NAME',createTime:label='CREATED')"

echo ""
echo "Next steps:"
echo "1. Deploy Functions: cd functions && firebase deploy --only functions"
echo "2. Deploy Java service to Cloud Run or App Engine"
echo "3. Configure Pub/Sub topics and subscriptions"
echo "4. Set up Cloud Scheduler for daily runs"