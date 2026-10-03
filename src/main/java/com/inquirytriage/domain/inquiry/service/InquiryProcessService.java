package com.inquirytriage.domain.inquiry.service;

import com.inquirytriage.domain.inquiry.entity.Inquiry;
import com.inquirytriage.domain.inquiry.entity.InquiryResult;
import com.inquirytriage.domain.inquiry.entity.ManualDoc;
import com.inquirytriage.domain.inquiry.enums.EvaluationType;
import com.inquirytriage.domain.inquiry.repository.InquiryRepository;
import com.inquirytriage.domain.inquiry.repository.InquiryResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 문의 1건에 대한 전체 파이프라인을 오케스트레이션.
 * 1) Claude 정규화 2) 키워드 기반 매뉴얼 검색(임베딩 없음) 3) Claude 답변 합성
 * 3단계 모두 Claude(ChatClient) 하나만 사용합니다.
 *
 * 트랜잭션 경계: Claude 호출은 응답까지 수 초가 걸리므로 트랜잭션(=DB 커넥션 점유) 밖에서
 * 수행하고, 결과 저장만 {@link TransactionTemplate}으로 짧게 묶습니다.
 * (같은 클래스 안의 @Transactional 메서드 호출은 프록시를 거치지 않아 적용되지 않기 때문에
 * 메서드 어노테이션 대신 TransactionTemplate을 씁니다.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryProcessService {

    private final InquiryRepository inquiryRepository;
    private final InquiryResultRepository inquiryResultRepository;
    private final InquiryNormalizeService normalizeService;
    private final ManualSearchService manualSearchService;
    private final AnswerSynthesisService answerSynthesisService;
    private final TransactionTemplate transactionTemplate;

    public InquiryResult process(Long inquiryId) {
        // 읽기만 하고 바로 커넥션을 반환 (Repository 호출 단위 트랜잭션)
        String rawContent = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("문의 없음: " + inquiryId))
            .getRawContent();

        // ---- 아래 Claude 호출 구간은 트랜잭션/커넥션을 잡지 않는다 ----
        InquiryNormalizationResult normalization = sanitize(inquiryId, normalizeService.normalize(rawContent));
        EvaluationType evaluationType = EvaluationType.valueOf(normalization.evaluationType());
        String keywordsCsv = String.join(", ", normalization.searchKeywords());

        List<ManualDoc> matchedManuals = manualSearchService.search(evaluationType, normalization.searchKeywords());
        String retrievedManualRefsCsv = matchedManuals.stream()
            .map(ManualDoc::getTitle)
            .collect(Collectors.joining(", "));

        String finalAnswer = answerSynthesisService.synthesize(rawContent, normalization, matchedManuals);
        // -------------------------------------------------------------

        return transactionTemplate.execute(status ->
            save(inquiryId, normalization, evaluationType, keywordsCsv, retrievedManualRefsCsv, finalAnswer));
    }

    private InquiryResult save(Long inquiryId, InquiryNormalizationResult normalization,
                               EvaluationType evaluationType, String keywordsCsv,
                               String retrievedManualRefsCsv, String finalAnswer) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("문의 없음: " + inquiryId));

        InquiryResult result = inquiryResultRepository.findByInquiryId(inquiryId).orElse(null);

        if (result == null) {
            result = InquiryResult.builder()
                .inquiry(inquiry)
                .isError(normalization.isError())
                .evaluationType(evaluationType)
                .symptomSummary(normalization.symptomSummary())
                .searchKeywords(keywordsCsv)
                .retrievedManualRefs(retrievedManualRefsCsv)
                .finalAnswer(finalAnswer)
                .build();
        } else {
            result.updateFromNormalization(
                normalization.isError(),
                evaluationType,
                normalization.symptomSummary(),
                keywordsCsv,
                retrievedManualRefsCsv,
                finalAnswer
            );
        }

        inquiryResultRepository.save(result);

        inquiry.markProcessed();
        inquiryRepository.save(inquiry);

        return result;
    }

    /**
     * LLM 응답은 신뢰하지 않고 시스템이 허용하는 값으로 보정합니다.
     * - evaluationType이 null/빈 값/enum에 없는 값이면 COMMON으로 대체하고 경고 로그를 남깁니다.
     * - searchKeywords가 null이거나 null 원소를 포함하면 제거합니다.
     */
    private InquiryNormalizationResult sanitize(Long inquiryId, InquiryNormalizationResult raw) {
        EvaluationType type = resolveEvaluationType(inquiryId, raw.evaluationType());
        List<String> keywords = raw.searchKeywords() == null
            ? List.of()
            : raw.searchKeywords().stream().filter(Objects::nonNull).toList();

        return new InquiryNormalizationResult(
            raw.tenantGuess(), raw.isError(), type.name(), raw.symptomSummary(), keywords);
    }

    private EvaluationType resolveEvaluationType(Long inquiryId, String rawType) {
        if (rawType != null) {
            try {
                return EvaluationType.valueOf(rawType.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // 아래에서 폴백 처리
            }
        }
        log.warn("[inquiry={}] Claude가 허용되지 않은 evaluationType을 반환해 COMMON으로 대체합니다. 원본 값: '{}'",
            inquiryId, rawType);
        return EvaluationType.COMMON;
    }
}
