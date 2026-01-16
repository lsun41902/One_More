package com.board.one_more_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

// @Schema: Swagger UI에서 데이터 모델(DTO)에 대한 설명을 제공합니다.
// LLM으로부터 받을 레시피 응답 구조입니다.
// 자바의 record 기능을 사용하면 코드가 매우 간결해집니다. (Java 14 이상)

@Schema(description = "AI가 생성한 최종 레시피 응답 정보")
public record RecipeResponse(
        @Schema(description = "요리 제목", example = "매콤한 김치볶음밥")
        String title,

        @Schema(description = "분석된/사용된 재료 리스트", example = "[\"김치\", \"밥\", \"참기름\"]")
        List<String> ingredients,

        @Schema(description = "조리 단계별 가이드", example = "[\"1. 김치를 썬다\", \"2. 팬에 볶는다\"]")
        List<String> steps,

        @Schema(description = "요리 꿀팁", example = "마지막에 깨를 뿌리면 더 고소해요!")
        String tip
) {}