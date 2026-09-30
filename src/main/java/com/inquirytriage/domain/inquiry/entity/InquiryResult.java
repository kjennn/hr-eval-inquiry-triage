package com.inquirytriage.domain.inquiry.entity;

import com.inquirytriage.domain.inquiry.enums.EvaluationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false, unique = true)
    private Inquiry inquiry;

    @Column(nullable = false)
    private boolean isError;

    @Enumerated(EnumType.STRING)
    private EvaluationType evaluationType; // 오류 아니면 null 가능

    @Column(columnDefinition = "TEXT")
    private String symptomSummary;

    @Column(columnDefinition = "TEXT")
    private String searchKeywords;

    @Column(columnDefinition = "TEXT")
    private String retrievedManualRefs; // 콤마(,)로 구분해 저장

    @Column(columnDefinition = "TEXT")
    private String finalAnswer;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InquiryResult(Inquiry inquiry, boolean isError, EvaluationType evaluationType,
                         String symptomSummary, String searchKeywords,
                         String retrievedManualRefs, String finalAnswer) {
        this.inquiry = inquiry;
        this.isError = isError;
        this.evaluationType = evaluationType;
        this.symptomSummary = symptomSummary;
        this.searchKeywords = searchKeywords;
        this.retrievedManualRefs = retrievedManualRefs;
        this.finalAnswer = finalAnswer;
        this.createdAt = LocalDateTime.now();
    }

    // 재분석(재처리) 시 새 레코드를 만들지 않고 기존 레코드를 갱신하기 위한 메서드
    public void updateFromNormalization(
            boolean isError,
            EvaluationType evaluationType,
            String symptomSummary,
            String searchKeywords,
            String retrievedManualRefs,
            String finalAnswer
    ) {
        this.isError = isError;
        this.evaluationType = evaluationType;
        this.symptomSummary = symptomSummary;
        this.searchKeywords = searchKeywords;
        this.retrievedManualRefs = retrievedManualRefs;
        this.finalAnswer = finalAnswer;
    }
}
