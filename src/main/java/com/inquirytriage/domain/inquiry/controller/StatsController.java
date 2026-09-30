package com.inquirytriage.domain.inquiry.controller;

import com.inquirytriage.domain.inquiry.dto.StatsResponseDto;
import com.inquirytriage.domain.inquiry.entity.InquiryResult;
import com.inquirytriage.domain.inquiry.enums.EvaluationType;
import com.inquirytriage.domain.inquiry.repository.InquiryResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final InquiryResultRepository inquiryResultRepository;

    @GetMapping
    public StatsResponseDto stats() {
        List<InquiryResult> results = inquiryResultRepository.findAllWithInquiryAndTenant();

        long total = results.size();
        long errorCount = results.stream().filter(InquiryResult::isError).count();

        Map<String, List<InquiryResult>> byTenant = results.stream()
            .collect(Collectors.groupingBy(r -> r.getInquiry().getTenant().getName()));

        List<StatsResponseDto.TenantStat> tenantStats = byTenant.entrySet().stream()
            .map(e -> new StatsResponseDto.TenantStat(
                e.getKey(),
                e.getValue().size(),
                e.getValue().stream().filter(InquiryResult::isError).count()
            ))
            .sorted(Comparator.comparingLong(StatsResponseDto.TenantStat::count).reversed())
            .toList();

        Map<EvaluationType, List<InquiryResult>> byType = results.stream()
            .filter(r -> r.getEvaluationType() != null)
            .collect(Collectors.groupingBy(InquiryResult::getEvaluationType));

        List<StatsResponseDto.TypeStat> typeStats = byType.entrySet().stream()
            .map(e -> new StatsResponseDto.TypeStat(e.getKey(), e.getValue().size()))
            .sorted(Comparator.comparingLong(StatsResponseDto.TypeStat::count).reversed())
            .toList();

        return new StatsResponseDto(total, errorCount, tenantStats, typeStats);
    }
}
