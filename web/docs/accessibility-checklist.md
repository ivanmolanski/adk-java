# Accessibility Checklist

## General
- [ ] All images have alt text
- [ ] Color contrast meets WCAG AA standards
- [ ] Page has proper heading hierarchy (h1-h6)
- [ ] Page has a descriptive title
- [ ] Language attribute set on HTML element
- [ ] Skip to content link available

## Keyboard Navigation  
- [ ] All interactive elements are focusable
- [ ] Focus indicators are visible
- [ ] Tab order follows visual flow
- [ ] Keyboard traps don't exist
- [ ] All functionality available via keyboard

## Forms
- [ ] All form fields have labels
- [ ] Required fields are marked
- [ ] Error messages are descriptive
- [ ] Field instructions are associated
- [ ] Form validation works with screen readers

## Interactive Elements
- [ ] Buttons have descriptive text
- [ ] Links make sense out of context
- [ ] Custom controls have proper ARIA roles
- [ ] State changes are announced
- [ ] Focus management is handled properly

## Multimedia
- [ ] Videos have captions
- [ ] Audio has transcripts  
- [ ] Animations can be paused
- [ ] No flashing content (3x per second)
- [ ] Auto-playing content can be stopped

## Testing
- [ ] Verified with screen reader (NVDA/VoiceOver)
- [ ] Tested keyboard navigation
- [ ] Checked color contrast
- [ ] Validated with axe DevTools
- [ ] Tested with zoom (200%)

## Dark Mode Specific
- [ ] Contrast maintained in dark mode
- [ ] Focus indicators remain visible
- [ ] Images/icons remain clear
- [ ] Text remains readable
- [ ] Interactive elements stay usable

## Mobile Accessibility
- [ ] Touch targets >= 44x44px
- [ ] No horizontal scrolling required
- [ ] Zoom works properly
- [ ] Orientation changes handled
- [ ] Gestures have alternatives

## ARIA Usage
- [ ] ARIA used only when necessary
- [ ] Roles match element behavior  
- [ ] States/properties updated dynamically
- [ ] Landmarks used properly
- [ ] Live regions for dynamic content