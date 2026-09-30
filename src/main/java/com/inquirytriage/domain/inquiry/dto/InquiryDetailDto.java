package com.inquirytriage.domain.inquiry.dto;

import com.inquirytriage.domain.inquiry.entity.Inquiry;
import com.inquirytriage.domain.inquiry.entity.InquiryResult;
import com.inquirytriage.domain.inquiry.enums.InquiryStatus;

import java.time.LocalDateTime;

public record InquiryDetailDto(
    Long id,
    Long tenantId,
    String tenantName,
    String rawContent,
    InquiryStatus status,
    LocalDateTime receivedAt,
    InquiryResultDto result
) {
    public static InquiryDetailDto from(Inquiry inquiry, InquiryResult resultOrNull) {
        return new InquiryDetailDto(
            inquiry.getId(),
            inquiry.getTenant().getId(),
            inquiry.getTenant().getName(),
            inquiry.getRawContent(),
            inquiry.getStatus(),
            inquiry.getReceivedAt(),
            resultOrNull == null ? null : InquiryResultDto.from(resultOrNull)
        );
    }
}
