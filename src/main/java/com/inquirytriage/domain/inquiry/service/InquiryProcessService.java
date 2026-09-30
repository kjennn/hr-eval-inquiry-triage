package com.inquirytriage.domain.inquiry.service;

import com.inquirytriage.domain.inquiry.entity.Inquiry;
import com.inquirytriage.domain.inquiry.entity.InquiryResult;
import com.inquirytriage.domain.inquiry.entity.ManualDoc;
import com.inquirytriage.domain.inquiry.enums.EvaluationType;
import com.inquirytriage.domain.inquiry.repository.InquiryRepository;
import com.inquirytriage.domain.inquiry.repository.InquiryResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 문의 1건에 대한 전체 파이프라인을 오케스트레이션.
 * 1) Claude 정규화 2) 키워드 기반 매뉴얼 검색(임베딩 없음) 3) Claude 답변 합성
 * 3단계 모두 Claude(ChatClient) 하나만 사용합니다.
 */
@Service
@RequiredArgsConstructor
public class InquiryProcessService {

    private final InquiryRepository inquiryRepository;
    private final InquiryResultRepository inquiryResultRepository;
    private final InquiryNormalizeService normalizeService;
    private final ManualSearchService manualSearchService;
    private final AnswerSynthesisService answerSynthesisService;

    @Transactional
    public InquiryResult process(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("문의 없음: " + inquiryId));

        InquiryNormalizationResult normalization = normalizeService.normalize(inquiry.getRawContent());
        EvaluationType evaluationType = EvaluationType.valueOf(normalization.evaluationType());
        String keywordsCsv = String.join(", ", normalization.searchKeywords());

        List<ManualDoc> matchedManuals = manualSearchService.search(evaluationType, normalization.searchKeywords());
        String retrievedManualRefsCsv = matchedManuals.stream()
            .map(ManualDoc::getTitle)
            .collect(Collectors.joining(", "));

        String finalAnswer = answerSynthesisService.synthesize(inquiry.getRawContent(), normalization, matchedManuals);

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
}
