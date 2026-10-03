package com.inquirytriage.domain.inquiry.controller;

import com.inquirytriage.domain.inquiry.dto.ManualDocDto;
import com.inquirytriage.domain.inquiry.dto.ManualDocRequestDto;
import com.inquirytriage.domain.inquiry.entity.ManualDoc;
import com.inquirytriage.domain.inquiry.repository.ManualDocRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자용 매뉴얼 관리 API. 매뉴얼은 검색 단계(ManualSearchService)가 즉시 읽으므로
 * 저장/수정/삭제 후 별도 반영 작업은 필요 없습니다.
 */
@RestController
@RequestMapping("/api/manuals")
@RequiredArgsConstructor
public class ManualController {

    private final ManualDocRepository manualDocRepository;

    @GetMapping
    public List<ManualDocDto> list() {
        return manualDocRepository.findAll(Sort.by(Sort.Direction.ASC, "evaluationType", "id")).stream()
            .map(ManualDocDto::from)
            .toList();
    }

    @PostMapping
    public ResponseEntity<ManualDocDto> create(@Valid @RequestBody ManualDocRequestDto req) {
        ManualDoc doc = ManualDoc.builder()
            .evaluationType(req.evaluationType())
            .title(req.title().trim())
            .content(req.content().trim())
            .keywords(normalizeKeywords(req.keywords()))
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(ManualDocDto.from(manualDocRepository.save(doc)));
    }

    @PutMapping("/{id}")
    @Transactional
    public ManualDocDto update(@PathVariable Long id, @Valid @RequestBody ManualDocRequestDto req) {
        ManualDoc doc = manualDocRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("매뉴얼 없음: " + id));
        doc.update(req.evaluationType(), req.title().trim(), req.content().trim(), normalizeKeywords(req.keywords()));
        return ManualDocDto.from(doc);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!manualDocRepository.existsById(id)) {
            throw new IllegalArgumentException("매뉴얼 없음: " + id);
        }
        manualDocRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // "a,  b ,,c" -> "a, b, c" (검색 서비스는 콤마 구분을 전제로 함)
    private String normalizeKeywords(String keywords) {
        if (keywords == null) {
            return "";
        }
        return java.util.Arrays.stream(keywords.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(java.util.stream.Collectors.joining(", "));
    }
}
