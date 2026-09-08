# HR 평가시스템 문의 자동 트리아지 & RAG 어시스턴트

## 왜 만들었나
- 실제 운영 중인 상황 한두 문장 (예: 매일 수십 건씩 들어오는 평가 관련 문의를
  담당자가 일일이 읽고 분류/원인추정까지 수작업으로 처리하는 비효율)
- 이 프로젝트가 해결하려는 문제 정의

## 무엇을 하는가
1. 자유 형식 문의 텍스트 입력
2. Claude가 오류/문의 여부 + 평가유형(성과/역량/다면/종합/공통) 분류 + 핵심증상 요약
3. 요약된 키워드로 벡터DB(pgvector)에서 관련 매뉴얼 조항 검색 (RAG)
4. Claude가 (분류결과 + 검색된 매뉴얼)을 종합해 답변 초안 생성

## 왜 이렇게 설계했나 (기술적 의사결정)
- 정규화 → 검색 → 합성 3단계로 분리한 이유 (사람마다 표현이 달라도 검색 정확도 확보)
- 임베딩(OpenAI)과 생성(Claude)을 분리한 이유
- pgvector를 선택한 이유 (관계형 데이터 + 벡터 검색을 한 DB에서)
- 멀티테넌트 구조로 설계한 이유 (실제 B2B SaaS 운영 경험 반영)

## 아키텍처
[다이어그램 이미지 or 텍스트 다이어그램]

## 기술 스택
- Backend: Spring Boot 3.x, Java 21, JPA, Spring AI
- DB: PostgreSQL + pgvector
- AI: Claude(분류/요약/생성), OpenAI Embedding(text-embedding-3-small)
- Frontend: Next.js

## 실행 방법
docker compose up -d
./gradlew bootRun
(환경변수: CLAUDE_API_KEY, OPENAI_API_KEY)

## 트러블슈팅 기록
- 패키지 스캔 범위 문제, ChatModel 빈 충돌 등 실제 겪은 이슈와 해결 과정
  (면접에서 "어떤 문제를 겪었고 어떻게 해결했나"에 바로 답할 수 있는 자료가 됨)

## 향후 개선 방향
- Next.js 프론트/통계 대시보드 (진행중이면 명시)
- 그룹웨어 등 타 업무 영역 확장 가능성
