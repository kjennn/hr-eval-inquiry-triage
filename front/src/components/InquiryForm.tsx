"use client";

import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";
import type { Tenant } from "@/lib/api";

export default function InquiryForm({
  tenants,
  onDone,
}: {
  tenants: Tenant[];
  onDone: () => void;
}) {
  const router = useRouter();
  const [tenantId, setTenantId] = useState<string>(tenants[0] ? String(tenants[0].id) : "");
  const [rawContent, setRawContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const res = await fetch("/api/inquiries", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ tenantId: Number(tenantId), rawContent }),
      });
      if (!res.ok) {
        throw new Error(`등록 실패 (${res.status})`);
      }
      setRawContent("");
      router.refresh();
      onDone();
    } catch (err) {
      setError(err instanceof Error ? err.message : "알 수 없는 오류");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={onSubmit}>
      <h4 style={{ fontWeight: 800, letterSpacing: "-0.02em" }}>새 문의 등록</h4>
      {tenants.length === 0 && (
        <p style={{ color: "#b36b00" }}>등록된 고객사가 없습니다. DB의 tenant 데이터를 확인하세요.</p>
      )}
      <div className="mb-3 mt-3">
        <div className="card-label">고객사</div>
        <select
          className="ios-input"
          value={tenantId}
          onChange={(e) => setTenantId(e.target.value)}
          required
        >
          {tenants.map((t) => (
            <option key={t.id} value={t.id}>
              {t.name}
            </option>
          ))}
        </select>
      </div>
      <div className="mb-3">
        <div className="card-label">문의 내용</div>
        <textarea
          className="ios-input"
          rows={5}
          value={rawContent}
          onChange={(e) => setRawContent(e.target.value)}
          placeholder="예) 다면평가 결과가 안 보여요. 평가 기간이 끝났는데 점수가 0으로 나옵니다."
          required
        />
      </div>
      {error && <p style={{ color: "#ff3b30" }}>{error}</p>}
      <div className="d-flex justify-content-end gap-2">
        <button type="button" className="btn-ios gray" onClick={onDone}>
          취소
        </button>
        <button className="btn-ios" disabled={submitting || tenants.length === 0}>
          {submitting ? "등록 중..." : "등록"}
        </button>
      </div>
    </form>
  );
}
