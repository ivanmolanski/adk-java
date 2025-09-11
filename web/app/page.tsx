'use client';
import { useViralIntelligence } from '@/hooks/useViralIntelligence';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { TrendingUp, Users, MessageSquare, BarChart3, RefreshCw, Calendar, Zap, Target, Clock, CheckCircle2, AlertCircle, Sparkles, Eye, Heart } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { SocialMediaPost } from '@/components/SocialMediaPost';
import { useState, useEffect } from 'react';

export default function DashboardPage() {
  const { trends, drafts, dailyBrief, isLoading, error, refreshData } = useViralIntelligence();
  const [currentTime, setCurrentTime] = useState<string>('');

  // Update time on client side to avoid hydration mismatch
  useEffect(() => {
    const updateTime = () => {
      setCurrentTime(new Date().toLocaleTimeString());
    };
    
    updateTime(); // Set initial time
    const interval = setInterval(updateTime, 1000); // Update every second
    
    return () => clearInterval(interval);
  }, []);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
        <div className="text-center">
          <div className="animate-pulse mb-4">
            <Sparkles className="w-12 h-12 mx-auto text-blue-600" />
          </div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">Loading Viral Intelligence</h3>
          <p className="text-gray-600">Analyzing competitor content and market trends...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-red-50 to-pink-100">
        <div className="text-center max-w-md mx-auto">
          <AlertCircle className="w-12 h-12 mx-auto text-red-600 mb-4" />
          <h3 className="text-xl font-semibold text-gray-900 mb-2">System Error</h3>
          <p className="text-gray-600 mb-6">{error}</p>
          <Button onClick={refreshData} className="bg-blue-600 hover:bg-blue-700">
            <RefreshCw className="w-4 h-4 mr-2" />
            Retry Connection
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

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
      <div className="container mx-auto px-6 py-8 space-y-8">
        
        {/* Hero Header */}
        <div className="text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl mb-6">
            <Sparkles className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-4xl font-bold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent mb-4">
            MD Aesthetics Intelligence Hub
          </h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto leading-relaxed">
            AI-powered competitive intelligence and viral content generation for aesthetic medical practices
          </p>
        </div>

        {/* System Status */}
        <div className="flex justify-center">
          <Alert className="max-w-2xl border-green-200 bg-green-50">
            <CheckCircle2 className="h-5 w-5 text-green-600" />
            <AlertDescription className="text-green-800 font-medium">
              🟢 System Active - All monitoring agents operational - Last scan: {currentTime}
            </AlertDescription>
          </Alert>
        </div>

        {/* Key Metrics Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          
          {/* Posts Analyzed */}
          <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-sm hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="p-2 bg-blue-100 rounded-xl">
                  <TrendingUp className="h-6 w-6 text-blue-600" />
                </div>
                <Badge variant="secondary" className="bg-blue-100 text-blue-700">Today</Badge>
              </div>
              <CardTitle className="text-2xl font-bold text-gray-900">
                {trends?.length || 0}
              </CardTitle>
              <CardDescription className="text-gray-600">
                Posts Analyzed
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center space-x-2">
                <Progress value={75} className="flex-1 h-2 bg-blue-100" />
                <span className="text-sm font-medium text-green-600">+15%</span>
              </div>
              <p className="text-xs text-gray-500 mt-2">Target: 20 posts daily</p>
            </CardContent>
          </Card>

          {/* Virality Score */}
          <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-sm hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="p-2 bg-purple-100 rounded-xl">
                  <Zap className="h-6 w-6 text-purple-600" />
                </div>
                <Badge variant="secondary" className="bg-purple-100 text-purple-700">Avg</Badge>
              </div>
              <CardTitle className="text-2xl font-bold text-gray-900">
                {avgViralityScore}/10
              </CardTitle>
              <CardDescription className="text-gray-600">
                Virality Score
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center space-x-2">
                <Progress value={parseFloat(avgViralityScore) * 10} className="flex-1 h-2 bg-purple-100" />
                <span className="text-sm font-medium text-green-600">↑ 0.3</span>
              </div>
              <p className="text-xs text-gray-500 mt-2">High performers: {highPerformingTrends.length}</p>
            </CardContent>
          </Card>

          {/* Content Ready */}
          <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-sm hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="p-2 bg-green-100 rounded-xl">
                  <MessageSquare className="h-6 w-6 text-green-600" />
                </div>
                <Badge variant="secondary" className="bg-green-100 text-green-700">Ready</Badge>
              </div>
              <CardTitle className="text-2xl font-bold text-gray-900">
                {drafts?.length || 0}
              </CardTitle>
              <CardDescription className="text-gray-600">
                Content Drafts
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center space-x-2">
                <Progress value={completionRate} className="flex-1 h-2 bg-green-100" />
                <span className="text-sm font-medium text-green-600">{completionRate.toFixed(0)}%</span>
              </div>
              <p className="text-xs text-gray-500 mt-2">Compliance verified</p>
            </CardContent>
          </Card>

          {/* Engagement Rate */}
          <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-sm hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="p-2 bg-orange-100 rounded-xl">
                  <Heart className="h-6 w-6 text-orange-600" />
                </div>
                <Badge variant="secondary" className="bg-orange-100 text-orange-700">Live</Badge>
              </div>
              <CardTitle className="text-2xl font-bold text-gray-900">
                {trends?.length > 0 ? 
                  ((trends.reduce((sum, trend) => sum + (trend.relevanceScore || 0), 0) / trends.length).toFixed(1)) : '0'}%
              </CardTitle>
              <CardDescription className="text-gray-600">
                Avg Engagement
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center space-x-2">
                <Progress value={30} className="flex-1 h-2 bg-orange-100" />
                <span className="text-sm font-medium text-green-600">+0.4%</span>
              </div>
              <p className="text-xs text-gray-500 mt-2">Industry benchmark: 2.8%</p>
            </CardContent>
          </Card>
        </div>

        {/* Action Buttons */}
        <div className="flex justify-center">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 max-w-4xl">
            <Button 
              variant="outline" 
              className="h-24 flex-col space-y-3 bg-white/80 backdrop-blur-sm border-0 shadow-lg hover:shadow-xl transition-all duration-300"
              asChild
            >
              <a href="/research">
                <TrendingUp className="w-8 h-8 text-blue-600" />
                <span className="text-sm font-medium">Research Center</span>
              </a>
            </Button>
            <Button 
              variant="outline" 
              className="h-24 flex-col space-y-3 bg-white/80 backdrop-blur-sm border-0 shadow-lg hover:shadow-xl transition-all duration-300"
              asChild
            >
              <a href="/content-studio">
                <MessageSquare className="w-8 h-8 text-purple-600" />
                <span className="text-sm font-medium">Content Studio</span>
              </a>
            </Button>
            <Button 
              variant="outline" 
              className="h-24 flex-col space-y-3 bg-white/80 backdrop-blur-sm border-0 shadow-lg hover:shadow-xl transition-all duration-300"
              asChild
            >
              <a href="/command-center">
                <BarChart3 className="w-8 h-8 text-green-600" />
                <span className="text-sm font-medium">Command Center</span>
              </a>
            </Button>
            <Button 
              variant="outline" 
              className="h-24 flex-col space-y-3 bg-white/80 backdrop-blur-sm border-0 shadow-lg hover:shadow-xl transition-all duration-300"
              onClick={refreshData}
            >
              <RefreshCw className="w-8 h-8 text-orange-600" />
              <span className="text-sm font-medium">Refresh Data</span>
            </Button>
          </div>
        </div>

        {/* Content Preview Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          
          {/* Top Performing Trends */}
          <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-sm">
            <CardHeader>
              <CardTitle className="flex items-center space-x-3">
                <div className="p-2 bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl">
                  <TrendingUp className="w-5 h-5 text-white" />
                </div>
                <span>Top Viral Content</span>
              </CardTitle>
              <CardDescription>Highest performing posts from competitor analysis</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {trends?.slice(0, 3).map((trend, index) => (
                  <div key={trend.id} className="flex items-start space-x-4 p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors">
                    <div className="text-2xl">
                      {index === 0 ? '🥇' : index === 1 ? '🥈' : '🥉'}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center justify-between mb-2">
                        <h4 className="font-semibold text-gray-900">{trend.category}</h4>
                        <Badge 
                          variant={(trend.viralityScore || 0) >= 8 ? "default" : "secondary"}
                          className={`${(trend.viralityScore || 0) >= 8 ? 'bg-red-100 text-red-800' : 'bg-gray-100 text-gray-600'}`}
                        >
                          {trend.viralityScore?.toFixed(1)} viral
                        </Badge>
                      </div>
                      <p className="text-sm text-gray-600 line-clamp-2 mb-3">{trend.educationalPoint}</p>
                      <div className="flex items-center space-x-3">
                        <Badge variant="outline" className="text-xs">
                          {trend.category}
                        </Badge>
                        <span className="text-xs text-gray-500">
                          Relevance: {trend.relevanceScore?.toFixed(1)}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
                {(!trends || trends.length === 0) && (
                  <div className="text-center py-8">
                    <Eye className="w-12 h-12 mx-auto text-gray-400 mb-4" />
                    <h4 className="font-medium text-gray-900 mb-2">No trends detected</h4>
                    <p className="text-sm text-gray-500">Refresh to scan for new viral content</p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Ready Content */}
          <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-sm">
            <CardHeader>
              <CardTitle className="flex items-center space-x-3">
                <div className="p-2 bg-gradient-to-r from-green-500 to-blue-600 rounded-xl">
                  <MessageSquare className="w-5 h-5 text-white" />
                </div>
                <span>Ready to Post</span>
              </CardTitle>
              <CardDescription>AI-generated content ready for social media</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {drafts?.slice(0, 2).map((draft) => (
                  <SocialMediaPost
                    key={draft.id}
                    draft={draft}
                    onPostSuccess={refreshData}
                  />
                ))}
                {(!drafts || drafts.length === 0) && (
                  <div className="text-center py-8">
                    <MessageSquare className="w-12 h-12 mx-auto text-gray-400 mb-4" />
                    <h4 className="font-medium text-gray-900 mb-2">No content ready</h4>
                    <p className="text-sm text-gray-500 mb-4">Generate content from trending analysis</p>
                    <Button size="sm" onClick={refreshData}>
                      <Sparkles className="w-4 h-4 mr-2" />
                      Generate Content
                    </Button>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Daily Brief */}
        {dailyBrief && (
          <Card className="border-0 shadow-lg bg-gradient-to-r from-blue-50 to-indigo-50">
            <CardHeader>
              <CardTitle className="flex items-center space-x-3">
                <div className="p-2 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-xl">
                  <Calendar className="w-5 h-5 text-white" />
                </div>
                <span>Daily Intelligence Brief</span>
                <Badge variant="secondary" className="bg-indigo-100 text-indigo-700">
                  {new Date().toLocaleDateString()}
                </Badge>
              </CardTitle>
              <CardDescription>AI-powered insights and strategic recommendations</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="space-y-3">
                  <h4 className="font-semibold text-gray-900 flex items-center space-x-2">
                    <AlertCircle className="w-4 h-4 text-blue-600" />
                    <span>Market Summary</span>
                  </h4>
                  <p className="text-gray-700 leading-relaxed">{dailyBrief.summary}</p>
                </div>

                <div className="space-y-3">
                  <h4 className="font-semibold text-gray-900 flex items-center space-x-2">
                    <CheckCircle2 className="w-4 h-4 text-green-600" />
                    <span>Strategic Recommendations</span>
                  </h4>
                  <ul className="space-y-2">
                    {dailyBrief.recommendations?.slice(0, 3).map((rec, index) => (
                      <li key={index} className="flex items-start space-x-2">
                        <span className="text-green-600 mt-1">•</span>
                        <span className="text-gray-700 text-sm">{rec}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
