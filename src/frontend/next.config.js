/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  images: {
    domains: ['localhost', 'hoanghamobile.com'],
  },
  env: {
    API_URL: process.env.API_URL || 'http://localhost:8080/api',
  },
}

module.exports = nextConfig
