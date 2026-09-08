package com.inquirytriage.domain.inquiry.controller;

/**
 * <pre>
 * com.inquirytriage.domain.inquiry.controller
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

import com.inquirytriage.domain.inquiry.entity.Inquiry;
import com.inquirytriage.domain.inquiry.repository.InquiryRepository;
import com.inquirytriage.domain.inquiry.service.InquiryNormalizationResult;
import com.inquirytriage.domain.inquiry.service.InquiryNormalizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryTestController {

    private final InquiryRepository inquiryRepository;
    private final InquiryNormalizeService normalizeService;

    // 테스트용: 특정 문의를 정규화해서 결과만 바로 확인
    @PostMapping("/{id}/normalize-test")
    public InquiryNormalizationResult normalizeTest(@PathVariable Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의 없음: " + id));

        return normalizeService.normalize(inquiry.getRawContent());
    }
}