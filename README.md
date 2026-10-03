# HR 평가시스템 문의 자동 트리아지

- 메인 화면
<img width="2202" height="1628" alt="image" src="https://github.com/user-attachments/assets/51d77a4a-23ab-4aeb-bbf8-aef68ca563dc" />

- 상세 화면
- 처리 실행을 통해 문의 정규화 +  키워드 검색 + 답변초안 생성
- 메뉴얼 업데이트 이후 재처리 진행시 다시 진행
<img width="2226" height="1520" alt="image" src="https://github.com/user-attachments/assets/45762528-ac25-4e8d-9f22-54f0b8a22963" />

---

- 메뉴얼 관리
  
<img width="2204" height="1070" alt="image" src="https://github.com/user-attachments/assets/164eb319-f13d-4571-911d-4eb5e511d539" />

- 메뉴얼 수정
<img width="1080" height="1133" alt="image" src="https://github.com/user-attachments/assets/b56aa5da-25d3-44cd-893c-937cf269b7f4" />


## 왜 만들었나
HR 평가시스템(성과/역량/다면/종합평가)을 여러 고객사(B2B SaaS)에 운영하다 보면
"오류인지 단순 문의인지", "어떤 평가 유형 건인지"를 담당자가 문의 원문을 읽고
매번 수작업으로 분류한 뒤 관련 매뉴얼을 찾아 답변을 작성하게 됩니다. 문의량이
많아질수록 이 반복 작업이 병목이 됩니다.

이 프로젝트는 문의 원문을 LLM으로 자동 분류·요약하고, 매뉴얼을 검색해 답변
초안까지 생성하는 파이프라인을 자동화합니다.

## 현재 구현 상태
- ✅ 고객사(Tenant)별 문의 등록/조회 API
- ✅ Claude 정규화: 오류 여부, 평가 유형(5종) 분류, 핵심 증상 요약, 검색 키워드 추출
- ✅ 매뉴얼 검색: 임베딩 없이 평가유형 필터 + 키워드 매칭으로 관련 조항 검색
- ✅ Claude 답변 합성: 분류 결과 + 검색된 매뉴얼을 근거로 답변 초안 생성
- ✅ 고객사별/평가유형별 집계 통계 API
- ✅ Next.js 프론트엔드: 문의 등록 폼, 목록/상세(처리 결과 전체) 보기, 처리 실행 버튼

## 무엇을 하는가 (파이프라인)
1. 자유 형식 문의 텍스트 등록 (`POST /api/inquiries`)
2. `POST /api/inquiries/{id}/process` 호출 시 3단계가 순서대로 실행됨:
   1. **정규화** — Claude가 오류/문의 여부, 평가유형, 핵심증상, 검색 키워드 추출
   2. **검색** — 같은 평가유형의 매뉴얼 중 키워드가 겹치는 상위 3건을 점수화해서 선택
   3. **합성** — Claude가 (분류 결과 + 검색된 매뉴얼)을 근거로 고객 응대용 답변 초안 생성
3. 결과(`InquiryResult`)는 문의 1건당 1개로 유지되며, 재처리 시 새로 쌓지 않고 갱신됨

## 왜 임베딩(RAG) 대신 키워드 검색을 썼나
원래 계획은 OpenAI 임베딩 + pgvector로 벡터 유사도 검색을 하는 것이었지만, 다음
이유로 **Claude 하나만 쓰는 키워드 기반 검색**으로 단순화했습니다.

- Anthropic(Claude)은 자체 임베딩 API를 제공하지 않습니다. 임베딩까지 하려면
  OpenAI 같은 별도 제공자가 필요했는데, 이 프로젝트는 비용/구성 최소화가 목표라
  제공자를 하나로 유지하기로 했습니다.
- 매뉴얼 데이터 규모가 평가유형당 수십 건 이하로 작아서, 벡터 유사도 없이도
  "평가유형으로 1차 필터 → 정규화 단계에서 뽑은 키워드가 제목/본문에 포함되는지로
  점수화" 정도로 충분히 관련 조항을 찾을 수 있었습니다. (`ManualSearchService`)
- 매뉴얼이 수백 건 이상으로 늘어나면 이 방식은 한계가 있으므로, 그때는 임베딩
  검색으로 교체하는 것을 향후 과제로 남겨둡니다.

