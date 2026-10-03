package com.inquirytriage.domain.inquiry.service;

import com.inquirytriage.domain.inquiry.entity.ManualDoc;
import com.inquirytriage.domain.inquiry.enums.EvaluationType;
import com.inquirytriage.domain.inquiry.repository.ManualDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        List<ManualDoc> candidates = new ArrayList<>(manualDocRepository.findByEvaluationType(evaluationType));
        // 평가유형 전용 매뉴얼로 부족할 수 있어(예: 퇴사자 계정, HRM 동기화) 공통 매뉴얼도 후보에 포함합니다.
        if (evaluationType != EvaluationType.COMMON) {
            candidates.addAll(manualDocRepository.findByEvaluationType(EvaluationType.COMMON));
        }

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

        // Claude가 뽑는 키워드는 "다면평가 대상자 업로드"처럼 구절인 경우가 많아서,
        // 구절 전체가 매뉴얼에 그대로 있을 때만 점수를 주면 거의 매칭되지 않습니다.
        // 구절 일치(가중치 3) + 구절을 공백으로 쪼갠 단어별 일치(가중치 1)로 점수화합니다.
        int score = 0;
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }
            String phrase = keyword.toLowerCase().trim();
            if (haystack.contains(phrase)) {
                score += 3;
            }
            for (String token : phrase.split("\\s+")) {
                if (token.length() >= 2 && haystack.contains(token)) {
                    score++;
                }
            }
        }
        return score;
    }

    private record ScoredManual(ManualDoc doc, int score) {}
}
