
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Configure Turbopack explicitly to avoid the "Webpack is configured while Turbopack is not" warning.
  // See: https://nextjs.org/docs/app/api-reference/next-config-js/turbopack
  turbopack: {
    root: __dirname,
    // Common aliases and extensions so Turbopack resolves modules predictably.
    resolveAlias: {
      // Map server-only package to a lightweight shim during client builds.
      'firebase-functions': './lib/firebase-functions-shim.js',
    },
    resolveExtensions: ['.tsx', '.ts', '.jsx', '.js', '.mjs', '.json'],
  },
  // Fix workspace root detection
  outputFileTracingRoot: __dirname,
  // If you need to exclude server-only packages from client bundles, prefer
  // using `serverExternalPackages` or conditional imports instead of a custom
  // webpack externals hook when running Turbopack.
};

export default nextConfig;
