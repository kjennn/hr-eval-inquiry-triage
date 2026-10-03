import Link from "next/link";
import ProcessButton from "@/components/ProcessButton";
import TypeBadge from "@/components/TypeBadge";
import { apiGet, formatDate, type InquiryDetail } from "@/lib/api";

export const dynamic = "force-dynamic";

function StepHead({ n, title }: { n: number; title: string }) {
  return (
    <div className="step-head">
      <span className="step-num">{n}</span>
      <h5 className="step-title">{title}</h5>
    </div>
  );
}

export default async function InquiryDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  let inquiry: InquiryDetail;
  try {
    inquiry = await apiGet<InquiryDetail>(`/api/inquiries/${id}`);
  } catch (err) {
    return (
      <div className="ios-card" style={{ color: "#ff3b30" }}>
        문의를 불러오지 못했습니다. {err instanceof Error ? err.message : ""}
        <div className="mt-2">
          <Link href="/">← 목록으로</Link>
        </div>
      </div>
    );
  }

  const r = inquiry.result;
  const [title, ...rest] = inquiry.rawContent.split("\n");
  const body = rest.join("\n").trim();

  return (
    <>
      <Link href="/" style={{ color: "var(--accent)", fontWeight: 600 }}>
        ‹ 문의 현황
      </Link>

      <div className="ios-card mt-3 mb-3">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
          <div className="d-flex align-items-center gap-2">
            <TypeBadge type={r?.evaluationType} />
            {r && (
              <span className={`chip ${r.isError ? "chip-error" : "chip-ok"}`}>
                {r.isError ? "오류" : "단순문의"}
              </span>
            )}
            <span className={`chip ${inquiry.status === "PROCESSED" ? "chip-done" : "chip-wait"}`}>
              {inquiry.status === "PROCESSED" ? "완료" : "대기"}
            </span>
          </div>
          <ProcessButton id={inquiry.id} reprocess={inquiry.status === "PROCESSED"} size="md" />
        </div>
        <h2 className="page-title" style={{ fontSize: "1.5rem" }}>
          {title}
        </h2>
        <div className="page-sub" style={{ marginBottom: 12 }}>
          #{inquiry.id} · {inquiry.tenantName} · {formatDate(inquiry.receivedAt)}
        </div>
        {body && <p style={{ whiteSpace: "pre-wrap", margin: 0 }}>{body}</p>}
      </div>

      {!r ? (
        <div className="ios-card text-center" style={{ color: "var(--muted)", padding: 40 }}>
          아직 분석 전입니다. 우측 상단의 “AI 분석 실행”을 눌러주세요.
        </div>
      ) : (
        <div className="row g-3">
          <div className="col-12 col-lg-6">
            <div className="ios-card h-100">
              <StepHead n={1} title="정규화 결과" />
              <div className="kv">
                <span className="kv-k">핵심 증상</span>
                <span>{r.symptomSummary ?? "-"}</span>
              </div>
              <div className="kv">
                <span className="kv-k">검색 키워드</span>
                <span>
                  {r.searchKeywords.length === 0
                    ? "-"
                    : r.searchKeywords.map((k) => (
                        <span key={k} className="kw">
                          {k}
                        </span>
                      ))}
                </span>
              </div>
            </div>
          </div>
          <div className="col-12 col-lg-6">
            <div className="ios-card h-100">
              <StepHead n={2} title="검색된 매뉴얼" />
              {r.retrievedManualRefs.length === 0 ? (
                <span style={{ color: "var(--muted)" }}>매칭된 매뉴얼이 없습니다.</span>
              ) : (
                r.retrievedManualRefs.map((m) => (
                  <div key={m} className="kv" style={{ gridTemplateColumns: "1fr" }}>
                    {m}
                  </div>
                ))
              )}
            </div>
          </div>
          <div className="col-12">
            <div className="ios-card">
              <StepHead n={3} title="답변 초안" />
              <div className="answer-box">{r.finalAnswer ?? "-"}</div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
