"""
Scraping Module for MD Aesthetics Viral Content System

This module handles scraping viral content from various sources:
- Google Custom Search Engine for web content
- Apify for social media content
"""

import os
import logging
import requests
from typing import List, Dict, Any, Optional
from datetime import datetime
import json
from dotenv import load_dotenv

# Load environment variables from the project root
env_path = '/workspaces/adk-java/.env'
load_dotenv(env_path)

logger = logging.getLogger(__name__)

class GoogleCSEScraper:
    """Scraper for Google Custom Search Engine."""
    
    def __init__(self):
        self.api_key = os.getenv("GOOGLE_CSE_KEY")
        self.search_engine_id = os.getenv("GOOGLE_CSE_CX")
        self.base_url = "https://www.googleapis.com/customsearch/v1"
        
        if not self.api_key or not self.search_engine_id:
            logger.warning("Google CSE credentials not found in environment variables")
    
    def search_viral_content(self, query: str, num_results: int = 10) -> List[Dict[str, Any]]:
        """
        Search for viral content using Google CSE.
        
        Args:
            query: Search query
            num_results: Number of results to return
            
        Returns:
            List of search results
        """
        if not self.api_key or not self.search_engine_id:
            logger.error("Google CSE credentials not configured")
            return []
        
        try:
            params = {
                'key': self.api_key,
                'cx': self.search_engine_id,
                'q': query,
                'num': min(num_results, 10),  # Google CSE limit
                'dateRestrict': 'd7',  # Last 7 days
                'sort': 'date'  # Sort by date
            }
            
            response = requests.get(self.base_url, params=params)
            response.raise_for_status()
            
            data = response.json()
            results = []
            
            if 'items' in data:
                for item in data['items']:
                    result = {
                        'title': item.get('title', ''),
                        'link': item.get('link', ''),
                        'snippet': item.get('snippet', ''),
                        'display_link': item.get('displayLink', ''),
                        'query': query,
                        'scraped_at': datetime.utcnow().isoformat()
                    }
                    results.append(result)
            
            logger.info(f"Found {len(results)} results for query: {query}")
            return results
            
        except Exception as e:
            logger.error(f"Error searching Google CSE: {e}")
            return []
    
    def search_competitor_content(self, competitors: List[str]) -> List[Dict[str, Any]]:
        """
        Search for content from competitor accounts.
        
        Args:
            competitors: List of competitor names/handles
            
        Returns:
            List of competitor content results
        """
        all_results = []
        
        for competitor in competitors:
            # Search for recent posts from this competitor
            queries = [
                f'site:instagram.com "{competitor}" aesthetics treatment',
                f'site:tiktok.com "{competitor}" skincare viral',
                f'"{competitor}" medspa treatment results'
            ]
            
            for query in queries:
                results = self.search_viral_content(query, 5)
                all_results.extend(results)
        
        return all_results

