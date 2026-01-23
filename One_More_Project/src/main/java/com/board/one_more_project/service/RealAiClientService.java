package com.board.one_more_project.service;
import com.board.one_more_project.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile("prod") // 운영(prod) 환경에서만 작동
public class RealAiClientService implements AiClientService {

    private final RestTemplate restTemplate;
    private final String aiServerUrl;

    private record PythonResponseWrapper<genericType>(List<genericType> result) {}

    // 반복되는 파이썬의 응답 구조 { "result": [...] }를 받기 위한 Wrapper class
    public RealAiClientService(@Value("${ai-server.url}") String aiServerUrl) {
        this.aiServerUrl = aiServerUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(5000);
        factory.setReadTimeout(120000);

        this.restTemplate = new RestTemplate(factory);
        log.info("[prod] RealAiClientService가 초기화되었습니다. Target URL: {}", aiServerUrl);
    }
    //region 이미지 분석 함수
    @Override
    public List<IngredientAnalysisResponse> analyzeImageReceipt(List<MultipartFile> files, List<String> preferences, String userId) {
        return sendImageRequest("/analyze-image-receipts", files, preferences, userId);
    }

    @Override
    public List<IngredientAnalysisResponse> analyzeImageIngredients(List<MultipartFile> files, List<String> preferences, String userId) {
        return sendImageRequest("/analyze-image-ingredients", files, preferences, userId);
    }

    private List<IngredientAnalysisResponse> sendImageRequest(String uri, List<MultipartFile> files, List<String> preferences, String userId) {
        log.info("[prod] 이미지 분석 요청: URI={}, Count={}, User={}", uri, files.size(), userId);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // [핵심] 리스트로 받은 파일을 순회하며 동일한 키("files")로 추가
        if (files != null) {
            for (MultipartFile file : files) {
                body.add("files", file.getResource());
            }
        }

        if (preferences != null) {
            for (String pref : preferences) {
                body.add("preference", pref);
            }
        }
        body.add("userId", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // MultiValueMap body와 headers를 HttpEntity로 합친다.
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {// 2. exchange 메서드를 사용하여 제네릭 타입(Wrapper<List<Response>>)을 명시
            ResponseEntity<PythonResponseWrapper<IngredientAnalysisResponse>> responseEntity =
                    restTemplate.exchange(
                            aiServerUrl + uri,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PythonResponseWrapper<IngredientAnalysisResponse>>() {}
                    );
            PythonResponseWrapper<IngredientAnalysisResponse> response = responseEntity.getBody();
            return response != null ? response.result() : Collections.emptyList();
        } catch (Exception e) {
            log.error("이미지 분석 통신 오류 (URI: {}): {}", uri, e.getMessage());
            throw new RuntimeException("AI 서버 통신 오류: " + e.getMessage());
        }

    }
    //endregion

    //region 레시피 생성 함수
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

    private List<RecipeResponse> sendRecipeRequest(String uri, RecipeGenerationRequest request) {
        log.info("[prod] 레시피 생성 요청 전송: URI={}, User={}", uri, request.userId());

        Map<String, Object> body = new HashMap<>();
        body.put("userId", request.userId());
        body.put("ingredients", request.ingredients());
        body.put("spices", request.spices());
        body.put("preferences", request.preferences());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<PythonResponseWrapper<RecipeResponse>> responseEntity =
                    restTemplate.exchange(
                            aiServerUrl + uri,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PythonResponseWrapper<RecipeResponse>>() {}
                    );

            PythonResponseWrapper<RecipeResponse> response = responseEntity.getBody();
            return response != null ? response.result() : Collections.emptyList();

        } catch (Exception e) {
            log.error("레시피 생성 통신 오류 (URI: {}): {}", uri, e.getMessage());
            throw new RuntimeException("AI 서버 레시피 생성 실패: " + e.getMessage());
        }
    }
    //endregion
}