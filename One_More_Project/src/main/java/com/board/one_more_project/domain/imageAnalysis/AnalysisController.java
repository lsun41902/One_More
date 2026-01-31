package com.board.one_more_project.domain.imageAnalysis;

import com.board.one_more_project.domain.ingredient.IngredientAnalysisResponse;
import com.board.one_more_project.domain.recipe.RecipeValidator;
import com.board.one_more_project.infrastructure.ai.AiClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter; // 추가됨
import io.swagger.v3.oas.annotations.media.Content; // 추가됨
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AiClientService aiClientService;
    private final RecipeValidator validator;

    public AnalysisController(AiClientService aiClientService, RecipeValidator validator) {
        this.aiClientService = aiClientService;
        this.validator = validator;
    }

    @Operation(summary = "이미지/영수증 분석", description = "사진을 분석하여 식재료 리스트를 추출합니다.")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<IngredientAnalysisResponse> analyze(
            // [수정 포인트] @RequestParam -> @RequestPart 변경
            // Swagger가 파일을 올바르게 인식하도록 @Parameter 설정 추가 (선택사항이나 권장됨)
            @Parameter(description = "업로드할 이미지 파일들", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestPart("files") List<MultipartFile> files,

            // 나머지 텍스트 필드는 @RequestParam 유지 (form-data의 텍스트 필드로 처리됨)
            @Parameter(description = "분석 타입 (image 또는 receipt)")
            @RequestParam("type") String type,

            @Parameter(description = "사용자 ID")
            @RequestParam(value = "userId", defaultValue = "user_01") String userId
    ) {
        log.info("이미지 분석 요청 수신: type={}, count={}", type, files.size());
        validator.validateFiles(files);

        if ("receipt".equalsIgnoreCase(type)) {
            return aiClientService.analyzeImageReceipt(files, userId);
        }
        return aiClientService.analyzeImageIngredients(files, userId);
    }
}