package com.inquirytriage.domain.inquiry.repository;


import com.inquirytriage.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    // 목록 조회용: N+1 방지를 위해 tenant를 함께 fetch, 최신순 정렬
    @Query("select i from Inquiry i join fetch i.tenant order by i.receivedAt desc")
    List<Inquiry> findAllWithTenantOrderByReceivedAtDesc();
}
