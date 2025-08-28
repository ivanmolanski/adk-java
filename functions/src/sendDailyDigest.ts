import { getFirestore } from 'firebase-admin/firestore';
import { initializeApp, getApps } from 'firebase-admin/app';
import { logger } from 'firebase-functions';
import { analyzeTrend, createContent } from './aiFlows';

// Initialize Firebase Admin if not already initialized
if (!getApps().length) {
  initializeApp();
}

const db = getFirestore();

interface DigestPostEntry {
	id: string;
	platform: string;
	profile: string;
	engagementRate?: number;
	caption?: string;
	hashtags?: string[];
	analysis?: any;
	draft?: any;
}

export async function generateDailyDigest(topN = 8) {
	const today = new Date();
	const dateKey = today.toISOString().split('T')[0];
	// Fetch top posts (last 36h) sorted by engagementRate
	const since = new Date(Date.now() - 36 * 3600 * 1000).toISOString();
	const snap = await db.collection('viral_research')
		.where('scrapedAt', '>=', since)
		.limit(300)
		.get();

	const posts: DigestPostEntry[] = [];
	snap.forEach(doc => {
		const d:any = doc.data();
		posts.push({
			id: doc.id,
			platform: d.platform,
			profile: d.profile,
			engagementRate: d.engagementRate || 0,
			caption: d.caption,
			hashtags: d.hashtags
		});
	});

	posts.sort((a,b)=> (b.engagementRate||0) - (a.engagementRate||0));
	const selected = posts.slice(0, topN);

	for (const p of selected) {
		try {
			p.analysis = await analyzeTrend({
				postId: p.id,
				platform: p.platform,
				profile: p.profile,
				caption: p.caption,
				hashtags: p.hashtags,
				engagementRate: p.engagementRate,
				timestamp: new Date().toISOString()
			});
			// Choose focus service heuristically
			const focus = /tyte|skintyte/i.test(p.caption||'') ? 'SkinTyte' : (/radiesse|biostim/i.test(p.caption||'') ? 'Radiesse' : 'Duo-C-Lift');
			p.draft = await createContent(p.analysis, focus);
		} catch (e:any) {
			logger.error('Digest post AI processing failed', { id: p.id, error: e.message });
		}
	}

	// Compose HTML
	const htmlParts = [
		`<h1>MdaViral Daily Brief – ${dateKey}</h1>`,
		`<p>Top ${selected.length} posts by engagement (last 36h). Automated analysis & draft captions included.</p>`
	];
	selected.forEach(p => {
		htmlParts.push(`<div style="margin-bottom:24px;">`+
			`<h3>${p.platform} / ${p.profile} (ER: ${(p.engagementRate||0).toFixed(2)}%)</h3>`+
			`<pre style="white-space:pre-wrap;font-size:12px;">${(p.caption||'').replace(/</g,'&lt;')}</pre>`+
			`<details><summary>Draft Caption</summary><pre style="white-space:pre-wrap;font-size:12px;">${(p.draft?.caption||'').replace(/</g,'&lt;')}</pre></details>`+
			`</div>`);
	});
	const html = htmlParts.join('\n');

	const recipients = (process.env.DIGEST_RECIPIENTS || '').split(/[,;\s]+/).filter(Boolean);
	const doc = {
		to: recipients,
		message: {
			subject: `MdaViral Daily Brief – ${dateKey}`,
			html
		},
		metadata: {
			type: 'daily_brief',
			generatedAt: new Date().toISOString(),
			postCount: selected.length
		}
	};

	// Write to mail collection for Email Extension
	await db.collection('mail').add(doc);
	// Persist digest record
	await db.collection('daily_briefs').doc(dateKey).set({ ...doc.metadata, html, recipients });
	logger.info('Daily digest generated', { postCount: selected.length, recipients });
	return { postCount: selected.length, recipients };
}

export default { generateDailyDigest };