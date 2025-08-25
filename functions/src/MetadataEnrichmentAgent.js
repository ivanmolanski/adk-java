// MetadataEnrichmentAgent.js
// Uses Google CSE to enrich scraped posts with open web metadata (mentions, backlinks, news, etc.)
import fetch from 'node-fetch';

const GOOGLE_CSE_KEY = process.env.GOOGLE_CSE_KEY;
const GOOGLE_CSE_CX = process.env.GOOGLE_CSE_CX;

export async function enrichMetadata(post) {
  // Use post URL or a unique phrase from the post as the query
  const query = post.url || post.caption?.substring(0, 50) || '';
  const url = `https://www.googleapis.com/customsearch/v1?key=${GOOGLE_CSE_KEY}&cx=${GOOGLE_CSE_CX}&q=${encodeURIComponent(query)}&sort=date&dateRestrict=d1`;
  const res = await fetch(url);
  if (!res.ok) return post;
  const data = await res.json();
  post.cseMentions = (data.items || []).map(item => ({
    title: item.title,
    link: item.link,
    snippet: item.snippet,
    displayLink: item.displayLink,
    published: item.pagemap?.metatags?.[0]?.['article:published_time'] || null
  }));
  return post;
}
