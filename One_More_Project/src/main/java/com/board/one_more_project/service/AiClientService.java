package com.board.one_more_project.service;

import com.board.one_more_project.dto.RecipeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * [AI 서버 통신 전담 서비스]
 * 역할: Python 서버로 데이터를 전달하고, 최종 레시피를 받아옵니다.
 */
@Service
public class AiClientService {

    private final RestClient restClient;

    public AiClientService(@Value("${ai-server.url}") String aiServerUrl) {
        this.restClient = RestClient.create(aiServerUrl);
    }

    /**
     * 이미지를 Python 서버로 보내고 최종 레시피를 받아옵니다.
     * @param type: "receipt" (영수증-OCR) 또는 "ingredients" (재료사진-YOLO)
     */
    public RecipeResponse sendImageForRecipe(MultipartFile file, String preference, String type) {
        // 멀티파트 데이터 생성 (파일 + 취향 + 타입)
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());
        builder.part("preference", preference);
        builder.part("type", type); // Python 서버에서 이 값을 보고 모델을 결정함

        try {
            return restClient.post()
                    .uri("/analyze-image")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .retrieve()
                    .body(RecipeResponse.class);
        } catch (Exception e) {
            // 에러 발생 시 로그 출력 (시니어의 팁: 에러 메시지를 자세히 남기면 디버깅이 쉬워집니다.)
            System.err.println("Python 서버 통신 중 오류 발생: " + e.getMessage());
            return createErrorResponse("AI 분석 서버와 통신 중 오류가 발생했습니다.");
        }
    }

    /**
     * 텍스트 재료 리스트를 Python 서버로 보냅니다.
     */
    public RecipeResponse sendTextForRecipe(List<String> ingredients, String preference) {
        try {
            return restClient.post()
                    .uri("/analyze-text")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "ingredients", ingredients,
                            "preference", preference,
                            "type", "text" // 텍스트임을 명시
                    ))
                    .retrieve()
                    .body(RecipeResponse.class);
        } catch (Exception e) {
            return createErrorResponse("레시피 생성 중 오류가 발생했습니다.");
        }
    }

    private RecipeResponse createErrorResponse(String message) {
        return new RecipeResponse(
                "서비스 일시 중단",
                List.of(),
                List.of(message),
                "잠시 후 다시 시도해주세요."
        );
    }
}