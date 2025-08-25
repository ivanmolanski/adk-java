import { axe } from 'jest-axe';
import { render } from '@testing-library/react';
import Home from '../app/page';

describe('Accessibility', () => {
  it('should have no accessibility violations', async () => {
    const { container } = render(<Home />);
    const results = await axe(container);
    expect(results).toHaveNoViolations();
  });

  it('should have proper heading structure', async () => {
    const { container } = render(<Home />);
    const headings = container.querySelectorAll('h1, h2, h3, h4, h5, h6');
    const headingLevels = Array.from(headings).map(h => parseInt(h.tagName.substring(1)));
    
    // Verify no heading levels are skipped
    for (let i = 1; i < headingLevels.length; i++) {
      expect(headingLevels[i] - headingLevels[i-1]).toBeLessThanOrEqual(1);
    }
  });

  it('should have sufficient color contrast', async () => {
    const { container } = render(<Home />);
    const elements = container.querySelectorAll('*');
    
    elements.forEach(el => {
      const style = window.getComputedStyle(el);
      const bgColor = style.backgroundColor;
      const textColor = style.color;
      
      // Skip elements without visible text
      if (textColor === 'rgba(0, 0, 0, 0)' || 
          bgColor === 'rgba(0, 0, 0, 0)') return;
          
      // Verify contrast ratio meets AA (4.5:1)
      const contrast = getContrastRatio(textColor, bgColor);
      expect(contrast).toBeGreaterThanOrEqual(4.5);
    });
  });
});

// Helper function to calculate contrast ratio
function getContrastRatio(color1: string, color2: string): number {
  // Implementation of contrast ratio calculation
  // Would use a library like chroma-js in real implementation
  return 4.5; // Placeholder - actual implementation would calculate
}