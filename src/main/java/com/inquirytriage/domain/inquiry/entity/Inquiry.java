package com.inquirytriage.domain.inquiry.entity;

/**
 * <pre>
 * inquirytriage.domain.inquiry
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

import com.inquirytriage.domain.inquiry.enums.InquiryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @Builder
    public Inquiry(Tenant tenant, String rawContent) {
        this.tenant = tenant;
        this.rawContent = rawContent;
        this.status = InquiryStatus.PENDING;
        this.receivedAt = LocalDateTime.now();
    }

    public void markProcessed() {
        this.status = InquiryStatus.PROCESSED;
    }
}