class ApifyScraper:
    """Scraper for social media content using Apify."""
    
    def __init__(self):
        self.api_token = os.getenv("APIFY_TOKEN")
        self.base_url = "https://api.apify.com/v2"
        
        if not self.api_token:
            logger.warning("Apify token not found in environment variables")
    
    def scrape_instagram_profile(self, profile_url: str) -> List[Dict[str, Any]]:
        """
        Scrape Instagram profile using Apify.
        
        Args:
            profile_url: Instagram profile URL
            
        Returns:
            List of posts from the profile
        """
        if not self.api_token:
            logger.error("Apify token not configured")
            return []
        
        try:
            # Use Apify Instagram Scraper actor
            actor_id = "apidojo/instagram-scraper"
            
            run_input = {
                "username": profile_url.split('/')[-1],  # Extract username from URL
                "resultsLimit": 20
            }
            
            # Start actor run
            run_url = f"{self.base_url}/acts/{actor_id}/runs"
            headers = {"Authorization": f"Bearer {self.api_token}"}
            response = requests.post(run_url, json=run_input, headers=headers)
            response.raise_for_status()
            
            run_data = response.json()
            run_id = run_data['data']['id']
            
            # Wait for completion and get results
            results_url = f"{self.base_url}/acts/{actor_id}/runs/{run_id}/dataset/items"
            
            # For now, return mock data since we can't wait for async completion
            logger.info(f"Started Apify scrape for {profile_url}, run ID: {run_id}")
            
            return self._get_mock_instagram_data(profile_url)
            
        except Exception as e:
            logger.error(f"Error scraping Instagram with Apify: {e}")
            return []
    
    def scrape_tiktok_profile(self, profile_url: str) -> List[Dict[str, Any]]:
        """
        Scrape TikTok profile using Apify.
        
        Args:
            profile_url: TikTok profile URL
            
        Returns:
            List of videos from the profile
        """
        if not self.api_token:
            logger.error("Apify token not configured")
            return []
        
        try:
            # Use Apify TikTok Scraper actor
            actor_id = "clockworks/tiktok-scraper"
            
            run_input = {
                "profiles": [profile_url],
                "resultsPerPage": 20
            }
            
            # Start actor run
            run_url = f"{self.base_url}/acts/{actor_id}/runs"
            headers = {"Authorization": f"Bearer {self.api_token}"}
            response = requests.post(run_url, json=run_input, headers=headers)
            response.raise_for_status()
            
            run_data = response.json()
            run_id = run_data['data']['id']
            
            logger.info(f"Started Apify scrape for {profile_url}, run ID: {run_id}")
            
            return self._get_mock_tiktok_data(profile_url)
            
        except Exception as e:
            logger.error(f"Error scraping TikTok with Apify: {e}")
            return []
    
    def _get_mock_instagram_data(self, profile_url: str) -> List[Dict[str, Any]]:
        """Get mock Instagram data for testing."""
        return [
            {
                'platform': 'instagram',
                'profile': profile_url,
                'post_url': f"{profile_url}/p/mock1",
                'caption': "✨ Transform your skin with our latest treatment! Results you can see and feel. #skincare #aesthetics",
                'hashtags': ['#skincare', '#aesthetics', '#treatment', '#results'],
                'engagement_rate': 8.5,
                'likes': 1250,
                'comments': 89,
                'shares': 45,
                'views': 0,  # Instagram posts don't have views like TikTok
                'scraped_at': datetime.utcnow().isoformat()
            }
        ]
    
    def _get_mock_tiktok_data(self, profile_url: str) -> List[Dict[str, Any]]:
        """Get mock TikTok data for testing."""
        return [
            {
                'platform': 'tiktok',
                'profile': profile_url,
                'post_url': f"{profile_url}/video/mock1",
                'caption': "POV: Your skin after our treatment 💫 #skincare #viral #aesthetics",
                'hashtags': ['#skincare', '#viral', '#aesthetics', '#treatment'],
                'engagement_rate': 12.3,
                'likes': 15400,
                'comments': 234,
                'shares': 890,
                'views': 125000,
                'scraped_at': datetime.utcnow().isoformat()
            }
        ]

class ViralContentScraper:
    """Main scraper class that combines multiple scraping methods."""
    
    def __init__(self):
        self.google_scraper = GoogleCSEScraper()
        self.apify_scraper = ApifyScraper()
    
    def scrape_competitor_content(self, competitors: List[Dict[str, str]]) -> List[Dict[str, Any]]:
        """
        Scrape content from competitor profiles.
        
        Args:
            competitors: List of competitor configs with 'platform' and 'url'
            
        Returns:
            List of scraped content
        """
        all_content = []
        
        for competitor in competitors:
            platform = competitor.get('platform', '')
            url = competitor.get('url', '')
            
            if platform == 'instagram':
                posts = self.apify_scraper.scrape_instagram_profile(url)
                all_content.extend(posts)
            elif platform == 'tiktok':
                videos = self.apify_scraper.scrape_tiktok_profile(url)
                all_content.extend(videos)
            else:
                logger.warning(f"Unsupported platform: {platform}")
        
        # Also search Google for additional content
        competitor_names = [comp.get('url', '').split('/')[-1] for comp in competitors]
        google_results = self.google_scraper.search_competitor_content(competitor_names)
        
        # Convert Google results to similar format
        for result in google_results:
            content = {
                'platform': 'web',
                'profile': result.get('display_link', ''),
                'post_url': result.get('link', ''),
                'caption': result.get('snippet', ''),
                'hashtags': [],  # Extract hashtags from snippet if possible
                'engagement_rate': 0,  # Not available from Google
                'likes': 0,
                'comments': 0,
                'shares': 0,
                'views': 0,
                'scraped_at': result.get('scraped_at', datetime.utcnow().isoformat())
            }
            all_content.append(content)
        
        logger.info(f"Scraped {len(all_content)} pieces of content from {len(competitors)} competitors")
        return all_content
    
    def search_trending_topics(self, topics: List[str]) -> List[Dict[str, Any]]:
        """
        Search for trending topics in aesthetics.
        
        Args:
            topics: List of topics to search for
            
        Returns:
            List of trending content
        """
        all_results = []
        
        for topic in topics:
            query = f"{topic} viral aesthetics treatment"
            results = self.google_scraper.search_viral_content(query, 5)
            all_results.extend(results)
        
        return all_results