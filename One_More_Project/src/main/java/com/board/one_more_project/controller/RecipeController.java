package com.board.one_more_project.controller;

import com.board.one_more_project.dto.RecipeResponse;
import com.board.one_more_project.service.AiClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * [레시피 요청 컨트롤러]
 * 역할: 유저의 입력을 받아 AI 서버(Python)로 중계하고 결과를 반환합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private final AiClientService aiClientService;

    public RecipeController(AiClientService aiClientService) {

        this.aiClientService = aiClientService;
    }

    // 사진 업로드 시 레시피 생성 요청
    @PostMapping("/upload-image")
    public RecipeResponse createRecipeFromImage(
            @RequestParam("file") MultipartFile file, // @param file: 이미지 파일
            @RequestParam("preference") String preference, // @param preference: 유저 취향 (예: 매콤하게, 비건 등)
            @RequestParam("type") String type // @param type: 이미지 종류
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

    /**
     * 텍스트 입력 시 레시피 생성 요청
     */
    @PostMapping("/text")
    public RecipeResponse createRecipeFromText(
            @RequestBody List<String> ingredients,
            @RequestParam("preference") String preference
    ) {
        // 텍스트 입력은 이미 재료 리스트이므로 type 구분이 필요 없음
        return aiClientService.sendTextForRecipe(ingredients, preference);
    }
}