#!/usr/bin/env node

const https = require('https');
const http = require('http');
const { URL } = require('url');

function testEndpoint(url) {
  return new Promise((resolve, reject) => {
    const urlObj = new URL(url);
    const client = urlObj.protocol === 'https:' ? https : http;
    
    const options = {
      hostname: urlObj.hostname,
      port: urlObj.port,
      path: urlObj.pathname + urlObj.search,
      method: 'GET',
      timeout: 5000
    };

    const req = client.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        resolve({
          status: res.statusCode,
          headers: res.headers,
          data: data
        });
      });
    });

    req.on('error', (err) => {
      reject(err);
    });

    req.on('timeout', () => {
      req.destroy();
      reject(new Error('Request timeout'));
    });

    req.end();
  });
}

async function main() {
  const endpoints = [
    'http://localhost:3453/viral-service/api/v1/health',
    'http://localhost:3453/viral-service/api/v1/ai/health',
    'http://10.0.10.16:3453/viral-service/api/v1/health',
  ];

  console.log('=== Network Connectivity Test ===\n');

  for (const url of endpoints) {
    try {
      console.log(`Testing: ${url}`);
      const result = await testEndpoint(url);
      console.log(`  Status: ${result.status}`);
      if (result.status === 200) {
        try {
          const json = JSON.parse(result.data);
          console.log(`  Response: ${JSON.stringify(json, null, 2)}`);
        } catch {
          console.log(`  Response: ${result.data}`);
        }
      } else {
        console.log(`  Error: ${result.data}`);
      }
    } catch (error) {
      console.log(`  Failed: ${error.message}`);
    }
    console.log();
  }
}

main().catch(console.error);