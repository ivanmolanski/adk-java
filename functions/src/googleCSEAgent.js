import fetch from 'node-fetch';
import { Firestore } from '@google-cloud/firestore';

/**
 * Google CSE Integration & Augmentation Agent
 * Responsibilities:
 *  - Perform configurable multi-term Google Custom Search queries
 *  - Exponential backoff & retry on transient failures
 *  - De-duplicate results (by link) across terms per run
 *  - Persist to Firestore (collection: csePosts) with term + metadata
 *  - Provide enrichment function to attach CSE context to existing post objects
 *  - Export orchestrator-friendly run function (runCseAugmentation)
 */

// Environment configuration (no hardcoded fallbacks; fail loud if missing)
const GOOGLE_CSE_KEY = process.env.GOOGLE_CSE_KEY;
const GOOGLE_CSE_CX = process.env.GOOGLE_CSE_CX; // Search Engine ID

if (!GOOGLE_CSE_KEY || !GOOGLE_CSE_CX) {
  console.warn('[googleCSEAgent] Missing GOOGLE_CSE_KEY or GOOGLE_CSE_CX env vars – CSE queries will be skipped.');
}

// Allow override of terms via env (comma-separated); otherwise default strategic list
const DEFAULT_TERMS = [
  'Duo-C-Lift',
  'SkinTyte',
  'Radiesse',
  'Ultherapy',
  'Vivier Vitamin C',
  'Toronto Aesthetics',
  'Whitby Medspa',
  'Body Contouring',
  'Cellulite Treatment',
  'Medical Grade Skincare',
  'Collagen Stimulation',
  'Non-surgical Butt Lift',
  'Firm and Smooth',
  'Biostimulator',
  'Aesthetics Trends',
  'Facial Balancing'
];

const ENABLE_GLOBAL_CSE = !/^false$/i.test(process.env.ENABLE_GLOBAL_CSE || 'true'); // default enabled

export function getSearchTerms() {
  const override = process.env.GOOGLE_CSE_TERMS;
  if (override && override.trim().length) {
    return override.split(',').map(s => s.trim()).filter(Boolean);
  }
  return DEFAULT_TERMS;
}

// Initialize Firestore (Application Default Credentials expected in Functions env)
const firestore = new Firestore();

/**
 * Perform a single CSE query with retries.
 * @param {string} query
 * @param {number} maxRetries
 * @returns {Promise<Array<object>>}
 */
export async function searchGoogleCSE(query, maxRetries = 3) {
  if (!GOOGLE_CSE_KEY || !GOOGLE_CSE_CX) return [];
  const baseUrl = 'https://www.googleapis.com/customsearch/v1';
  let attempt = 0;
  let lastErr;
  while (attempt <= maxRetries) {
    try {
      const url = `${baseUrl}?key=${GOOGLE_CSE_KEY}&cx=${GOOGLE_CSE_CX}&q=${encodeURIComponent(query)}`;
      const res = await fetch(url, { timeout: 15000 });
      if (!res.ok) {
        // Non-transient errors (4xx other than 429) break early
        if (res.status >= 400 && res.status < 500 && res.status !== 429) {
          throw new Error(`Non-retryable CSE status ${res.status}`);
        }
        throw new Error(`HTTP ${res.status}`);
      }
      const data = await res.json();
      const items = (data.items || []).map(item => ({
        platform: 'google-cse',
        term: query,
        title: item.title ?? null,
        link: item.link ?? null,
        snippet: item.snippet ?? null,
        displayLink: item.displayLink ?? null,
        image: item.pagemap?.cse_image?.[0]?.src ?? null,
        publishedAt: item.pagemap?.metatags?.[0]?.['article:published_time'] ?? null,
        scrapedAt: new Date().toISOString()
      }));
      return items;
    } catch (err) {
      lastErr = err;
      attempt++;
      if (attempt > maxRetries) break;
      const backoffMs = Math.min(5000, 500 * Math.pow(2, attempt));
      await new Promise(r => setTimeout(r, backoffMs));
    }
  }
  console.error(`[googleCSEAgent] Failed query '${query}' after ${maxRetries} retries:`, lastErr?.message);
  return [];
}

