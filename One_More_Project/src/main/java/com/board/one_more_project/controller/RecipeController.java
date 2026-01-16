package com.board.one_more_project.controller;

import com.board.one_more_project.dto.RecipeResponse;
import com.board.one_more_project.service.AiClientService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * [레시피 요청 컨트롤러]
 * 역할: 유저의 입력을 받아 AI 서버(Python)로 중계하고 결과를 반환합니다.
 */
@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private final AiClientService aiClientService;

    // 생성자 주입 (RecipeService는 이제 필요 없으므로 제거되었습니다.)
    public RecipeController(AiClientService aiClientService) {
        this.aiClientService = aiClientService;
    }

    /**
     * 사진 업로드 시 레시피 생성 요청
     */
    @PostMapping("/upload-image")
    public RecipeResponse createRecipeFromImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("preference") String preference
    ) {
        // Python 서버로 중계 후 결과 반환
        return aiClientService.sendImageForRecipe(file, preference);
    }

    /**
     * 텍스트 입력 시 레시피 생성 요청
     */
    @PostMapping("/text")
    public RecipeResponse createRecipeFromText(
            @RequestBody List<String> ingredients,
            @RequestParam("preference") String preference
    ) {
        // Python 서버로 중계 후 결과 반환
        return aiClientService.sendTextForRecipe(ingredients, preference);
    }
}