// TargetDiscoveryAgent.js
// Uses Google CSE to discover new trending post URLs, hashtags, or account handles from the last 24h
import fetch from 'node-fetch';
import { differenceInHours, parseISO } from 'date-fns';

const GOOGLE_CSE_KEY = process.env.GOOGLE_CSE_KEY;
const GOOGLE_CSE_CX = process.env.GOOGLE_CSE_CX;

// Helper to restrict CSE to last 24h
function buildCSEUrl(query) {
  // dateRestrict=d1 means last 1 day
  return `https://www.googleapis.com/customsearch/v1?key=${GOOGLE_CSE_KEY}&cx=${GOOGLE_CSE_CX}&q=${encodeURIComponent(query)}&sort=date&dateRestrict=d1`;
}

export async function discoverTargets(queries) {
  const discovered = [];
  for (const query of queries) {
    const url = buildCSEUrl(query);
    const res = await fetch(url);
    if (!res.ok) continue;
    const data = await res.json();
    for (const item of data.items || []) {
      // Only include if published in last 24h (extra check)
      const published = item.pagemap?.metatags?.[0]?.['article:published_time'] || null;
      if (published) {
        const hoursAgo = differenceInHours(new Date(), parseISO(published));
        if (hoursAgo > 24) continue;
      }
      discovered.push({
        url: item.link,
        title: item.title,
        snippet: item.snippet,
        published,
        source: 'google-cse',
        query
      });
    }
  }
  return discovered;
}
