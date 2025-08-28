import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  webpack: (config, { isServer }) => {
    // Exclude Firebase Functions code from Next.js build
    config.externals = [...(config.externals || []), 'firebase-functions'];
    
    return config;
  },
  // Explicitly set output to 'export' for static hosting
  output: 'export'
};

export default nextConfig;
