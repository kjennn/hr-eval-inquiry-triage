package com.inquirytriage.domain.inquiry.service;

import com.inquirytriage.domain.inquiry.entity.ManualDoc;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 정규화 결과 + 검색된 매뉴얼 조항을 합성해 고객 응대용 답변 초안을 생성.
 * 파이프라인 3단계(정규화 → 검색 → 합성) 중 마지막 단계이며, 정규화와
 * 마찬가지로 Claude(ChatClient) 하나만 사용합니다.
 */
@Service
public class AnswerSynthesisService {

    private final ChatClient chatClient;

    public AnswerSynthesisService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    private static final String SYSTEM_PROMPT = """
        너는 B2B SaaS 인사평가 시스템의 고객 지원 답변 작성 어시스턴트다.
        아래 문의, 분류 정보, 참고 매뉴얼 조항을 바탕으로 고객에게 바로 보낼 수 있는
        답변 초안을 한국어로 작성하라.

        규칙:
        - 참고 매뉴얼에 근거가 있는 내용만 사실처럼 answer에 담아라. 매뉴얼에 없는
          내용은 추측하지 말고, "담당자 확인이 필요합니다" 라고 안내하라.
        - 참고 매뉴얼이 비어 있으면, 일반적인 안내와 함께 담당자 확인이 필요함을 명시하라.
        - 정중하고 간결한 고객 응대 톤으로 작성하라.
        - 다른 설명 없이 답변 본문만 출력하라 (인사말/서명 포함 가능).
        """;

    public String synthesize(String rawContent, InquiryNormalizationResult normalization, List<ManualDoc> matchedManuals) {
        String manualsBlock = matchedManuals.isEmpty()
            ? "(참고할 매뉴얼 조항 없음)"
            : matchedManuals.stream()
                .map(doc -> "- [%s] %s\n%s".formatted(doc.getEvaluationType(), doc.getTitle(), doc.getContent()))
                .collect(Collectors.joining("\n\n"));

        String userMessage = """
            [문의 원문]
            %s

            [분류 결과]
            - 오류 여부: %s
            - 평가 유형: %s
            - 핵심 증상 요약: %s

            [참고 매뉴얼 조항]
            %s
            """.formatted(
                rawContent,
                normalization.isError() ? "오류 신고" : "단순 문의/요청",
                normalization.evaluationType(),
                normalization.symptomSummary(),
                manualsBlock
            );

        return chatClient.prompt()
            .system(SYSTEM_PROMPT)
            .user(userMessage)
            .call()
            .content();
    }
}
