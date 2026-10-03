package com.inquirytriage.domain.inquiry.controller;

import com.inquirytriage.domain.inquiry.dto.CreateInquiryRequestDto;
import com.inquirytriage.domain.inquiry.dto.InquiryDetailDto;
import com.inquirytriage.domain.inquiry.dto.InquiryListItemDto;
import com.inquirytriage.domain.inquiry.entity.Inquiry;
import com.inquirytriage.domain.inquiry.entity.InquiryResult;
import com.inquirytriage.domain.inquiry.entity.Tenant;
import com.inquirytriage.domain.inquiry.repository.InquiryRepository;
import com.inquirytriage.domain.inquiry.repository.InquiryResultRepository;
import com.inquirytriage.domain.inquiry.repository.TenantRepository;
import com.inquirytriage.domain.inquiry.service.InquiryProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 기존 InquiryTestController(/api/inquiries/{id}/normalize-test)와는
// 별개 클래스로, 프론트가 실제로 쓰는 CRUD + 파이프라인 실행 엔드포인트를 담당합니다.
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryApiController {

    private final InquiryRepository inquiryRepository;
    private final InquiryResultRepository inquiryResultRepository;
    private final TenantRepository tenantRepository;
    private final InquiryProcessService inquiryProcessService;

    @GetMapping
    public List<InquiryListItemDto> list() {
        return inquiryRepository.findAllWithTenantOrderByReceivedAtDesc().stream()
            .map(inquiry -> {
                InquiryResult result = inquiryResultRepository.findByInquiryId(inquiry.getId())
                    .orElse(null);
                return InquiryListItemDto.from(
                    inquiry,
                    result == null ? null : result.isError(),
                    result == null ? null : result.getEvaluationType()
                );
            })
            .toList();
    }

    @GetMapping("/{id}")
    public InquiryDetailDto detail(@PathVariable Long id) {
        Inquiry inquiry = inquiryRepository.findByIdWithTenant(id)
            .orElseThrow(() -> new IllegalArgumentException("문의 없음: " + id));
        InquiryResult result = inquiryResultRepository.findByInquiryId(id).orElse(null);
        return InquiryDetailDto.from(inquiry, result);
    }

    @PostMapping
    public ResponseEntity<InquiryDetailDto> create(@Valid @RequestBody CreateInquiryRequestDto req) {
        Tenant tenant = tenantRepository.findById(req.tenantId())
            .orElseThrow(() -> new IllegalArgumentException("고객사 없음: " + req.tenantId()));

        Inquiry inquiry = Inquiry.builder()
            .tenant(tenant)
            .rawContent(req.rawContent())
            .build();
        inquiryRepository.save(inquiry);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(InquiryDetailDto.from(inquiry, null));
    }

    @PostMapping("/{id}/process")
    public InquiryDetailDto process(@PathVariable Long id) {
        InquiryResult result = inquiryProcessService.process(id);
        Inquiry inquiry = inquiryRepository.findByIdWithTenant(id)
            .orElseThrow(() -> new IllegalArgumentException("문의 없음: " + id));
        return InquiryDetailDto.from(inquiry, result);
    }
}
