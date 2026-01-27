package com.board.one_more_project.domain.ingredient;

import io.swagger.v3.oas.annotations.media.Schema;

// 이미지 분석 결과와 레시피 추천 결과 양쪽에서 공통으로 사용할 "재료 이름 + 수량" 객체
@Schema(description = "재료 정보 (이름 + 수량)")
public record IngredientDto(
        @Schema(description = "재료 이름", example = "돼지고기")
        String ingredient,

        @Schema(description = "재료 수량 (단위 포함)", example = "200g")
        String quantity
) {}