package com.board.one_more_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * [공통 재료 DTO]
 * 이미지 분석 결과와 레시피 재료 목록에서 공통으로 사용되는 객체입니다.
 * 팀원과 협의하여 key 값을 'ingredient'로 통일했습니다.
 */
// 이미지 분석 결과와 레시피 추천 결과 양쪽에서 공통으로 사용할 "재료 이름 + 수량" 객체
@Schema(description = "재료 정보 (이름 + 수량)")
public record IngredientDto(
        @Schema(description = "재료 이름", example = "돼지고기")
        String ingredient,

        @Schema(description = "재료 수량 (단위 포함)", example = "200g")
        String quantity
) {}