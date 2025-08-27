// MetadataEnrichmentAgent.js (Deprecated path)
// -----------------------------------------------------------------------------
// This legacy module previously performed a per-post Google CSE query, storing
// results under post.cseMentions. The system now uses global multi-term
// augmentation via googleCSEAgent (post.cseContext) for efficiency & relevance.
// A feature flag ENABLE_LEGACY_METADATA_ENRICHMENT controls whether this older
// per-post query runs as a fallback. Default: disabled (no per-post query).
// -----------------------------------------------------------------------------
import fetch from 'node-fetch';

const GOOGLE_CSE_KEY = process.env.GOOGLE_CSE_KEY;
const GOOGLE_CSE_CX = process.env.GOOGLE_CSE_CX;
const ENABLE_LEGACY = /^true$/i.test(process.env.ENABLE_LEGACY_METADATA_ENRICHMENT || '');

/**
 * Legacy per-post enrichment. If disabled, returns post unchanged.
 * If enabled and env keys present, executes a narrow query for potential
 * ultra-fresh mentions and attaches cseMentions (distinct from cseContext).
 */
export async function enrichMetadata(post) {
  if (!post) return post;
  if (!ENABLE_LEGACY) {
    // Feature flag off: no-op, maintain interface.
    return post;
  }
  if (!GOOGLE_CSE_KEY || !GOOGLE_CSE_CX) return post;
  try {
    const basis = post.postURL || post.url || post.caption || '';
    const query = basis.substring(0, 80);
    if (!query.trim()) return post;
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
  } catch (e) {
    // Non-fatal; we deliberately do not throw to avoid breaking pipeline.
  }
  return post;
}

export default { enrichMetadata };
