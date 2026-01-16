package com.board.one_more_project.controller;

import com.board.one_more_project.dto.AiAnalysisResponse;
import com.board.one_more_project.dto.RecipeResponse;
import com.board.one_more_project.service.AiClientService;
import com.board.one_more_project.service.RecipeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private final AiClientService aiClientService;
    private final RecipeService recipeService;

    public RecipeController(AiClientService aiClientService, RecipeService recipeService) {
        this.aiClientService = aiClientService;
        this.recipeService = recipeService;
    }

    /**
     * [전체 실행 흐름]
     * 1. 유저로부터 이미지와 취향(preference)을 받음
     * 2. aiClientService를 통해 Python 서버에 이미지를 보내고 키워드 리스트를 받아옴
     * 3. 받아온 키워드 리스트를 recipeService(LLM)에게 넘겨서 레시피를 생성함
     * 4. 최종 레시피를 유저에게 반환함
     */
    @PostMapping("/upload-image")
    public RecipeResponse createRecipeFromImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("preference") String preference
    ) {
        // [Step 1 & 2] Python 서버로 이미지 전송 및 분석 결과(키워드) 수신
        AiAnalysisResponse aiResponse = aiClientService.sendImageForAnalysis(file);

        // [Step 3] 추출된 키워드가 있는지 확인
        List<String> keywords = aiResponse.keywords();

        if (keywords == null || keywords.isEmpty()) {
            // 만약 AI가 아무것도 인식 못 했다면 예외 처리가 필요합니다.
            return new RecipeResponse("인식된 재료가 없습니다.", List.of(), List.of(), "사진을 다시 찍어주세요.");
        }

        // [Step 4] 키워드 리스트를 LLM 서비스에 전달하여 레시피 생성
        return recipeService.generateRecipe(keywords, preference);
    }

    /**
     * 텍스트로 직접 입력했을 때 (AI 서버를 거칠 필요 없이 바로 LLM 호출)
     */
    @PostMapping("/text")
    public RecipeResponse createRecipeFromText(
            @RequestBody List<String> ingredients,
            @RequestParam("preference") String preference
    ) {
        return recipeService.generateRecipe(ingredients, preference);
    }
}