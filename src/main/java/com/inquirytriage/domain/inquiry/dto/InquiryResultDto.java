package com.inquirytriage.domain.inquiry.dto;

import com.inquirytriage.domain.inquiry.entity.InquiryResult;
import com.inquirytriage.domain.inquiry.enums.EvaluationType;

import java.util.Arrays;
import java.util.List;

public record InquiryResultDto(
    boolean isError,
    EvaluationType evaluationType,
    String symptomSummary,
    List<String> searchKeywords,
    List<String> retrievedManualRefs,
    String finalAnswer
) {
    private static List<String> splitOrEmpty(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    public static InquiryResultDto from(InquiryResult result) {
        return new InquiryResultDto(
            result.isError(),
            result.getEvaluationType(),
            result.getSymptomSummary(),
            splitOrEmpty(result.getSearchKeywords()),
            splitOrEmpty(result.getRetrievedManualRefs()),
            result.getFinalAnswer()
        );
    }
}
