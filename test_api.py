#!/usr/bin/env python3
"""Test script to validate API endpoints"""

import requests
import json
import sys

def test_endpoint(url, name):
    """Test a single endpoint"""
    try:
        print(f"Testing {name}: {url}")
        response = requests.get(url, timeout=5)
        print(f"  Status: {response.status_code}")
        if response.status_code == 200:
            try:
                data = response.json()
                print(f"  Response: {json.dumps(data, indent=2)}")
            except:
                print(f"  Response: {response.text}")
        else:
            print(f"  Error: {response.text}")
        print()
        return response.status_code == 200
    except Exception as e:
        print(f"  Failed: {e}")
        print()
        return False

def main():
    base_url = "http://localhost:3453"
    
    # Test endpoints
    endpoints = [
        (f"{base_url}/viral-service/api/v1/health", "Health Check"),
        (f"{base_url}/viral-service/api/v1/ai/health", "AI Health Check"),
        (f"{base_url}/viral-service/api/v1/viral/trends", "Viral Trends"),
        (f"{base_url}/viral-service/api/v1/viral/drafts", "Content Drafts"),
    ]
    
    print("=== API Endpoint Testing ===\n")
    
    results = []
    for url, name in endpoints:
        success = test_endpoint(url, name)
        results.append((name, success))
    
    print("=== Summary ===")
    for name, success in results:
        status = "✓" if success else "✗"
        print(f"{status} {name}")
    
    # Count successes
    success_count = sum(1 for _, success in results if success)
    print(f"\nPassed: {success_count}/{len(results)}")
    
    return success_count == len(results)

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)