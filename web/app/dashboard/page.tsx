'use client';
import { useState } from 'react';
import { useViralIntelligence } from '@/hooks/useViralIntelligence';
// import types if needed, but use local Trend/Draft from hook
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { RefreshCw, TrendingUp, FileText, AlertCircle, ExternalLink } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';

export default function Dashboard() {
  const {
    trends,
    drafts,
    dailyBrief,
    isLoading,
    error,
    refreshData
  } = useViralIntelligence();

  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isScraping, setIsScraping] = useState(false);

  const handleRefresh = async () => {
    setIsRefreshing(true);
    await refreshData();
    setIsRefreshing(false);
  };

  const handleTriggerScraping = async () => {
    // TODO: Implement scraping trigger logic
    setIsScraping(true);
    setTimeout(() => {
      setIsScraping(false);
      console.log('Scraping triggered (placeholder)');
    }, 1500);
  };

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'Process Demystified': return 'bg-blue-100 text-blue-800';
      case 'Science Explained': return 'bg-green-100 text-green-800';
      case 'Transformation': return 'bg-purple-100 text-purple-800';
      case 'Myth Busting': return 'bg-orange-100 text-orange-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getPlatformIcon = (platform: string) => {
    return platform === 'instagram' ? '📸' : '🎵';
  };

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Viral Intelligence Dashboard</h1>
          <p className="text-muted-foreground">MD Aesthetics Content Strategy Center</p>
        </div>
        <div className="flex space-x-2">
          <Button 
            variant="outline" 
            onClick={handleRefresh} 
            disabled={isRefreshing}
          >
            <RefreshCw className={`w-4 h-4 mr-2 ${isRefreshing ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          <Button 
            onClick={handleTriggerScraping} 
            disabled={isScraping}
          >
            <TrendingUp className="w-4 h-4 mr-2" />
            {isScraping ? 'Scraping...' : 'Start Scraping'}
          </Button>
        </div>
      </div>

      {/* Service Status */}
      {/* Service is always online for now. TODO: Add health check logic. */}

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Metrics Overview */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Trends</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{trends.length}</div>
            <p className="text-xs text-muted-foreground">
              +{trends.filter(t => t.viralityScore > 0.7).length} high-viral
            </p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Ready Drafts</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{drafts.length}</div>
            <p className="text-xs text-muted-foreground">
              {drafts.filter(d => d.complianceChecked).length} compliance-checked
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg. Virality</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {trends.length > 0 ? 
                (trends.reduce((acc, t) => acc + t.viralityScore, 0) / trends.length).toFixed(1) : 
                '0.0'
              }
            </div>
            <p className="text-xs text-muted-foreground">out of 1.0</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Relevance Score</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {trends.length > 0 ? 
                (trends.reduce((acc, t) => acc + t.relevanceScore, 0) / trends.length).toFixed(1) : 
                '0.0'
              }
            </div>
            <p className="text-xs text-muted-foreground">to MD Aesthetics</p>
          </CardContent>
        </Card>
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Trending Analysis */}
        <Card>
          <CardHeader>
            <CardTitle>Top Viral Trends</CardTitle>
            <CardDescription>
              Latest competitor analysis sorted by virality score
            </CardDescription>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-3">
                {[...Array(3)].map((_, i) => (
                  <div key={i} className="animate-pulse">
                    <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                    <div className="h-3 bg-gray-100 rounded w-1/2"></div>
                  </div>
                ))}
              </div>
            ) : trends.length === 0 ? (
              <p className="text-muted-foreground text-center py-8">
                No trends available. Try triggering a scraping session.
              </p>
            ) : (
              <div className="space-y-4">
                {trends.slice(0, 5).map((trend) => (
                  <div key={trend.id} className="border rounded-lg p-4">
                    <div className="flex items-start justify-between mb-2">
                      <Badge className={getCategoryColor(trend.category)}>
                        {trend.category}
                      </Badge>
                      <div className="flex space-x-2 text-xs text-muted-foreground">
                        <span>V: {trend.viralityScore.toFixed(1)}</span>
                        <span>R: {trend.relevanceScore.toFixed(1)}</span>
                      </div>
                    </div>
                    <h4 className="font-medium mb-1">{trend.hook}</h4>
                    <p className="text-sm text-muted-foreground mb-2">
                      {trend.educationalPoint}
                    </p>
                    <div className="flex flex-wrap gap-1">
                      <Badge variant="outline" className="text-xs">{trend.cta}</Badge>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Content Drafts */}
        <Card>
          <CardHeader>
            <CardTitle>Generated Content</CardTitle>
            <CardDescription>
              Ready-to-post content for MD Aesthetics
            </CardDescription>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-3">
                {[...Array(2)].map((_, i) => (
                  <div key={i} className="animate-pulse">
                    <div className="h-4 bg-gray-200 rounded w-full mb-2"></div>
                    <div className="h-3 bg-gray-100 rounded w-2/3"></div>
                  </div>
                ))}
              </div>
            ) : drafts.length === 0 ? (
              <p className="text-muted-foreground text-center py-8">
                No drafts available. Content will appear after trend analysis.
              </p>
            ) : (
              <div className="space-y-4">
                {drafts.slice(0, 3).map((draft) => (
                  <div key={draft.id} className="border rounded-lg p-4">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center space-x-2">
                        <span>{getPlatformIcon(draft.platform)}</span>
                        <Badge variant={draft.complianceChecked ? "default" : "secondary"}>
                          {draft.platform}
                        </Badge>
                        {draft.complianceChecked && (
                          <Badge variant="outline" className="text-green-600">
                            ✓ Compliant
                          </Badge>
                        )}
                      </div>
                      <Badge variant="outline">{draft.suggestedMediaType}</Badge>
                    </div>
                    <p className="text-sm mb-3 line-clamp-3">{draft.caption}</p>
                    <div className="flex flex-wrap gap-1">
                      {draft.hashtags.slice(0, 5).map((tag, i) => (
                        <Badge key={i} variant="outline" className="text-xs">
                          {tag}
                        </Badge>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Daily Brief */}
      {dailyBrief && (
        <Card>
          <CardHeader>
            <CardTitle>Daily Brief</CardTitle>
            <CardDescription>
              {dailyBrief && dailyBrief.recommendations && dailyBrief.recommendations.length > 0 ? 'Recommendations available' : 'No recommendations'}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p className="mb-4">{dailyBrief.summary}</p>
            {dailyBrief.recommendations.length > 0 && (
              <div>
                <h4 className="font-medium mb-2">Recommendations:</h4>
                <ul className="space-y-1">
                  {dailyBrief.recommendations.map((rec, i) => (
                    <li key={i} className="text-sm text-muted-foreground">
                      • {rec}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}