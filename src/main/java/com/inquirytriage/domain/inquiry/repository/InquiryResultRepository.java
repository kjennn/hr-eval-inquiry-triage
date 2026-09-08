package com.inquirytriage.domain.inquiry.repository;


import com.inquirytriage.domain.inquiry.entity.InquiryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InquiryResultRepository extends JpaRepository<InquiryResult, Long> {
    Optional<InquiryResult> findByInquiryId(Long inquiryId);
}
