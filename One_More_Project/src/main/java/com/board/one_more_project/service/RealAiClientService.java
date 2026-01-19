package com.board.one_more_project.service;

import com.board.one_more_project.dto.IngredientAnalysisResponse;
import com.board.one_more_project.dto.RecipeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


//[실제 AI 서버 통신 구현체]
//@Profile("prod"): 운영 환경(prod) 프로파일이 활성화될 때만 스프링 빈으로 등록됩니다.
@Slf4j
@Service
@Profile("prod")
public class RealAiClientService implements AiClientService {

    private final RestClient restClient;

    public RealAiClientService(@Value("${ai-server.url}") String aiServerUrl) {
        // application.yml에 정의된 파이썬 서버 URL로 RestClient 초기화
        this.restClient = RestClient.create(aiServerUrl);
    }

    @Override
    public IngredientAnalysisResponse analyzeIngredients(MultipartFile file, String preference, String type, String userId) {
        log.info("[Real] 파이썬 서버로 이미지 분석 요청을 보냅니다. User: {}", userId);

        // Multipart/form-data 포장을 위한 빌더 생성
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource()); // 바이너리 이미지 파일 담기
        builder.part("preference", preference);   // 요리 취향 텍스트 담기
        builder.part("type", type);               // image 또는 receipt 타입 담기
        builder.part("userId", userId);           // 팀원 요청사항: 유저 ID 담기

        try {
            return restClient.post()
                    .uri("/analyze-image") // 파이썬의 이미지 분석 엔드포인트
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .retrieve()
                    .body(IngredientAnalysisResponse.class); // JSON 응답을 분석 DTO로 변환
        } catch (Exception e) {
            log.error("이미지 분석 서버 통신 중 오류: {}", e.getMessage());
            return new IngredientAnalysisResponse(null, List.of("분석 실패"), preference, "error");
        }
    }

    @Override
    public RecipeResponse generateRecipe(List<String> ingredients, String preference, String userId) {
        log.info("[Real] 파이썬 서버로 레시피 생성 요청을 보냅니다. User: {}", userId);

        try {
            return restClient.post()
                    .uri("/generate-recipe") // 파이썬의 레시피 생성 엔드포인트
                    .contentType(MediaType.APPLICATION_JSON) // 텍스트 데이터이므로 JSON 전송
                    .body(Map.of(
                            "ingredients", ingredients,
                            "preference", preference,
                            "userId", userId,
                            "type", "text" // 레시피 생성을 위한 고정 타입
                    ))
                    .retrieve()
                    .body(RecipeResponse.class); // JSON 응답을 레시피 DTO로 변환
        } catch (Exception e) {
            log.error("레시피 생성 서버 통신 중 오류: {}", e.getMessage());
            return new RecipeResponse("오류 발생", ingredients, List.of("통신 오류"), "다시 시도해주세요.");
        }
    }
}

//    private RecipeResponse createErrorResponse(String message) {
//        return new RecipeResponse("서비스 일시 중단", List.of(), List.of(message), "잠시 후 다시 시도해주세요.");
//    }
//}