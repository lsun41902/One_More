package com.board.one_more_project.controller;

import com.board.one_more_project.config.RecipeValidator;
import com.board.one_more_project.dto.IngredientAnalysisResponse;
import com.board.one_more_project.dto.RecipeGenerationRequest;
import com.board.one_more_project.dto.RecipeResponse;
import com.board.one_more_project.service.AiClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Recipe Flow", description = "이미지 분석 및 레시피 생성 프로세스")
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

    // 재료 이미지 or 영수증 분석 (Routing: type에 따라 다른 서비스 메서드 호출)
    @Operation(summary = "1단계: 이미지/영수증 분석 요청", description = "type 파라미터에 따라 일반 이미지 분석 또는 영수증 분석을 수행합니다.")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<IngredientAnalysisResponse> analyzeImage(
            @Parameter(description = "식재료 사진 또는 영수증 파일") @RequestParam("file") MultipartFile file,
            @Parameter(description = "유저의 요리 취향") @RequestParam("preference") List<String> preference,
            @Parameter(description = "데이터 타입 (image: 식재료, receipt: 영수증)") @RequestParam("type") String type,
            @Parameter(description = "유저 식별 ID") @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        log.info("1단계 분석 요청: userId={}, type={}", userId, type);
        validator.validate(file, preference);

        // Routing
        if ("receipt".equalsIgnoreCase(type)) { // 영수증 분석 서비스 호출 (/analyze-image-receipts)
            return aiClientService.analyzeImageReceipt(file, preference, userId);
        } else { // 일반 식재료 이미지 분석 (/analyze-image-ingredients)
            return aiClientService.analyzeImageIngredients(file, preference, userId);
        }
    }

    // 레시피 생성
    @Operation(summary = "2단계: 레시피 생성 요청", description = "action 값(initial, basic, more)에 따라 알맞은 레시피 생성 API를 호출합니다.")
    @PostMapping("/generate")
    public List<RecipeResponse> generateRecipe(
            @Parameter(description = "요청 데이터 (action 필드 필수)") @RequestBody RecipeGenerationRequest request
    ) {
        String action = request.action(); // DTO에서 action 값 추출

        log.info("2단계 생성 요청: userId={}, action={}", request.userId(), action);
        validator.validate(request.ingredients(), request.preference());

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
            default:
                // 약속되지 않은 action 값이 오면 에러 발생
                throw new IllegalArgumentException("잘못된 요청 타입입니다: " + action);
        }
    }
}