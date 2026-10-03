package com.inquirytriage.domain.inquiry.repository;


import com.inquirytriage.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    // 목록 조회용: N+1 방지를 위해 tenant를 함께 fetch, 최신순 정렬
    @Query("select i from Inquiry i join fetch i.tenant order by i.receivedAt desc")
    List<Inquiry> findAllWithTenantOrderByReceivedAtDesc();

    // 단건 조회용: open-in-view를 끈 상태에서도 tenant를 읽을 수 있도록 함께 fetch
    @Query("select i from Inquiry i join fetch i.tenant where i.id = :id")
    Optional<Inquiry> findByIdWithTenant(@Param("id") Long id);
}
