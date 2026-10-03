// 서버 컴포넌트는 백엔드를 직접 호출하고, 브라우저(클라이언트 컴포넌트)는 /api/* 프록시를 씁니다.
export const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type EvaluationType =
  | "PERFORMANCE"
  | "COMPETENCY"
  | "MULTI_RATER"
  | "COMPREHENSIVE"
  | "COMMON";

export type InquiryStatus = "PENDING" | "PROCESSED";

export interface Tenant {
  id: number;
  name: string;
}

export interface InquiryListItem {
  id: number;
  tenantId: number;
  tenantName: string;
  rawContent: string;
  status: InquiryStatus;
  receivedAt: string;
  isError: boolean | null;
  evaluationType: EvaluationType | null;
}

export interface InquiryResult {
  isError: boolean;
  evaluationType: EvaluationType | null;
  symptomSummary: string | null;
  searchKeywords: string[];
  retrievedManualRefs: string[];
  finalAnswer: string | null;
}

export interface InquiryDetail {
  id: number;
  tenantId: number;
  tenantName: string;
  rawContent: string;
  status: InquiryStatus;
  receivedAt: string;
  result: InquiryResult | null;
}

export interface ManualDoc {
  id: number;
  evaluationType: EvaluationType;
  title: string;
  content: string;
  keywords: string | null;
  createdAt: string;
}

export interface Stats {
  totalCount: number;
  errorCount: number;
  byTenant: { tenantName: string; count: number; errorCount: number }[];
  byEvaluationType: { evaluationType: EvaluationType; count: number }[];
}

export const EVALUATION_TYPE_LABEL: Record<EvaluationType, string> = {
  PERFORMANCE: "성과평가",
  COMPETENCY: "역량평가",
  MULTI_RATER: "다면평가",
  COMPREHENSIVE: "종합평가",
  COMMON: "공통",
};

export async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, { cache: "no-store" });
  if (!res.ok) {
    throw new Error(`API ${path} 실패 (${res.status})`);
  }
  return res.json() as Promise<T>;
}

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleString("ko-KR");
}
