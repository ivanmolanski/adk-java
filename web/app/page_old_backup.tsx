'use client';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { TrendingUp, BarChart3, RefreshCw, Sparkles, Eye, FileText, Zap, Clock, CheckCircle2, AlertCircle } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useState, useEffect } from 'react';
import Link from 'next/link';

export default function HomePage() {
  const [currentTime, setCurrentTime] = useState<string>('');
  const [systemStatus, setSystemStatus] = useState<{
    backend: string;
    aiStatus: string;
    trends: number;
    drafts: number;
  }>({
    backend: 'checking...',
    aiStatus: 'checking...',
    trends: 0,
    drafts: 0
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const updateTime = () => {
      setCurrentTime(new Date().toLocaleTimeString());
    };
    updateTime();
    const interval = setInterval(updateTime, 1000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const checkSystem = async () => {
      try {
        // Check backend health
        const healthRes = await fetch('http://localhost:3453/viral-service/api/v1/health');
        const healthData = await healthRes.json();
        
        // Check metrics
        const metricsRes = await fetch('http://localhost:3453/viral-service/api/v1/metrics');
        const metricsData = await metricsRes.json();

        setSystemStatus({
          backend: healthData.status || 'healthy',
          aiStatus: 'configured',
          trends: metricsData.counters?.analyses_created || 0,
          drafts: metricsData.counters?.drafts_created || 0
        });
      } catch (error) {
        setSystemStatus({
          backend: 'offline',
          aiStatus: 'unknown',
          trends: 0,
          drafts: 0
        });
      } finally {
        setIsLoading(false);
      }
    };

    checkSystem();
    const interval = setInterval(checkSystem, 30000);
    return () => clearInterval(interval);
  }, []);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
        <div className="text-center">
          <div className="animate-pulse mb-4">
            <Sparkles className="w-12 h-12 mx-auto text-blue-600" />
          </div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">Connecting to System</h3>
          <p className="text-gray-600">Please wait...</p>
        </div>
      </div>
    );
  }

  const isSystemHealthy = systemStatus.backend === 'healthy';

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
      <div className="container mx-auto px-6 py-8 space-y-8">
        
        {/* Hero Header */}
        <div className="text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl mb-6 shadow-lg">
            <Sparkles className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-5xl font-bold bg-gradient-to-r from-gray-900 via-blue-900 to-indigo-900 bg-clip-text text-transparent mb-4">
            MDAesthetics Viral Forge
          </h1>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto leading-relaxed">
            AI-powered competitive intelligence and viral content generation for aesthetic medical practices
          </p>
        </div>

        {/* System Status */}
        <div className="flex justify-center">
          {isSystemHealthy ? (
            <Alert className="max-w-2xl border-green-200 bg-green-50/80 backdrop-blur-sm shadow-lg">
              <CheckCircle2 className="h-5 w-5 text-green-600" />
              <AlertDescription className="text-green-800 font-medium">
                System Active - All monitoring agents operational - Last scan: {currentTime}
              </AlertDescription>
            </Alert>
          ) : (
            <Alert className="max-w-2xl border-red-200 bg-red-50/80 backdrop-blur-sm shadow-lg">
              <AlertCircle className="h-5 w-5 text-red-600" />
              <AlertDescription className="text-red-800 font-medium">
                System Offline - Backend not responding. Please start the backend server.
              </AlertDescription>
            </Alert>
          )}
        </div>

        {/* Key Metrics Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          
          {/* Posts Analyzed */}
          <Card className="border-0 shadow-xl bg-white/90 backdrop-blur-sm hover:shadow-2xl transition-all duration-300 hover:-translate-y-1">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="p-3 bg-gradient-to-br from-blue-100 to-blue-200 rounded-xl shadow-sm">
                  <TrendingUp className="h-6 w-6 text-blue-600" />
                </div>
                <Badge variant="secondary" className="text-xs bg-blue-100 text-blue-700">
                  Today
                </Badge>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-1">
                <p className="text-3xl font-bold text-gray-900">{systemStatus.trends}</p>
                <p className="text-sm text-gray-600 font-medium">Posts Analyzed</p>
                <p className="text-xs text-gray-500">Target: 20 posts daily</p>
              </div>
            </CardContent>
          </Card>

          {/* Virality Score */}
          <Card className="border-0 shadow-xl bg-white/90 backdrop-blur-sm hover:shadow-2xl transition-all duration-300 hover:-translate-y-1">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="p-3 bg-gradient-to-br from-purple-100 to-purple-200 rounded-xl shadow-sm">
                  <Zap className="h-6 w-6 text-purple-600" />
                </div>
                <Badge variant="secondary" className="text-xs bg-purple-100 text-purple-700">
                  Avg
                </Badge>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-1">
                <p className="text-3xl font-bold text-gray-900">0/10</p>
                <p className="text-sm text-gray-600 font-medium">Virality Score</p>
                <p className="text-xs text-gray-500">High performers: 0</p>
              </div>
            </CardContent>
          </Card>

          {/* Content Drafts */}
          <Card className="border-0 shadow-xl bg-white/90 backdrop-blur-sm hover:shadow-2xl transition-all duration-300 hover:-translate-y-1">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="p-3 bg-gradient-to-br from-emerald-100 to-emerald-200 rounded-xl shadow-sm">
                  <FileText className="h-6 w-6 text-emerald-600" />
                </div>
                <Badge variant="secondary" className="text-xs bg-emerald-100 text-emerald-700">
                  Ready
                </Badge>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-1">
                <p className="text-3xl font-bold text-gray-900">{systemStatus.drafts}</p>
                <p className="text-sm text-gray-600 font-medium">Content Drafts</p>
                <p className="text-xs text-gray-500">Compliance verified</p>
              </div>
            </CardContent>
          </Card>

          {/* Avg Engagement */}
          <Card className="border-0 shadow-xl bg-white/90 backdrop-blur-sm hover:shadow-2xl transition-all duration-300 hover:-translate-y-1">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="p-3 bg-gradient-to-br from-amber-100 to-amber-200 rounded-xl shadow-sm">
                  <BarChart3 className="h-6 w-6 text-amber-600" />
                </div>
                <Badge variant="secondary" className="text-xs bg-amber-100 text-amber-700">
                  Live
                </Badge>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-1">
                <p className="text-3xl font-bold text-gray-900">0%</p>
                <p className="text-sm text-gray-600 font-medium">Avg Engagement</p>
                <p className="text-xs text-gray-500">Industry benchmark: 2.8%</p>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Navigation Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          
          {/* Research Center */}
          <Link href="/research">
            <Card className="border-0 shadow-xl bg-gradient-to-br from-blue-500 to-indigo-600 hover:shadow-2xl transition-all duration-300 hover:scale-105 cursor-pointer group">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="p-3 bg-white/20 rounded-xl backdrop-blur-sm">
                    <Eye className="h-6 w-6 text-white" />
                  </div>
                  <div>
                    <CardTitle className="text-white text-xl">Research Center</CardTitle>
                    <CardDescription className="text-blue-100 mt-1">
                      Highest performing posts from competitor analysis
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <p className="text-white/90 text-sm">View trending content</p>
                  <div className="text-white/60 group-hover:text-white transition-colors">→</div>
                </div>
              </CardContent>
            </Card>
          </Link>

          {/* Content Studio */}
          <Link href="/content-studio">
            <Card className="border-0 shadow-xl bg-gradient-to-br from-purple-500 to-pink-600 hover:shadow-2xl transition-all duration-300 hover:scale-105 cursor-pointer group">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="p-3 bg-white/20 rounded-xl backdrop-blur-sm">
                    <FileText className="h-6 w-6 text-white" />
                  </div>
                  <div>
                    <CardTitle className="text-white text-xl">Content Studio</CardTitle>
                    <CardDescription className="text-purple-100 mt-1">
                      AI-generated content ready for social media
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <p className="text-white/90 text-sm">Generate new content</p>
                  <div className="text-white/60 group-hover:text-white transition-colors">→</div>
                </div>
              </CardContent>
            </Card>
          </Link>

          {/* Command Center */}
          <Link href="/command-center">
            <Card className="border-0 shadow-xl bg-gradient-to-br from-emerald-500 to-teal-600 hover:shadow-2xl transition-all duration-300 hover:scale-105 cursor-pointer group">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="p-3 bg-white/20 rounded-xl backdrop-blur-sm">
                    <Sparkles className="h-6 w-6 text-white" />
                  </div>
                  <div>
                    <CardTitle className="text-white text-xl">Command Center</CardTitle>
                    <CardDescription className="text-emerald-100 mt-1">
                      Chat with AI to control the system
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <p className="text-white/90 text-sm">Open AI chat</p>
                  <div className="text-white/60 group-hover:text-white transition-colors">→</div>
                </div>
              </CardContent>
            </Card>
          </Link>
        </div>

        {/* Top Viral Content Section */}
        <Card className="border-0 shadow-xl bg-white/90 backdrop-blur-sm">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-2xl flex items-center gap-2">
                  <TrendingUp className="h-6 w-6 text-blue-600" />
                  Top Viral Content
                </CardTitle>
                <CardDescription className="mt-2">
                  Highest performing posts from competitor analysis
                </CardDescription>
              </div>
              <Button 
                variant="outline" 
                size="sm"
                className="gap-2 hover:bg-blue-50 hover:text-blue-600 hover:border-blue-300 transition-colors"
              >
                <RefreshCw className="h-4 w-4" />
                Refresh Data
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <div className="p-6 bg-gray-100 rounded-full mb-6">
                <Eye className="h-12 w-12 text-gray-400" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">No trends detected</h3>
              <p className="text-gray-600 mb-6 max-w-md">
                Refresh to scan for new viral content from competitors
              </p>
              <Button 
                className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-lg"
                size="lg"
              >
                <RefreshCw className="h-4 w-4 mr-2" />
                Scan for Viral Content
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Ready to Post Section */}
        <Card className="border-0 shadow-xl bg-white/90 backdrop-blur-sm">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-2xl flex items-center gap-2">
                  <CheckCircle2 className="h-6 w-6 text-emerald-600" />
                  Ready to Post
                </CardTitle>
                <CardDescription className="mt-2">
                  AI-generated content ready for social media
                </CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <div className="p-6 bg-gray-100 rounded-full mb-6">
                <FileText className="h-12 w-12 text-gray-400" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">No content ready</h3>
              <p className="text-gray-600 mb-6 max-w-md">
                Generate content from trending analysis
              </p>
              <Button 
                className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white shadow-lg"
                size="lg"
              >
                <Sparkles className="h-4 w-4 mr-2" />
                Generate Content
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Footer */}
        <div className="text-center py-6 text-gray-500 text-sm border-t border-gray-200">
          <p className="font-medium">MD Aesthetics Viral Intelligence System v2.0.0</p>
          <p className="mt-1">Powered by FastAPI, Next.js, and GitHub Models GPT-4o</p>
        </div>
      </div>
    </div>
  );
}
