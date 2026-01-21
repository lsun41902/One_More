package com.board.one_more_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * [이미지 분석 응답 DTO]
 * 파이썬 AI 서버가 이미지를 분석한 후 반환하는 결과 구조입니다.
 * 여러 장의 이미지를 분석할 수 있으므로 image_index가 포함됩니다.
 */
// 파이썬의 이미지 분석 결과(이미지 분석.txt) 구조에 맞춤. 기존의 단순 리스트에서 image_index를 포함한 구조로 변경.
@Schema(description = "AI 이미지 분석 결과 (개별 이미지)")
public record IngredientAnalysisResponse(
        @Schema(description = "이미지 번호 (0부터 시작)", example = "0")
        int image_index,

        @Schema(description = "분석된 재료 리스트")
        List<IngredientDto> ingredients
) {}