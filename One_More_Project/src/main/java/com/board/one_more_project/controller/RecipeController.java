package com.board.one_more_project.controller;

import com.board.one_more_project.dto.RecipeResponse;
import com.board.one_more_project.service.AiClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Recipe", description = "AI 레시피 생성 관련 API") // @Tag: 이 컨트롤러가 어떤 기능을 담당하는지 그룹화합니다.
@Slf4j
@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private final AiClientService aiClientService;

    public RecipeController(AiClientService aiClientService) {

        this.aiClientService = aiClientService;
    }

    // @Operation: 특정 API(메서드)가 어떤 일을 하는지 설명합니다.
    @Operation(summary = "이미지 기반 레시피 생성", description = "영수증 사진 또는 식재료 사진을 업로드하여 AI가 레시피를 생성합니다.")
    @PostMapping("/upload-image")  // 사진 업로드 시 레시피 생성 요청
    public RecipeResponse createRecipeFromImage(
            @Parameter(description = "업로드할 이미지 파일 (JPG, PNG)") @RequestParam("file") MultipartFile file,
            @Parameter(description = "유저의 요리 취향 (예: 매콤하게, 간단하게)") @RequestParam("preference") String preference,
            @Parameter(description = "이미지 종류 (receipt: 영수증, ingredients: 재료사진)") @RequestParam("type") String type
    ) {
        // log.info: 요청 기록이 남음.
        log.info("이미지 레시피 요청 수신: type={}, filename={}", type, file.getOriginalFilename());

        // 1. 간단한 데이터 검증 (비어있는지 확인)
        if (file.isEmpty()) {
            log.warn("빈 파일이 업로드되었습니다."); // 경고 로그
            return new RecipeResponse("오류", List.of(), List.of("파일이 없습니다."), "사진을 업로드해주세요.");
        }

        // 2. Python 서버로 중계 (type 정보를 함께 보냄)
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
        return aiClientService.sendTextForRecipe(ingredients, preference);
    }
}