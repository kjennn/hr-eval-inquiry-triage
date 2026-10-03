"use client";

import { useRouter } from "next/navigation";
import { MouseEvent, useState } from "react";

export default function ProcessButton({
  id,
  reprocess = false,
  size = "sm",
}: {
  id: number;
  reprocess?: boolean;
  size?: "sm" | "md";
}) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function run(e: MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`/api/inquiries/${id}/process`, { method: "POST" });
      if (!res.ok) {
        const body = await res.json().catch(() => null);
        throw new Error(body?.message ?? `처리 실패 (${res.status})`);
      }
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "알 수 없는 오류");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <button
        className={`btn-ios ${reprocess ? "gray" : ""} ${size === "sm" ? "sm" : ""}`}
        onClick={run}
        disabled={loading}
      >
        {loading ? "분석 중..." : reprocess ? "재처리" : "AI 분석 실행"}
      </button>
      {error && <div style={{ color: "#ff3b30", fontSize: "0.78rem", marginTop: 6 }}>{error}</div>}
    </div>
  );
}
