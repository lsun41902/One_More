package com.board.one_more_project.controller;

import com.board.one_more_project.config.RecipeValidator;
import com.board.one_more_project.dto.IngredientAnalysisResponse;
import com.board.one_more_project.dto.RecipeResponse;
import com.board.one_more_project.service.AiClientService;
import io.swagger.v3.oas.annotations.Operation;
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
    private final RecipeValidator validator; // 스프링이 관리하는 검증 전문가 부품

    /**
     * [생성자 주입]
     * 스프링은 빈 바구니에 담긴 AiClientService 구현체와 RecipeValidator를 찾아
     * 이 컨트롤러를 만들 때 자동으로 주입(DI)해줍니다.
     */
    public RecipeController(AiClientService aiClientService, RecipeValidator validator) {
        this.aiClientService = aiClientService;
        this.validator = validator;
    }

    @Operation(summary = "1단계: 이미지/영수증 분석 요청")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public IngredientAnalysisResponse analyzeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("preference") String preference,
            @RequestParam("type") String type,
            @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        log.info("1단계 분석 요청 수신: userId={}", userId);

        // 전문가에게 검증을 맡깁니다. 문제가 있으면 여기서 IllegalArgumentException이 터집니다.
        validator.validate(file, preference);

        return aiClientService.analyzeIngredients(file, preference, type, userId);
    }

    @Operation(summary = "2단계: 분석 결과 확정 및 생성")
    @PostMapping("/confirm")
    public RecipeResponse confirmRecipe(
            @RequestBody List<String> ingredients,
            @RequestParam("preference") String preference,
            @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        log.info("2단계 확정 요청 수신: userId={}", userId);

        // 전문가에게 검증을 맡깁니다.
        validator.validate(ingredients, preference);

        return aiClientService.generateRecipe(ingredients, preference, userId);
    }

    @Operation(summary = "텍스트 기반 레시피 생성")
    @PostMapping("/text")
    public RecipeResponse createRecipeFromText(
            @RequestBody List<String> ingredients,
            @RequestParam("preference") String preference,
            @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        log.info("텍스트 직접 입력 수신: userId={}", userId);

        // 전문가에게 검증을 맡깁니다.
        validator.validate(ingredients, preference);

        return aiClientService.generateRecipe(ingredients, preference, userId);
    }
}