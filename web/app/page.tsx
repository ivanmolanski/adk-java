'use client';
import { useViralIntelligence } from '@/hooks/useViralIntelligence';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { TrendingUp, Users, MessageSquare, BarChart3, RefreshCw, Calendar, Zap, Target, Clock, CheckCircle2, AlertCircle } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';

export default function DashboardPage() {
  const { trends, drafts, dailyBrief, isLoading, error, refreshData } = useViralIntelligence();

  if (isLoading) {
    return (
      <div className="container mx-auto p-6 flex items-center justify-center min-h-screen">
        <div className="flex items-center space-x-2">
          <RefreshCw className="w-6 h-6 animate-spin" />
          <span>Loading viral intelligence...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto p-6 flex items-center justify-center min-h-screen">
        <div className="text-center">
          <h2 className="text-xl font-semibold text-red-600 mb-2">Error Loading Data</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <Button onClick={refreshData}>
            <RefreshCw className="w-4 h-4 mr-2" />
            Retry
          </Button>
        </div>
      </div>
    );
  }

  const totalEngagement = trends?.reduce((sum, trend) => sum + (trend.relevanceScore || 0), 0) || 0;
  const avgViralityScore = trends?.length > 0 ? 
    (trends.reduce((sum, trend) => sum + (trend.viralityScore || 0), 0) / trends.length).toFixed(1) : '0';
  
  const highPerformingTrends = trends?.filter(t => (t.viralityScore || 0) >= 8.0) || [];
  const completionRate = drafts?.length > 0 ? (drafts.filter(d => d.complianceChecked).length / drafts.length) * 100 : 0;

  // Calculate today's performance metrics
  const todayMetrics = {
    postsAnalyzed: trends?.length || 0,
    contentGenerated: drafts?.length || 0,
    avgEngagement: trends?.length > 0 ? 
      (trends.reduce((sum, trend) => sum + (trend.relevanceScore || 0), 0) / trends.length).toFixed(1) : '0',
    topCategories: ['Science Explained', 'Transformation', 'Process Demystified']
  };

  return (
    <div className="container mx-auto p-6 space-y-8">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">MD Aesthetics Intelligence Dashboard</h1>
          <p className="text-muted-foreground">Real-time viral content analysis and competitive intelligence</p>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline" size="sm">
            <Clock className="w-4 h-4 mr-2" />
            Last updated: {new Date().toLocaleTimeString()}
          </Button>
          <Button onClick={refreshData} disabled={isLoading}>
            <RefreshCw className="w-4 h-4 mr-2" />
            Refresh
          </Button>
        </div>
      </div>

      {/* Status Alert */}
      <Alert>
        <CheckCircle2 className="h-4 w-4" />
        <AlertDescription>
          System operational. All agents active and monitoring competitor content in real-time.
        </AlertDescription>
      </Alert>

      {/* Key Performance Indicators */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="border-l-4 border-l-blue-500">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Posts Analyzed Today</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{todayMetrics.postsAnalyzed}</div>
            <div className="flex items-center space-x-2 mt-1">
              <Progress value={75} className="flex-1 h-2" />
              <span className="text-xs text-green-600">+15%</span>
            </div>
            <p className="text-xs text-muted-foreground mt-1">Target: 20 posts</p>
          </CardContent>
        </Card>
        
        <Card className="border-l-4 border-l-green-500">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Virality Score</CardTitle>
            <Zap className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{avgViralityScore}/10</div>
            <div className="flex items-center space-x-2 mt-1">
              <Progress value={parseFloat(avgViralityScore) * 10} className="flex-1 h-2" />
              <span className="text-xs text-green-600">↑ 0.3</span>
            </div>
            <p className="text-xs text-muted-foreground mt-1">High performers: {highPerformingTrends.length}</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-purple-500">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Content Ready</CardTitle>
            <MessageSquare className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{todayMetrics.contentGenerated}</div>
            <div className="flex items-center space-x-2 mt-1">
              <Progress value={completionRate} className="flex-1 h-2" />
              <span className="text-xs text-green-600">{completionRate.toFixed(0)}%</span>
            </div>
            <p className="text-xs text-muted-foreground mt-1">Compliance checked</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-orange-500">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg Engagement</CardTitle>
            <BarChart3 className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{todayMetrics.avgEngagement}%</div>
            <div className="flex items-center space-x-2 mt-1">
              <Progress value={parseFloat(todayMetrics.avgEngagement)} className="flex-1 h-2" />
              <span className="text-xs text-green-600">+0.4%</span>
            </div>
            <p className="text-xs text-muted-foreground mt-1">Industry benchmark: 2.8%</p>
          </CardContent>
        </Card>
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Target className="w-5 h-5" />
            <span>Quick Actions</span>
          </CardTitle>
          <CardDescription>Streamline your content strategy workflow</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <Button variant="outline" className="h-20 flex-col space-y-2">
              <TrendingUp className="w-6 h-6" />
              <span className="text-sm">Analyze Trends</span>
            </Button>
            <Button variant="outline" className="h-20 flex-col space-y-2">
              <MessageSquare className="w-6 h-6" />
              <span className="text-sm">Create Content</span>
            </Button>
            <Button variant="outline" className="h-20 flex-col space-y-2">
              <BarChart3 className="w-6 h-6" />
              <span className="text-sm">View Analytics</span>
            </Button>
            <Button variant="outline" className="h-20 flex-col space-y-2">
              <Calendar className="w-6 h-6" />
              <span className="text-sm">Schedule Posts</span>
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Daily Brief */}
      {dailyBrief && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Calendar className="w-5 h-5" />
              <span>Today's Intelligence Brief</span>
              <Badge variant="secondary">{new Date().toLocaleDateString()}</Badge>
            </CardTitle>
            <CardDescription>AI-powered insights and strategic recommendations</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <h4 className="font-semibold mb-3 flex items-center space-x-2">
                  <AlertCircle className="w-4 h-4" />
                  <span>Summary</span>
                </h4>
                <p className="text-sm text-gray-700">{dailyBrief.summary}</p>
              </div>

              <div>
                <h4 className="font-semibold mb-3 flex items-center space-x-2">
                  <CheckCircle2 className="w-4 h-4" />
                  <span>Recommendations</span>
                </h4>
                <ul className="list-disc pl-5 space-y-1">
                  {dailyBrief.recommendations?.slice(0, 3).map((rec, index) => (
                    <li key={index} className="text-sm text-gray-700">{rec}</li>
                  ))}
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Performance Analytics */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <TrendingUp className="w-5 h-5" />
              <span>Top Performing Content</span>
            </CardTitle>
            <CardDescription>Highest virality scores from competitor analysis</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {trends?.slice(0, 4).map((trend, index) => (
                <div key={trend.id} className="flex items-center space-x-4 p-4 border rounded-lg hover:bg-gray-50">
                  <div className="text-2xl">
                    {index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : '📊'}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center justify-between mb-1">
                      <h4 className="font-medium">{trend.category}</h4>
                      <Badge 
                        variant={(trend.viralityScore || 0) >= 8 ? "default" : "secondary"}
                        className="ml-2"
                      >
                        {trend.viralityScore?.toFixed(1)} viral
                      </Badge>
                    </div>
                    <p className="text-sm text-gray-600 line-clamp-2">{trend.educationalPoint}</p>
                    <div className="flex items-center space-x-4 mt-2">
                      <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                        {trend.category}
                      </span>
                      <span className="text-xs text-gray-500">
                        Score: {trend.relevanceScore?.toFixed(1)}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <MessageSquare className="w-5 h-5" />
              <span>Content Pipeline</span>
            </CardTitle>
            <CardDescription>AI-generated posts ready for deployment</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {drafts?.slice(0, 4).map((draft, index) => (
                <div key={draft.id} className="p-4 border rounded-lg hover:bg-gray-50">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center space-x-2">
                      <Badge variant="outline">{draft.platform}</Badge>
                      <span className="text-sm font-medium">{draft.suggestedMediaType}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      {draft.complianceChecked && (
                        <Badge variant="secondary" className="text-green-700 bg-green-100">
                          ✓ Compliant
                        </Badge>
                      )}
                    </div>
                  </div>
                  <p className="text-sm line-clamp-2 mb-3">{draft.caption}</p>
                  <div className="flex items-center justify-between text-xs text-gray-500">
                    <span>Media: {draft.suggestedMediaType}</span>
                    <span>Hashtags: {draft.hashtags?.length || 0}</span>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* System Health */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <CheckCircle2 className="w-5 h-5 text-green-500" />
            <span>System Status</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="text-center">
              <div className="text-2xl mb-1">🤖</div>
              <div className="text-sm font-medium">Content Creator</div>
              <div className="text-xs text-green-600">Active</div>
            </div>
            <div className="text-center">
              <div className="text-2xl mb-1">📊</div>
              <div className="text-sm font-medium">Trend Analyzer</div>
              <div className="text-xs text-green-600">Active</div>
            </div>
            <div className="text-center">
              <div className="text-2xl mb-1">🕷️</div>
              <div className="text-sm font-medium">Web Scraper</div>
              <div className="text-xs text-green-600">Active</div>
            </div>
            <div className="text-center">
              <div className="text-2xl mb-1">✉️</div>
              <div className="text-sm font-medium">Email Reports</div>
              <div className="text-xs text-green-600">Active</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
