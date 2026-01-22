package com.board.one_more_project.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.board.one_more_project.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile("prod") // 운영(prod) 환경에서만 작동
public class RealAiClientService implements AiClientService {

    private final RestClient restClient;
    @Value("${ai-server.url}")
    private String aiServerUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 반복되는 파이썬의 응답 구조 { "result": [...] }를 받기 위한 Wrapper class
    private record PythonResponseWrapper<T>(List<T> result) {}

    public RealAiClientService(@Value("${ai-server.url}") String aiServerUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // 서버 연결 타임아웃
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(60)); // 데이터 수신 타임아웃

        this.restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .requestFactory(factory)
                .build();

    }
    @Override
    public List<IngredientAnalysisResponse> analyzeImageReceipt(MultipartFile file, List<String> preference, String userId) {
        return sendImageRequest("/analyze-image-receipts", file, preference, userId);
    }

    @Override
    public List<IngredientAnalysisResponse> analyzeImageIngredients(MultipartFile file, List<String> preference, String userId) {
        return sendImageRequest("/analyze-image-ingredients", file, preference, userId);
    }

    private List<IngredientAnalysisResponse> sendImageRequest(String uri, @RequestParam("file") MultipartFile file, @RequestParam("preference")List<String> preference, @RequestParam("userId")String userId) {
        log.info("[prod] 이미지 분석 요청 URI: {}, User: {}", uri, userId);
        // 1. RestTemplate 사용 (가장 확실함)
        RestTemplate restTemplate = new RestTemplate();

        // 2. 바디 구성
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", file.getResource());
        body.add("preference", preference);
        body.add("userId", "local_tester");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {

            // 2. exchange 메서드를 사용하여 제네릭 타입(Wrapper<List<Response>>)을 명시
            ResponseEntity<PythonResponseWrapper<IngredientAnalysisResponse>> responseEntity =
                    restTemplate.exchange(
                            aiServerUrl+uri,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PythonResponseWrapper<IngredientAnalysisResponse>>() {}
                    );
            // 3. 주석 코드와 동일한 로직으로 결과 반환
            PythonResponseWrapper<IngredientAnalysisResponse> response = responseEntity.getBody();
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

    private List<RecipeResponse> sendRecipeRequest(String uri, RecipeGenerationRequest request) {
        log.info("[prod] 레시피 생성 요청 URI: {}, User: {}", uri, request.userId());

        RestTemplate restTemplate = new RestTemplate();

        // Body 구성
        Map<String, Object> body = new HashMap<>();
        body.put("userId", request.userId());
        body.put("ingredients", request.ingredients());
        body.put("spices", request.spices());
        // body.put("action", request.action());
        body.put("preferences", request.preference()); // 파이썬이 원하는 키 이름

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // [중요] 일단 String.class로 받아서 내용을 눈으로 확인합니다.
            // 2. exchange 메서드를 사용하여 제네릭 타입(Wrapper<List<Response>>)을 명시
            ResponseEntity<PythonResponseWrapper<RecipeResponse>> responseEntity =
                    restTemplate.exchange(
                            aiServerUrl+uri,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PythonResponseWrapper<RecipeResponse>>() {}
                    );
            // 3. 주석 코드와 동일한 로직으로 결과 반환
            PythonResponseWrapper<RecipeResponse> response = responseEntity.getBody();
            return response != null ? response.result() : Collections.emptyList();

        } catch (Exception e) {
            log.error("데이터 파싱 오류! 원본 응답을 확인하세요. 메시지: {}", e.getMessage());
            throw new RuntimeException("AI 서버 응답 파싱 실패");
        }
    }
}