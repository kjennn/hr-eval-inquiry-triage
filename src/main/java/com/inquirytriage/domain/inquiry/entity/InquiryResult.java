package com.inquirytriage.domain.inquiry.entity;

/**
 * <pre>
 * com.inquirytriage.domain.inquiry
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
    private String retrievedManualRefs; // JSON 문자열로 저장 (조항 목록)

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
}