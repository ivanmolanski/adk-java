#!/bin/bash

# Firebase Functions Secret Setup Script for MD Aesthetics Viral Forge
# Run this script to set all required secrets for the Firebase Functions

set -e

PROJECT_ID="contentforge-ai-ygy25"
echo "Setting up Firebase Functions secrets for project: $PROJECT_ID"

# Check if firebase CLI is installed
if ! command -v firebase &> /dev/null; then
    echo "Firebase CLI is not installed. Please install it first:"
    echo "npm install -g firebase-tools"
    exit 1
fi

# Check if user is logged in
if ! firebase projects:list &> /dev/null; then
    echo "Please login to Firebase first:"
    echo "firebase login"
    exit 1
fi

echo "Setting Firebase project to: $PROJECT_ID"
firebase use $PROJECT_ID

# Read secrets from main .env file
if [ -f "../.env" ]; then
    echo "Reading configuration from main .env file..."
    source ../.env
elif [ -f ".env" ]; then
    echo "Reading configuration from local .env file..."
    source .env
else
    echo "No .env file found. Please create one with required values."
    exit 1
fi

# Function to create or update a secret
set_secret() {
    local SECRET_NAME=$1
    local SECRET_VALUE=$2

    if [ -z "$SECRET_VALUE" ] || [ "$SECRET_VALUE" = "REPLACE_WITH_REAL_"* ] || [ "$SECRET_VALUE" = "your-"* ]; then
        echo "Skipping $SECRET_NAME (not set or placeholder value: $SECRET_VALUE)"
        return
    fi

    echo "Setting secret: $SECRET_NAME"
    firebase functions:secrets:set $SECRET_NAME --data "$SECRET_VALUE"
}

# Set up all required secrets
echo "Setting up secrets..."

# APIFY Token for scraping
if [ -n "$APIFY_TOKEN" ]; then
    set_secret "APIFY_TOKEN" "$APIFY_TOKEN"
fi

# Note: GEMINI_API_KEY is deprecated. Use OPENROUTER_API_KEY instead.

# OpenRouter API Key (new)
if [ -n "$OPENROUTER_API_KEY" ]; then
    set_secret "OPENROUTER_API_KEY" "$OPENROUTER_API_KEY"
fi

# Google Custom Search API Key
if [ -n "$GOOGLE_CSE_KEY" ]; then
    set_secret "GOOGLE_CSE_KEY" "$GOOGLE_CSE_KEY"
fi

# Google Custom Search Engine ID
if [ -n "$GOOGLE_CSE_CX" ]; then
    set_secret "GOOGLE_CSE_CX" "$GOOGLE_CSE_CX"
fi

# Firebase Configuration
if [ -n "$FIREBASE_PROJECT_ID" ]; then
    set_secret "FIREBASE_PROJECT_ID" "$FIREBASE_PROJECT_ID"
fi

if [ -n "$FIREBASE_API_KEY" ]; then
    set_secret "FIREBASE_API_KEY" "$FIREBASE_API_KEY"
fi

if [ -n "$FIREBASE_AUTH_DOMAIN" ]; then
    set_secret "FIREBASE_AUTH_DOMAIN" "$FIREBASE_AUTH_DOMAIN"
fi

if [ -n "$FIREBASE_STORAGE_BUCKET" ]; then
    set_secret "FIREBASE_STORAGE_BUCKET" "$FIREBASE_STORAGE_BUCKET"
fi

if [ -n "$FIREBASE_APP_ID" ]; then
    set_secret "FIREBASE_APP_ID" "$FIREBASE_APP_ID"
fi

if [ -n "$FIREBASE_MESSAGING_SENDER_ID" ]; then
    set_secret "FIREBASE_MESSAGING_SENDER_ID" "$FIREBASE_MESSAGING_SENDER_ID"
fi

if [ -n "$FIREBASE_MEASUREMENT_ID" ]; then
    set_secret "FIREBASE_MEASUREMENT_ID" "$FIREBASE_MEASUREMENT_ID"
fi

# Service Account JSON (if service-account.json file exists in project root)
if [ -f "../service-account.json" ]; then
    echo "Setting SERVICE_ACCOUNT_JSON from ../service-account.json file"
    firebase functions:secrets:set SERVICE_ACCOUNT_JSON --data-file ../service-account.json
elif [ -f "service-account.json" ]; then
    echo "Setting SERVICE_ACCOUNT_JSON from service-account.json file"
    firebase functions:secrets:set SERVICE_ACCOUNT_JSON --data-file service-account.json
else
    echo "Warning: service-account.json not found. Please create it and run:"
    echo "firebase functions:secrets:set SERVICE_ACCOUNT_JSON --data-file service-account.json"
fi

# Gmail Service Account (if provided)
if [ -n "$GMAIL_SERVICE_ACCOUNT" ]; then
    set_secret "GMAIL_SERVICE_ACCOUNT" "$GMAIL_SERVICE_ACCOUNT"
fi

echo "✅ Firebase Functions secrets setup complete!"
echo ""
echo "To verify secrets were set correctly:"
echo "firebase functions:secrets:access APIFY_TOKEN"
echo ""
echo "Next steps:"
echo "1. Deploy Functions: cd functions && firebase deploy --only functions"
echo "2. Test the integration: curl https://us-central1-$PROJECT_ID.cloudfunctions.net/runOrchestrationHttp"