import { z } from 'zod';
export declare const TrendInputSchema: z.ZodObject<{
    post: z.ZodObject<{
        platform: z.ZodString;
        profile: z.ZodString;
        caption: z.ZodOptional<z.ZodString>;
        hashtags: z.ZodOptional<z.ZodArray<z.ZodString, "many">>;
        likes: z.ZodOptional<z.ZodNumber>;
        comments: z.ZodOptional<z.ZodNumber>;
        shares: z.ZodOptional<z.ZodNumber>;
        views: z.ZodOptional<z.ZodNumber>;
        engagementRate: z.ZodOptional<z.ZodNumber>;
    }, "strip", z.ZodTypeAny, {
        platform: string;
        profile: string;
        caption?: string | undefined;
        hashtags?: string[] | undefined;
        likes?: number | undefined;
        comments?: number | undefined;
        shares?: number | undefined;
        views?: number | undefined;
        engagementRate?: number | undefined;
    }, {
        platform: string;
        profile: string;
        caption?: string | undefined;
        hashtags?: string[] | undefined;
        likes?: number | undefined;
        comments?: number | undefined;
        shares?: number | undefined;
        views?: number | undefined;
        engagementRate?: number | undefined;
    }>;
}, "strip", z.ZodTypeAny, {
    post: {
        platform: string;
        profile: string;
        caption?: string | undefined;
        hashtags?: string[] | undefined;
        likes?: number | undefined;
        comments?: number | undefined;
        shares?: number | undefined;
        views?: number | undefined;
        engagementRate?: number | undefined;
    };
}, {
    post: {
        platform: string;
        profile: string;
        caption?: string | undefined;
        hashtags?: string[] | undefined;
        likes?: number | undefined;
        comments?: number | undefined;
        shares?: number | undefined;
        views?: number | undefined;
        engagementRate?: number | undefined;
    };
}>;
export declare const TrendOutputSchema: z.ZodObject<{
    category: z.ZodOptional<z.ZodEnum<["Process Demystified", "Science Explained", "Transformation", "Myth Busting"]>>;
    hook: z.ZodOptional<z.ZodString>;
    cta: z.ZodOptional<z.ZodString>;
    educationalPoint: z.ZodOptional<z.ZodString>;
    summary: z.ZodOptional<z.ZodString>;
}, "strip", z.ZodTypeAny, {
    category?: "Process Demystified" | "Science Explained" | "Transformation" | "Myth Busting" | undefined;
    hook?: string | undefined;
    cta?: string | undefined;
    educationalPoint?: string | undefined;
    summary?: string | undefined;
}, {
    category?: "Process Demystified" | "Science Explained" | "Transformation" | "Myth Busting" | undefined;
    hook?: string | undefined;
    cta?: string | undefined;
    educationalPoint?: string | undefined;
    summary?: string | undefined;
}>;
export declare function analyzeTrend(post: any): Promise<any>;
export declare function createContent(analysis: any, focusService: string): Promise<any>;
export declare function classifyIntent(message: string, apiKey?: string): Promise<any>;
export declare function summarizeConversation(messages: Array<{
    role: string;
    content: string;
}>, apiKey?: string): Promise<any>;
export declare function storeChatSession(sessionId: string, data: any): Promise<void>;
export declare function loadChatSession(sessionId: string): Promise<FirebaseFirestore.DocumentData | null | undefined>;
//# sourceMappingURL=aiFlows.d.ts.map