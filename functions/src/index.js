"use strict";
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g = Object.create((typeof Iterator === "function" ? Iterator : Object).prototype);
    return g.next = verb(0), g["throw"] = verb(1), g["return"] = verb(2), typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.writeDemoOutput = exports.onResearchCreate = exports.viralScraper = exports.scheduleDaily = exports.orchestrateDailyRun = void 0;
var app_1 = require("firebase-admin/app");
var firestore_1 = require("firebase-admin/firestore");
var apify_client_1 = require("apify-client");
var https_1 = require("firebase-functions/v2/https");
var pubsub_1 = require("firebase-functions/v2/pubsub");
var firestore_2 = require("firebase-functions/v2/firestore");
var scheduler_1 = require("firebase-functions/v2/scheduler");
var pubsub_2 = require("@google-cloud/pubsub");
var appAdmin = (0, app_1.initializeApp)();
var db = (0, firestore_1.getFirestore)(appAdmin);
var pubsub = new pubsub_2.PubSub();

// Apify client initialization - will use environment variable
var apifyClient = new apify_client_1.ApifyApi({
    token: process.env.APIFY_TOKEN || 'apify_api_0iYhpmB1eJZ51b1cbwA8v3117hcRmT4AAIy2'
});

// Config
var START_TOPIC = 'start-daily-run';
var ANALYZE_TOPIC = 'analyze-new-post';

// MD Aesthetics competitor profiles to monitor
var COMPETITOR_PROFILES = [
    '_thelookaesthetics',
    'subtle.enhancements', 
    'skinvitalityofficial'
];

// Helper: compute Engagement Velocity Score
function computeEVS(likes, comments, hours) {
    var safeHours = Math.max(1, hours);
    return (likes + comments) / safeHours;
}

