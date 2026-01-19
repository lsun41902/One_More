package com.board.one_more_project.service;

import com.board.one_more_project.dto.IngredientAnalysisResponse;
import com.board.one_more_project.dto.RecipeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@Profile("dev") // 개발 환경에서 파이썬 서버 없이 테스트할 때 사용
public class MockAiClientService implements AiClientService {

    @Override
    public IngredientAnalysisResponse analyzeIngredients(MultipartFile file, String preference, String type, String userId) {
        log.info("[Mock] 가짜 이미지 분석을 수행합니다. User: {}", userId);
        return new IngredientAnalysisResponse(
                "fake_base64_image_string", // 실제 구현 시에는 테스트용 base64 문자열 삽입 가능
                List.of("양파", "당근", "돼지고기"),
                preference,
                "review"
        );
    }

    @Override
    public RecipeResponse generateRecipe(List<String> ingredients, String preference, String userId) {
        log.info("[Mock] 가짜 레시피를 생성합니다. User: {}", userId);
        return new RecipeResponse(
                "[Mock] 맛있는 요리",
                ingredients,
                List.of("1. 재료를 씻는다", "2. 볶는다", "3. 먹는다"),
                "Mock 취향 반영: " + preference
        );
    }
}