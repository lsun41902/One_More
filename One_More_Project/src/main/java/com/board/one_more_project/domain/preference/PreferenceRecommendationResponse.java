package com.board.one_more_project.domain.preference;

import com.board.one_more_project.domain.common.MasterDataResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "취향 기반 AI 추천 결과 (통합 DTO 적용)")
public record PreferenceRecommendationResponse (
        @Schema(description = "추천된 재료 목록")
        List<MasterDataResponse> ingredients,

        @Schema(description = "추천된 조미료 목록")
        List<MasterDataResponse> spices
) {}