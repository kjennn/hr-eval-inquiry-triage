import Link from "next/link";
import ManualManager from "@/components/ManualManager";
import { apiGet, type ManualDoc } from "@/lib/api";

export const dynamic = "force-dynamic";

export default async function ManualsPage({
  searchParams,
}: {
  searchParams: Promise<{ new?: string }>;
}) {
  const { new: openNew } = await searchParams;

  let manuals: ManualDoc[] = [];
  try {
    manuals = await apiGet<ManualDoc[]>("/api/manuals");
  } catch (err) {
    return (
      <div className="ios-card" style={{ color: "#ff3b30" }}>
        매뉴얼을 불러오지 못했습니다. 백엔드(:8080)가 실행 중인지 확인하세요.
        <div style={{ fontSize: "0.85rem", marginTop: 6 }}>
          {err instanceof Error ? err.message : ""}
        </div>
      </div>
    );
  }

  return (
    <>
      <Link href="/" style={{ color: "var(--accent)", fontWeight: 600 }}>
        ‹ 문의 현황
      </Link>
      <h1 className="page-title mt-3">매뉴얼 관리</h1>
      <p className="page-sub">
        여기서 저장한 매뉴얼은 다음 AI 분석부터 바로 검색 대상에 반영됩니다.
      </p>
      <ManualManager manuals={manuals} openNew={openNew === "1"} />
    </>
  );
}
