package com.board.one_more_project.controller;

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

@Tag(name = "Recipe Flow", description = "이미지 분석 후 유저 확인을 거치는 레시피 생성 프로세스")
@Slf4j
@RestController
@RequestMapping("/api/recipe")
public class RecipeController {
    // 이 필드는 Class가 아닌 Interface를 가리킵니다.
    // 인터페이스를 참조함으로써 실제 구현체가 Real인지 Mock인지 상관없이 동작하게 설계(약결합)합니다.
    private final AiClientService aiClientService;

    public RecipeController(AiClientService aiClientService) {
        // 스프링이 실행될 때 Profile 설정(dev/prod)에 따라 적절한 객체를 여기에 주입(DI)합니다.
        this.aiClientService = aiClientService;
    }

    // @Operation: 특정 API(메서드)가 어떤 일을 하는지 Swagger 문서에 설명합니다.
    @Operation(summary = "1단계: 이미지/영수증 분석 요청", description = "이미지를 파이썬으로 보내 라벨링 이미지와 재료 리스트를 받습니다.")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)  // consumes = API가 Multipart 형식을 사용함을 명시.
    public IngredientAnalysisResponse analyzeImage(
            @Parameter(description = "식재료 사진 또는 영수증 파일") @RequestParam("file") MultipartFile file,
            @Parameter(description = "유저의 요리 취향") @RequestParam("preference") String preference,
            @Parameter(description = "데이터 타입 (image 또는 receipt)") @RequestParam("type") String type,
            @Parameter(description = "유저 식별 ID (팀원 요청으로 추가)") @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        // 요청 기록: 어떤 유저가 어떤 파일을 올렸는지 로그를 남깁니다.
        log.info("1단계 분석 요청 수신: userId={}, type={}, filename={}", userId, type, file.getOriginalFilename());

        if (file.isEmpty()) { // 아무것도 입력 안하고, 레시피를 생성했을 경우
            log.warn("빈 파일이 업로드되었습니다.");
            // 파일이 없을 경우 에러 메시지를 담은 응답을 반환합니다.
            return new IngredientAnalysisResponse(null, List.of("파일이 없습니다."), preference, "error");
        }

        // [멘티님의 소중한 로직] 악의적 입력 검증: 텍스트 길이를 제한하여 서버 부하를 방지합니다.
        if(preference.length() >= 100) {
            log.warn("너무 긴 요리 취향이 입력되었습니다.");
            // DTO 구조에 맞춰 에러 메시지를 반환합니다.
            return new IngredientAnalysisResponse(null, List.of("너무 긴 텍스트 입력입니다."), "100자 이내로 입력해주세요.", "error");
        }

        // 2. Python 서버로 중계: 이미지 분석을 요청하고 리뷰용 데이터를 받아옵니다.
        return aiClientService.analyzeIngredients(file, preference, type, userId);
    }

    @Operation(summary = "2단계: 유저 확인 후 레시피 생성", description = "유저가 수정한 재료 리스트를 바탕으로 최종 레시피를 생성합니다.")
    @PostMapping("/confirm")
    public RecipeResponse confirmRecipe(
            @RequestBody List<String> ingredients,
            @Parameter(description = "유저의 요리 취향") @RequestParam("preference") String preference,
            @Parameter(description = "유저 식별 ID") @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        // 유저가 1단계 결과를 보고 수정한 재료 리스트를 받는 엔드포인트입니다.
        log.info("2단계 레시피 확정 요청: userId={}, ingredientsCount={}", userId, ingredients.size());

        if(ingredients.isEmpty()) { // 재료를 모두 삭제하고 요청했을 경우
            log.warn("빈 재료 정보가 입력되었습니다.");
            return new RecipeResponse("오류", List.of(), List.of("입력 재료가 없습니다."), "재료를 직접 입력하거나 선택해주세요.");
        }

        // 악의적 입력 검증
        if(preference.length() >= 100) {
            log.warn("너무 긴 요리 취향이 입력되었습니다.");
            return new RecipeResponse("오류", List.of(), List.of("너무 긴 텍스트 입력입니다."), "100자 이내로 입력해주세요.");
        }

        // 재료 입력 중간에 빈 칸이 있을 경우 제거하여 데이터 정제
        ingredients.removeIf(ingredient -> ingredient.isEmpty());

        // Python 서버로 최종 레시피 생성을 요청합니다.
        return aiClientService.generateRecipe(ingredients, preference, userId);
    }

    @Operation(summary = "텍스트 기반 레시피 생성", description = "사진 없이 직접 입력한 재료 리스트를 바탕으로 AI가 레시피를 생성합니다.")
    @PostMapping("/text")
    public RecipeResponse createRecipeFromText(
            @RequestBody List<String> ingredients,
            @Parameter(description = "유저의 요리 취향") @RequestParam("preference") String preference,
            @Parameter(description = "유저 식별 ID") @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        // 텍스트 입력은 이미 재료 리스트이므로 type 구분이 필요 없음
        log.info("텍스트 레시피 요청 수신: userId={}, ingredientsCount={}, preference={}", userId, ingredients.size(), preference);

        if(ingredients.isEmpty()) { // 아무것도 입력 안하고, 레시피를 생성했을 경우
            log.warn("빈 재료 정보가 입력되었습니다.");
            return new RecipeResponse("오류", List.of(), List.of("입력 재료가 없습니다."), "재료를 직접 입력하거나 선택해주세요.");
        }

        // 악의적 입력 검증
        if(preference.length() >= 100) {
            log.warn("너무 긴 요리 취향이 입력되었습니다.");
            return  new RecipeResponse("오류", List.of(), List.of("너무 긴 텍스트 입력입니다."), "100자 이내로 입력해주세요.");
        }

        // 재료 입력 중간에 빈 재료를 입력했을 경우, 빈칸 제거하고 칸 땡기기.
        ingredients.removeIf(ingredient -> ingredient.isEmpty());

        // 텍스트 기반 생성도 결국 최종 레시피 생성 로직(generateRecipe)과 동일합니다.
        return aiClientService.generateRecipe(ingredients, preference, userId);
    }
}