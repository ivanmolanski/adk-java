// AgentOrchestrator.js
// Orchestrates the workflow: TargetDiscoveryAgent -> PuppeteerScraperAgent -> FreshnessFilterAgent -> MetadataEnrichmentAgent -> TrendAnalyzerAgent -> ContentCreatorAgent
import { discoverTargets } from './TargetDiscoveryAgent.js';
import { scrapeTargets } from './PuppeteerScraperAgent.js';
import { filterFreshPosts } from './FreshnessFilterAgent.js';
import { enrichMetadata } from './MetadataEnrichmentAgent.js';
import { runCseAugmentation, enrichPostWithCse, mergeCseContextIntoViralResearch } from './googleCSEAgent.js';
// import { analyzeTrends } from './TrendAnalyzerAgent.js';
// import { createContent } from './ContentCreatorAgent.js';

const discoveryQueries = [
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

/**
 * Full orchestration pipeline:
 *  1. Target discovery
 *  2. Scraping
 *  3. Freshness filter (placeholder)
 *  4. Google CSE augmentation (global, independent of individual posts)
 *  5. Post-level metadata enrichment (CSE contextual linking)
 *  6. (Future) Trend analysis -> Content creation -> QA
 */
export async function runOrchestration(options = {}) {
  const started = Date.now();
  const result = { started: new Date().toISOString(), targetsDiscovered: 0, cse: {}, enrichedPosts: 0, platformCounts: {} };
  try {
    // 1. Discover new targets (last 24h)
    const targets = await discoverTargets(discoveryQueries);
    result.targetsDiscovered = targets.length;

    // 2. Scrape with Puppeteer (assumes scraper stores posts in Firestore)
    await scrapeTargets(targets);

    // 3. TODO: Retrieve scraped posts (placeholder; real implementation would query Firestore)
    const scrapedPosts = options.posts || [];
    // Platform distribution (instagram, tiktok, others)
    scrapedPosts.forEach(p => {
      const key = (p.platform || 'unknown').toLowerCase();
      result.platformCounts[key] = (result.platformCounts[key] || 0) + 1;
    });
    // const freshPosts = filterFreshPosts(scrapedPosts);
    const freshPosts = scrapedPosts; // placeholder

    // 4. Run global CSE augmentation (multi-term) once per orchestration
    result.cse = await runCseAugmentation();

    // 5. Enrich each post with contextual CSE snippets (in-memory)
    const enriched = [];
    for (const p of freshPosts) {
      try {
        const withCse = await enrichPostWithCse(p);
        const meta = await enrichMetadata(withCse); // existing enrichment
        enriched.push(meta);
        // If original document id is provided, persist CSE context back
        if (meta && meta.id && meta.cseContext) {
          await mergeCseContextIntoViralResearch(meta.id, meta.cseContext);
        }
      } catch (e) {
        // Non-fatal: continue
      }
    }
    result.enrichedPosts = enriched.length;

    result.durationMs = Date.now() - started;
    result.status = 'ok';
    return result;
  } catch (e) {
    result.status = 'error';
    result.error = e.message;
    result.durationMs = Date.now() - started;
    return result;
  }
}
