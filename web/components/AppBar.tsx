'use client';

import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import Link from 'next/link';

export default function CustomAppBar() {
  return (
    <AppBar position="static" elevation={0}>
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          Viral Forge
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button color="inherit" component={Link} href="/dashboard">
            Dashboard
          </Button>
          <Button color="inherit" component={Link} href="/research">
            Research
          </Button>
          <Button color="inherit" component={Link} href="/command-center">
            Command Center
          </Button>
          <Button color="inherit" component={Link} href="/content-studio">
            Content Studio
          </Button>
        </Box>
      </Toolbar>
    </AppBar>
  );
}