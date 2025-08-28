import React, { useState } from 'react';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Instagram, Copy, CheckCircle, AlertCircle, Send } from 'lucide-react';
// Fix import path by using relative path for reliable resolution
import { Textarea } from '../components/ui/textarea';

interface SocialMediaPostProps {
  draft: {
    id: string;
    platform: string;
    caption: string;
    hashtags?: string[];
    suggestedMediaType?: string;
    complianceChecked?: boolean;
  };
  onPostSuccess?: () => void;
}

export function SocialMediaPost({ draft, onPostSuccess }: SocialMediaPostProps) {
  const [isCopied, setIsCopied] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [editedCaption, setEditedCaption] = useState(draft.caption);
  const [editedHashtags, setEditedHashtags] = useState(draft.hashtags?.join(' ') || '');

  const handleCopy = () => {
    const fullText = `${editedCaption}\n\n${editedHashtags}`;
    navigator.clipboard.writeText(fullText).then(() => {
      setIsCopied(true);
      setTimeout(() => setIsCopied(false), 2000);
    });
  };

  const handleSend = () => {
    setIsSending(true);
    // Simulate sending to social media platform
    setTimeout(() => {
      setIsSending(false);
      setIsSuccess(true);
      setTimeout(() => {
        setIsSuccess(false);
        if (onPostSuccess) onPostSuccess();
      }, 2000);
    }, 1500);
  };

  return (
    <Card className="overflow-hidden">
      <CardHeader className="bg-gradient-to-r from-blue-50 to-purple-50 pb-2">
        <div className="flex justify-between items-center">
          <CardTitle className="text-lg flex items-center space-x-2">
            <Instagram className="h-5 w-5 text-pink-500" />
            <span>{draft.platform === 'instagram' ? 'Instagram' : draft.platform}</span>
          </CardTitle>
          <div className="flex space-x-2">
            <Badge variant={draft.complianceChecked ? "default" : "destructive"} className="h-6">
              {draft.complianceChecked ? (
                <span className="flex items-center space-x-1">
                  <CheckCircle className="h-3 w-3 mr-1" />
                  Compliant
                </span>
              ) : (
                <span className="flex items-center space-x-1">
                  <AlertCircle className="h-3 w-3 mr-1" />
                  Review Needed
                </span>
              )}
            </Badge>
            <Badge variant="outline" className="h-6">
              {draft.suggestedMediaType || 'Image'}
            </Badge>
          </div>
        </div>
      </CardHeader>
      
      <CardContent className="pt-4">
        <div className="space-y-4">
          <div>
            <label className="text-sm font-medium mb-1 block">Caption</label>
            <Textarea 
              value={editedCaption} 
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setEditedCaption(e.target.value)}
              className="resize-none min-h-[100px]"
              placeholder="Enter caption here..."
            />
          </div>
          
          <div>
            <label className="text-sm font-medium mb-1 block">Hashtags</label>
            <Textarea 
              value={editedHashtags} 
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setEditedHashtags(e.target.value)}
              className="resize-none min-h-[60px]"
              placeholder="Enter hashtags here..."
            />
          </div>
        </div>
      </CardContent>
      
      <CardFooter className="flex justify-between border-t bg-gray-50 p-3">
        <Button 
          variant="outline" 
          size="sm" 
          onClick={handleCopy}
          className={isCopied ? "bg-green-50 text-green-700" : ""}
        >
          {isCopied ? (
            <>
              <CheckCircle className="h-4 w-4 mr-2" />
              Copied!
            </>
          ) : (
            <>
              <Copy className="h-4 w-4 mr-2" />
              Copy
            </>
          )}
        </Button>
        
        <Button 
          size="sm" 
          onClick={handleSend}
          disabled={isSending || isSuccess}
          className={isSuccess ? "bg-green-600" : ""}
        >
          {isSending ? (
            <>
              <span className="animate-pulse">Sending...</span>
            </>
          ) : isSuccess ? (
            <>
              <CheckCircle className="h-4 w-4 mr-2" />
              Posted!
            </>
          ) : (
            <>
              <Send className="h-4 w-4 mr-2" />
              Post to {draft.platform}
            </>
          )}
        </Button>
      </CardFooter>
    </Card>
  );
}