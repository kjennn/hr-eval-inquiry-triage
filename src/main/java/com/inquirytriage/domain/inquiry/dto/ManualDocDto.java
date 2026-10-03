package com.inquirytriage.domain.inquiry.dto;

import com.inquirytriage.domain.inquiry.entity.ManualDoc;
import com.inquirytriage.domain.inquiry.enums.EvaluationType;

import java.time.LocalDateTime;

public record ManualDocDto(
    Long id,
    EvaluationType evaluationType,
    String title,
    String content,
    String keywords,
    LocalDateTime createdAt
) {
    public static ManualDocDto from(ManualDoc doc) {
        return new ManualDocDto(
            doc.getId(),
            doc.getEvaluationType(),
            doc.getTitle(),
            doc.getContent(),
            doc.getKeywords(),
            doc.getCreatedAt()
        );
    }
}
