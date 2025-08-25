'use client';

import { useThemeMode } from '../app/ThemeRegistry';
import { lightTheme, darkTheme } from '../config/theme';
import { useMemo } from 'react';

export default function useTheme() {
  const { mode, toggleThemeMode } = useThemeMode();
  
  const theme = useMemo(() => {
    return mode === 'light' ? lightTheme : darkTheme;
  }, [mode]);

  return {
    theme,
    mode,
    toggleThemeMode,
    isDarkMode: mode === 'dark',
    isLightMode: mode === 'light'
  };
}