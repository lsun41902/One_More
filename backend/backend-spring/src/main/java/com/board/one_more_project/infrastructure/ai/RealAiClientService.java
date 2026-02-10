package com.board.one_more_project.infrastructure.ai;

import com.board.one_more_project.domain.ingredient.dto.IngredientAnalysisResponse;
import com.board.one_more_project.domain.recipe.RecipeGenerationRequest;
import com.board.one_more_project.domain.recipe.RecipeResponse;
import com.board.one_more_project.global.error.exception.AiServerException;
import lombok.RequiredArgsConstructor; // 추가됨: 생성자 자동 생성
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor // final이 붙은 필드의 생성자를 자동으로 만들어줍니다.
public class RealAiClientService implements AiClientService {

    private final RestTemplate restTemplate; // 아까 설정 파일에서 만든 걸 주입받음

    @Value("${ai-server.url}")
    private String aiServerUrl;

    // 파이썬 응답 껍데기 (이건 그대로 둠)
    private record PythonResponseWrapper<T>(List<T> result) {}

    // --- [1. 이미지 분석 기능] ---
    @Override
    public List<IngredientAnalysisResponse> analyzeImageReceipt(List<MultipartFile> files, String userId) {
        return sendImageRequest("/analyze-image-receipts", files, userId);
    }

    @Override
    public List<IngredientAnalysisResponse> analyzeImageIngredients(List<MultipartFile> files, String userId) {
        return sendImageRequest("/analyze-image-ingredients", files, userId);
    }

    // --- [2. 레시피 생성 기능] ---
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

    @Override
    public List<RecipeResponse> generateRecipeReal(RecipeGenerationRequest request) {
        return sendRecipeRequest("/recipes-generate-real", request);
    }

    // --- [공통 내부 함수: 이미지 전송용] ---
    private List<IngredientAnalysisResponse> sendImageRequest(String uri, List<MultipartFile> files, String userId) {
        log.info("[AI 요청] 이미지 분석: URI={}, User={}", uri, userId);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (files != null) {
            for (MultipartFile file : files) {
                body.add("files", file.getResource());
            }
        }
        body.add("userId", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return sendRequestToAiServer(uri, new HttpEntity<>(body, headers), new ParameterizedTypeReference<>() {});
    }

    // --- [공통 내부 함수: 레시피 요청용] ---
    private List<RecipeResponse> sendRecipeRequest(String uri, RecipeGenerationRequest request) {
        log.info("[AI 요청] 레시피 생성: URI={}, User={}", uri, request.userId());

        Map<String, Object> body = new HashMap<>();
        body.put("userId", request.userId());
        body.put("ingredients", request.ingredients());
        body.put("spices", request.spices());
        body.put("preferences", request.preferences());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return sendRequestToAiServer(uri, new HttpEntity<>(body, headers), new ParameterizedTypeReference<>() {});
    }

    // --- [진짜 핵심: 실제로 보내는 곳을 하나로 통합] ---
    // 중복되는 try-catch 문을 하나로 합쳤습니다.
    private <T> List<T> sendRequestToAiServer(String uri, HttpEntity<?> entity, ParameterizedTypeReference<PythonResponseWrapper<T>> responseType) {
        try {
            ResponseEntity<PythonResponseWrapper<T>> responseEntity = restTemplate.exchange(
                    aiServerUrl + uri,
                    HttpMethod.POST,
                    entity,
                    responseType
            );
            PythonResponseWrapper<T> body = responseEntity.getBody();
            return body != null ? body.result() : Collections.emptyList();

        } catch (ResourceAccessException e) {
            log.error("AI 서버 연결 실패 (URI: {}): {}", uri, e.getMessage());
            throw new AiServerException("AI 서버에 연결할 수 없습니다. (Timeout/Connection Error)");
        } catch (RestClientException e) {
            log.error("AI 서버 응답 오류 (URI: {}): {}", uri, e.getMessage());
            throw new AiServerException("AI 분석 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("알 수 없는 오류 (URI: {}): {}", uri, e.getMessage());
            throw new AiServerException("예기치 않은 오류가 발생했습니다.");
        }
    }
}