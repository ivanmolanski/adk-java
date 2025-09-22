import { useState, useEffect } from 'react';
import { TrendAnalysis, ContentDraft, DailyBrief } from '../types';

export function useViralIntelligence() {
  const [trends, setTrends] = useState<TrendAnalysis[]>([]);
  const [drafts, setDrafts] = useState<ContentDraft[]>([]);
  const [dailyBrief, setDailyBrief] = useState<DailyBrief | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      setError(null);

      // Fetch trends from Python FastAPI backend
      const trendsResponse = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/viral/trends`);
      if (trendsResponse.ok) {
        const trendsData = await trendsResponse.json();
        // Map 'cta' to 'callToAction' for compatibility
        const mappedTrends = (trendsData.trends || []).map((t: any) => ({
          ...t,
          callToAction: t.callToAction ?? t.cta,
        }));
        setTrends(mappedTrends);
      }

      // Fetch drafts from Python FastAPI backend
      const draftsResponse = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/viral/drafts`);
      if (draftsResponse.ok) {
        const draftsData = await draftsResponse.json();
        setDrafts(draftsData.drafts || []);
      }

      // Fetch daily brief from Python FastAPI backend
      const briefResponse = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/viral/daily-brief`);
      if (briefResponse.ok) {
        const briefData = await briefResponse.json();
        setDailyBrief(briefData.brief);
      }

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch data');
    } finally {
      setIsLoading(false);
    }
  };

  const refreshData = () => {
    fetchData();
  };

  useEffect(() => {
    fetchData();
  }, []);

  return {
    trends,
    drafts,
    dailyBrief,
    isLoading,
    error,
    refreshData
  };
}