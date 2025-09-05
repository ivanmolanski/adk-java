import asyncio
import httpx
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import (
    NoSuchElementException, 
    WebDriverException, 
    TimeoutException,
    ElementNotInteractableException,
    StaleElementReferenceException
)
from bs4 import BeautifulSoup
import logging
import time
from typing import List, Dict, Any, Optional
from datetime import datetime
import re

from ..core.config import get_settings
from ..models.schemas import CompetitorPostCreate, Platform

logger = logging.getLogger(__name__)

class ViralScraper:
    """Web scraper for Instagram and TikTok competitor content"""
    
    def __init__(self):
        self.settings = get_settings()
        self.competitors = [
            {
                "platform": Platform.INSTAGRAM,
                "name": "Artisan Aesthetics",
                "url": "https://www.instagram.com/artisanaesthetics/",
                "username": "artisanaesthetics"
            },
            {
                "platform": Platform.INSTAGRAM, 
                "name": "Skin Vitality",
                "url": "https://www.instagram.com/skinvitality/",
                "username": "skinvitality"
            },
            {
                "platform": Platform.INSTAGRAM,
                "name": "Subtle Aesthetics", 
                "url": "https://www.instagram.com/subtleaesthetics/",
                "username": "subtleaesthetics"
            },
            {
                "platform": Platform.INSTAGRAM,
                "name": "The Look Aesthetics",
                "url": "https://www.instagram.com/thelookaesthetics/",
                "username": "thelookaesthetics"
            }
        ]
    
    def _setup_driver(self) -> webdriver.Chrome:
        """Setup Chrome driver with appropriate options"""
        chrome_options = Options()
        chrome_options.add_argument("--headless")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("--window-size=1920,1080")
        chrome_options.add_argument("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        
        return webdriver.Chrome(options=chrome_options)
    
    async def scrape_competitor_posts(self, limit_per_profile: int = 10) -> List[CompetitorPostCreate]:
        """Scrape posts from all competitor profiles"""
        
        all_posts = []
        
        for competitor in self.competitors:
            try:
                logger.info(f"Scraping {competitor['name']} on {competitor['platform']}")
                
                if competitor["platform"] == Platform.INSTAGRAM:
                    posts = await self._scrape_instagram_profile(competitor, limit_per_profile)
                elif competitor["platform"] == Platform.TIKTOK:
                    posts = await self._scrape_tiktok_profile(competitor, limit_per_profile)
                else:
                    logger.warning(f"Unsupported platform: {competitor['platform']}")
                    continue
                
                all_posts.extend(posts)
                logger.info(f"Scraped {len(posts)} posts from {competitor['name']}")
                
                # Rate limiting
                await asyncio.sleep(2)
                
            except Exception as e:
                logger.error(f"Error scraping {competitor['name']}: {str(e)}")
                continue
        
        return all_posts
    
    async def _scrape_instagram_profile(self, competitor: Dict[str, Any], limit: int) -> List[CompetitorPostCreate]:
        """Scrape Instagram profile posts"""
        
        posts = []
        driver = None
        
        try:
            driver = self._setup_driver()
            driver.get(competitor["url"])
            
            # Wait for page to load
            await asyncio.sleep(3)
            
            # Try to handle any popups or login prompts
            try:
                # Close any "Log In" modal if it appears
                close_button = driver.find_element(By.XPATH, "//button[contains(text(), 'Not Now') or contains(@aria-label, 'Close')]")
                close_button.click()
                await asyncio.sleep(1)
            except (NoSuchElementException, ElementNotInteractableException, TimeoutException):
                # No popup found or couldn't interact with it - this is expected behavior
                pass
            
            # Scroll to load more posts
            for i in range(3):  # Scroll 3 times to get more posts
                driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
                await asyncio.sleep(2)
            
            # Find post elements
            post_elements = driver.find_elements(By.XPATH, "//article//a[contains(@href, '/p/')]")
            
            for i, post_element in enumerate(post_elements[:limit]):
                try:
                    post_url = post_element.get_attribute("href")
                    
                    # Get post details by clicking or opening in new tab
                    post_data = await self._scrape_instagram_post_details(driver, post_url, competitor)
                    
                    if post_data:
                        posts.append(post_data)
                    
                    if i < len(post_elements) - 1:  # Don't sleep after last iteration
                        await asyncio.sleep(1)  # Rate limiting
                        
                except Exception as e:
                    logger.error(f"Error scraping Instagram post: {str(e)}")
                    continue
        
        finally:
            if driver:
                driver.quit()
        
        return posts
    
    async def _scrape_instagram_post_details(self, driver: webdriver.Chrome, post_url: str, competitor: Dict[str, Any]) -> Optional[CompetitorPostCreate]:
        """Scrape detailed information from an Instagram post"""
        
        try:
            # Open post in new tab to avoid navigation issues
            driver.execute_script(f"window.open('{post_url}', '_blank');")
            driver.switch_to.window(driver.window_handles[-1])
            
            await asyncio.sleep(3)  # Wait for page to load
            
            # Extract caption
            caption = ""
            try:
                # Try multiple selectors for caption
                caption_selectors = [
                    "//span[contains(@class, '_ap3a')]//span",
                    "//div[contains(@data-testid, 'post-caption')]//span",
                    "//div[contains(@class, 'caption')]//span"
                ]
                
                for selector in caption_selectors:
                    try:
                        caption_element = driver.find_element(By.XPATH, selector)
                        caption = caption_element.text
                        break
                    except (NoSuchElementException, StaleElementReferenceException):
                        continue
            except WebDriverException as e:
                logger.warning(f"Error extracting caption: {str(e)}")
                pass
            
            # Extract hashtags from caption
            hashtags = re.findall(r'#\w+', caption) if caption else []
            
            # Extract engagement metrics (simplified - Instagram doesn't always show exact numbers)
            likes = 0
            comments = 0
            
            try:
                # Look for like count
                like_elements = driver.find_elements(By.XPATH, "//button[contains(@aria-label, 'like')]//span | //span[contains(text(), 'likes')]")
                for element in like_elements:
                    text = element.text
                    if 'like' in text.lower():
                        # Extract number from text like "1,234 likes"
                        numbers = re.findall(r'[\d,]+', text)
                        if numbers:
                            likes = int(numbers[0].replace(',', ''))
                        break
            except (NoSuchElementException, StaleElementReferenceException, ValueError) as e:
                logger.debug(f"Could not extract like count: {str(e)}")
                pass
            
            try:
                # Look for comment count
                comment_elements = driver.find_elements(By.XPATH, "//a[contains(@href, '/comments/')]//span | //span[contains(text(), 'comment')]")
                for element in comment_elements:
                    text = element.text
                    if 'comment' in text.lower():
                        numbers = re.findall(r'[\d,]+', text)
                        if numbers:
                            comments = int(numbers[0].replace(',', ''))
                        break
            except (NoSuchElementException, StaleElementReferenceException, ValueError) as e:
                logger.debug(f"Could not extract comment count: {str(e)}")
                pass
            
            # Close the tab and switch back
            driver.close()
            driver.switch_to.window(driver.window_handles[0])
            
            # Create post data
            post_data = CompetitorPostCreate(
                platform=Platform.INSTAGRAM,
                profile_url=competitor["url"],
                post_url=post_url,
                caption=caption,
                hashtags=hashtags,
                likes=likes,
                comments=comments,
                shares=0,  # Instagram doesn't show share count
                views=0,   # Instagram doesn't always show view count
                post_date=datetime.now()  # Approximate - would need more complex parsing for exact date
            )
            
            return post_data
            
        except Exception as e:
            logger.error(f"Error scraping Instagram post details: {str(e)}")
            return None
    
    async def _scrape_tiktok_profile(self, competitor: Dict[str, Any], limit: int) -> List[CompetitorPostCreate]:
        """Scrape TikTok profile posts (placeholder implementation)"""
        
        # TikTok scraping is more complex due to heavy JavaScript and anti-bot measures
        # For now, return empty list - would need specialized TikTok scraping solution
        
        logger.info(f"TikTok scraping not fully implemented yet for {competitor['name']}")
        return []
    
    async def scrape_hashtag_posts(self, hashtag: str, platform: Platform, limit: int = 20) -> List[CompetitorPostCreate]:
        """Scrape posts by hashtag"""
        
        posts = []
        
        if platform == Platform.INSTAGRAM:
            posts = await self._scrape_instagram_hashtag(hashtag, limit)
        elif platform == Platform.TIKTOK:
            posts = await self._scrape_tiktok_hashtag(hashtag, limit)
        
        return posts
    
    async def _scrape_instagram_hashtag(self, hashtag: str, limit: int) -> List[CompetitorPostCreate]:
        """Scrape Instagram hashtag page"""
        
        posts = []
        driver = None
        
        try:
            hashtag = hashtag.replace('#', '')  # Remove # if present
            url = f"https://www.instagram.com/explore/tags/{hashtag}/"
            
            driver = self._setup_driver()
            driver.get(url)
            
            await asyncio.sleep(3)
            
            # Similar logic to profile scraping but for hashtag page
            # This is a simplified implementation
            
            post_elements = driver.find_elements(By.XPATH, "//article//a[contains(@href, '/p/')]")
            
            for post_element in post_elements[:limit]:
                try:
                    post_url = post_element.get_attribute("href")
                    
                    # Create basic post data (would need full implementation)
                    post_data = CompetitorPostCreate(
                        platform=Platform.INSTAGRAM,
                        profile_url="",  # Would extract from post
                        post_url=post_url,
                        caption="",      # Would extract from post
                        hashtags=[f"#{hashtag}"],
                        likes=0,
                        comments=0,
                        shares=0,
                        views=0,
                        post_date=datetime.now()
                    )
                    
                    posts.append(post_data)
                    
                except Exception as e:
                    logger.error(f"Error scraping hashtag post: {str(e)}")
                    continue
        
        finally:
            if driver:
                driver.quit()
        
        return posts
    
    async def _scrape_tiktok_hashtag(self, hashtag: str, limit: int) -> List[CompetitorPostCreate]:
        """Scrape TikTok hashtag page (placeholder)"""
        
        logger.info(f"TikTok hashtag scraping not implemented yet for #{hashtag}")
        return []

# Convenience function for scheduled scraping
async def run_daily_scrape() -> List[CompetitorPostCreate]:
    """Run daily competitor scraping"""
    
    scraper = ViralScraper()
    
    # Scrape competitor profiles
    competitor_posts = await scraper.scrape_competitor_posts(limit_per_profile=5)
    
    # Scrape relevant hashtags
    md_hashtags = ["torontoaesthetics", "whitbymedspa", "skintyte", "radiesse", "ultherapy"]
    
    for hashtag in md_hashtags[:2]:  # Limit to avoid rate limiting
        try:
            hashtag_posts = await scraper.scrape_hashtag_posts(hashtag, Platform.INSTAGRAM, limit=5)
            competitor_posts.extend(hashtag_posts)
        except Exception as e:
            logger.error(f"Error scraping hashtag {hashtag}: {str(e)}")
    
    logger.info(f"Daily scrape completed: {len(competitor_posts)} posts collected")
    return competitor_posts