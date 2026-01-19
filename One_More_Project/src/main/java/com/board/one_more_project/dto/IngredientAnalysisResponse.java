package com.board.one_more_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

// @Schema: Swagger UI에서 데이터 모델(DTO)에 대한 설명을 제공
// 파이썬서버의 AI모델이 분석한 이미지와, 분석된 재료 리스트 등을 받을 응답 구조
@Schema(description = "AI 식재료 분석 결과 (리뷰용)")
public record IngredientAnalysisResponse(
        @Schema(description = "라벨링된 이미지 데이터 (Base64 문자열)", example = "data:image/jpeg;base64,...")
        String labeledImage,

        @Schema(description = "인식된 식재료 리스트", example = "[\"감자\", \"양파\", \"스팸\"]")
        List<String> ingredients,

        @Schema(description = "유저의 요리 취향 (전달용)", example = "매콤하게")
        String preference,

        @Schema(description = "데이터 타입 (review 고정)", example = "review")
        String type
) {}