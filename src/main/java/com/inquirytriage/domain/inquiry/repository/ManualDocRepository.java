package com.inquirytriage.domain.inquiry.repository;

import com.inquirytriage.domain.inquiry.entity.ManualDoc;
import com.inquirytriage.domain.inquiry.enums.EvaluationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualDocRepository extends JpaRepository<ManualDoc, Long> {
    // 평가유형별 조항 수가 적다고 가정하고, 1차로 evaluationType만 필터링한 뒤
    // 실제 키워드 매칭/점수화는 서비스 레이어(ManualSearchService)에서 처리합니다.
    List<ManualDoc> findByEvaluationType(EvaluationType evaluationType);
}
