'use client';

// import { ThemeProvider } from '@mui/material/styles';
// import CssBaseline from '@mui/material/CssBaseline';
import { ReactNode, useState, createContext, useContext, useEffect } from 'react';
// import { lightTheme, darkTheme } from '../config/theme';

type ThemeMode = 'light' | 'dark';

const ThemeModeContext = createContext({
  toggleThemeMode: () => {},
  mode: 'light' as ThemeMode,
});

export function useThemeMode() {
  return useContext(ThemeModeContext);
}

export default function ThemeRegistry({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<ThemeMode>('light');
  const [isMounted, setIsMounted] = useState(false);

  const getStoredTheme = (): ThemeMode | null => {
    try {
      const savedMode = localStorage.getItem('themeMode') as ThemeMode;
      return savedMode || null;
    } catch (error) {
      console.warn('Failed to access localStorage:', error);
      return null;
    }
  };

  const setStoredTheme = (mode: ThemeMode) => {
    try {
      localStorage.setItem('themeMode', mode);
    } catch (error) {
      console.warn('Failed to save theme preference:', error);
    }
  };

  useEffect(() => {
    setIsMounted(true);
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleSystemChange = (e: MediaQueryListEvent) => {
      const newMode = e.matches ? 'dark' : 'light';
      setMode(newMode);
    };

    const savedMode = getStoredTheme();
    const systemMode = mediaQuery.matches ? 'dark' : 'light';
    setMode(savedMode || systemMode);

    mediaQuery.addEventListener('change', handleSystemChange);
    return () => mediaQuery.removeEventListener('change', handleSystemChange);
  }, []);

  useEffect(() => {
    if (isMounted) {
      setStoredTheme(mode);
    }
  }, [mode, isMounted]);

  // const theme = mode === 'light' ? lightTheme : darkTheme;

  const toggleThemeMode = () => {
    setMode((prevMode) => (prevMode === 'light' ? 'dark' : 'light'));
  };

  // Don't render until mounted to avoid SSR mismatch
  if (!isMounted) {
    return null;
  }

  return (
    <ThemeModeContext.Provider value={{ toggleThemeMode, mode }}>
      <div className={mode}>
        {children}
      </div>
    </ThemeModeContext.Provider>
  );
}