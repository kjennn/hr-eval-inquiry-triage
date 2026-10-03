"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import InquiryForm from "./InquiryForm";
import ProcessButton from "./ProcessButton";
import TypeBadge from "./TypeBadge";
import {
  EVALUATION_TYPE_LABEL,
  formatDate,
  type EvaluationType,
  type InquiryListItem,
  type Tenant,
} from "@/lib/api";

type Filter = "ALL" | "NONE" | EvaluationType;

const FILTERS: { key: Filter; label: string }[] = [
  { key: "ALL", label: "전체" },
  { key: "PERFORMANCE", label: EVALUATION_TYPE_LABEL.PERFORMANCE },
  { key: "COMPETENCY", label: EVALUATION_TYPE_LABEL.COMPETENCY },
  { key: "MULTI_RATER", label: EVALUATION_TYPE_LABEL.MULTI_RATER },
  { key: "COMPREHENSIVE", label: EVALUATION_TYPE_LABEL.COMPREHENSIVE },
  { key: "COMMON", label: EVALUATION_TYPE_LABEL.COMMON },
  { key: "NONE", label: "분류 전" },
];

const FILTER_COLOR: Partial<Record<Filter, string>> = {
  PERFORMANCE: "var(--t-performance)",
  COMPETENCY: "var(--t-competency)",
  MULTI_RATER: "var(--t-multi)",
  COMPREHENSIVE: "var(--t-comprehensive)",
  COMMON: "var(--t-common)",
};

export default function InquiryBoard({
  inquiries,
  tenants,
}: {
  inquiries: InquiryListItem[];
  tenants: Tenant[];
}) {
  const router = useRouter();
  const [filter, setFilter] = useState<Filter>("ALL");
  const [sheetOpen, setSheetOpen] = useState(false);
  const [batchRunning, setBatchRunning] = useState(false);
  const [batchMsg, setBatchMsg] = useState<string | null>(null);

  const pending = inquiries.filter((q) => q.status === "PENDING");

  const visible = inquiries.filter((q) => {
    if (filter === "ALL") return true;
    if (filter === "NONE") return q.evaluationType == null;
    return q.evaluationType === filter;
  });

  // 미처리 문의를 순차 처리 (Claude 호출이라 동시 호출하지 않음)
  async function processAllPending() {
    setBatchRunning(true);
    setBatchMsg(null);
    let done = 0;
    for (const q of pending) {
      setBatchMsg(`분석 중... (${done + 1}/${pending.length})`);
      try {
        const res = await fetch(`/api/inquiries/${q.id}/process`, { method: "POST" });
        if (!res.ok) throw new Error(String(res.status));
        done++;
      } catch {
        setBatchMsg(`#${q.id} 처리 실패 — 중단했습니다. (${done}건 완료)`);
        setBatchRunning(false);
        router.refresh();
        return;
      }
    }
    setBatchMsg(`${done}건 분석 완료`);
    setBatchRunning(false);
    router.refresh();
  }

  return (
    <>
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <div className="segmented">
          {FILTERS.map((f) => (
            <button
              key={f.key}
              className={filter === f.key ? "active" : ""}
              style={{ ["--c" as string]: FILTER_COLOR[f.key] ?? "var(--text)" }}
              onClick={() => setFilter(f.key)}
            >
              {f.label}
            </button>
          ))}
        </div>
        <div className="d-flex align-items-center gap-2">
          {batchMsg && <span style={{ color: "var(--muted)", fontSize: "0.82rem" }}>{batchMsg}</span>}
          {pending.length > 0 && (
            <button className="btn-ios soft" onClick={processAllPending} disabled={batchRunning}>
              미처리 {pending.length}건 일괄 분석
            </button>
          )}
          <button className="btn-ios" onClick={() => setSheetOpen(true)}>
            + 새 문의
          </button>
        </div>
      </div>

      {visible.length === 0 ? (
        <div className="ios-card text-center" style={{ color: "var(--muted)", padding: 48 }}>
          해당하는 문의가 없습니다.
        </div>
      ) : (
        <div className="inq-grid">
          {visible.map((q) => {
            const [title, ...rest] = q.rawContent.split("\n");
            const body = rest.join(" ").trim();
            return (
              <Link
                key={q.id}
                href={`/inquiries/${q.id}`}
                className={`ios-card hover inq-card type-${q.evaluationType ?? "NONE"}`}
              >
                <div className="d-flex justify-content-between align-items-center gap-2 flex-wrap">
                  <TypeBadge type={q.evaluationType} />
                  <div className="d-flex gap-1">
                    {q.isError != null && (
                      <span className={`chip ${q.isError ? "chip-error" : "chip-ok"}`}>
                        {q.isError ? "오류" : "단순문의"}
                      </span>
                    )}
                    <span className={`chip ${q.status === "PROCESSED" ? "chip-done" : "chip-wait"}`}>
                      {q.status === "PROCESSED" ? "완료" : "대기"}
                    </span>
                  </div>
                </div>
                <h3 className="inq-title">{title}</h3>
                {body && <p className="inq-body">{body}</p>}
                <div className="inq-meta">
                  <span>
                    {q.tenantName} · {formatDate(q.receivedAt)}
                  </span>
                  <ProcessButton id={q.id} reprocess={q.status === "PROCESSED"} />
                </div>
              </Link>
            );
          })}
        </div>
      )}

      {sheetOpen && (
        <div className="sheet-backdrop" onClick={() => setSheetOpen(false)}>
          <div className="sheet" onClick={(e) => e.stopPropagation()}>
            <InquiryForm tenants={tenants} onDone={() => setSheetOpen(false)} />
          </div>
        </div>
      )}
    </>
  );
}
