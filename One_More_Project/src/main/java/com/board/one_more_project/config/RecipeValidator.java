package com.board.one_more_project.config;

import com.board.one_more_project.dto.IngredientDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * [레시피 검증 전문가 클래스]
 * 데이터 구조 변경(DTO 도입)에 맞춰 검증 로직을 업데이트했습니다.
 */
@Component
public class RecipeValidator {

    // 1단계: 이미지 분석 요청 검증 (파일 + 문자열 취향)
    // 1단계는 프론트엔드에서 FormData로 보내기 때문에 preference가 String으로 옵니다.
    public void validate(MultipartFile file, String preference) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }
        checkPreferenceLength(preference);
    }

    // 2단계: 레시피 생성 요청 검증 (DTO 리스트 + 리스트 취향)
    // 변경점: List<String> -> List<IngredientDto>로 변경되었습니다.
    public void validate(List<IngredientDto> ingredients, List<String> preference) {
        // 1. 재료 리스트 자체의 null 여부 확인
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("입력된 재료가 없습니다.");
        }

        // 2. 리스트 안의 재료 객체 하나하나를 뜯어서 이름이 있는지 확인
        boolean hasValidIngredient = ingredients.stream()
                .anyMatch(item -> item.ingredient() != null && !item.ingredient().trim().isEmpty());

        if (!hasValidIngredient) {
            throw new IllegalArgumentException("유효한 재료 이름이 하나도 없습니다.");
        }

        // 3. 취향 리스트 검증 (리스트를 문자열로 합쳐서 길이 검사)
        if (preference != null) {
            String joinedPreference = String.join(", ", preference);
            checkPreferenceLength(joinedPreference);
        }
    }

    // 공통 검증: 취향 텍스트 길이 제한
    private void checkPreferenceLength(String preference) {
        if (preference != null && preference.length() >= 100) {
            throw new IllegalArgumentException("요리 취향은 100자 이내로 입력해주세요.");
        }
    }
}