const admin = require('firebase-admin');

// Initialize Firebase Admin with service account
const serviceAccount = require('./service-account.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'contentforge-ai-ygy25'
});

const db = admin.firestore();

async function testFirebaseFunctions() {
  console.log('🔥 Testing Firebase Functions workflow...');
  
  // Step 1: Call the orchestrateDailyRun function via HTTPS
  console.log('\n1. Triggering orchestrateDailyRun function...');
  
  try {
    const response = await fetch('https://us-central1-contentforge-ai-ygy25.cloudfunctions.net/orchestrateDailyRun', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ data: {} })
    });
    
    const result = await response.json();
    console.log('✅ Function triggered successfully:', result);
    
    // Step 2: Wait a bit and check Firestore for scraped data
    console.log('\n2. Waiting 10 seconds for scraping to complete...');
    await new Promise(resolve => setTimeout(resolve, 10000));
    
    // Step 3: Check viral_research collection
    console.log('\n3. Checking viral_research collection...');
    const today = new Date().toISOString().slice(0, 10);
    const researchSnapshot = await db.collection('viral_research').doc(today).collection('posts').limit(5).get();
    
    if (!researchSnapshot.empty) {
      console.log(`✅ Found ${researchSnapshot.size} research posts for ${today}:`);
      researchSnapshot.forEach(doc => {
        const data = doc.data();
        console.log(`   - Post ${doc.id}: EVS=${data.evs?.toFixed(2)}, Tag=${data.tag}`);
      });
    } else {
      console.log('⚠️ No research posts found yet - checking root viral_research...');
      const rootSnapshot = await db.collection('viral_research').limit(10).get();
      if (!rootSnapshot.empty) {
        console.log(`✅ Found ${rootSnapshot.size} research documents:`);
        rootSnapshot.forEach(doc => {
          const data = doc.data();
          console.log(`   - ${doc.id}: ${JSON.stringify(data).substring(0, 100)}...`);
        });
      } else {
        console.log('❌ No viral research data found');
      }
    }
    
    // Step 4: Check viral_outputs collection
    console.log('\n4. Checking viral_outputs collection...');
    const outputsSnapshot = await db.collection('viral_outputs').orderBy('createdAt', 'desc').limit(5).get();
    
    if (!outputsSnapshot.empty) {
      console.log(`✅ Found ${outputsSnapshot.size} output documents:`);
      outputsSnapshot.forEach(doc => {
        const data = doc.data();
        console.log(`   - ${doc.id}: ${data.preview?.hook || 'No hook'}`);
      });
    } else {
      console.log('❌ No viral outputs found yet');
    }
    
  } catch (error) {
    console.error('❌ Error testing functions:', error);
  }
}

// Run the test
testFirebaseFunctions().then(() => {
  console.log('\n🎉 Firebase Functions test completed!');
  process.exit(0);
}).catch(error => {
  console.error('💥 Test failed:', error);
  process.exit(1);
});