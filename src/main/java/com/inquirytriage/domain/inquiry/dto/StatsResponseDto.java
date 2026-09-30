package com.inquirytriage.domain.inquiry.dto;

import com.inquirytriage.domain.inquiry.enums.EvaluationType;

import java.util.List;

public record StatsResponseDto(
    long totalCount,
    long errorCount,
    List<TenantStat> byTenant,
    List<TypeStat> byEvaluationType
) {
    public record TenantStat(String tenantName, long count, long errorCount) {}

    public record TypeStat(EvaluationType evaluationType, long count) {}
}
