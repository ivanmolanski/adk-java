'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  TrendingUp, 
  Sparkles, 
  Eye, 
  FileText, 
  Zap, 
  BarChart3, 
  RefreshCw, 
  CheckCircle2, 
  AlertCircle,
  ArrowRight,
  Activity,
  Users,
  Target
} from 'lucide-react';

interface SystemMetrics {
  backend: string;
  postsAnalyzed: number;
  contentDrafts: number;
  viralityScore: number;
  avgEngagement: number;
}

export default function HomePage() {
  const [metrics, setMetrics] = useState<SystemMetrics>({
    backend: 'offline',
    postsAnalyzed: 0,
    contentDrafts: 0,
    viralityScore: 0,
    avgEngagement: 0
  });
  const [currentTime, setCurrentTime] = useState('');

  useEffect(() => {
    // Update time
    const updateTime = () => setCurrentTime(new Date().toLocaleTimeString());
    updateTime();
    const timeInterval = setInterval(updateTime, 1000);

    // Check system status
    const checkSystem = async () => {
      try {
        const response = await fetch('http://localhost:3453/viral-service/api/v1/health', {
          signal: AbortSignal.timeout(2000)
        });
        
        if (response.ok) {
          const metricsRes = await fetch('http://localhost:3453/viral-service/api/v1/metrics', {
            signal: AbortSignal.timeout(2000)
          });
          const metricsData = await metricsRes.json();
          
          setMetrics({
            backend: 'healthy',
            postsAnalyzed: metricsData.counters?.analyses_created || 0,
            contentDrafts: metricsData.counters?.drafts_created || 0,
            viralityScore: 0,
            avgEngagement: 0
          });
        }
      } catch (error) {
        // Backend offline - show demo data so UI is still functional
        setMetrics({
          backend: 'offline',
          postsAnalyzed: 0,
          contentDrafts: 0,
          viralityScore: 0,
          avgEngagement: 0
        });
      }
    };

    checkSystem();
    const statusInterval = setInterval(checkSystem, 30000);

    return () => {
      clearInterval(timeInterval);
      clearInterval(statusInterval);
    };
  }, []);

  const isSystemHealthy = metrics.backend === 'healthy';

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      {/* Header */}
      <header className="border-b border-white/10 bg-black/20 backdrop-blur-xl">
        <div className="container mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-r from-cyan-500 to-blue-500 rounded-lg flex items-center justify-center">
                <Sparkles className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-white">MDAesthetics Viral Forge</h1>
                <p className="text-xs text-gray-400">AI-Powered Content Intelligence</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                {isSystemHealthy ? (
                  <CheckCircle2 className="w-4 h-4 text-green-400" />
                ) : (
                  <AlertCircle className="w-4 h-4 text-red-400" />
                )}
                <span className="text-sm text-gray-300">{currentTime}</span>
              </div>
            </div>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-6 py-12">
        {/* Status Banner */}
        <div className="mb-12">
          {isSystemHealthy ? (
            <div className="bg-gradient-to-r from-green-500/10 to-emerald-500/10 border border-green-500/20 rounded-2xl p-6 backdrop-blur-sm">
              <div className="flex items-center space-x-3">
                <CheckCircle2 className="w-6 h-6 text-green-400 flex-shrink-0" />
                <div>
                  <h3 className="text-lg font-semibold text-white">System Operational</h3>
                  <p className="text-gray-300">All monitoring agents active • Last scan: {currentTime}</p>
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-gradient-to-r from-amber-500/10 to-red-500/10 border border-amber-500/20 rounded-2xl p-6 backdrop-blur-sm">
              <div className="flex items-center space-x-3">
                <AlertCircle className="w-6 h-6 text-amber-400 flex-shrink-0" />
                <div>
                  <h3 className="text-lg font-semibold text-white">Backend Offline</h3>
                  <p className="text-gray-300">Start the backend server on port 3453 to enable live monitoring</p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Metrics Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
          {/* Posts Analyzed */}
          <div className="bg-gradient-to-br from-blue-500/10 to-cyan-500/10 border border-blue-500/20 rounded-2xl p-6 backdrop-blur-sm hover:scale-105 transition-transform duration-300">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-xl flex items-center justify-center">
                <TrendingUp className="w-6 h-6 text-white" />
              </div>
              <span className="px-3 py-1 bg-blue-500/20 text-blue-300 text-xs font-semibold rounded-full">TODAY</span>
            </div>
            <h3 className="text-3xl font-bold text-white mb-1">{metrics.postsAnalyzed}</h3>
            <p className="text-gray-400 text-sm">Posts Analyzed</p>
            <div className="mt-3 flex items-center text-xs text-green-400">
              <ArrowRight className="w-3 h-3 mr-1" />
              <span>Target: 20 daily</span>
            </div>
          </div>

          {/* Virality Score */}
          <div className="bg-gradient-to-br from-purple-500/10 to-pink-500/10 border border-purple-500/20 rounded-2xl p-6 backdrop-blur-sm hover:scale-105 transition-transform duration-300">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-pink-500 rounded-xl flex items-center justify-center">
                <Zap className="w-6 h-6 text-white" />
              </div>
              <span className="px-3 py-1 bg-purple-500/20 text-purple-300 text-xs font-semibold rounded-full">LIVE</span>
            </div>
            <h3 className="text-3xl font-bold text-white mb-1">{metrics.viralityScore}/10</h3>
            <p className="text-gray-400 text-sm">Avg Virality Score</p>
            <div className="mt-3 flex items-center text-xs text-purple-400">
              <Activity className="w-3 h-3 mr-1" />
              <span>High performers: 0</span>
            </div>
          </div>

          {/* Content Drafts */}
          <div className="bg-gradient-to-br from-emerald-500/10 to-teal-500/10 border border-emerald-500/20 rounded-2xl p-6 backdrop-blur-sm hover:scale-105 transition-transform duration-300">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-gradient-to-br from-emerald-500 to-teal-500 rounded-xl flex items-center justify-center">
                <FileText className="w-6 h-6 text-white" />
              </div>
              <span className="px-3 py-1 bg-emerald-500/20 text-emerald-300 text-xs font-semibold rounded-full">READY</span>
            </div>
            <h3 className="text-3xl font-bold text-white mb-1">{metrics.contentDrafts}</h3>
            <p className="text-gray-400 text-sm">Content Drafts</p>
            <div className="mt-3 flex items-center text-xs text-emerald-400">
              <CheckCircle2 className="w-3 h-3 mr-1" />
              <span>Compliance verified</span>
            </div>
          </div>

          {/* Engagement */}
          <div className="bg-gradient-to-br from-amber-500/10 to-orange-500/10 border border-amber-500/20 rounded-2xl p-6 backdrop-blur-sm hover:scale-105 transition-transform duration-300">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-gradient-to-br from-amber-500 to-orange-500 rounded-xl flex items-center justify-center">
                <BarChart3 className="w-6 h-6 text-white" />
              </div>
              <span className="px-3 py-1 bg-amber-500/20 text-amber-300 text-xs font-semibold rounded-full">AVG</span>
            </div>
            <h3 className="text-3xl font-bold text-white mb-1">{metrics.avgEngagement}%</h3>
            <p className="text-gray-400 text-sm">Avg Engagement</p>
            <div className="mt-3 flex items-center text-xs text-amber-400">
              <Target className="w-3 h-3 mr-1" />
              <span>Benchmark: 2.8%</span>
            </div>
          </div>
        </div>

        {/* Action Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          {/* Research Center */}
          <Link href="/research" className="group">
            <div className="relative overflow-hidden bg-gradient-to-br from-blue-600 to-cyan-600 rounded-2xl p-8 hover:scale-105 transition-all duration-300 cursor-pointer">
              <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full -mr-16 -mt-16"></div>
              <div className="relative">
                <div className="w-14 h-14 bg-white/20 rounded-xl flex items-center justify-center mb-4 backdrop-blur-sm">
                  <Eye className="w-7 h-7 text-white" />
                </div>
                <h3 className="text-2xl font-bold text-white mb-2">Research Center</h3>
                <p className="text-blue-100 mb-4">Analyze trending competitor content and viral patterns</p>
                <div className="flex items-center text-white font-semibold group-hover:translate-x-2 transition-transform">
                  <span>Explore trends</span>
                  <ArrowRight className="w-5 h-5 ml-2" />
                </div>
              </div>
            </div>
          </Link>

          {/* Content Studio */}
          <Link href="/content-studio" className="group">
            <div className="relative overflow-hidden bg-gradient-to-br from-purple-600 to-pink-600 rounded-2xl p-8 hover:scale-105 transition-all duration-300 cursor-pointer">
              <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full -mr-16 -mt-16"></div>
              <div className="relative">
                <div className="w-14 h-14 bg-white/20 rounded-xl flex items-center justify-center mb-4 backdrop-blur-sm">
                  <FileText className="w-7 h-7 text-white" />
                </div>
                <h3 className="text-2xl font-bold text-white mb-2">Content Studio</h3>
                <p className="text-purple-100 mb-4">Generate AI-powered content ready for social media</p>
                <div className="flex items-center text-white font-semibold group-hover:translate-x-2 transition-transform">
                  <span>Create content</span>
                  <ArrowRight className="w-5 h-5 ml-2" />
                </div>
              </div>
            </div>
          </Link>

          {/* Command Center */}
          <Link href="/command-center" className="group">
            <div className="relative overflow-hidden bg-gradient-to-br from-emerald-600 to-teal-600 rounded-2xl p-8 hover:scale-105 transition-all duration-300 cursor-pointer">
              <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full -mr-16 -mt-16"></div>
              <div className="relative">
                <div className="w-14 h-14 bg-white/20 rounded-xl flex items-center justify-center mb-4 backdrop-blur-sm">
                  <Sparkles className="w-7 h-7 text-white" />
                </div>
                <h3 className="text-2xl font-bold text-white mb-2">Command Center</h3>
                <p className="text-emerald-100 mb-4">Chat with AI to control and automate the system</p>
                <div className="flex items-center text-white font-semibold group-hover:translate-x-2 transition-transform">
                  <span>Open AI chat</span>
                  <ArrowRight className="w-5 h-5 ml-2" />
                </div>
              </div>
            </div>
          </Link>
        </div>

        {/* Content Sections */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Top Viral Content */}
          <div className="bg-gradient-to-br from-white/5 to-white/10 border border-white/10 rounded-2xl p-8 backdrop-blur-sm">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center space-x-3">
                <TrendingUp className="w-6 h-6 text-cyan-400" />
                <h2 className="text-2xl font-bold text-white">Top Viral Content</h2>
              </div>
              <button className="px-4 py-2 bg-white/10 hover:bg-white/20 rounded-lg text-white text-sm font-semibold transition-colors flex items-center space-x-2">
                <RefreshCw className="w-4 h-4" />
                <span>Refresh</span>
              </button>
            </div>
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-gradient-to-br from-gray-700 to-gray-800 rounded-full flex items-center justify-center mx-auto mb-4">
                <Eye className="w-8 h-8 text-gray-400" />
              </div>
              <h3 className="text-lg font-semibold text-white mb-2">No trends detected</h3>
              <p className="text-gray-400 text-sm mb-6">Start scanning to discover viral content from competitors</p>
              <button className="px-6 py-3 bg-gradient-to-r from-cyan-500 to-blue-500 hover:from-cyan-600 hover:to-blue-600 rounded-xl text-white font-semibold transition-all">
                Scan for Viral Content
              </button>
            </div>
          </div>

          {/* Ready to Post */}
          <div className="bg-gradient-to-br from-white/5 to-white/10 border border-white/10 rounded-2xl p-8 backdrop-blur-sm">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center space-x-3">
                <CheckCircle2 className="w-6 h-6 text-emerald-400" />
                <h2 className="text-2xl font-bold text-white">Ready to Post</h2>
              </div>
            </div>
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-gradient-to-br from-gray-700 to-gray-800 rounded-full flex items-center justify-center mx-auto mb-4">
                <FileText className="w-8 h-8 text-gray-400" />
              </div>
              <h3 className="text-lg font-semibold text-white mb-2">No content ready</h3>
              <p className="text-gray-400 text-sm mb-6">Generate content from trending analysis</p>
              <button className="px-6 py-3 bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600 rounded-xl text-white font-semibold transition-all">
                Generate Content
              </button>
            </div>
          </div>
        </div>

        {/* Footer */}
        <footer className="mt-12 pt-8 border-t border-white/10 text-center">
          <p className="text-gray-400 text-sm mb-2">MD Aesthetics Viral Intelligence System v2.0.0</p>
          <p className="text-gray-500 text-xs">Powered by FastAPI, Next.js, and GitHub Models GPT-4o</p>
        </footer>
      </main>
    </div>
  );
}
