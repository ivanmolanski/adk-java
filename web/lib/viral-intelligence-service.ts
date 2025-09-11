import axios, { AxiosResponse } from 'axios';
import { 
  TrendAnalysis, 
  ContentDraft, 
  DailyBrief,
  TrendsResponse,
  DraftsResponse,
  BriefResponse 
} from '../types';

const VIRAL_SERVICE_BASE_URL = process.env.NEXT_PUBLIC_VIRAL_SERVICE_URL || 'http://localhost:3453';

class ViralIntelligenceService {
  private baseURL: string;

  constructor() {
    this.baseURL = VIRAL_SERVICE_BASE_URL;
    console.log('ViralIntelligenceService initialized with baseURL:', this.baseURL);
  }

  /**
   * Get trending posts analysis
   */
  async getTrends(
    category?: string, 
    limit: number = 10,
    minViralityScore?: number,
    minRelevanceScore?: number
  ): Promise<TrendAnalysis[]> {
    try {
      const params = new URLSearchParams();
      if (category) params.append('category', category);
      params.append('limit', limit.toString());
      if (minViralityScore) params.append('minViralityScore', minViralityScore.toString());
      if (minRelevanceScore) params.append('minRelevanceScore', minRelevanceScore.toString());

      const response: AxiosResponse<TrendsResponse> = await axios.get(
        `${this.baseURL}/viral-service/api/v1/viral/trends?${params.toString()}`
      );

      return response.data.trends;
    } catch (error) {
      console.error('Error fetching trends:', error);
      return [];
    }
  }

  /**
   * Get content drafts
   */
  async getDrafts(
    category?: string,
    limit: number = 5,
    platform?: string
  ): Promise<ContentDraft[]> {
    try {
      const params = new URLSearchParams();
      if (category) params.append('category', category);
      params.append('limit', limit.toString());
      if (platform) params.append('platform', platform);

      const response: AxiosResponse<DraftsResponse> = await axios.get(
        `${this.baseURL}/viral-service/api/v1/viral/drafts?${params.toString()}`
      );

      return response.data.drafts;
    } catch (error) {
      console.error('Error fetching drafts:', error);
      return [];
    }
  }

  /**
   * Get daily brief
   */
  async getDailyBrief(date?: string): Promise<DailyBrief | null> {
    try {
      const params = new URLSearchParams();
      if (date) params.append('date', date);

      const response: AxiosResponse<BriefResponse> = await axios.get(
        `${this.baseURL}/viral-service/api/v1/viral/brief?${params.toString()}`
      );

      return response.data.brief;
    } catch (error) {
      console.error('Error fetching daily brief:', error);
      return null;
    }
  }

  /**
   * Trigger scraping manually
   */
  async triggerScraping(): Promise<boolean> {
    try {
      const response = await axios.post(`${this.baseURL}/viral-service/api/v1/viral/scrape`);
      return response.status === 200;
    } catch (error) {
      console.error('Error triggering scraping:', error);
      return false;
    }
  }

  /**
   * Get scraping status
   */
  async getScrapingStatus(): Promise<any> {
    try {
      const response = await axios.get(`${this.baseURL}/viral-service/api/scraping/status`);
      return response.data;
    } catch (error) {
      console.error('Error getting scraping status:', error);
      return { status: 'unknown' };
    }
  }

  /**
   * Execute a natural language command via chat
   */
  async executeCommand(command: string): Promise<any> {
    try {
      const response = await axios.post(`${this.baseURL}/viral-service/api/v1/viral/command`, {
        prompt: command
      });
      return response.data;
    } catch (error) {
      console.error('Error executing command:', error);
      return { error: 'Failed to execute command' };
    }
  }

  /**
   * Check service health
   */
  async healthCheck(): Promise<boolean> {
    try {
      console.log('Performing health check to:', `${this.baseURL}/viral-service/api/v1/health`);
      const response = await axios.get(`${this.baseURL}/viral-service/api/v1/health`);
      console.log('Health check response:', response.data);
      return response.data.status === 'healthy';
    } catch (error) {
      console.error('Error checking service health:', error);
      if (axios.isAxiosError(error)) {
        console.error('Axios error details:', {
          message: error.message,
          code: error.code,
          response: error.response?.status,
          responseData: error.response?.data
        });
      }
      return false;
    }
  }
}

// Export singleton instance
export const viralIntelligenceService = new ViralIntelligenceService();
export default ViralIntelligenceService;