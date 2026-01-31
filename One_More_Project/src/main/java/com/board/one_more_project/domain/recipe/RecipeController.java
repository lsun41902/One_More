package com.board.one_more_project.domain.recipe;

import com.board.one_more_project.infrastructure.ai.AiClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private final AiClientService aiClientService;
    private final RecipeValidator validator;

    public RecipeController(AiClientService aiClientService, RecipeValidator validator) {
        this.aiClientService = aiClientService;
        this.validator = validator;
    }

    //  레시피 생성
    @Operation(summary = "레시피 생성 요청", description = "action 값(initial, basic, more)에 따라 알맞은 레시피 생성 API를 호출합니다.")
    @PostMapping("/generate")
    public List<RecipeResponse> generateRecipe(
            @Parameter(description = "요청 데이터 (action 필드 필수)") @RequestBody RecipeGenerationRequest request
    ) {
        String action = request.action(); // DTO에서 action 값 추출

        log.info("2단계 생성 요청: userId={}, action={}", request.userId(), action);
        validator.validateIngredients(request.ingredients(), request.preferences());

        // action이 null이거나 비어있으면 에러 처리
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("요청 타입(action)이 누락되었습니다.");
        }
        // Routing
        switch (action.toLowerCase()) {
            case "initial":
                // 최초 추천 3종 (/recipes-generate-initial)
                return aiClientService.generateRecipeInitial(request);
            case "basic":
                // 기본 재료 레시피 (/recipes-generate-basic)
                return aiClientService.generateRecipeBasic(request);
            case "more":
                // more 레시피 (/recipes-generate-more)
                return aiClientService.generateRecipeMore(request);
            case "real":
                // 만개의 레시피 (/recipes-generate-more)
                return aiClientService.generateRecipeReal(request);
            default:
                // 약속되지 않은 action 값이 오면 에러 발생
                throw new IllegalArgumentException("잘못된 요청 타입입니다: " + action);
        }
    }
}