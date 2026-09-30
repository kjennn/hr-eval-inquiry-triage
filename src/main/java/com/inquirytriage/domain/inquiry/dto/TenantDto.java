package com.inquirytriage.domain.inquiry.dto;

import com.inquirytriage.domain.inquiry.entity.Tenant;

public record TenantDto(Long id, String name) {
    public static TenantDto from(Tenant tenant) {
        return new TenantDto(tenant.getId(), tenant.getName());
    }
}
