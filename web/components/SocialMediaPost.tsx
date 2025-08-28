'use client';

import { useState } from 'react';
import { ContentDraft } from '@/types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Copy, ExternalLink, Share2, Instagram } from 'lucide-react';
import { toast } from 'sonner';

interface SocialMediaPostProps {
  draft: ContentDraft;
  onPostSuccess?: () => void;
}

export function SocialMediaPost({ draft, onPostSuccess }: SocialMediaPostProps) {
  const [isPosting, setIsPosting] = useState(false);

  const handleCopyCaption = async () => {
    try {
      await navigator.clipboard.writeText(draft.caption);
      toast.success('Caption copied to clipboard!');
    } catch (error) {
      toast.error('Failed to copy caption');
    }
  };

  const handleCopyHashtags = async () => {
    try {
      const hashtagString = draft.hashtags.join(' ');
      await navigator.clipboard.writeText(hashtagString);
      toast.success('Hashtags copied to clipboard!');
    } catch (error) {
      toast.error('Failed to copy hashtags');
    }
  };

  const handlePost = async () => {
    setIsPosting(true);
    try {
      // In a real implementation, this would post to the actual social media platform
      // For now, we'll just simulate a successful post
      await new Promise(resolve => setTimeout(resolve, 2000));
      toast.success(`Posted to ${draft.platform.toUpperCase()} successfully!`);
      onPostSuccess?.();
    } catch (error) {
      toast.error('Failed to post content');
    } finally {
      setIsPosting(false);
    }
  };

  const getPlatformIcon = (platform: string) => {
    switch (platform.toLowerCase()) {
      case 'instagram':
        return <Instagram className="h-4 w-4" />;
      case 'tiktok':
        return <Share2 className="h-4 w-4" />;
      default:
        return <Share2 className="h-4 w-4" />;
    }
  };

  return (
    <Card className="w-full">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-lg flex items-center gap-2">
            {getPlatformIcon(draft.platform)}
            {draft.platform.charAt(0).toUpperCase() + draft.platform.slice(1)} Draft
          </CardTitle>
          <div className="flex gap-2">
            <Badge variant={draft.complianceChecked ? "default" : "destructive"}>
              {draft.complianceChecked ? "Compliant" : "Needs Review"}
            </Badge>
            <Badge variant="outline">
              {draft.suggestedMediaType}
            </Badge>
          </div>
        </div>
        <CardDescription>
          Created {new Date(draft.createdAt).toLocaleDateString()}
        </CardDescription>
      </CardHeader>
      
      <CardContent className="space-y-4">
        {/* Caption */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <h4 className="text-sm font-medium">Caption</h4>
            <Button
              variant="ghost"
              size="sm"
              onClick={handleCopyCaption}
              className="h-8"
            >
              <Copy className="h-3 w-3 mr-1" />
              Copy
            </Button>
          </div>
          <div className="bg-muted rounded-lg p-3 text-sm whitespace-pre-wrap">
            {draft.caption}
          </div>
        </div>

        {/* Hashtags */}
        {draft.hashtags && draft.hashtags.length > 0 && (
          <div>
            <div className="flex items-center justify-between mb-2">
              <h4 className="text-sm font-medium">Hashtags ({draft.hashtags.length})</h4>
              <Button
                variant="ghost"
                size="sm"
                onClick={handleCopyHashtags}
                className="h-8"
              >
                <Copy className="h-3 w-3 mr-1" />
                Copy
              </Button>
            </div>
            <div className="flex flex-wrap gap-1">
              {draft.hashtags.map((tag, index) => (
                <Badge key={index} variant="secondary" className="text-xs">
                  {tag}
                </Badge>
              ))}
            </div>
          </div>
        )}

        {/* Actions */}
        <div className="flex gap-2 pt-2">
          <Button
            onClick={handlePost}
            disabled={isPosting || !draft.complianceChecked}
            className="flex-1"
          >
            {isPosting ? 'Posting...' : `Post to ${draft.platform.charAt(0).toUpperCase() + draft.platform.slice(1)}`}
          </Button>
          <Button
            variant="outline"
            size="icon"
            onClick={() => window.open(`https://${draft.platform}.com`, '_blank')}
          >
            <ExternalLink className="h-4 w-4" />
          </Button>
        </div>
        
        {!draft.complianceChecked && (
          <p className="text-xs text-muted-foreground">
            Content needs compliance review before posting
          </p>
        )}
      </CardContent>
    </Card>
  );
}