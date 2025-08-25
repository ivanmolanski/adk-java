// FreshnessFilterAgent.js
// Filters posts to only those from the last 24 hours
import { differenceInHours, parseISO } from 'date-fns';

export function filterFreshPosts(posts) {
  return posts.filter(post => {
    const published = post.published || post.date || post.scrapedAt;
    if (!published) return false;
    const dateObj = typeof published === 'string' ? parseISO(published) : new Date(published);
    return differenceInHours(new Date(), dateObj) <= 24;
  });
}
