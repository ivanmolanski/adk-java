import fetch from 'node-fetch';
import { Firestore } from '@google-cloud/firestore';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const serviceAccountPath = path.resolve(__dirname, '../../service-account.json');
const firestore = new Firestore({
  projectId: 'contentforge-ai-ygy25',
  keyFilename: serviceAccountPath
});
const GOOGLE_CSE_KEY = process.env.GOOGLE_CSE_KEY || 'AIzaSyDU52kV6fb8_Wv7TtJbCq1UHuJseVNf6Ug';
const GOOGLE_CSE_CX = process.env.GOOGLE_CSE_CX || '9563294497ffb46ac'; // Set to your CSE CX

const searchTerms = [
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

async function searchGoogleCSE(query) {
  const url = `https://www.googleapis.com/customsearch/v1?key=${GOOGLE_CSE_KEY}&cx=${GOOGLE_CSE_CX}&q=${encodeURIComponent(query)}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error(`CSE search failed: ${res.status}`);
  const data = await res.json();
  return (data.items || []).map(item => ({
    platform: 'google-cse',
    title: item.title ?? null,
    link: item.link ?? null,
    snippet: item.snippet ?? null,
    displayLink: item.displayLink ?? null,
    image: item.pagemap?.cse_image?.[0]?.src ?? null,
    date: item.pagemap?.metatags?.[0]?.['article:published_time'] ?? null,
    scrapedAt: new Date()
  }));
}

async function main() {
  for (let term of searchTerms) {
    try {
      const results = await searchGoogleCSE(term);
      for (let result of results) {
        await firestore.collection('csePosts').add(result);
      }
      console.log(`CSE results for '${term}' saved.`);
    } catch (e) {
      console.error(`Error for term '${term}':`, e);
    }
  }
}

main().catch(console.error);
