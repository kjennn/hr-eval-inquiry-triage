package com.inquirytriage.domain.inquiry.controller;

import com.inquirytriage.domain.inquiry.dto.TenantDto;
import com.inquirytriage.domain.inquiry.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantRepository tenantRepository;

    @GetMapping
    public List<TenantDto> list() {
        return tenantRepository.findAll().stream()
            .map(TenantDto::from)
            .toList();
    }
}