/**
 * Run multi-term augmentation, store unique results.
 * @returns {Promise<{total:int, terms:int, saved:int}>}
 */
export async function runCseAugmentation() {
  if (!ENABLE_GLOBAL_CSE) {
    return { total: 0, terms: 0, saved: 0, disabled: true };
  }
  const terms = getSearchTerms();
  const seen = new Set();
  let saved = 0;
  for (const term of terms) {
    const results = await searchGoogleCSE(term);
    for (const r of results) {
      if (!r.link || seen.has(r.link)) continue;
      seen.add(r.link);
      try {
        // Use deterministic doc ID hash (basic) to avoid duplicates across runs
        const docId = Buffer.from(r.link).toString('base64').replace(/[/+=]/g,'');
        await firestore.collection('csePosts').doc(docId).set({
          ...r,
          crossRef: {
            // Placeholder for potential future reverse references
            relatedPostIds: []
          }
        }, { merge: true });
        saved++;
      } catch (e) {
        console.error('[googleCSEAgent] Firestore save failed', e.message);
      }
    }
  }
  return { total: seen.size, terms: terms.length, saved };
}

/**
 * Enrich an existing post object with related CSE documents (simple keyword match on caption or focus terms)
 * @param {object} post { caption, hashtags[] }
 * @param {number} limit
 */
export async function enrichPostWithCse(post, limit = 5) {
  if (!post) return post;
  const tokens = new Set();
  if (post.caption) {
    post.caption.split(/[^A-Za-z0-9+#]+/).forEach(w => { if (w && w.length > 3) tokens.add(w.toLowerCase()); });
  }
  (post.hashtags || []).forEach(h => tokens.add(h.replace('#','').toLowerCase()));
  const candidate = Array.from(tokens).slice(0, 5).join(' ');
  if (!candidate) return post;
  // Simple client-side filter: fetch latest 50 CSE posts and score
  const snap = await firestore.collection('csePosts').orderBy('scrapedAt','desc').limit(50).get();
  const scored = [];
  snap.forEach(doc => {
    const d = doc.data();
    const text = `${d.title||''} ${d.snippet||''}`.toLowerCase();
    let score = 0;
    for (const t of tokens) if (text.includes(t)) score++;
    if (score>0) scored.push({ score, ...d });
  });
  scored.sort((a,b)=>b.score-a.score);
  post.cseContext = scored.slice(0, limit);
  return post;
}

/**
 * Persist the enriched CSE context into the corresponding viral_research document.
 * The docId format currently: {date}_{profile}_{index}. We accept full docId string.
 * This performs a merge to add/replace cseContext array.
 */
export async function mergeCseContextIntoViralResearch(docId, cseContext) {
  if (!docId || !Array.isArray(cseContext)) return false;
  try {
    const ref = firestore.collection('viral_research').doc(docId);
    await ref.set({ cseContext, updatedAt: new Date().toISOString() }, { merge: true });
    return true;
  } catch (e) {
    console.error('[googleCSEAgent] Failed merging CSE context into viral_research', docId, e.message);
    return false;
  }
}

// Allow direct CLI invocation (node googleCSEAgent.js)
if (process.argv[1] && process.argv[1].endsWith('googleCSEAgent.js')) {
  runCseAugmentation()
    .then(r => console.log('[googleCSEAgent] Completed augmentation', r))
    .catch(e => { console.error('[googleCSEAgent] Run failed', e); process.exit(1); });
}

export default {
  getSearchTerms,
  searchGoogleCSE,
  runCseAugmentation,
  enrichPostWithCse,
  mergeCseContextIntoViralResearch
};
