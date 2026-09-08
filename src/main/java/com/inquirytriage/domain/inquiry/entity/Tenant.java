package com.inquirytriage.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // 회사별 평가 정책 커스텀 항목 (가중치, 특이사항 등) - 일단 텍스트로 단순화
    @Column(columnDefinition = "TEXT")
    private String evaluationPolicyNote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Tenant(String name, String evaluationPolicyNote) {
        this.name = name;
        this.evaluationPolicyNote = evaluationPolicyNote;
        this.createdAt = LocalDateTime.now();
    }
}