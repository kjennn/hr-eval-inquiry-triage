package com.inquirytriage.domain.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInquiryRequestDto(
    @NotNull Long tenantId,
    @NotBlank String rawContent
) {}
