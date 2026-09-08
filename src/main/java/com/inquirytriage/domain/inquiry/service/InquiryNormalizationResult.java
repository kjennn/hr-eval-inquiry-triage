package com.inquirytriage.domain.inquiry.service;

/**
 * <pre>
 * com.inquirytriage.domain.inquiry.service
 *
 * Description :
 * </pre>
 *
 * @author gwonjieun
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 * 수정일     수정자   수정내용
 * ---------- -------- -------------------
 * 26. 9. 8.   gwonjieun   최초 생성
 * </pre>
 * @since 26. 9. 8.
 */

import java.util.List;

public record InquiryNormalizationResult(
        String tenantGuess,
        boolean isError,
        String evaluationType,   // PERFORMANCE, COMPETENCY, MULTI_RATER, COMPREHENSIVE, COMMON
        String symptomSummary,
        List<String> searchKeywords
) {}