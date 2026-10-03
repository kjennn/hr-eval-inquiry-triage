import { EVALUATION_TYPE_LABEL, type EvaluationType } from "@/lib/api";

// 평가유형별 색상은 globals.css의 .type-* 클래스에서 정의합니다.
export default function TypeBadge({ type }: { type: EvaluationType | null | undefined }) {
  if (!type) {
    return <span className="type-badge type-NONE">분류 전</span>;
  }
  return <span className={`type-badge type-${type}`}>{EVALUATION_TYPE_LABEL[type]}</span>;
}
