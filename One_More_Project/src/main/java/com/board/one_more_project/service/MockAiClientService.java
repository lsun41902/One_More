package com.board.one_more_project.service;

import com.board.one_more_project.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@Profile("dev")
public class MockAiClientService implements AiClientService {

    // 1-1. 일반 이미지 분석 Mock
    @Override
    public List<IngredientAnalysisResponse> analyzeImageIngredients(MultipartFile file, List<String> preference, String userId) {
        log.info("[Mock] 일반 이미지 분석. User: {}", userId);
        return List.of(new IngredientAnalysisResponse(0, List.of(new IngredientDto("목살", "300g"))));
    }

    // 1-2. 영수증 분석 Mock
    @Override
    public List<IngredientAnalysisResponse> analyzeImageReceipt(MultipartFile file, List<String> preference, String userId) {
        log.info("[Mock] 영수증 분석. User: {}", userId);
        return List.of(new IngredientAnalysisResponse(0, List.of(new IngredientDto("영수증_두부", "1모"))));
    }

    // 2-1. 최초 추천 Mock
    @Override
    public List<RecipeResponse> generateRecipeInitial(RecipeGenerationRequest request) {
        log.info("[Mock] 최초 레시피 추천. User: {}", request.userId());
        return List.of(new RecipeResponse("최초 추천 요리", "맛있습니다", request.ingredients(), null, List.of("요리하세요"), List.of("팁"), null, null));
    }

    // 2-2. 기본 레시피 Mock
    @Override
    public List<RecipeResponse> generateRecipeBasic(RecipeGenerationRequest request) {
        log.info("[Mock] 기본 레시피 생성. User: {}", request.userId());
        return List.of(new RecipeResponse("기본 재료 요리", "간단합니다", request.ingredients(), null, List.of("볶으세요"), List.of("팁"), null, null));
    }

    // 2-3. 응용 레시피 Mock
    @Override
    public List<RecipeResponse> generateRecipeMore(RecipeGenerationRequest request) {
        log.info("[Mock] 응용 레시피 생성. User: {}", request.userId());
        return List.of(new RecipeResponse("응용 요리", "특별합니다", request.ingredients(), List.of(new IngredientDto("치즈", "1장")), List.of("치즈를 넣으세요"), List.of("팁"), null, null));
    }
}