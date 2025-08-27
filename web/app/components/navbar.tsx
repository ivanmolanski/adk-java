'use client';
import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { Instagram, Music, LogOut, User } from 'lucide-react';

interface AuthStatus {
  tiktok: boolean;
  instagram: boolean;
  tiktokProfile?: any;
  instagramProfile?: any;
}

export function Navbar() {
  const [authStatus, setAuthStatus] = useState<AuthStatus>({
    tiktok: false,
    instagram: false
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const response = await fetch('/api/social/auth/status');
      const data = await response.json();
      if (data.success) {
        setAuthStatus(data.authStatus);
      }
    } catch (error) {
      console.error('Failed to check auth status:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogin = (platform: 'tiktok' | 'instagram') => {
    // Open OAuth2 login in a popup window
    const width = 600;
    const height = 700;
    const left = window.innerWidth / 2 - width / 2;
    const top = window.innerHeight / 2 - height / 2;

    const popup = window.open(
      `/oauth2/authorization/${platform}`,
      `${platform}Login`,
      `width=${width},height=${height},left=${left},top=${top},scrollbars=yes,resizable=yes`
    );

    // Listen for popup messages (when OAuth2 completes)
    const handleMessage = (event: MessageEvent) => {
      if (event.origin === window.location.origin && event.data === 'oauth_success') {
        popup?.close();
        checkAuthStatus(); // Refresh auth status
        window.removeEventListener('message', handleMessage);
      }
    };

    window.addEventListener('message', handleMessage);
  };

  const handleLogout = async (platform: 'tiktok' | 'instagram') => {
    try {
      await fetch(`/api/social/disconnect/${platform}`, { method: 'POST' });
      checkAuthStatus();
    } catch (error) {
      console.error(`Failed to logout from ${platform}:`, error);
    }
  };

  return (
    <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center space-x-4">
            <h1 className="text-xl font-bold">MDAesthetics Viral Forge</h1>
          </div>

          <div className="flex items-center space-x-4">
            {/* TikTok Auth Status */}
            <div className="flex items-center space-x-2">
              <Music className="w-5 h-5 text-pink-500" />
              {isLoading ? (
                <Badge variant="secondary">Loading...</Badge>
              ) : authStatus.tiktok ? (
                <div className="flex items-center space-x-2">
                  <Badge variant="default" className="bg-green-500">
                    <User className="w-3 h-3 mr-1" />
                    Connected
                  </Badge>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleLogout('tiktok')}
                  >
                    <LogOut className="w-3 h-3 mr-1" />
                    Logout
                  </Button>
                </div>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleLogin('tiktok')}
                >
                  Login
                </Button>
              )}
            </div>

            {/* Instagram Auth Status */}
            <div className="flex items-center space-x-2">
              <Instagram className="w-5 h-5 text-pink-500" />
              {isLoading ? (
                <Badge variant="secondary">Loading...</Badge>
              ) : authStatus.instagram ? (
                <div className="flex items-center space-x-2">
                  <Badge variant="default" className="bg-green-500">
                    <User className="w-3 h-3 mr-1" />
                    Connected
                  </Badge>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleLogout('instagram')}
                  >
                    <LogOut className="w-3 h-3 mr-1" />
                    Logout
                  </Button>
                </div>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleLogin('instagram')}
                >
                  Login
                </Button>
              )}
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
}