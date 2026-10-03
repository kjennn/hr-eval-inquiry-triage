package com.inquirytriage.domain.inquiry.dto;

import com.inquirytriage.domain.inquiry.enums.EvaluationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ManualDocRequestDto(
    @NotNull EvaluationType evaluationType,
    @NotBlank String title,
    @NotBlank String content,
    String keywords
) {}
