package com.inquirytriage.domain.inquiry.service;

/**
 * <pre>
 * com.inquirytriage.domain.inquiry.service
 *
 * Description :
 * </pre>
 *
 * @author gwonjieun
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 * 수정일     수정자   수정내용
 * ---------- -------- -------------------
 * 26. 9. 8.   gwonjieun   최초 생성
 * </pre>
 * @since 26. 9. 8.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InquiryNormalizeService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public InquiryNormalizeService(ChatClient builder, ObjectMapper objectMapper) {
        this.chatClient = builder;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
        너는 B2B SaaS 인사평가 시스템의 문의 트리아지 어시스턴트다.
        이 시스템은 4가지 평가 유형을 다룬다:
        - PERFORMANCE(성과평가): 목표 수립, 목표 입력/수정, 진행상태 변경, 업적평가 업로드 관련
        - COMPETENCY(역량평가): 상사평가, 평가자 지정/삭제, 점수 입력 오류
        - MULTI_RATER(다면평가): 대상자/피평가자 명단 업로드, 평가자 배정 누락
        - COMPREHENSIVE(종합평가): 등급 부여, 평균/편차보정 로직
        - COMMON(공통/시스템): 비밀번호, 로그인, 조직/구성원 세팅, HRM 동기화 등 평가 유형과 무관한 시스템 문의

        문의 내용을 읽고 다음을 JSON으로만 응답하라. 다른 설명은 절대 붙이지 마라.
        {
          "isError": boolean,               // 오류 신고인지, 단순 문의/요청인지
          "evaluationType": "PERFORMANCE|COMPETENCY|MULTI_RATER|COMPREHENSIVE|COMMON",
          "symptomSummary": "핵심 증상을 한 문장으로 요약 (한국어)",
          "searchKeywords": ["매뉴얼 검색에 쓸 키워드 2~4개"]
        }
        """;

    public InquiryNormalizationResult normalize(String rawContent) {
        String json = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(rawContent)
                .call()
                .content();

        return parseJson(json);
    }

    private InquiryNormalizationResult parseJson(String raw) {
        String cleaned = raw.replaceAll("```json|```", "").trim();
        try {
            return objectMapper.readValue(cleaned, InquiryNormalizationResult.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Claude 응답 파싱 실패: " + cleaned, e);
        }
    }
}