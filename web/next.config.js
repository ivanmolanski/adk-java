/** @type {import('next').NextConfig} */
const nextConfig = {
  /* config options here */
  webpack: (config, { isServer }) => {
    // Exclude Firebase Functions code from Next.js build
    config.externals = [...(config.externals || []), 'firebase-functions'];
    
    return config;
  }
};

module.exports = nextConfig;
