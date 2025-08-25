# Theme System Documentation

## Overview
The theme system provides consistent styling across the application with support for light/dark modes. It's built using:
- Material UI theming
- React context
- LocalStorage persistence
- System preference detection

## Key Files
- `app/ThemeRegistry.tsx` - Main theme provider component
- `config/theme.ts` - Theme configuration
- `hooks/useTheme.ts` - Custom hook for theme access
- `components/ThemeDemo.tsx` - Example usage

## Usage Guide

### Accessing Theme Values
Use the `useTheme` hook in any component:

```tsx
import useTheme from '../hooks/useTheme';

function MyComponent() {
  const { 
    theme,        // Full theme object
    mode,         // Current mode ('light'|'dark')
    isDarkMode,   // Boolean for dark mode
    toggleThemeMode // Toggle function
  } = useTheme();

  return (
    <div style={{ color: theme.palette.text.primary }}>
      Current mode: {mode}
      <button onClick={toggleThemeMode}>
        Toggle Theme
      </button>
    </div>
  );
}
```

### Styling Components
Use theme-aware styles with the `sx` prop:

```tsx
<Box
  sx={{
    bgcolor: 'background.paper',
    color: 'text.primary',
    p: 2
  }}
>
  Theme-aware content
</Box>
```

### Adding New Theme Values
Edit `config/theme.ts`:

```ts
const lightTheme = createTheme({
  palette: {
    customColor: {
      main: '#123456',
      contrastText: '#ffffff'
    }
  }
});
```

### Best Practices
1. Always use theme colors rather than hardcoded values
2. For conditional styles, use `isDarkMode` flag
3. Test components in both light and dark modes
4. Add new colors to both theme variants

## Theme Structure
The theme object contains:
- `palette` - Color definitions
- `typography` - Font settings
- `spacing` - Margin/padding scale
- `breakpoints` - Responsive design
- `zIndex` - Layer management
- `transitions` - Animation timing

## Troubleshooting
**Issue:** Theme changes not persisting
- Solution: Ensure `ThemeRegistry` wraps your app

**Issue:** SSR mismatch
- Solution: Components should handle undefined theme during SSR

**Issue:** Colors not updating
- Solution: Verify colors are defined in both theme variants