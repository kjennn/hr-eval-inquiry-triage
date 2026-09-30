package com.inquirytriage.domain.inquiry.repository;


import com.inquirytriage.domain.inquiry.entity.InquiryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InquiryResultRepository extends JpaRepository<InquiryResult, Long> {
    Optional<InquiryResult> findByInquiryId(Long inquiryId);

    // 통계 집계용: tenant까지 한 번에 fetch해서 N+1 방지
    @Query("select r from InquiryResult r join fetch r.inquiry i join fetch i.tenant t")
    List<InquiryResult> findAllWithInquiryAndTenant();
}
