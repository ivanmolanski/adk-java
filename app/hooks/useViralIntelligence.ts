'use client';
import { useState, useEffect } from 'react';

// Updated interfaces to match Python backend schemas
interface CompetitorPost {
  id: number;
  platform: string;
  profile_url: string;
  post_url: string;
  caption: string;
  hashtags: string[];
  likes: number;
  comments: number;
  engagement_rate: number;
  relevance_score: number;
  virality_score: number;
  content_category: string;
  hook_analysis: string;
  cta_analysis: string;
}

interface GeneratedContent {
  id: number;
  platform: string;
  caption: string;
  hashtags: string[];
  suggested_media_type: string;
  compliance_checked: boolean;
  brand_voice_score: number;
  created_at: string;
  approved: boolean;
}

interface TrendAnalysis {
  analysis: {
    hook: string;
    cta: string;
    content_category: string;
    thematic_keywords: string[];
    educational_value: number;
    brand_alignment: number;
  };
  relevance_score: number;
  virality_score: number;
  recommendations: string[];
}

interface ContentCreation {
  generated_content: GeneratedContent;
  alternative_versions: GeneratedContent[];
  brand_voice_score: number;
}

// Base URL for the new Python backend
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:3453/api/v1';

export function useViralIntelligence() {
  const [competitorPosts, setCompetitorPosts] = useState<CompetitorPost[]>([]);
  const [generatedContent, setGeneratedContent] = useState<GeneratedContent[]>([]);
  const [insights, setInsights] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      setError(null);

      // Fetch competitor posts
      const postsResponse = await fetch(`${API_BASE_URL}/viral/posts?limit=50`);
      if (postsResponse.ok) {
        const postsData = await postsResponse.json();
        setCompetitorPosts(postsData || []);
      }

      // Fetch generated content
      const contentResponse = await fetch(`${API_BASE_URL}/viral/content?limit=20`);
      if (contentResponse.ok) {
        const contentData = await contentResponse.json();
        setGeneratedContent(contentData || []);
      }

      // Fetch insights
      const insightsResponse = await fetch(`${API_BASE_URL}/viral/insights`);
      if (insightsResponse.ok) {
        const insightsData = await insightsResponse.json();
        setInsights(insightsData);
      }

    } catch (err) {
      console.error('Error fetching viral intelligence data:', err);
      setError(err instanceof Error ? err.message : 'Failed to fetch data');
    } finally {
      setIsLoading(false);
    }
  };

  // Analyze trends for a specific post
  const analyzeTrends = async (postData: any): Promise<TrendAnalysis | null> => {
    try {
      const response = await fetch(`${API_BASE_URL}/viral/analyze`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(postData)
      });

      if (response.ok) {
        const result = await response.json();
        return result.data;
      }
      return null;
    } catch (err) {
      console.error('Error analyzing trends:', err);
      return null;
    }
  };

  // Generate content based on trend analysis
  const generateContent = async (analysisData: any, targetServices: string[] = ['Duo-C-Lift'], tone: string = 'educational'): Promise<ContentCreation | null> => {
    try {
      const response = await fetch(`${API_BASE_URL}/viral/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          trend_analysis: analysisData,
          target_services: targetServices,
          tone: tone
        })
      });

      if (response.ok) {
        const result = await response.json();
        return result.data;
      }
      return null;
    } catch (err) {
      console.error('Error generating content:', err);
      return null;
    }
  };

  // Run complete pipeline: analyze and generate
  const analyzeAndGenerate = async (postData: any, targetServices: string[] = ['Duo-C-Lift', 'SkinTyte'], tone: string = 'educational') => {
    try {
      setIsLoading(true);
      const response = await fetch(`${API_BASE_URL}/viral/analyze-and-generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          post_data: postData,
          target_services: targetServices,
          tone: tone
        })
      });

      if (response.ok) {
        const result = await response.json();
        // Refresh data to show new generated content
        await fetchData();
        return result;
      }
      return null;
    } catch (err) {
      console.error('Error in analyze and generate pipeline:', err);
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  // Approve generated content
  const approveContent = async (contentId: number) => {
    try {
      const response = await fetch(`${API_BASE_URL}/viral/content/${contentId}/approve`, {
        method: 'PUT',
      });

      if (response.ok) {
        // Refresh data to update approval status
        await fetchData();
        return true;
      }
      return false;
    } catch (err) {
      console.error('Error approving content:', err);
      return false;
    }
  };

  const refreshData = () => {
    fetchData();
  };

  useEffect(() => {
    fetchData();
  }, []);

  return {
    // Data
    competitorPosts,
    generatedContent,
    insights,
    isLoading,
    error,
    
    // Legacy compatibility (map to new data structure)
    trends: competitorPosts.map(post => ({
      id: post.id.toString(),
      category: post.content_category || 'General',
      viralityScore: post.virality_score || 0,
      relevanceScore: post.relevance_score || 0,
      educationalPoint: post.caption?.substring(0, 100) + '...' || '',
      hook: post.hook_analysis || '',
      cta: post.cta_analysis || ''
    })),
    drafts: generatedContent.map(content => ({
      id: content.id.toString(),
      platform: content.platform,
      caption: content.caption,
      hashtags: content.hashtags,
      suggestedMediaType: content.suggested_media_type,
      complianceChecked: content.compliance_checked
    })),
    dailyBrief: insights ? {
      summary: `Analyzed ${insights.statistics?.total_posts_analyzed || 0} posts, generated ${insights.statistics?.total_content_generated || 0} pieces of content`,
      recommendations: insights.trends?.top_categories || []
    } : null,
    
    // Actions
    refreshData,
    analyzeTrends,
    generateContent,
    analyzeAndGenerate,
    approveContent
  };
}