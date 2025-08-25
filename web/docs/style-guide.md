# Accessibility Guidelines

## Color Contrast
All theme colors meet WCAG 2.1 AA contrast requirements:

| Element | Light Mode Ratio | Dark Mode Ratio |
|---------|------------------|------------------|
| Primary text | 15.8:1 | 7.3:1 |
| Secondary text | 8.3:1 | 4.5:1 |
| Primary buttons | 4.6:1 | 4.5:1 |
| Error text | 4.5:1 | 7.1:1 |

## Interactive Elements
1. Buttons must have:
   - Minimum 44x44px touch target
   - Visible focus state
   - ARIA labels when icon-only

```tsx
<Button 
  aria-label="Submit form"
  sx={{ 
    minWidth: 44,
    minHeight: 44,
    '&:focus': { 
      outline: '2px solid',
      outlineColor: 'primary.main'
    }
  }}
>
  Submit
</Button>
```

## ARIA Attributes
Common patterns:

```tsx
// Navigation
<nav aria-label="Main navigation">...</nav>

// Form fields  
<TextField 
  label="Email"
  aria-required="true"
  inputProps={{
    'aria-describedby': 'email-help'
  }}
/>
<span id="email-help">Enter your email address</span>

// Status messages
<div role="status" aria-live="polite">
  Form submitted successfully
</div>
```

## Keyboard Navigation
1. Ensure all interactive elements are focusable
2. Maintain logical tab order
3. Support keyboard shortcuts:
   - `Enter` to activate buttons/links
   - `Space` to toggle checkboxes
   - `Esc` to close modals

## Testing Tools
1. Use Chrome Lighthouse audits
2. Run axe DevTools scanner
3. Test with keyboard only
4. Verify with screen readers (NVDA, VoiceOver)
5. Check color contrast with WebAIM Contrast Checker

## Visual Hierarchy
1. Use heading levels properly (h1-h6)
2. Maintain consistent spacing
3. Provide visible focus states
4. Use semantic HTML elements
5. Ensure text resizes properly (up to 200%)

## Dark Mode Considerations
1. Test contrast in both modes
2. Avoid pure black/white
3. Provide reduced motion options
4. Ensure focus indicators remain visible
5. Test with Windows high contrast mode