#!/usr/bin/env python3
"""Test script to demonstrate the MD Aesthetics Viral Intelligence system."""

import asyncio
import json
import httpx
from datetime import datetime

BASE_URL = "http://localhost:3453/viral-service/api/v1"

# Sample competitor posts for testing
SAMPLE_POSTS = [
    {
        "id": "sample_001",
        "platform": "instagram",
        "profile": "_thelookaesthetics",
        "caption": "✨ 3-second glow up transformation! Before & after SkinTyte treatment. The infrared technology literally tightens your skin in real-time. DM for consultation! #SkinTyte #GlowUp #TorontoAesthetics #SkinTightening",
        "hashtags": ["#SkinTyte", "#GlowUp", "#TorontoAesthetics", "#SkinTightening"],
        "engagement_rate": 8.5,
        "likes": 1250,
        "comments": 89,
        "shares": 45,
        "views": 15000,
        "post_url": "https://instagram.com/p/sample001",
        "scraped_at": datetime.utcnow().isoformat()
    },
    {
        "id": "sample_002", 
        "platform": "instagram",
        "profile": "subtle.enhancements",
        "caption": "Stop wasting money on botox every 3 months 🛑 Radiesse biostimulator rebuilds your own collagen. Results last 18+ months! Book your consultation today. #Radiesse #AntiAging #BiostimulatorFiller #WhitbyMedSpa",
        "hashtags": ["#Radiesse", "#AntiAging", "#BiostimulatorFiller", "#WhitbyMedSpa"],
        "engagement_rate": 7.2,
        "likes": 980,
        "comments": 76,
        "shares": 32,
        "views": 12500,
        "post_url": "https://instagram.com/p/sample002",
        "scraped_at": datetime.utcnow().isoformat()
    },
    {
        "id": "sample_003",
        "platform": "instagram", 
        "profile": "skinvitality",
        "caption": "The ULTIMATE buttock transformation package! 🍑 SkinTyte + Radiesse = lifted, firm, smooth results. No surgery, no downtime. Real results in 4 weeks! Link in bio to book. #ButtockLift #SkinTyte #Radiesse #BodyContouring #TorontoMedSpa",
        "hashtags": ["#ButtockLift", "#SkinTyte", "#Radiesse", "#BodyContouring", "#TorontoMedSpa"],
        "engagement_rate": 9.1,
        "likes": 1876,
        "comments": 134,
        "shares": 89,
        "views": 28500,
        "post_url": "https://instagram.com/p/sample003", 
        "scraped_at": datetime.utcnow().isoformat()
    }
]

async def test_viral_system():
    """Test the complete viral intelligence pipeline."""
    async with httpx.AsyncClient(timeout=60) as client:
        print("🔥 MD Aesthetics Viral Intelligence System Test")
        print("=" * 50)
        
        # 1. Test health
        print("\n1. Testing system health...")
        try:
            health = await client.get(f"{BASE_URL}/../health")
            health_data = health.json()
            print(f"✅ Backend: {health_data.get('status', 'unknown')}")
        except Exception as e:
            print(f"❌ Backend health check failed: {e}")
            
        try:
            ai_health = await client.get(f"{BASE_URL}/ai/health")
            ai_data = ai_health.json()
            print(f"✅ AI Integration: {ai_data.get('status', 'unknown')} ({ai_data.get('latency_ms', 0)}ms)")
        except Exception as e:
            print(f"❌ AI health check failed: {e}")
        
        # 2. Analyze sample posts
        print("\n2. Analyzing competitor posts...")
        analyze_response = await client.post(f"{BASE_URL}/viral/analyze", json={"posts": SAMPLE_POSTS})
        if analyze_response.status_code == 200:
            analyses = analyze_response.json()
            print(f"✅ Analyzed {len(analyses)} posts")
            for analysis in analyses:
                print(f"   📊 {analysis['content_category']}: {analysis['hook'][:50]}...")
        else:
            print(f"❌ Analysis failed: {analyze_response.status_code}")
            print(analyze_response.text)
            return
            
        # 3. Generate MD Aesthetics content
        print("\n3. Generating MD Aesthetics branded content...")
        generate_response = await client.post(f"{BASE_URL}/viral/generate", json={"trend_analysis": analyses})
        if generate_response.status_code == 200:
            drafts = generate_response.json()
            print(f"✅ Generated {len(drafts)} content drafts")
            for draft in drafts[:2]:  # Show first 2
                print(f"   📝 {draft['platform']}: {draft['caption'][:100]}...")
                print(f"      🏷️ Tags: {', '.join(draft['hashtags'][:5])}")
        else:
            print(f"❌ Content generation failed: {generate_response.status_code}")
            print(generate_response.text)
            return
            
        # 4. Get trends summary
        print("\n4. Fetching trend insights...")
        trends_response = await client.get(f"{BASE_URL}/viral/trends?limit=5")
        if trends_response.status_code == 200:
            trends = trends_response.json()
            print(f"✅ Retrieved {len(trends)} trend insights")
            for trend in trends:
                print(f"   📈 Virality: {trend['virality_score']:.2f} | {trend['content_category']}")
        else:
            print(f"❌ Trends fetch failed: {trends_response.status_code}")
            
        # 5. Test daily digest
        print("\n5. Testing email digest generation...")
        digest_response = await client.post(f"{BASE_URL}/viral/digest/send")
        if digest_response.status_code == 200:
            digest_result = digest_response.json()
            print(f"✅ Email digest: {digest_result['status']}")
            print(f"   📧 Subject: {digest_result['subject']}")
            print(f"   👥 Recipients: {', '.join(digest_result['recipients'])}")
        else:
            print(f"❌ Digest failed: {digest_response.status_code}")
            
        print("\n🎉 Viral Intelligence System Test Complete!")
        print("The system is ready for automated competitor analysis and content generation.")

if __name__ == "__main__":
    asyncio.run(test_viral_system())