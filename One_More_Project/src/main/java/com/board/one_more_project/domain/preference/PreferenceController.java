package com.board.one_more_project.domain.preference;

import com.board.one_more_project.domain.common.MasterDataResponse;
import com.board.one_more_project.domain.common.MasterDataService;
import com.board.one_more_project.domain.common.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "3. Preference & Recommendation", description = "취향 관리 및 AI 기반 키워드 추천")
@Slf4j
@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController { // 기존에 개별 Repository를 직접 의존하던 방식에서 벗어나, 우리가 만든 MasterDataService와 RecommendationService를 사용하도록 전면 개편합니다.


    private final MasterDataService masterDataService; // 통합 마스터 데이터 서비스
    private final RecommendationService recommendationService; // AI 추천 오케스트레이터

    @Operation(summary = "취향 목록 조회", description = "모든 취향(STYLE, TASTE, CONDITION) 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<List<MasterDataResponse>> getAllPreferences() {
        return ResponseEntity.ok(masterDataService.getAllData(MasterDataResponse.MasterDataType.PREFERENCE));
    }

    @Operation(summary = "취향 검색", description = "의미론적 검색을 통해 유사한 취향을 찾습니다.")
    @GetMapping("/search")
    public ResponseEntity<List<MasterDataResponse>> searchPreferences(@RequestParam("q") String keyword) {
        return ResponseEntity.ok(masterDataService.searchData(MasterDataResponse.MasterDataType.PREFERENCE, keyword));
    }

    @Operation(summary = "취향 기반 AI 추천", description = "선택한 취향을 LLM이 분석하여 어울리는 재료와 조미료를 추천합니다.")
    @PostMapping("/analyze")
    public ResponseEntity<PreferenceRecommendationResponse> analyze(@RequestBody List<String> preferences) {
        log.info("AI 취향 분석 시작: {}", preferences);
        // RecommendationService가 LLM 호출부터 DB 매칭까지 한 번에 처리
        return ResponseEntity.ok(recommendationService.recommend(preferences));
    }
}