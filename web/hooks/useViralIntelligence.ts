import { useState, useEffect, useCallback } from 'react';
import { TrendAnalysis, ContentDraft, DailyBrief } from '../types';
import { viralIntelligenceService } from '../lib/viral-intelligence-service';

export interface UseViralIntelligenceOptions {
  autoRefresh?: boolean;
  refreshInterval?: number; // in milliseconds
}

export interface UseViralIntelligenceReturn {
  trends: TrendAnalysis[];
  drafts: ContentDraft[];
  dailyBrief: DailyBrief | null;
  isLoading: boolean;
  error: string | null;
  refreshData: () => Promise<void>;
  triggerScraping: () => Promise<boolean>;
  executeCommand: (command: string) => Promise<any>;
  isServiceHealthy: boolean;
}

export function useViralIntelligence(
  options: UseViralIntelligenceOptions = {}
): UseViralIntelligenceReturn {
  const { autoRefresh = true, refreshInterval = 30000 } = options;
  
  const [trends, setTrends] = useState<TrendAnalysis[]>([]);
  const [drafts, setDrafts] = useState<ContentDraft[]>([]);
  const [dailyBrief, setDailyBrief] = useState<DailyBrief | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isServiceHealthy, setIsServiceHealthy] = useState(false);

  const refreshData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      // Check service health first
      const healthy = await viralIntelligenceService.healthCheck();
      setIsServiceHealthy(healthy);
      
      if (!healthy) {
        setError('Viral intelligence service is not available');
        return;
      }

      // Fetch all data in parallel
      const [trendsData, draftsData, briefData] = await Promise.all([
        viralIntelligenceService.getTrends('all', 10),
        viralIntelligenceService.getDrafts('all', 5),
        viralIntelligenceService.getDailyBrief()
      ]);

      setTrends(trendsData);
      setDrafts(draftsData);
      setDailyBrief(briefData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch viral intelligence data');
      console.error('Error refreshing viral intelligence data:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const triggerScraping = useCallback(async (): Promise<boolean> => {
    try {
      const success = await viralIntelligenceService.triggerScraping();
      if (success) {
        // Refresh data after a short delay to get new results
        setTimeout(refreshData, 3000);
      }
      return success;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to trigger scraping');
      return false;
    }
  }, [refreshData]);

  const executeCommand = useCallback(async (command: string) => {
    try {
      const result = await viralIntelligenceService.executeCommand(command);
      // Refresh data after command execution
      setTimeout(refreshData, 1000);
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to execute command');
      return { error: 'Command execution failed' };
    }
  }, [refreshData]);

  // Initial data load and auto-refresh setup
  useEffect(() => {
    refreshData();

    if (autoRefresh) {
      const interval = setInterval(refreshData, refreshInterval);
      return () => clearInterval(interval);
    }
  }, [refreshData, autoRefresh, refreshInterval]);

  return {
    trends,
    drafts,
    dailyBrief,
    isLoading,
    error,
    refreshData,
    triggerScraping,
    executeCommand,
    isServiceHealthy
  };
}