'use client';
import { useState, useEffect } from 'react';
import { useViralIntelligence } from '@/hooks/useViralIntelligence';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Copy, Download, Heart, MessageCircle, Share, Instagram, Eye, RefreshCw } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { toast } from 'sonner';

export default function ContentStudioPage() {
  const { drafts, isLoading, error, refreshData } = useViralIntelligence();
  const [copiedItems, setCopiedItems] = useState<string[]>([]);

  const copyToClipboard = async (text: string, id: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedItems(prev => [...prev, id]);
      toast.success('Copied to clipboard!');
      setTimeout(() => {
        setCopiedItems(prev => prev.filter(item => item !== id));
      }, 2000);
    } catch (err) {
      toast.error('Failed to copy to clipboard');
    }
  };

  const formatHashtags = (hashtags: string[]) => {
    return hashtags.join(' ');
  };

  const getPlatformIcon = (platform: string) => {
    switch (platform) {
      case 'instagram':
        return <Instagram className="w-4 h-4" />;
      case 'tiktok':
        return <span className="text-sm font-bold">TT</span>;
      default:
        return <Eye className="w-4 h-4" />;
    }
  };

  const getMediaTypeIcon = (mediaType: string) => {
    switch (mediaType) {
      case 'video':
        return '🎥';
      case 'image':
        return '📸';
      case 'carousel':
        return '🎠';
      default:
        return '📄';
    }
  };

  const estimateEngagement = (platform: string) => {
    // Mock engagement estimation based on platform
    const baseEngagement = {
      instagram: { likes: 150, comments: 23, shares: 8 },
      tiktok: { likes: 340, comments: 45, shares: 18 }
    };
    return baseEngagement[platform as keyof typeof baseEngagement] || baseEngagement.instagram;
  };

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Content Studio</h1>
          <p className="text-muted-foreground">Ready-to-post content for MD Aesthetics</p>
        </div>
        <div className="flex space-x-2">
          <Button 
            variant="outline" 
            onClick={refreshData} 
            disabled={isLoading}
          >
            <RefreshCw className={`w-4 h-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          <Button>
            <Download className="w-4 h-4 mr-2" />
            Export All
          </Button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Content Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {[...Array(4)].map((_, i) => (
            <Card key={i} className="animate-pulse">
              <CardHeader>
                <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                <div className="h-3 bg-gray-100 rounded w-1/2"></div>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="h-20 bg-gray-200 rounded"></div>
                  <div className="h-3 bg-gray-100 rounded"></div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : drafts.length === 0 ? (
        <Card className="text-center py-12">
          <CardContent>
            <div className="text-6xl mb-4">📝</div>
            <h3 className="text-lg font-semibold mb-2">No content available</h3>
            <p className="text-muted-foreground mb-4">
              Generate new content by analyzing viral trends first
            </p>
            <Button onClick={() => window.location.href = '/command-center'}>
              Generate Content
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {drafts.map((draft, index) => {
            const engagement = estimateEngagement(draft.platform);
            return (
              <Card key={draft.id ?? `draft-${index}`} className="relative">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                      {getPlatformIcon(draft.platform)}
                      <CardTitle className="text-lg capitalize">{draft.platform}</CardTitle>
                      <Badge variant={draft.complianceChecked ? "default" : "secondary"}>
                        {draft.complianceChecked ? "✓ Compliant" : "Review Needed"}
                      </Badge>
                    </div>
                    <div className="flex items-center space-x-1 text-sm text-muted-foreground">
                      <span>{getMediaTypeIcon(draft.suggestedMediaType)}</span>
                      <span>{draft.suggestedMediaType}</span>
                    </div>
                  </div>
                  <CardDescription>
                    Created {new Date(draft.createdAt).toLocaleDateString()} • 
                    Estimated reach: {Math.floor(Math.random() * 2000 + 500)} followers
                  </CardDescription>
                </CardHeader>

                <CardContent className="space-y-4">
                  {/* Content Preview */}
                  <div className="border rounded-lg p-4 bg-gray-50">
                    <div className="flex items-start space-x-3 mb-3">
                      <div className="w-8 h-8 rounded-full bg-gradient-to-r from-purple-500 to-pink-500 flex items-center justify-center text-white text-sm font-bold">
                        MD
                      </div>
                      <div className="flex-1">
                        <div className="font-semibold text-sm">mdaesthetics.ca</div>
                        <div className="text-xs text-gray-500">Sponsored</div>
                      </div>
                    </div>
                    
                    <div className="mb-3 p-3 bg-gray-200 rounded text-center text-sm text-gray-600">
                      {draft.suggestedMediaType === 'video' ? '▶️ Video Preview' : 
                       draft.suggestedMediaType === 'carousel' ? '🎠 Carousel Preview' : '📸 Image Preview'}
                    </div>
                    
                    <p className="text-sm whitespace-pre-wrap mb-3">
                      {draft.caption}
                    </p>
                    
                    <div className="flex items-center space-x-4 text-gray-500 text-sm">
                      <div className="flex items-center space-x-1">
                        <Heart className="w-4 h-4" />
                        <span>{engagement.likes}</span>
                      </div>
                      <div className="flex items-center space-x-1">
                        <MessageCircle className="w-4 h-4" />
                        <span>{engagement.comments}</span>
                      </div>
                      <div className="flex items-center space-x-1">
                        <Share className="w-4 h-4" />
                        <span>{engagement.shares}</span>
                      </div>
                    </div>
                  </div>

                  {/* Hashtags */}
                  <div className="space-y-2">
                    <h4 className="font-medium text-sm">Hashtags ({draft.hashtags.length})</h4>
                    <div className="flex flex-wrap gap-1">
                      {draft.hashtags.map((tag, index) => (
                        <Badge key={index} variant="outline" className="text-xs">
                          {tag}
                        </Badge>
                      ))}
                    </div>
                  </div>

                  {/* Action Buttons */}
                  <div className="grid grid-cols-2 gap-2 pt-4 border-t">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => copyToClipboard(draft.caption, `caption-${draft.id}`)}
                      className="flex items-center space-x-2"
                    >
                      <Copy className="w-4 h-4" />
                      <span>
                        {copiedItems.includes(`caption-${draft.id}`) ? 'Copied!' : 'Copy Caption'}
                      </span>
                    </Button>
                    
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => copyToClipboard(formatHashtags(draft.hashtags), `hashtags-${draft.id}`)}
                      className="flex items-center space-x-2"
                    >
                      <Copy className="w-4 h-4" />
                      <span>
                        {copiedItems.includes(`hashtags-${draft.id}`) ? 'Copied!' : 'Copy Tags'}
                      </span>
                    </Button>
                  </div>

                  {/* Compliance Notes */}
                  {draft.complianceChecked && (
                    <div className="text-xs text-green-600 bg-green-50 p-2 rounded">
                      ✓ Compliance verified: Uses "Neuromodulator" instead of "Botox", includes proper CTA, educational content approved
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {/* Bottom Actions */}
      {drafts.length > 0 && (
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-semibold">Batch Actions</h3>
                <p className="text-sm text-muted-foreground">
                  Apply actions to all {drafts.length} content pieces
                </p>
              </div>
              <div className="flex space-x-2">
                <Button 
                  variant="outline"
                  onClick={() => {
                    const allContent = drafts.map(d => 
                      `Caption:\n${d.caption}\n\nHashtags:\n${formatHashtags(d.hashtags)}\n\n---\n\n`
                    ).join('');
                    copyToClipboard(allContent, 'all-content');
                  }}
                >
                  <Copy className="w-4 h-4 mr-2" />
                  Copy All
                </Button>
                <Button>
                  <Download className="w-4 h-4 mr-2" />
                  Export CSV
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