## API
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/tenants` | 고객사 목록 조회 |
| GET | `/api/inquiries` | 문의 목록 조회 (수신일 역순, 처리 결과 요약 포함) |
| GET | `/api/inquiries/{id}` | 문의 상세 조회 (정규화/검색/합성 결과 전체 포함) |
| POST | `/api/inquiries` | 문의 등록 |
| POST | `/api/inquiries/{id}/process` | 정규화 → 검색 → 합성 파이프라인 실행 |
| GET | `/api/stats` | 전체/고객사별/평가유형별 집계 통계 |

## 도메인 모델
- **Tenant**: 고객사. 평가 정책 메모(`evaluationPolicyNote`)를 텍스트로 보유
- **Inquiry**: 고객사에 속한 문의 원문. 상태는 `PENDING → PROCESSED`
- **InquiryResult**: 문의 1건당 1개, 파이프라인 처리 결과(오류 여부, 평가유형, 증상 요약,
  검색 키워드, 매칭된 매뉴얼 제목, 최종 답변)를 누적 갱신
- **ManualDoc**: 고객 응대용 매뉴얼 조항. 평가유형 + 제목/본문/검색 키워드로 구성

## 왜 이렇게 설계했나 (기술적 의사결정)
- **정규화 → 검색 → 합성 3단계로 분리**: 사람마다 문의 표현이 제각각이라(오탈자, 줄임말,
  두서없는 서술) 원문을 그대로 검색에 쓰면 정확도가 떨어집니다. LLM으로 먼저 핵심
  증상·키워드를 정제한 뒤 검색하고, 검색 결과를 다시 LLM에 근거로 주는 식으로 나눠서
  각 단계를 독립적으로 디버깅할 수 있게 했습니다. (실제로 검색 단계가 항상 빈 결과를
  내던 문제를, 파이프라인이 단계별로 나뉘어 있어서 "정규화는 정상, 매뉴얼 시딩이
  안 된 것"이라고 바로 특정할 수 있었습니다.)
- **Claude 단일 제공자**: 분류/검색 점수화/생성 모두 별도 임베딩 제공자 없이 Claude와
  일반 텍스트 매칭만으로 구성해 의존성과 설정을 최소화했습니다.
- **멀티테넌트 구조**: 실제 B2B SaaS 운영을 가정해 고객사(Tenant)마다 평가 정책이
  달라질 수 있다는 점을 반영했습니다. (`Tenant.evaluationPolicyNote`)
- **InquiryResult를 upsert 방식으로 갱신**: 재처리 시 새 레코드를 쌓지 않고 기존 결과를
  갱신(`updateFromNormalization`)해 문의 1건당 결과가 항상 1개로 유지되도록 했습니다.
- **프론트에서 백엔드 직접 호출 시 CORS 없이 처리**: 초기 데이터 로딩(목록 조회)은
  Next.js 서버 컴포넌트가 서버 대 서버로 직접 fetch하고, 폼 제출/처리 실행처럼 브라우저에서
  나가는 요청은 `next.config.js`의 `rewrites`로 `/api/*`를 백엔드로 프록시해, Spring
  쪽에 별도 CORS 설정을 추가하지 않고도 동작하게 했습니다.

## 아키텍처
```
[Browser: Next.js 클라이언트 컴포넌트]
   │  fetch('/api/inquiries/{id}/process')  ── same-origin
   ▼
[Next.js dev server] ── next.config.js rewrites ──▶ [Spring Boot :8080]
                                                          │
                                            [InquiryApiController]
                                                          ▼
                        [InquiryProcessService] 오케스트레이션
                                │
       (1) 정규화 ──▶ [InquiryNormalizeService] ──▶ Claude(Anthropic)
                                │
       (2) 검색   ──▶ [ManualSearchService] ──▶ PostgreSQL(manual_doc, 키워드 매칭)
                                │
       (3) 합성   ──▶ [AnswerSynthesisService] ──▶ Claude(Anthropic)
                                ▼
                     [InquiryResult] 저장 (PostgreSQL)
```

## 기술 스택
- Backend: Spring Boot 4.1.1, Java 21, Spring Data JPA, Spring AI 2.0.1
- DB: PostgreSQL (`pgvector/pgvector:pg16` 이미지지만 현재는 일반 Postgres로만 사용)
- AI: Claude(`claude-sonnet-4-6`) 하나만 사용 — 정규화/검색 점수화/답변 합성 전부
- Frontend: Next.js 15 (App Router), React 19, TypeScript

## 프로젝트 구조
```
demo/
├── src/                  # Spring Boot 백엔드 (루트에 위치)
├── build.gradle
├── docker-compose.yml    # PostgreSQL
└── front/                # Next.js 프론트엔드
    └── src/app/
```

## 실행 방법
```bash
# 1. DB
docker compose up -d

# 2. 백엔드 (프로젝트 루트에서)
CLAUDE_API_KEY=sk-ant-여기에실제키 ./gradlew bootRun

# 3. 프론트엔드 (front/ 에서, 별도 터미널)
cd front
npm install
npm run dev
```
- `http://localhost:3000` 접속 → 문의 등록/목록/처리 실행 UI
- Anthropic API 키는 [console.anthropic.com](https://console.anthropic.com)에서 발급
  (Billing에 결제수단 등록 필요). **`application.yaml`에 직접 하드코딩하지 말고 항상
  환경변수로 주입하세요** — 이 파일은 git에 커밋되는 파일입니다.
- 기본 접속 DB: `inquiry_triage` (user: `dev` / password: `dev1234`, `docker-compose.yml` 참고)

### 매뉴얼(`manual_doc`) 데이터 추가하기
`data.sql`은 **최초 세팅 참고용**일 뿐, 서버를 껐다 켜도 자동 반영되지 않습니다
(`spring.sql.init.mode`가 꺼져 있어서 외부 DB에는 실행되지 않고, 켜더라도
`tenant`/`inquiry`가 중복 삽입되는 문제가 있어 계속 꺼둔 상태). 매뉴얼을 추가하고
싶으면 psql이나 DBeaver 등으로 직접 넣으세요:
```bash
docker exec -it inquiry-triage-db psql -U dev -d inquiry_triage
```
```sql
INSERT INTO manual_doc (evaluation_type, title, content, keywords, created_at)
VALUES ('COMMON', '제목', '본문', '키워드1, 키워드2', now());
```
`evaluation_type`은 반드시 `PERFORMANCE`, `COMPETENCY`, `MULTI_RATER`,
`COMPREHENSIVE`, `COMMON` 중 하나여야 합니다 (Java enum과 이름이 정확히 일치해야 함).

## 트러블슈팅 기록
- **`ddl-auto: update`가 기존 CHECK 제약을 안 고쳐줌**: `EvaluationType` enum에
  `COMMON`을 나중에 추가했는데, 테이블이 그 이전에 이미 만들어져 있어서 DB의
  `inquiry_result_evaluation_type_check` 제약이 `COMMON`을 막고 있었습니다.
  `ddl-auto: update`는 새 컬럼/테이블은 만들어도 기존 제약조건은 건드리지
  않는다는 걸 직접 겪었습니다. → 제약조건을 수동으로 `DROP`해서 해결.
- **Claude 호출 단계에서만 500 에러**: `CLAUDE_API_KEY` 환경변수 없이 서버를
  띄우면, DB만 쓰는 조회 API는 멀쩡히 동작하다가 `/process`(Claude를 호출하는
  유일한 엔드포인트)에서만 500이 났습니다. 원인 파악을 쉽게 하려고
  `server.error.include-message: always`를 추가해 응답에 실제 에러 메시지가
  보이도록 했습니다 (운영 배포 전엔 꺼야 함).
- **git에서 삭제된 파일이 로컬엔 남아있던 문제**: 로컬 디스크엔 `config/`,
  `service/` 등 패키지가 있는데 원격(GitHub)엔 없던 적이 있었습니다. 원인은
  과거 커밋에서 해당 파일들이 git 추적에서 `삭제`로 기록된 채 그대로
  푸시되어 있었고, 삭제 이후 다시 커밋된 적이 없었던 것이었습니다 (`git show
  --stat <commit>`으로 확인). 로컬에 파일이 "보인다"는 것과 "git에 커밋되어
  있다"는 것은 별개라는 걸 확인한 사례입니다.

## 향후 개선 방향
- 매뉴얼 데이터가 많아지면 임베딩 기반 검색으로 교체 검토
- 매뉴얼 등록을 위한 관리자 API (지금은 psql로 직접 insert)
- 필요하면 `src/`를 `backend/`로 정식 이동해 프론트/백엔드 구조 대칭으로 정리
- 통계 대시보드 프론트엔드
- 그룹웨어 등 타 업무 영역으로 확장 가능성 검토
