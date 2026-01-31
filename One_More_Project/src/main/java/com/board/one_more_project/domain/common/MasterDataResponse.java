package com.board.one_more_project.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "마스터 데이터 공통 응답 객체 (재료, 조미료, 취향 통합)")
@Builder
public record MasterDataResponse(
        @Schema(description = "고유 ID", example = "1")
        Long id,

        @Schema(description = "이름", example = "돼지고기 / 고추장 / 매운맛")
        String name,

        @Schema(description = "데이터 타입", example = "INGREDIENT / SPICE / PREFERENCE")
        MasterDataType type,

        @Schema(description = "카테고리 (취향일 경우 사용)", example = "STYLE / TASTE / CONDITION")
        String category
) {
    public enum MasterDataType {
        INGREDIENT, SPICE, PREFERENCE
    }

    // 정적 팩토리 메서드: 다양한 엔티티를 하나의 DTO로 변환하는 로직을 캡슐화
    public static MasterDataResponse of(Long id, String name, MasterDataType type, String category) {
        return MasterDataResponse.builder()
                .id(id)
                .name(name)
                .type(type)
                .category(category)
                .build();
    }
}