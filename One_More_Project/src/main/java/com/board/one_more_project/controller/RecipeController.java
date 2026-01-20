package com.board.one_more_project.controller;

import com.board.one_more_project.config.RecipeValidator;
import com.board.one_more_project.dto.IngredientAnalysisResponse;
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

@Tag(name = "Recipe Flow", description = "이미지 분석 및 레시피 생성 프로세스") // Swagger 상단 그룹 명칭
@Slf4j
@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    // 인터페이스를 참조하여 실제 구현체(Real/Mock)에 유연하게 대응합니다.
    private final AiClientService aiClientService;
    // 별도 파일로 분리한 검증 전문가 클래스입니다.
    private final RecipeValidator validator;

    // 생성자 주입: 스프링이 빈 바구니에서 필요한 부품들을 찾아 자동으로 연결해줍니다.
    public RecipeController(AiClientService aiClientService, RecipeValidator validator) {
        this.aiClientService = aiClientService;
        this.validator = validator;
    }

    @Operation(summary = "1단계: 이미지/영수증 분석 요청", description = "이미지를 파이썬으로 보내 라벨링 이미지와 재료 리스트를 받습니다.")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Multipart 전송 명시
    public IngredientAnalysisResponse analyzeImage(
            @Parameter(description = "식재료 사진 또는 영수증 파일 (JPG, PNG)") @RequestParam("file") MultipartFile file,
            @Parameter(description = "유저의 요리 취향 (예: 매콤하게, 간단하게)") @RequestParam("preference") String preference,
            @Parameter(description = "데이터 타입 (image: 일반사진, receipt: 영수증)") @RequestParam("type") String type,
            @Parameter(description = "유저 식별 ID (테스트 및 로그용)") @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        // 요청 로그 기록
        log.info("1단계 분석 요청 수신: userId={}, type={}, filename={}", userId, type, file.getOriginalFilename());

        // [검증] 전문가 클래스에게 파일과 취향 검증을 맡깁니다.
        // 내부에서 에러 발생 시 IllegalArgumentException을 던지며, 이는 GlobalExceptionHandler가 처리합니다.
        validator.validate(file, preference);

        // 검증 통과 시 파이썬 서버로 분석 요청을 중계합니다.
        return aiClientService.analyzeIngredients(file, preference, type, userId);
    }

    @Operation(summary = "2단계: 분석 결과 확정 및 생성", description = "유저가 수정한 재료 리스트를 바탕으로 최종 레시피를 생성합니다.")
    @PostMapping("/confirm")
    public RecipeResponse confirmRecipe(
            @Parameter(description = "유저가 최종 확정한 재료 리스트") @RequestBody List<String> ingredients,
            @Parameter(description = "유저의 요리 취향") @RequestParam("preference") String preference,
            @Parameter(description = "유저 식별 ID") @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        // 유저가 1단계 결과를 확인하고 수정한 데이터를 받는 단계입니다.
        log.info("2단계 레시피 확정 요청: userId={}, ingredientsCount={}", userId, ingredients.size());

        // [검증] 전문가 클래스에게 재료 리스트와 취향 검증을 맡깁니다.
        validator.validate(ingredients, preference);

        // 검증 통과 시 파이썬 서버로 최종 레시피 생성을 요청합니다.
        return aiClientService.generateRecipe(ingredients, preference, userId);
    }

    @Operation(summary = "텍스트 기반 레시피 생성", description = "사진 없이 직접 입력한 재료 리스트를 바탕으로 AI가 레시피를 생성합니다.")
    @PostMapping("/text")
    public RecipeResponse createRecipeFromText(
            @Parameter(description = "직접 입력하거나 선택한 재료 리스트") @RequestBody List<String> ingredients,
            @Parameter(description = "유저의 요리 취향") @RequestParam("preference") String preference,
            @Parameter(description = "유저 식별 ID") @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        // 사진 없이 텍스트로만 진행하는 경로입니다.
        log.info("텍스트 레시피 요청 수신: userId={}, ingredientsCount={}, preference={}", userId, ingredients.size(), preference);

        // [검증] 전문가 클래스에게 재료 리스트와 취향 검증을 맡깁니다.
        validator.validate(ingredients, preference);

        // 텍스트 기반 생성도 결국 최종 레시피 생성 로직과 동일한 파이썬 엔드포인트를 사용합니다.
        return aiClientService.generateRecipe(ingredients, preference, userId);
    }
}