import Link from "next/link";
import InquiryBoard from "@/components/InquiryBoard";
import TypeBadge from "@/components/TypeBadge";
import { apiGet, type InquiryListItem, type Stats, type Tenant } from "@/lib/api";

export const dynamic = "force-dynamic";

export default async function HomePage() {
  let tenants: Tenant[] = [];
  let inquiries: InquiryListItem[] = [];
  let stats: Stats | null = null;
  let loadError: string | null = null;

  try {
    [tenants, inquiries, stats] = await Promise.all([
      apiGet<Tenant[]>("/api/tenants"),
      apiGet<InquiryListItem[]>("/api/inquiries"),
      apiGet<Stats>("/api/stats"),
    ]);
  } catch (err) {
    loadError = err instanceof Error ? err.message : "알 수 없는 오류";
  }

  if (loadError) {
    return (
      <div className="ios-card" style={{ color: "#ff3b30" }}>
        백엔드에 연결할 수 없습니다. Spring Boot(:8080)가 실행 중인지 확인하세요.
        <div style={{ fontSize: "0.85rem", marginTop: 6 }}>{loadError}</div>
      </div>
    );
  }

  const pendingCount = inquiries.filter((q) => q.status === "PENDING").length;

  return (
    <>
      <div className="d-flex flex-wrap justify-content-between align-items-start gap-2">
        <div>
          <h1 className="page-title">문의 현황</h1>
          <p className="page-sub">고객사 문의를 AI가 분류하고 매뉴얼 기반 답변 초안을 만듭니다.</p>
        </div>
        <div className="d-flex gap-2">
          <Link href="/manuals" className="btn-ios gray">
            매뉴얼 관리
          </Link>
          <Link href="/manuals?new=1" className="btn-ios">
            + 매뉴얼 추가하기
          </Link>
        </div>
      </div>

      {stats && (
        <div className="stat-grid">
          <div className="ios-card">
            <div className="card-label">전체 문의</div>
            <div className="stat-value">{inquiries.length}</div>
          </div>
          <div className="ios-card">
            <div className="card-label">분석 대기</div>
            <div className="stat-value" style={{ color: "#ff9500" }}>
              {pendingCount}
            </div>
          </div>
          <div className="ios-card">
            <div className="card-label">오류 건수</div>
            <div className="stat-value" style={{ color: "#ff3b30" }}>
              {stats.errorCount}
            </div>
          </div>
          <div className="ios-card">
            <div className="card-label">평가유형별</div>
            {stats.byEvaluationType.length === 0 ? (
              <span style={{ color: "var(--muted)" }}>분석된 문의 없음</span>
            ) : (
              <div className="d-flex flex-wrap gap-2">
                {stats.byEvaluationType.map((t) => (
                  <span key={t.evaluationType} className="d-inline-flex align-items-center gap-1">
                    <TypeBadge type={t.evaluationType} />
                    <strong>{t.count}</strong>
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      <InquiryBoard inquiries={inquiries} tenants={tenants} />
    </>
  );
}
