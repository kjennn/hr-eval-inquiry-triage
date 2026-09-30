package com.inquirytriage.domain.inquiry.repository;

import com.inquirytriage.domain.inquiry.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
