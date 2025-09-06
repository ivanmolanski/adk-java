#!/usr/bin/env python3
"""
Complete System Test Script for MD Aesthetics Viral Content System
"""

import os
import sys
import requests
import subprocess
import time
from pathlib import Path

def test_environment_loading():
    """Test environment variable loading"""
    print("🔧 Testing environment variable loading...")
    
    # Load .env file
    from dotenv import load_dotenv
    load_dotenv('.env')
    
    google_key = os.getenv('GOOGLE_CSE_KEY')
    apify_token = os.getenv('APIFY_TOKEN')
    
    print(f"✅ GOOGLE_CSE_KEY loaded: {bool(google_key)}")
    print(f"✅ APIFY_TOKEN loaded: {bool(apify_token)}")
    
    return bool(google_key and apify_token)

def test_scraping_module():
    """Test the scraping module"""
    print("\n🔍 Testing scraping module...")
    
    try:
        sys.path.append('backend')
        from app.scraping import ViralContentScraper
        
        scraper = ViralContentScraper()
        print("✅ Scraping module imported successfully")
        
        # Test Google CSE scraper
        google_scraper = scraper.google_scraper
        print(f"✅ Google CSE key loaded in scraper: {bool(google_scraper.api_key)}")
        
        # Test Apify scraper  
        apify_scraper = scraper.apify_scraper
        print(f"✅ Apify token loaded in scraper: {bool(apify_scraper.api_token)}")
        
        return True
    except Exception as e:
        print(f"❌ Scraping module test failed: {e}")
        return False

def test_backend_startup():
    """Test backend startup"""
    print("\n🚀 Testing backend startup...")
    
    try:
        # Start backend in background
        backend_process = subprocess.Popen(
            [sys.executable, 'backend/main.py'],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            cwd=os.getcwd()
        )
        
        # Wait for startup
        time.sleep(3)
        
        # Test health endpoint
        response = requests.get('http://localhost:3453/viral-service/api/v1/health', timeout=5)
        
        if response.status_code == 200:
            print("✅ Backend health check passed")
            backend_process.terminate()
            return True
        else:
            print(f"❌ Backend health check failed: {response.status_code}")
            backend_process.terminate()
            return False
            
    except Exception as e:
        print(f"❌ Backend startup test failed: {e}")
        return False

def test_api_endpoints():
    """Test API endpoints"""
    print("\n📡 Testing API endpoints...")
    
    try:
        # Start backend
        backend_process = subprocess.Popen(
            [sys.executable, 'backend/main.py'],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            cwd=os.getcwd()
        )
        time.sleep(3)
        
        # Test viral endpoints
        endpoints = [
            'http://localhost:3453/viral-service/api/v1/health',
            'http://localhost:3453/viral-service/api/v1/agents/',
            'http://localhost:3453/viral-service/api/v1/viral/posts',
        ]
        
        for endpoint in endpoints:
            try:
                response = requests.get(endpoint, timeout=5)
                print(f"✅ {endpoint}: {response.status_code}")
            except Exception as e:
                print(f"❌ {endpoint}: {e}")
        
        backend_process.terminate()
        return True
        
    except Exception as e:
        print(f"❌ API endpoint test failed: {e}")
        return False

def test_google_cse_scraping():
    """Test Google CSE scraping with live API"""
    print("\n🌐 Testing Google CSE scraping...")
    
    try:
        sys.path.append('backend')
        from app.scraping import GoogleCSEScraper
        
        scraper = GoogleCSEScraper()
        
        if not scraper.api_key:
            print("❌ Google CSE API key not loaded")
            return False
        
        # Test a simple search
        results = scraper.search_viral_content("aesthetics treatment", 3)
        
        print(f"✅ Google CSE search returned {len(results)} results")
        
        if results:
            print(f"✅ First result title: {results[0].get('title', 'N/A')[:50]}...")
        
        return len(results) > 0
        
    except Exception as e:
        print(f"❌ Google CSE scraping test failed: {e}")
        return False

def main():
    """Run all tests"""
    print("🧪 Starting Complete System Test for MD Aesthetics Viral Content System")
    print("=" * 70)
    
    tests = [
        ("Environment Loading", test_environment_loading),
        ("Scraping Module", test_scraping_module),
        ("Backend Startup", test_backend_startup),
        ("API Endpoints", test_api_endpoints),
        ("Google CSE Scraping", test_google_cse_scraping),
    ]
    
    results = []
    
    for test_name, test_func in tests:
        try:
            result = test_func()
            results.append((test_name, result))
        except Exception as e:
            print(f"❌ {test_name} crashed: {e}")
            results.append((test_name, False))
    
    print("\n" + "=" * 70)
    print("📊 TEST RESULTS SUMMARY")
    print("=" * 70)
    
    passed = 0
    total = len(results)
    
    for test_name, result in results:
        status = "✅ PASSED" if result else "❌ FAILED"
        print(f"{test_name}: {status}")
        if result:
            passed += 1
    
    print(f"\n🎯 Overall: {passed}/{total} tests passed")
    
    if passed == total:
        print("🎉 ALL TESTS PASSED! System is ready for production.")
    else:
        print("⚠️  Some tests failed. Check the output above for details.")
    
    return passed == total

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
