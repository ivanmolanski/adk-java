'use client';
import { useState, useEffect } from 'react';
import { useViralIntelligence } from '@/hooks/useViralIntelligence';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Search, Filter, TrendingUp, Eye, Heart, MessageCircle, Share, ExternalLink, Calendar } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';

interface ResearchPost {
  id: string;
  platform: string;
  profile: string;
  postUrl: string;
  caption: string;
  hashtags: string[];
  likes: number;
  comments: number;
  shares: number;
  views: number;
  engagementRate: number;
  scrapedAt: string;
  viralityScore?: number;
  category?: string;
}

export default function ResearchPage() {
  const { trends, isLoading, error, refreshData } = useViralIntelligence();
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [selectedProfile, setSelectedProfile] = useState<string>('all');
  const [searchTerm, setSearchTerm] = useState('');
  
  // Mock research data - in production this would come from your API
  const [researchPosts] = useState<ResearchPost[]>([
    {
      id: '1',
      platform: 'instagram',
      profile: '_thelookaesthetics',
      postUrl: 'https://instagram.com/p/sample1',
      caption: 'Transform your skin with our advanced BBL treatment! ✨ The science behind perfect skin: IPL technology targets melanin and hemoglobin in 7 layers of your skin. Results you can see and feel! Book your consultation today 📞',
      hashtags: ['#bblforever', '#ipllaser', '#skintreatment', '#glowup', '#sciencebasedskincare'],
      likes: 1247,
      comments: 89,
      shares: 23,
      views: 5680,
      engagementRate: 2.4,
      scrapedAt: '2025-01-25T10:30:00Z',
      viralityScore: 8.5,
      category: 'Science Explained'
    },
    {
      id: '2',
      platform: 'instagram',
      profile: 'subtle.enhancements',
      postUrl: 'https://instagram.com/p/sample2',
      caption: 'SkinTyte session walkthrough 🔥 See exactly what happens during your skin tightening treatment. No surprises, just professional results! Our infrared technology works at 40-45°C for optimal collagen stimulation.',
      hashtags: ['#skintyte', '#skinlaxity', '#treatmentwalkthrough', '#beforeandafter', '#professionalskincare'],
      likes: 892,
      comments: 67,
      shares: 15,
      views: 3240,
      engagementRate: 3.1,
      scrapedAt: '2025-01-25T09:15:00Z',
      viralityScore: 7.2,
      category: 'Process Demystified'
    },
    {
      id: '3',
      platform: 'instagram',
      profile: 'skinvitalityofficial',
      postUrl: 'https://instagram.com/p/sample3',
      caption: '6 months post-treatment results! 😍 This client combined Ultherapy with Radiesse for the ultimate non-surgical lift. The transformation speaks for itself! See the difference professional treatments make.',
      hashtags: ['#ultherapy', '#radiesse', '#nonsurgicallift', '#transformation', '#beforeandafter', '#6monthsresults'],
      likes: 1556,
      comments: 134,
      shares: 45,
      views: 8920,
      engagementRate: 1.9,
      scrapedAt: '2025-01-25T08:45:00Z',
      viralityScore: 9.1,
      category: 'Transformation'
    }
  ]);

  const categories = ['all', 'Science Explained', 'Process Demystified', 'Transformation', 'Myth Busting'];
  const profiles = ['all', '_thelookaesthetics', 'subtle.enhancements', 'skinvitalityofficial'];

  const filteredPosts = researchPosts.filter(post => {
    const matchesCategory = selectedCategory === 'all' || post.category === selectedCategory;
    const matchesProfile = selectedProfile === 'all' || post.profile === selectedProfile;
    const matchesSearch = post.caption.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         post.hashtags.some(tag => tag.toLowerCase().includes(searchTerm.toLowerCase()));
    return matchesCategory && matchesProfile && matchesSearch;
  });

  const getEngagementColor = (rate: number) => {
    if (rate >= 3) return 'text-green-600 bg-green-100';
    if (rate >= 2) return 'text-yellow-600 bg-yellow-100';
    return 'text-red-600 bg-red-100';
  };

  const getViralityColor = (score: number = 0) => {
    if (score >= 8) return 'bg-red-100 text-red-800';
    if (score >= 7) return 'bg-orange-100 text-orange-800';
    if (score >= 6) return 'bg-yellow-100 text-yellow-800';
    return 'bg-gray-100 text-gray-800';
  };

  const formatNumber = (num: number) => {
    if (num >= 1000000) return `${(num / 1000000).toFixed(1)}M`;
    if (num >= 1000) return `${(num / 1000).toFixed(1)}K`;
    return num.toString();
  };

  const calculateAverageEngagement = () => {
    if (filteredPosts.length === 0) return 0;
    return (filteredPosts.reduce((sum, post) => sum + post.engagementRate, 0) / filteredPosts.length).toFixed(1);
  };

  const calculateTotalReach = () => {
    return filteredPosts.reduce((sum, post) => sum + post.views, 0);
  };

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Research Center</h1>
          <p className="text-muted-foreground">Competitive intelligence and viral content analysis</p>
        </div>
        <Button onClick={refreshData} disabled={isLoading}>
          <TrendingUp className="w-4 h-4 mr-2" />
          Refresh Data
        </Button>
      </div>

      {/* Analytics Overview */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Posts</CardTitle>
            <Eye className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{filteredPosts.length}</div>
            <p className="text-xs text-muted-foreground">Analyzed posts</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg. Engagement</CardTitle>
            <Heart className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{calculateAverageEngagement()}%</div>
            <p className="text-xs text-muted-foreground">Engagement rate</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Reach</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatNumber(calculateTotalReach())}</div>
            <p className="text-xs text-muted-foreground">Total views</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">High Performers</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {filteredPosts.filter(p => (p.viralityScore || 0) >= 8).length}
            </div>
            <p className="text-xs text-muted-foreground">Viral posts (8.0+)</p>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Filters & Search</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Search</label>
              <div className="relative">
                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                <input
                  placeholder="Search posts, hashtags..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-8 w-full px-3 py-2 border border-input rounded-md"
                />
              </div>
            </div>
            
            <div className="space-y-2">
              <label className="text-sm font-medium">Category</label>
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="w-full px-3 py-2 border border-input rounded-md"
              >
                {categories.map(category => (
                  <option key={category} value={category}>
                    {category === 'all' ? 'All Categories' : category}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Profile</label>
              <select
                value={selectedProfile}
                onChange={(e) => setSelectedProfile(e.target.value)}
                className="w-full px-3 py-2 border border-input rounded-md"
              >
                {profiles.map(profile => (
                  <option key={profile} value={profile}>
                    {profile === 'all' ? 'All Profiles' : `@${profile}`}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Sort By</label>
              <select className="w-full px-3 py-2 border border-input rounded-md">
                <option value="virality">Virality Score</option>
                <option value="engagement">Engagement Rate</option>
                <option value="recent">Most Recent</option>
                <option value="likes">Most Liked</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Error Alert */}
      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Posts Grid */}
      {filteredPosts.length === 0 ? (
        <Card className="text-center py-12">
          <CardContent>
            <div className="text-6xl mb-4">🔍</div>
            <h3 className="text-lg font-semibold mb-2">No posts found</h3>
            <p className="text-muted-foreground mb-4">
              Try adjusting your filters or search terms
            </p>
            <Button onClick={() => {
              setSelectedCategory('all');
              setSelectedProfile('all');
              setSearchTerm('');
            }}>
              Clear Filters
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
          {filteredPosts.map((post) => (
            <Card key={post.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <div className="text-2xl">📸</div>
                    <div>
                      <CardTitle className="text-sm">@{post.profile}</CardTitle>
                      <CardDescription className="text-xs">
                        {new Date(post.scrapedAt).toLocaleDateString()}
                      </CardDescription>
                    </div>
                  </div>
                  <div className="flex flex-col items-end space-y-1">
                    {post.viralityScore && (
                      <Badge className={getViralityColor(post.viralityScore)}>
                        {post.viralityScore.toFixed(1)} viral
                      </Badge>
                    )}
                    <Badge className={`${getEngagementColor(post.engagementRate)} text-xs`}>
                      {post.engagementRate.toFixed(1)}% eng
                    </Badge>
                  </div>
                </div>
                {post.category && (
                  <Badge variant="outline" className="w-fit">
                    {post.category}
                  </Badge>
                )}
              </CardHeader>

              <CardContent className="space-y-4">
                <p className="text-sm line-clamp-4">{post.caption}</p>

                <div className="flex flex-wrap gap-1">
                  {post.hashtags.slice(0, 3).map((tag, index) => (
                    <Badge key={index} variant="secondary" className="text-xs">
                      {tag}
                    </Badge>
                  ))}
                  {post.hashtags.length > 3 && (
                    <Badge variant="secondary" className="text-xs">
                      +{post.hashtags.length - 3} more
                    </Badge>
                  )}
                </div>

                <div className="grid grid-cols-2 gap-4 pt-2 border-t">
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2 text-sm text-muted-foreground">
                      <Heart className="w-4 h-4" />
                      <span>{formatNumber(post.likes)} likes</span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-muted-foreground">
                      <MessageCircle className="w-4 h-4" />
                      <span>{formatNumber(post.comments)} comments</span>
                    </div>
                  </div>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2 text-sm text-muted-foreground">
                      <Share className="w-4 h-4" />
                      <span>{formatNumber(post.shares)} shares</span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-muted-foreground">
                      <Eye className="w-4 h-4" />
                      <span>{formatNumber(post.views)} views</span>
                    </div>
                  </div>
                </div>

                <div className="pt-2 border-t">
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="w-full"
                    onClick={() => window.open(post.postUrl, '_blank')}
                  >
                    <ExternalLink className="w-4 h-4 mr-2" />
                    View Original Post
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
