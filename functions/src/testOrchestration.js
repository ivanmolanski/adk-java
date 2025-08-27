// Simple manual test harness for runOrchestration without deploying functions
import { runOrchestration } from './AgentOrchestrator.js';

(async () => {
  const mockPosts = [
    { id: 'test_1', platform: 'instagram', caption: 'Radiesse biostimulator for collagen stimulation and firm smooth results', hashtags: ['#radiesse','#collagen','#firmandsmooth'] },
    { id: 'test_2', platform: 'instagram', caption: 'SkinTyte non-surgical butt lift tightening treatment in Toronto aesthetics clinic', hashtags: ['#skintyte','#torontoaesthetics'] },
    { id: 'test_3', platform: 'tiktok', caption: 'Ultherapy duo-c-lift tightening collagen stimulation', hashtags: ['#ultherapy','#duoclift'] },
    { id: 'test_4', platform: 'tiktok', caption: 'Vivier vitamin c medical grade skincare brightening protocol', hashtags: ['#vivierskin','#medicalgradeskincare'] }
  ];
  const result = await runOrchestration({ posts: mockPosts });
  console.log('Platform counts:', result.platformCounts);
  console.log('CSE summary:', result.cse);
  console.log('Enriched posts count:', result.enrichedPosts);
  console.log('Full orchestration result:', JSON.stringify(result, null, 2));
})();
