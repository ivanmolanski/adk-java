// Test frontend-backend connection
const axios = require('axios');

async function testConnection() {
  console.log('Testing Frontend-Backend Connection...\n');
  
  try {
    // Test health endpoint
    console.log('1. Testing health endpoint...');
    const healthResponse = await axios.get('http://localhost:3453/viral-service/api/v1/health');
    console.log('✅ Health check:', healthResponse.data);
    
    // Test trends endpoint
    console.log('\n2. Testing trends endpoint...');
    const trendsResponse = await axios.get('http://localhost:3453/viral-service/api/v1/viral/trends?limit=2');
    console.log('✅ Trends data:', trendsResponse.data.length, 'trends found');
    console.log('   Sample trend:', trendsResponse.data[0]?.hook?.substring(0, 50) + '...');
    
    // Test drafts endpoint
    console.log('\n3. Testing drafts endpoint...');
    const draftsResponse = await axios.get('http://localhost:3453/viral-service/api/v1/viral/drafts?limit=2');
    console.log('✅ Drafts data:', draftsResponse.data.length, 'drafts found');
    console.log('   Sample draft:', draftsResponse.data[0]?.caption?.substring(0, 50) + '...');
    
    // Test frontend health
    console.log('\n4. Testing frontend...');
    const frontendResponse = await axios.get('http://localhost:3001');
    console.log('✅ Frontend responding:', frontendResponse.status === 200 ? 'OK' : 'ERROR');
    
    console.log('\n🎉 All tests passed! Frontend-Backend connection is working.');
    
  } catch (error) {
    console.error('❌ Connection test failed:', error.message);
    if (error.response) {
      console.error('   Response status:', error.response.status);
      console.error('   Response data:', error.response.data);
    }
  }
}

testConnection();