package com.board.one_more_project.domain.ingredient;

import io.swagger.v3.oas.annotations.media.Schema;

// 프론트엔드에서 사용자가 재료를 선택할 때 보여줄 리스트 아이템
@Schema(description = "재료 마스터 데이터 (카테고리 + 이름)")
public record IngredientResponse(
        @Schema(description = "재료 고유 ID", example = "1")
        Long id,

        @Schema(description = "재료 이름", example = "돼지고기")
        String name
) {
    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드 패턴)
    public static IngredientResponse from(Ingredient entity) {
        return new IngredientResponse(
                entity.getId(),
                entity.getName()

                // 정적 팩토리 메서드의 표준 규격 (Effective Java)
                // from: 매개변수를 하나 받아서 해당 타입의 인스턴스를 반환할 때 (형변환)
                // 예: IngredientResponse.from(entity) -> "엔티티로부터 응답 객체를 만든다."
                // of: 매개변수를 여러 개 받아서 적절한 인스턴스를 반환할 때
                // 예: LocalDate.of(2024, 1, 30) -> "연, 월, 일을 조합해 날짜 객체를 만든다."
                // valueOf: from이나 of와 비슷하지만 좀 더 상세한 버전
        );
    }
}