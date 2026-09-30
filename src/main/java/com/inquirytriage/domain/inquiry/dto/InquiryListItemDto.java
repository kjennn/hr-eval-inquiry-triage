package com.inquirytriage.domain.inquiry.dto;

import com.inquirytriage.domain.inquiry.entity.Inquiry;
import com.inquirytriage.domain.inquiry.enums.EvaluationType;
import com.inquirytriage.domain.inquiry.enums.InquiryStatus;

import java.time.LocalDateTime;

public record InquiryListItemDto(
    Long id,
    Long tenantId,
    String tenantName,
    String rawContent,
    InquiryStatus status,
    LocalDateTime receivedAt,
    Boolean isError,
    EvaluationType evaluationType
) {
    public static InquiryListItemDto from(Inquiry inquiry, Boolean isError, EvaluationType evaluationType) {
        return new InquiryListItemDto(
            inquiry.getId(),
            inquiry.getTenant().getId(),
            inquiry.getTenant().getName(),
            inquiry.getRawContent(),
            inquiry.getStatus(),
            inquiry.getReceivedAt(),
            isError,
            evaluationType
        );
    }
}
