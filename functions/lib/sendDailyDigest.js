"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.generateDailyDigest = generateDailyDigest;
const firestore_1 = require("firebase-admin/firestore");
const app_1 = require("firebase-admin/app");
const v2_1 = require("firebase-functions/v2");
const aiFlows_1 = require("./aiFlows");
// Initialize Firebase Admin if not already initialized
if (!(0, app_1.getApps)().length) {
    (0, app_1.initializeApp)();
}
const db = (0, firestore_1.getFirestore)();
async function generateDailyDigest(topN = 8) {
    const today = new Date();
    const dateKey = today.toISOString().split('T')[0];
    // Fetch top posts (last 36h) sorted by engagementRate
    const since = new Date(Date.now() - 36 * 3600 * 1000).toISOString();
    const snap = await db.collection('viral_research')
        .where('scrapedAt', '>=', since)
        .limit(300)
        .get();
    const posts = [];
    snap.forEach(doc => {
        const d = doc.data();
        posts.push({
            id: doc.id,
            platform: d.platform,
            profile: d.profile,
            engagementRate: d.engagementRate || 0,
            caption: d.caption,
            hashtags: d.hashtags
        });
    });
    posts.sort((a, b) => (b.engagementRate || 0) - (a.engagementRate || 0));
    const selected = posts.slice(0, topN);
    for (const p of selected) {
        try {
            p.analysis = await (0, aiFlows_1.analyzeTrend)({
                postId: p.id,
                platform: p.platform,
                profile: p.profile,
                caption: p.caption,
                hashtags: p.hashtags,
                engagementRate: p.engagementRate,
                timestamp: new Date().toISOString()
            });
            // Choose focus service heuristically
            const focus = /tyte|skintyte/i.test(p.caption || '') ? 'SkinTyte' : (/radiesse|biostim/i.test(p.caption || '') ? 'Radiesse' : 'Duo-C-Lift');
            p.draft = await (0, aiFlows_1.createContent)(p.analysis, focus);
        }
        catch (e) {
            v2_1.logger.error('Digest post AI processing failed', { id: p.id, error: e.message });
        }
    }
    // Compose HTML
    const htmlParts = [
        `<h1>MdaViral Daily Brief – ${dateKey}</h1>`,
        `<p>Top ${selected.length} posts by engagement (last 36h). Automated analysis & draft captions included.</p>`
    ];
    selected.forEach(p => {
        htmlParts.push(`<div style="margin-bottom:24px;">` +
            `<h3>${p.platform} / ${p.profile} (ER: ${(p.engagementRate || 0).toFixed(2)}%)</h3>` +
            `<pre style="white-space:pre-wrap;font-size:12px;">${(p.caption || '').replace(/</g, '&lt;')}</pre>` +
            `<details><summary>Draft Caption</summary><pre style="white-space:pre-wrap;font-size:12px;">${(p.draft?.caption || '').replace(/</g, '&lt;')}</pre></details>` +
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
    v2_1.logger.info('Daily digest generated', { postCount: selected.length, recipients });
    return { postCount: selected.length, recipients };
}
exports.default = { generateDailyDigest };
//# sourceMappingURL=sendDailyDigest.js.map