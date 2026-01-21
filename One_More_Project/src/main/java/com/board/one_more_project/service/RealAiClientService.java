package com.board.one_more_project.service;

import com.board.one_more_project.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Profile("prod") // 운영(prod) 환경에서만 작동
public class RealAiClientService implements AiClientService {

    private final RestClient restClient;

    // 반복되는 파이썬의 응답 구조 { "result": [...] }를 받기 위한 Wrapper class
    // 이 클래스는 이 파일 안에서만 쓰이므로 내부에 작성함.
    // genericType: 여러 dto 타입의 데이터를 요청하게 되는데, 일일히 객체를 생성하지 않고, 필요한 타입의 객체를 생성할 수 있게 함.
    private record PythonResponseWrapper<genericType>(List<genericType> result) {}

    public RealAiClientService(@Value("${ai-server.url}") String aiServerUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // 서버 로딩 타임아웃 설정
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(60)); // 파이썬 서버 응답 타임아웃 설정

        this.restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public List<IngredientAnalysisResponse> analyzeImageIngredients(MultipartFile file, List<String> preference, String userId) {
        // 재료 이미지 분석 URL 호출
        return sendImageRequest("/analyze-image-ingredients", file, preference, userId);
    }

    @Override
    public List<IngredientAnalysisResponse> analyzeImageReceipt(MultipartFile file, List<String> preference, String userId) {
        // 영수증 분석 URL 호출
        return sendImageRequest("/analyze-image-receipts", file, preference, userId);
    }

    // 이미지 전송 로직
    private List<IngredientAnalysisResponse> sendImageRequest(String uri, MultipartFile file, List<String> preference, String userId) {
        log.info("[prod] 이미지 분석 요청 URI: {}, User: {}", uri, userId);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("files", file.getResource());
        builder.part("preference", preference);
        builder.part("userId", userId);

        try {
            PythonResponseWrapper<IngredientAnalysisResponse> response = restClient.post()
                    .uri(uri) // 전달받은 URI 사용
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<PythonResponseWrapper<IngredientAnalysisResponse>>() {});

            return response != null ? response.result() : Collections.emptyList();
        } catch (Exception e) {
            log.error("이미지 분석 통신 오류 (URI: {}): {}", uri, e.getMessage());
            throw new RuntimeException("AI 서버 통신 오류: " + e.getMessage());
        }
    }

    @Override
    public List<RecipeResponse> generateRecipeInitial(RecipeGenerationRequest request) {
        return sendRecipeRequest("/recipes-generate-initial", request);
    }

    @Override
    public List<RecipeResponse> generateRecipeBasic(RecipeGenerationRequest request) {
        return sendRecipeRequest("/recipes-generate-basic", request);
    }

    @Override
    public List<RecipeResponse> generateRecipeMore(RecipeGenerationRequest request) {
        return sendRecipeRequest("/recipes-generate-more", request);
    }

    // 레시피(JSON) 전송 로직
    private List<RecipeResponse> sendRecipeRequest(String uri, RecipeGenerationRequest request) {
        log.info("[prod] 레시피 생성 요청 URI: {}, User: {}", uri, request.userId());

        try {
            PythonResponseWrapper<RecipeResponse> response = restClient.post()
                    .uri(uri) // 전달받은 URI 사용
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<PythonResponseWrapper<RecipeResponse>>() {});

            return response != null ? response.result() : Collections.emptyList();
        } catch (Exception e) {
            log.error("레시피 생성 통신 오류 (URI: {}): {}", uri, e.getMessage());
            throw new RuntimeException("AI 서버 통신 오류: " + e.getMessage());
        }
    }
}