package com.inquirytriage.domain.inquiry.service;

import com.inquirytriage.domain.inquiry.entity.ManualDoc;
import com.inquirytriage.domain.inquiry.enums.EvaluationType;
import com.inquirytriage.domain.inquiry.repository.ManualDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 벡터 임베딩 없이 "검색" 단계를 구현한 최소 버전.
 * 1) evaluationType으로 후보를 좁히고
 * 2) 정규화 단계에서 뽑은 searchKeywords가 title/content/keywords에
 *    얼마나 포함되는지로 점수를 매겨 상위 N개만 반환합니다.
 *
 * 매뉴얼 양이 많아지면(수백 건 이상) DB ILIKE 검색이나 별도 검색엔진으로
 * 바꾸는 게 맞지만, 지금 규모(평가유형당 몇~수십 건)에서는 이 정도로 충분합니다.
 */
@Service
@RequiredArgsConstructor
public class ManualSearchService {

    private static final int MAX_RESULTS = 3;

    private final ManualDocRepository manualDocRepository;

    public List<ManualDoc> search(EvaluationType evaluationType, List<String> searchKeywords) {
        List<ManualDoc> candidates = manualDocRepository.findByEvaluationType(evaluationType);

        if (searchKeywords == null || searchKeywords.isEmpty()) {
            return candidates.stream().limit(MAX_RESULTS).toList();
        }

        return candidates.stream()
            .map(doc -> new ScoredManual(doc, score(doc, searchKeywords)))
            .filter(scored -> scored.score() > 0)
            .sorted(Comparator.comparingInt(ScoredManual::score).reversed())
            .limit(MAX_RESULTS)
            .map(ScoredManual::doc)
            .toList();
    }

    private int score(ManualDoc doc, List<String> keywords) {
        String haystack = (doc.getTitle() + " " + doc.getContent() + " " + doc.getKeywords())
            .toLowerCase();

        int score = 0;
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isBlank() && haystack.contains(keyword.toLowerCase().trim())) {
                score++;
            }
        }
        return score;
    }

    private record ScoredManual(ManualDoc doc, int score) {}
}
