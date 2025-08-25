'use client';

import { Button, Typography, Paper } from '@mui/material';
import useTheme from '../hooks/useTheme';

export default function ThemeDemo() {
  const { 
    theme, 
    mode, 
    toggleThemeMode, 
    isDarkMode 
  } = useTheme();

  return (
    <Paper 
      elevation={3} 
      sx={{ 
        p: 3, 
        mb: 3,
        backgroundColor: theme.palette.background.paper
      }}
    >
      <Typography variant="h6" gutterBottom>
        Theme Demo Component
      </Typography>
      
      <Typography paragraph>
        Current mode: <strong>{mode}</strong>
      </Typography>

      <Typography paragraph sx={{ color: theme.palette.text.secondary }}>
        Secondary text color example
      </Typography>

      <Button 
        variant="contained" 
        color="primary"
        onClick={toggleThemeMode}
        sx={{ mr: 2 }}
      >
        Toggle Theme
      </Button>

      <Button 
        variant="outlined"
        sx={{ 
          color: isDarkMode ? 'warning.main' : 'error.main',
          borderColor: isDarkMode ? 'warning.main' : 'error.main'
        }}
      >
        Conditional Styling
      </Button>
    </Paper>
  );
}