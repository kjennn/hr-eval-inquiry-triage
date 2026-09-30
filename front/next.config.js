const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // 브라우저에서 호출하는 클라이언트 컴포넌트(폼 제출 등)가 CORS 없이
  // 백엔드를 호출할 수 있도록, 같은 origin의 /api/* 요청을 Spring으로 프록시합니다.
  async rewrites() {
    return [
      { source: "/api/:path*", destination: `${API_BASE}/api/:path*` },
    ];
  },
};

module.exports = nextConfig;
