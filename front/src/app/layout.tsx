import "bootstrap/dist/css/bootstrap.min.css";
import "./globals.css";
import Link from "next/link";
import type { ReactNode } from "react";

export const metadata = {
  title: "HR 평가시스템 문의 트리아지",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <header className="topbar">
          <div className="topbar-inner">
            <Link href="/" className="brand">
              <span className="brand-dot" />
              문의 트리아지
            </Link>
            <span style={{ color: "var(--muted)", fontSize: "0.85rem" }}>HR 평가시스템</span>
          </div>
        </header>
        <main className="page">{children}</main>
      </body>
    </html>
  );
}
