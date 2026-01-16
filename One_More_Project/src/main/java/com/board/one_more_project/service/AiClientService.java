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

    // application.yml에 설정된 Python 서버 주소를 사용합니다.
    public AiClientService(@Value("${ai-server.url}") String aiServerUrl) {
        this.restClient = RestClient.create(aiServerUrl);
    }

    /**
     * 1. 이미지를 Python 서버로 보내고 최종 레시피를 받아옵니다.
     */
    public RecipeResponse sendImageForRecipe(MultipartFile file, String preference) {
        // 멀티파트 데이터(이미지 + 취향 정보) 생성
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());
        builder.part("preference", preference); // 유저의 취향 정보도 함께 전달

        try {
            return restClient.post()
                    .uri("/analyze-image") // Python 서버의 이미지 기반 레시피 생성 엔드포인트
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .retrieve()
                    .body(RecipeResponse.class); // Python이 준 JSON을 바로 레시피 객체로 변환
        } catch (Exception e) {
            System.err.println("이미지 분석 및 레시피 생성 중 오류: " + e.getMessage());
            return createErrorResponse("이미지 분석에 실패했습니다.");
        }
    }

    /**
     * 2. 텍스트 재료 리스트를 Python 서버로 보내고 최종 레시피를 받아옵니다.
     */
    public RecipeResponse sendTextForRecipe(List<String> ingredients, String preference) {
        try {
            return restClient.post()
                    .uri("/analyze-text") // Python 서버의 텍스트 기반 레시피 생성 엔드포인트
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "ingredients", ingredients,
                            "preference", preference
                    ))
                    .retrieve()
                    .body(RecipeResponse.class);
        } catch (Exception e) {
            System.err.println("텍스트 기반 레시피 생성 중 오류: " + e.getMessage());
            return createErrorResponse("레시피 생성에 실패했습니다.");
        }
    }

    // 에러 발생 시 사용자에게 보여줄 임시 응답 생성 메서드
    private RecipeResponse createErrorResponse(String message) {
        return new RecipeResponse(
                "오류 발생",
                List.of(),
                List.of(message),
                "잠시 후 다시 시도해주세요."
        );
    }
}