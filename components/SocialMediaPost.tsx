'use client';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Instagram, Music, Upload, Send, CheckCircle, AlertCircle } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface SocialMediaPostProps {
  draft: {
    id?: string;
    platform: string;
    caption: string;
    hashtags: string[];
    suggestedMediaType: string;
    complianceChecked: boolean;
  };
  onPostSuccess?: () => void;
}

export function SocialMediaPost({ draft, onPostSuccess }: SocialMediaPostProps) {
  const [isPosting, setIsPosting] = useState(false);
  const [caption, setCaption] = useState(draft.caption);
  const [hashtags, setHashtags] = useState(draft.hashtags.join(' '));
  const [mediaFile, setMediaFile] = useState<File | null>(null);
  const { toast } = useToast();

  const handlePost = async () => {
    if (!caption.trim()) {
      toast({
        title: "Error",
        description: "Caption cannot be empty",
        variant: "destructive",
      });
      return;
    }

    setIsPosting(true);

    try {
      const formData = new FormData();
      formData.append('content', caption);
      formData.append('hashtags', hashtags);
      formData.append('platform', draft.platform);

      if (mediaFile) {
        formData.append('media', mediaFile);
      }

      const response = await fetch(`/api/social/post/${draft.platform}`, {
        method: 'POST',
        body: formData,
      });

      const result = await response.json();

      if (result.success) {
        toast({
          title: "Success!",
          description: `Posted successfully to ${draft.platform}`,
        });
        onPostSuccess?.();
      } else {
        toast({
          title: "Posting Failed",
          description: result.error || "Unknown error occurred",
          variant: "destructive",
        });
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to post content",
        variant: "destructive",
      });
    } finally {
      setIsPosting(false);
    }
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setMediaFile(file);
    }
  };

  const platformIcon = draft.platform === 'instagram' ? Instagram : Music;
  const IconComponent = platformIcon;

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="flex items-center space-x-2">
          <IconComponent className="w-5 h-5" />
          <span>Post to {draft.platform}</span>
          {draft.complianceChecked && (
            <Badge variant="secondary" className="text-green-700 bg-green-100">
              <CheckCircle className="w-3 h-3 mr-1" />
              Compliant
            </Badge>
          )}
        </CardTitle>
        <CardDescription>
          {draft.suggestedMediaType} • {draft.hashtags.length} hashtags
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div>
          <Label htmlFor="caption">Caption</Label>
          <Textarea
            id="caption"
            value={caption}
            onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setCaption(e.target.value)}
            placeholder="Enter your caption..."
            className="mt-1"
            rows={4}
          />
        </div>

        <div>
          <Label htmlFor="hashtags">Hashtags</Label>
          <Input
            id="hashtags"
            value={hashtags}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setHashtags(e.target.value)}
            placeholder="#hashtag1 #hashtag2"
            className="mt-1"
          />
        </div>

        {(draft.suggestedMediaType === 'image' || draft.suggestedMediaType === 'video') && (
          <div>
            <Label htmlFor="media">Media File</Label>
            <Input
              id="media"
              type="file"
              accept={draft.suggestedMediaType === 'image' ? 'image/*' : 'video/*'}
              onChange={handleFileChange}
              className="mt-1"
            />
            {mediaFile && (
              <p className="text-sm text-muted-foreground mt-1">
                Selected: {mediaFile.name}
              </p>
            )}
          </div>
        )}

        <Button
          onClick={handlePost}
          disabled={isPosting || !caption.trim()}
          className="w-full"
        >
          {isPosting ? (
            <>
              <Upload className="w-4 h-4 mr-2 animate-spin" />
              Posting...
            </>
          ) : (
            <>
              <Send className="w-4 h-4 mr-2" />
              Post to {draft.platform}
            </>
          )}
        </Button>
      </CardContent>
    </Card>
  );
}