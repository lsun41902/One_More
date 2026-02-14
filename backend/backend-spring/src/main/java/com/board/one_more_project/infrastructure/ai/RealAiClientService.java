package com.board.one_more_project.infrastructure.ai;

import com.board.one_more_project.domain.ingredient.dto.IngredientAnalysisResponse;
import com.board.one_more_project.domain.recipe.RecipeGenerationRequest;
import com.board.one_more_project.domain.recipe.RecipeResponse;
import com.board.one_more_project.global.error.exception.AiServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor // [변경] 생성자 주입 (Config에서 만든 RestTemplate을 받아옴)
public class RealAiClientService implements AiClientService {

    private final RestTemplate restTemplate; // 주입받은 강력한 RestTemplate

    @Value("${ai-server.url}")
    private String aiServerUrl;

    private record PythonResponseWrapper<genericType>(List<genericType> result) {}

    //region 이미지 분석 함수
    @Override
    public List<IngredientAnalysisResponse> analyzeImageReceipt(List<MultipartFile> files, String userId) {
        return sendImageRequest("/analyze-image-receipts", files, userId);
    }

    @Override
    public List<IngredientAnalysisResponse> analyzeImageIngredients(List<MultipartFile> files, String userId) {
        return sendImageRequest("/analyze-image-ingredients", files, userId);
    }

    // [원래대로 복구됨] 이미지 요청 전용 메서드
    private List<IngredientAnalysisResponse> sendImageRequest(String uri, List<MultipartFile> files, String userId) {
        log.info("[prod] 이미지 분석 요청: URI={}, Count={}, User={}", uri, (files != null ? files.size() : 0), userId);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (files != null) {
            for (MultipartFile file : files) {
                body.add("files", file.getResource());
            }
        }
        body.add("userId", userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<PythonResponseWrapper<IngredientAnalysisResponse>> responseEntity =
                    restTemplate.exchange(
                            aiServerUrl + uri,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PythonResponseWrapper<IngredientAnalysisResponse>>() {}
                    );
            PythonResponseWrapper<IngredientAnalysisResponse> response = responseEntity.getBody();
            return response != null ? response.result() : Collections.emptyList();

        } catch (ResourceAccessException e) {
            log.error("AI 서버 연결 실패 (URI: {}): {}", uri, e.getMessage());
            throw new AiServerException("현재 AI 분석 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
        } catch (RestClientException e) {
            log.error("AI 서버 응답 오류 (URI: {}): {}", uri, e.getMessage());
            throw new AiServerException("AI 분석 중 오류가 발생했습니다. 관리자에게 문의하세요.");
        } catch (Exception e) {
            log.error("알 수 없는 오류 (URI: {}): {}", uri, e.getMessage());
            throw new AiServerException("서비스 처리 중 예기치 않은 오류가 발생했습니다.");
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

    @Override
    public List<RecipeResponse> generateRecipeReal(RecipeGenerationRequest request) {
        return sendRecipeRequest("/recipes-generate-real", request);
    }

    // [원래대로 복구됨] 레시피 요청 전용 메서드
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

        } catch (ResourceAccessException e) {
            log.error("AI 서버 연결 실패 (URI: {}): {}", uri, e.getMessage());
            throw new AiServerException("현재 AI 요리사 서버가 응답하지 않습니다. (Connection Error)");
        } catch (RestClientException e) {
            log.error("AI 서버 응답 오류 (URI: {}): {}", uri, e.getMessage());
            throw new AiServerException("레시피 생성 중 AI 서버 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("알 수 없는 오류 (URI: {}): {}", uri, e.getMessage());
            throw new AiServerException("서비스 처리 중 예기치 않은 오류가 발생했습니다.");
        }
    }
    //endregion
}