export interface SocialMediaPost {
  id: string;
  platform: string;
  profile: string;
  postUrl: string;
  caption: string;
  hashtags: string[];
  engagementRate: number;
  likes: number;
  comments: number;
  shares: number;
  views: number;
  timestamp: string;
  analysis?: {
    category: string;
    hook: string;
    cta: string;
    educationalPoint: string;
  };
  generatedContent?: string;
  processedAt?: string;
}

export interface FirebaseConfig {
  apiKey: string;
  authDomain: string;
  projectId: string;
  storageBucket: string;
  messagingSenderId: string;
  appId: string;
}

// Viral Intelligence Types (matching Java backend models)
export interface CompetitorPost {
  id?: string;
  platform: string;
  profile: string;
  postUrl: string;
  caption: string;
  hashtags: string[];
  likes?: number;
  comments?: number;
  shares?: number;
  views?: number;
  engagementRate?: number;
  scrapedAt: string;
}

export interface TrendAnalysis {
  id?: string;
  competitorPostId: string;
  category: string; // "Process Demystified", "Science Explained", "Transformation", "Myth Busting"
  hook: string;
  callToAction: string;
  educationalPoint: string;
  extractedHashtags: string[];
  viralityScore: number;
  relevanceScore: number;
  rawAnalysis: string;
  analyzedAt: string;
}

export interface ContentDraft {
  id: string;
  trendAnalysisId: string;
  platform: string; // "instagram" | "tiktok"
  caption: string;
  hashtags: string[];
  suggestedMediaType: string; // "video" | "image" | "carousel"
  complianceChecked: boolean;
  createdAt: string;
}

export interface DailyBrief {
  id?: string;
  date: string;
  topTrends: TrendAnalysis[];
  generatedDrafts: ContentDraft[];
  summary: string;
  recommendations: string[];
  sentAt: string;
}

export interface ValidationResult {
  isValid: boolean;
  hasCallToAction: boolean;
  hashtagCount: number;
  hasForbiddenWords: boolean;
  hasServiceKeyword: boolean;
  issues: string[];
}

// API Response types
export interface ApiResponse<T> {
  status: 'success' | 'error';
  timestamp: string;
  data?: T;
  error?: string;
}

export interface TrendsResponse extends ApiResponse<TrendAnalysis[]> {
  trends: TrendAnalysis[];
}

export interface DraftsResponse extends ApiResponse<ContentDraft[]> {
  drafts: ContentDraft[];
}

export interface BriefResponse extends ApiResponse<DailyBrief> {
  brief: DailyBrief | null;
}