// Helper: extract hashtags from caption text
function extractHashtags(caption) {
    if (!caption) return [];
    var hashtags = caption.match(/#[a-zA-Z0-9_]+/g) || [];
    return hashtags.map(function(tag) { return tag.toLowerCase(); });
}

// Helper: calculate hours since post was published
function hoursAgo(timestamp) {
    if (!timestamp) return 1;
    var now = Date.now();
    var postTime = new Date(timestamp).getTime();
    return Math.max(1, Math.round((now - postTime) / 3600000));
}
// HTTP callable to trigger daily run manually (allow unauthenticated for demo)
exports.orchestrateDailyRun = (0, https_1.onCall)({
    cors: true,
    region: 'us-central1'
}, function (_req) { return __awaiter(void 0, void 0, void 0, function () {
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, pubsub.topic(START_TOPIC).publishMessage({ json: { ts: Date.now(), source: 'manual' } })];
            case 1:
                _a.sent();
                return [2 /*return*/, { ok: true }];
        }
    });
}); });
// Scheduler to kick off at 08:00 EST daily
exports.scheduleDaily = (0, scheduler_1.onSchedule)({
    schedule: '0 8 * * *',
    timeZone: 'America/Toronto',
    region: 'us-central1'
}, function (event) { return __awaiter(void 0, void 0, void 0, function () {
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, pubsub.topic(START_TOPIC).publishMessage({ json: { ts: Date.now(), source: 'scheduler' } })];
            case 1:
                _a.sent();
                return [2 /*return*/];
        }
    });
}); });
// Pub/Sub -> scrape using Apify Instagram Scraper
exports.viralScraper = (0, pubsub_1.onMessagePublished)({
    topic: START_TOPIC,
    region: 'us-central1',
    timeoutSeconds: 300,
    memory: '1GiB',
    concurrency: 1
}, function (event) { return __awaiter(void 0, void 0, void 0, function () {
    var startedAt, results, _i, COMPETITOR_PROFILES_1, profile, runResult, dataset, items, _a, items_1, item, postId, hours, evs, competitorPost, e_1, dateKey, synthetic, batch, _b, results_1, r, ref;
    return __generator(this, function (_c) {
        switch (_c.label) {
            case 0:
                startedAt = Date.now();
                results = [];
                _i = 0, COMPETITOR_PROFILES_1 = COMPETITOR_PROFILES;
                _c.label = 1;
            case 1:
                if (!(_i < COMPETITOR_PROFILES_1.length)) return [3 /*break*/, 8];
                profile = COMPETITOR_PROFILES_1[_i];
                _c.label = 2;
            case 2:
                _c.trys.push([2, 6, , 7]);
                console.log("Scraping Instagram profile: ".concat(profile));
                return [4 /*yield*/, apifyClient.actor('apify/instagram-scraper').call({
                        directUrls: ["https://www.instagram.com/" + profile + "/"],
                        resultsType: 'posts',
                        resultsLimit: 20,
                        // Get posts from last 7 days for freshness
                        addParentData: false
                    })];
            case 3:
                runResult = _c.sent();
                return [4 /*yield*/, apifyClient.dataset(runResult.defaultDatasetId).listItems()];
            case 4:
                dataset = _c.sent();
                items = dataset.items;
                // Process each Instagram post
                for (_a = 0, items_1 = items; _a < items_1.length; _a++) {
                    item = items_1[_a];
                    if (item.type !== 'Post' || !item.url)
                        continue;
                    postId = Buffer.from(item.url).toString('base64').replace(/=+$/, '');
                    hours = hoursAgo(item.timestamp);
                    evs = computeEVS(item.likesCount || 0, item.commentsCount || 0, hours);
                    competitorPost = {
                        id: postId,
                        platform: 'instagram',
                        profile: profile,
                        postUrl: item.url,
                        caption: item.caption || '',
                        hashtags: extractHashtags(item.caption || ''),
                        likes: item.likesCount || 0,
                        comments: item.commentsCount || 0,
                        shares: 0,
                        views: item.videoViewCount || 0,
                        engagementRate: item.likesCount > 0 ? ((item.likesCount + item.commentsCount) / (item.likesCount * 10)) : 0,
                        evs: evs,
                        scrapedAt: new Date().toISOString(),
                        // Additional Apify metadata
                        mediaType: item.type,
                        displayUrl: item.displayUrl,
                        ownerFullName: item.ownerFullName,
                        ownerUsername: item.ownerUsername,
                        timestamp: item.timestamp
                    };
                    results.push(competitorPost);
                }
                return [4 /*yield*/, new Promise(function (resolve) { return setTimeout(resolve, 2000); })];
            case 5:
                // Rate limiting between profiles
                _c.sent();
                return [3 /*break*/, 7];
            case 6:
                e_1 = _c.sent();
                console.error("Apify scraping failed for profile ".concat(profile, ":"), e_1);
                return [3 /*break*/, 7];
            case 7:
                _i++;
                return [3 /*break*/, 1];
            case 8:
                dateKey = new Date().toISOString().slice(0, 10);
                if (!(results.length === 0)) return [3 /*break*/, 10];
                console.log("No posts scraped, creating synthetic data for testing");
                synthetic = {
                    id: 'synthetic-' + Date.now(),
                    platform: 'instagram',
                    profile: '_thelookaesthetics',
                    postUrl: 'https://www.instagram.com/p/synthetic-skintyte/',
                    caption: 'Experience the transformative power of SkinTyte - non-invasive skin firming technology #SkinTyte #MedicalAesthetics #SkinFirming #Toronto',
                    hashtags: ['#skintyte', '#medicalaesthetics', '#skinfirming', '#toronto'],
                    likes: Math.floor(Math.random() * 200) + 50,
                    comments: Math.floor(Math.random() * 30) + 3,
                    shares: 0,
                    views: 0,
                    engagementRate: 0.05,
                    evs: computeEVS(150, 20, 1),
                    scrapedAt: new Date().toISOString(),
                    mediaType: 'Post',
                    displayUrl: null,
                    ownerFullName: 'The Look Aesthetics',
                    ownerUsername: '_thelookaesthetics',
                    timestamp: new Date(Date.now() - 3600000).toISOString()
                };
                return [4 /*yield*/, db.doc("/viral_research/".concat(dateKey, "/").concat(synthetic.id))
                        .set(__assign(__assign({}, synthetic), { createdAt: firestore_1.FieldValue.serverTimestamp(), synthetic: true }))];
            case 9:
                _c.sent();
                return [3 /*break*/, 12];
            case 10:
                console.log("Successfully scraped ".concat(results.length, " Instagram posts"));
                batch = db.batch();
                for (_b = 0, results_1 = results; _b < results_1.length; _b++) {
                    r = results_1[_b];
                    ref = db.doc("/viral_research/".concat(dateKey, "/").concat(r.id));
                    batch.set(ref, __assign(__assign({}, r), { createdAt: firestore_1.FieldValue.serverTimestamp() }));
                }
                return [4 /*yield*/, batch.commit()];
            case 11:
                _c.sent();
                _c.label = 12;
            case 12: return [2 /*return*/];
        }
    });
}); });
// Firestore -> fanout to analysis topic
exports.onResearchCreate = (0, firestore_2.onDocumentCreated)({
    document: '/viral_research/{date}/{postId}',
    region: 'us-central1'
}, function (event) { return __awaiter(void 0, void 0, void 0, function () {
    var path;
    var _a;
    return __generator(this, function (_b) {
        switch (_b.label) {
            case 0:
                path = (_a = event.data) === null || _a === void 0 ? void 0 : _a.ref.path;
                return [4 /*yield*/, pubsub.topic(ANALYZE_TOPIC).publishMessage({ json: { path: path, ts: Date.now() } })];
            case 1:
                _b.sent();
                return [2 /*return*/];
        }
    });
}); });
// Fan-in: write demo output for Hosting to display
exports.writeDemoOutput = (0, pubsub_1.onMessagePublished)({
    topic: ANALYZE_TOPIC,
    region: 'us-central1'
}, function (event) { return __awaiter(void 0, void 0, void 0, function () {
    var path, doc, data, outRef;
    var _a, _b;
    return __generator(this, function (_c) {
        switch (_c.label) {
            case 0:
                path = (((_b = (_a = event.data) === null || _a === void 0 ? void 0 : _a.message) === null || _b === void 0 ? void 0 : _b.json) || {}).path;
                if (!path)
                    return [2 /*return*/];
                return [4 /*yield*/, db.doc(path).get()];
            case 1:
                doc = _c.sent();
                data = doc.data() || {};
                outRef = db.collection('viral_outputs').doc();
                return [4 /*yield*/, outRef.set({
                        sourcePath: path,
                        preview: {
                            hook: "Clinical, non-surgical firming \u2014 ".concat(data.tag || 'trend', " focus"),
                            cta: 'Book a physician-led consultation to see if you are a candidate.',
                            point: 'SkinTyte uses infrared light to contract collagen and promote neocollagenesis.'
                        },
                        createdAt: firestore_1.FieldValue.serverTimestamp()
                    })];
            case 2:
                _c.sent();
                return [2 /*return*/];
        }
    });
}); });
