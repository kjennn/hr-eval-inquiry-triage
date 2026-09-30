# HR 평가시스템 문의 자동 트리아지 & RAG 어시스턴트

## 왜 만들었나
HR 평가시스템(성과/역량/다면/종합평가)을 여러 고객사(B2B SaaS)에 운영하다 보면
"오류인지 단순 문의인지", "어떤 평가 유형 건인지"를 담당자가 문의 원문을 읽고
매번 수작업으로 분류한 뒤 관련 매뉴얼을 찾아 답변을 작성하게 됩니다. 문의량이
많아질수록 이 반복 작업이 병목이 됩니다.

이 프로젝트는 문의 원문을 LLM으로 자동 분류·요약하고, 이후 RAG로 매뉴얼을 검색해
답변 초안까지 생성하는 파이프라인을 만들어 이 과정을 자동화하는 것을 목표로 합니다.

## 현재 구현 상태
- ✅ 고객사(Tenant)별 문의 등록/조회 API
- ✅ Claude를 이용한 문의 정규화(1단계): 오류 여부, 평가 유형(5종) 분류, 핵심 증상 한 줄 요약, 검색 키워드 추출
- ✅ 고객사별/평가유형별 집계 통계 API
- ⏳ (미구현) pgvector 기반 매뉴얼 벡터 검색(RAG) — `InquiryResult.retrievedManualRefs`는 현재 항상 `null`
- ⏳ (미구현) 검색 결과 + 분류 결과를 합성한 최종 답변 생성 — `InquiryResult.finalAnswer`는 현재 항상 `null`
- ⏳ (미구현) Next.js 프론트엔드

## 무엇을 하는가 (목표 파이프라인)
1. 자유 형식 문의 텍스트 등록 (`POST /api/inquiries`)
2. Claude가 오류/문의 여부 + 평가유형(성과/역량/다면/종합/공통) 분류 + 핵심증상 요약 + 검색 키워드 추출 (`POST /api/inquiries/{id}/process`, 구현됨)
3. 요약된 키워드로 벡터DB(pgvector)에서 관련 매뉴얼 조항 검색 (RAG, 미구현)
4. Claude가 (분류결과 + 검색된 매뉴얼)을 종합해 답변 초안 생성 (미구현)

## API
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/tenants` | 고객사 목록 조회 |
| GET | `/api/inquiries` | 문의 목록 조회 (수신일 역순, 처리 결과 요약 포함) |
| GET | `/api/inquiries/{id}` | 문의 상세 조회 |
| POST | `/api/inquiries` | 문의 등록 |
| POST | `/api/inquiries/{id}/process` | 문의 정규화 파이프라인 실행(Claude 호출) |
| GET | `/api/stats` | 전체/고객사별/평가유형별 집계 통계 |

## 도메인 모델
- **Tenant**: 고객사. 평가 정책 메모(`evaluationPolicyNote`)를 텍스트로 보유
- **Inquiry**: 고객사에 속한 문의 원문. 상태는 `PENDING → PROCESSED`
- **InquiryResult**: 문의 1건당 1개, 파이프라인 처리 결과(오류 여부, 평가유형, 증상 요약, 검색 키워드, 매뉴얼 참조, 최종 답변)를 누적 갱신

## 왜 이렇게 설계했나 (기술적 의사결정)
- **정규화 → 검색 → 합성 3단계로 분리**: 사람마다 문의 표현이 제각각이라(오탈자, 줄임말, 두서없는 서술) 원문을 그대로 벡터 검색에 넣으면 정확도가 떨어집니다. LLM으로 먼저 핵심 증상·키워드를 정제한 뒤 검색해 정확도를 높이려는 의도입니다.
- **임베딩(OpenAI)과 생성(Claude)을 분리**: 임베딩은 비용이 저렴하고 표준화된 모델(`text-embedding-3-small`)을 쓰고, 분류/요약/생성처럼 맥락 이해가 필요한 작업은 Claude를 쓰는 식으로 역할을 나눴습니다.
- **pgvector 선택**: 문의/고객사 같은 정형 데이터와 매뉴얼 임베딩(벡터)을 같은 PostgreSQL 안에서 조인해 다룰 수 있어, 별도 벡터DB를 운영하지 않아도 됩니다.
- **멀티테넌트 구조**: 실제 B2B SaaS 운영을 가정해 고객사(Tenant)마다 평가 정책이 달라질 수 있다는 점을 반영했습니다. (`Tenant.evaluationPolicyNote`)
- **InquiryResult를 upsert 방식으로 갱신**: 재처리 시 새 레코드를 쌓지 않고 기존 결과를 갱신(`updateFromNormalization`)해 문의 1건당 결과가 항상 1개로 유지되도록 했습니다.

## 아키텍처
```
[Client]
   │  POST /api/inquiries/{id}/process
   ▼
[InquiryApiController]
   ▼
[InquiryProcessService] ── (1) 정규화 ──▶ [InquiryNormalizeService] ──▶ Claude(Anthropic)
   │
   ├─ (2) RAG 검색  [미구현] ──▶ pgvector (매뉴얼 임베딩)
   └─ (3) 답변 합성 [미구현] ──▶ Claude(Anthropic)
   ▼
[InquiryResult] 저장 (PostgreSQL)
```

## 기술 스택
- Backend: Spring Boot 4.1.1, Java 21, Spring Data JPA, Spring AI 2.0.1
- DB: PostgreSQL + pgvector (`pgvector/pgvector:pg16` 이미지)
- AI: Claude(`claude-sonnet-4-6`, 분류/요약/생성), OpenAI Embedding(`text-embedding-3-small`, 검색용 벡터화 — 파이프라인 미연결)
- Frontend: Next.js (예정, 아직 미착수)

## 실행 방법
```bash
docker compose up -d          # PostgreSQL + pgvector 기동 (localhost:5432)
export CLAUDE_API_KEY=...     # Anthropic API 키
export OPENAI_API_KEY=...     # OpenAI API 키 (임베딩용, 현재 파이프라인엔 미연결)
./gradlew bootRun
```
- 기본 접속 DB: `inquiry_triage` (user: `dev` / password: `dev1234`, `docker-compose.yml` 참고)
- 애플리케이션 기동 시 `data.sql`로 고객사 3곳(A전자/B물산/C시스템)과 예시 문의 30여 건이 자동 시딩됩니다.

## 향후 개선 방향
- pgvector 매뉴얼 임베딩/검색 서비스 구현 후 `InquiryProcessService`에 연결
- 분류 결과 + 검색된 매뉴얼을 합성해 최종 답변(`finalAnswer`) 생성
- Next.js 프론트엔드 및 통계 대시보드
- 그룹웨어 등 타 업무 영역으로 확장 가능성 검토
