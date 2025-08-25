import puppeteer from 'puppeteer';
import { Firestore } from '@google-cloud/firestore';

const firestore = new Firestore();

const competitors = [
  { platform: 'instagram', url: 'https://www.instagram.com/artisanaesthetics/' },
  { platform: 'instagram', url: 'https://www.instagram.com/skinvitality/' },
  { platform: 'instagram', url: 'https://www.instagram.com/subtleaesthetics/' },
  { platform: 'instagram', url: 'https://www.instagram.com/thelookaesthetics/' }
  // Add TikTok URLs as needed
];

async function scrapeProfile(profile) {
  const browser = await puppeteer.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto(profile.url, { waitUntil: 'networkidle2' });

  // Scroll to load more posts
  let prevHeight;
  for (let i = 0; i < 5; i++) {
    prevHeight = await page.evaluate('document.body.scrollHeight');
    await page.evaluate('window.scrollTo(0, document.body.scrollHeight)');
    await page.waitForTimeout(2000);
    let newHeight = await page.evaluate('document.body.scrollHeight');
    if (newHeight === prevHeight) break;
  }

  // Extract posts
  const posts = await page.evaluate(() => {
    const postElements = document.querySelectorAll('article a');
    return Array.from(postElements).map(el => ({
      postURL: el.href,
      caption: el.innerText || '',
      hashtags: (el.innerText.match(/#[a-zA-Z0-9_]+/g) || []),
      engagementRate: null, // Calculated later
      likes: null,
      comments: null,
      shares: null,
      views: null,
      date: null
    }));
  });

  await browser.close();

  // Save to Firestore
  for (let post of posts) {
    await firestore.collection('competitorPosts').add({
      platform: profile.platform,
      profile: profile.url,
      ...post,
      scrapedAt: new Date()
    });
  }
}

async function main() {
  for (let profile of competitors) {
    console.log(`Scraping ${profile.url}`);
    await scrapeProfile(profile);
  }
}

main().catch(console.error);
