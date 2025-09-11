#!/usr/bin/env node
import puppeteer from 'puppeteer';
import axios from 'axios';

/*
 Basic competitor scraper.
 - Navigates to public Instagram profile pages (limited without login; placeholder extraction of anchor tags)
 - TODO: Enhance with authenticated session cookies (IG_SESSION_ID) for richer data, and TikTok support.
 - Posts normalized data to backend ingestion endpoint /viral-service/api/v1/ingest/posts
*/

const competitors = [
  { platform: 'instagram', profile: '_thelookaesthetics', url: 'https://www.instagram.com/_thelookaesthetics/' },
  { platform: 'instagram', profile: 'skinvitality', url: 'https://www.instagram.com/skinvitality/' },
  { platform: 'instagram', profile: 'subtle.enhancements', url: 'https://www.instagram.com/subtle.enhancements/' }
];

const BACKEND_BASE = process.env.VIRAL_SERVICE_URL || 'http://localhost:3453/viral-service';
const INGEST_URL = `${BACKEND_BASE}/api/v1/ingest/posts`;

function extractHashtags(text) {
  return Array.from(new Set((text.match(/#[a-zA-Z0-9_]+/g) || []).map(h => h.trim())));
}

async function scrapeInstagramProfile(page, comp) {
  await page.goto(comp.url, { waitUntil: 'networkidle2' });
  // Basic scroll for lazy loading
  for (let i = 0; i < 3; i++) {
    await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
    await page.waitForTimeout(1500);
  }
  const posts = await page.evaluate(() => {
    const anchors = Array.from(document.querySelectorAll('a'));
    const postLinks = anchors.map(a => a.href).filter(h => /\/p\//.test(h));
    return Array.from(new Set(postLinks)).slice(0, 12).map(href => ({ postURL: href }));
  });
  // Placeholder: Instagram dynamic content requires additional queries; we just map minimal fields
  const normalized = posts.map(p => ({
    id: p.postURL.split('/').filter(Boolean).pop(),
    platform: comp.platform,
    profile: comp.profile,
    caption: '',
    hashtags: [],
    engagement_rate: 0,
    likes: 0,
    comments: 0,
    shares: 0,
    views: 0,
    post_url: p.postURL,
    scraped_at: new Date().toISOString()
  }));
  return normalized;
}

async function run() {
  const browser = await puppeteer.launch({ headless: true });
  const page = await browser.newPage();
  const all = [];
  for (const comp of competitors) {
    try {
      console.log(`Scraping ${comp.profile}`);
      const posts = await scrapeInstagramProfile(page, comp);
      all.push(...posts);
    } catch (e) {
      console.error(`Error scraping ${comp.profile}:`, e.message);
    }
  }
  await browser.close();
  if (!all.length) {
    console.warn('No posts scraped. Exiting.');
    return;
  }
  try {
    console.log(`Posting ${all.length} scraped posts to ingestion endpoint: ${INGEST_URL}`);
    const resp = await axios.post(INGEST_URL, { posts: all, refine: true });
    console.log('Ingestion response status', resp.status);
  } catch (e) {
    console.error('Failed to ingest scraped posts:', e.response?.status, e.response?.data || e.message);
  }
}

run().catch(err => {
  console.error('Fatal scraper error', err);
  process.exit(1);
});
