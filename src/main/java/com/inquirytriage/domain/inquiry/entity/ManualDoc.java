package com.inquirytriage.domain.inquiry.entity;

import com.inquirytriage.domain.inquiry.enums.EvaluationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 고객 응대용 매뉴얼 조항 1건.
 * 임베딩/벡터 검색 없이, evaluationType + 키워드 포함 여부로만 찾는
 * 최소 구현(키워드 검색)을 전제로 한 엔티티입니다.
 */
@Entity
@Table(name = "manual_doc")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManualDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationType evaluationType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 검색 매칭용 키워드 (콤마로 구분)
    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ManualDoc(EvaluationType evaluationType, String title, String content, String keywords) {
        this.evaluationType = evaluationType;
        this.title = title;
        this.content = content;
        this.keywords = keywords;
        this.createdAt = LocalDateTime.now();
    }

    // 관리자 화면에서 매뉴얼 수정 시 사용 (createdAt은 유지)
    public void update(EvaluationType evaluationType, String title, String content, String keywords) {
        this.evaluationType = evaluationType;
        this.title = title;
        this.content = content;
        this.keywords = keywords;
    }
}
