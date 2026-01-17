package com.board.one_more_project.controller;
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

@Tag(name = "Recipe", description = "AI 레시피 생성 관련 API") // @Tag: 이 컨트롤러가 어떤 기능을 담당하는지 그룹화합니다.
@Slf4j
@RestController
@RequestMapping("/api/recipe")
public class RecipeController {
    // 이 필드는 Class가 아닌 Interface를 가리킵니다.
    // 스프링이 실행될 때 Profile 설정에 따라 Real 또는 Mock 객체를 여기에 주입(DI)합니다.
    private final AiClientService aiClientService;
    public RecipeController(AiClientService aiClientService) {

        this.aiClientService = aiClientService;
    }

    // @Operation: 특정 API(메서드)가 어떤 일을 하는지 설명합니다.
    @Operation(summary = "이미지 기반 레시피 생성", description = "영수증 사진 또는 식재료 사진을 업로드하여 AI가 레시피를 생성합니다.")
    @PostMapping(value ="/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)  // consumes =  API가 Multipart 형식을 사용함을 명시.
    public RecipeResponse createRecipeFromImage(
            @Parameter(description = "업로드할 이미지 파일 (JPG, PNG)") @RequestParam("file") MultipartFile file,
            @Parameter(description = "유저의 요리 취향 (예: 매콤하게, 간단하게)") @RequestParam("preference") String preference,
            @Parameter(description = "이미지 종류 (receipt: 영수증, ingredients: 재료사진)") @RequestParam("type") String type
    ) {
        log.info("이미지 레시피 요청 수신: type={}, filename={}", type, file.getOriginalFilename()); // 요청 기록

        if (file.isEmpty()) { // 아무것도 입력 안하고, 레시피를 생성했을 경우
            log.warn("빈 파일이 업로드되었습니다."); // 경고 로그
            return new RecipeResponse("오류", List.of(), List.of("파일이 없습니다."), "사진을 업로드해주세요.");
        }
        // 악의적 입력 검증
        if(preference.length() >= 100) {
            log.warn("너무 긴 요리 취향이 입력되었습니다.");
            return  new RecipeResponse("오류", List.of(), List.of("너무 긴 텍스트 입력입니다."), "100자 이내로 입력해주세요.");
        }

        // 2. Python 서버로 중계
        return aiClientService.sendImageForRecipe(file, preference, type);
    }

    @Operation(summary = "텍스트 기반 레시피 생성", description = "직접 입력한 재료 리스트를 바탕으로 AI가 레시피를 생성합니다.")
    @PostMapping("/text")
    public RecipeResponse createRecipeFromText(
            @RequestBody List<String> ingredients,
            @Parameter(description = "유저의 요리 취향") @RequestParam("preference") String preference
    ) {
        // 텍스트 입력은 이미 재료 리스트이므로 type 구분이 필요 없음
        log.info("텍스트 레시피 요청 수신: ingredientsCount={}, preference={}", ingredients.size(), preference);

        if(ingredients.isEmpty()) { // 아무것도 입력 안하고, 레시피를 생성했을 경우
            log.warn("빈 재료 정보가 입력되었습니다.");
            return new RecipeResponse("오류", List.of(), List.of("입력 재료가 없습니다."), "재료를 직접 입력하거나 선택해주세요.");
        }

        // 악의적 입력 검증
        if(preference.length() >= 100) {
            log.warn("너무 긴 요리 취향이 입력되었습니다.");
            return  new RecipeResponse("오류", List.of(), List.of("너무 긴 텍스트 입력입니다."), "100자 이내로 입력해주세요.");
        }

        ingredients.removeIf(ingredient -> ingredient.isEmpty()); // 재료 입력 중간에 빈 재료를 입력했을 경우, 빈칸 제거하고 칸 땡기기.

        return aiClientService.sendTextForRecipe(ingredients, preference);
    }
}