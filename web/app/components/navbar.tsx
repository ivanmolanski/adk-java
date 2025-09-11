'use client';
import { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { 
  Instagram, 
  Music, 
  LogOut, 
  User, 
  TrendingUp, 
  PenTool, 
  Search, 
  Bot,
  Home,
  Menu,
  X
} from 'lucide-react';
import { ModeToggle } from '@/components/mode-toggle';

interface AuthStatus {
  tiktok: boolean;
  instagram: boolean;
  tiktokProfile?: any;
  instagramProfile?: any;
}

const navigationItems = [
  { name: 'Dashboard', href: '/dashboard', icon: Home },
  { name: 'Research', href: '/research', icon: Search },
  { name: 'Content Studio', href: '/content-studio', icon: PenTool },
  { name: 'Command Center', href: '/command-center', icon: Bot },
];

export function Navbar() {
  const [authStatus, setAuthStatus] = useState<AuthStatus>({
    tiktok: false,
    instagram: false
  });
  const [isLoading, setIsLoading] = useState(true);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const pathname = usePathname();

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
    <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Logo and Brand */}
          <div className="flex items-center space-x-4">
            <Link href="/" className="flex items-center space-x-2">
              <TrendingUp className="w-6 h-6 text-primary" />
              <h1 className="text-xl font-bold">MDAesthetics Viral Forge</h1>
            </Link>
          </div>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center space-x-6">
            {navigationItems.map((item) => {
              const Icon = item.icon;
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={`flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-primary text-primary-foreground'
                      : 'text-muted-foreground hover:text-foreground hover:bg-accent'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{item.name}</span>
                </Link>
              );
            })}
          </div>

          {/* Right side - Auth Status & Theme Toggle */}
          <div className="flex items-center space-x-4">
            {/* Social Media Auth Status - Compact for desktop */}
            <div className="hidden md:flex items-center space-x-2">
              {/* TikTok Status */}
              <div className="flex items-center space-x-1">
                <Music className="w-4 h-4 text-pink-500" />
                {authStatus.tiktok ? (
                  <Badge variant="default" className="bg-green-500 text-xs">
                    TikTok
                  </Badge>
                ) : (
                  <Badge variant="outline" className="text-xs">
                    TikTok
                  </Badge>
                )}
              </div>

              {/* Instagram Status */}
              <div className="flex items-center space-x-1">
                <Instagram className="w-4 h-4 text-pink-500" />
                {authStatus.instagram ? (
                  <Badge variant="default" className="bg-green-500 text-xs">
                    IG
                  </Badge>
                ) : (
                  <Badge variant="outline" className="text-xs">
                    IG
                  </Badge>
                )}
              </div>
            </div>

            {/* Theme Toggle */}
            <ModeToggle />

            {/* Mobile Menu Button */}
            <Button
              variant="ghost"
              size="sm"
              className="md:hidden"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </Button>
          </div>
        </div>

        {/* Mobile Navigation Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t py-4 space-y-2">
            {navigationItems.map((item) => {
              const Icon = item.icon;
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={`flex items-center space-x-3 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-primary text-primary-foreground'
                      : 'text-muted-foreground hover:text-foreground hover:bg-accent'
                  }`}
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Icon className="w-4 h-4" />
                  <span>{item.name}</span>
                </Link>
              );
            })}
            
            {/* Mobile Social Auth Section */}
            <div className="pt-4 border-t space-y-3">
              <h3 className="text-sm font-medium text-muted-foreground px-3">Social Accounts</h3>
              
              {/* TikTok Auth */}
              <div className="px-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Music className="w-4 h-4 text-pink-500" />
                    <span className="text-sm">TikTok</span>
                  </div>
                  {authStatus.tiktok ? (
                    <div className="flex items-center space-x-2">
                      <Badge variant="default" className="bg-green-500">Connected</Badge>
                      <Button variant="outline" size="sm" onClick={() => handleLogout('tiktok')}>
                        Logout
                      </Button>
                    </div>
                  ) : (
                    <Button variant="outline" size="sm" onClick={() => handleLogin('tiktok')}>
                      Login
                    </Button>
                  )}
                </div>
              </div>

              {/* Instagram Auth */}
              <div className="px-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Instagram className="w-4 h-4 text-pink-500" />
                    <span className="text-sm">Instagram</span>
                  </div>
                  {authStatus.instagram ? (
                    <div className="flex items-center space-x-2">
                      <Badge variant="default" className="bg-green-500">Connected</Badge>
                      <Button variant="outline" size="sm" onClick={() => handleLogout('instagram')}>
                        Logout
                      </Button>
                    </div>
                  ) : (
                    <Button variant="outline" size="sm" onClick={() => handleLogin('instagram')}>
                      Login
                    </Button>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </nav>
  );
}