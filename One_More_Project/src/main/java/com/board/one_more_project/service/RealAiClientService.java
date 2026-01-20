package com.board.one_more_project.service;

import com.board.one_more_project.dto.IngredientAnalysisResponse;
import com.board.one_more_project.dto.RecipeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;


// [실제 AI 서버 통신 구현체]
@Slf4j
@Service
@Profile("prod") // application.yml에서 운영환경(prod) 설정되어야만 작동.
public class RealAiClientService implements AiClientService {

    private final RestClient restClient;

    // 0. application.yml에 정의된 파이썬 서버 URL로 RestClient 초기화
    public RealAiClientService(@Value("${ai-server.url}") String aiServerUrl) {

        // 하부 네트워크 엔진 설정: 연결(Connect) 자체에 대한 타임아웃을 설정합니다.
        // springframework 6버전 이상부터는 setConnectTimeout() 함수 사용이 불가능해서, JDK기본 기능으로 구현
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // 서버 생존 여부 확인(5초 이내)
                .build();

        // JdkClientHttpRequestFactory(): HTTP 통신을 위해 Apache등의 도움 없이 JDK기능으로 Spring의 HTTP 요청을 처리하게 해주는 툴
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient); // 틀과 HttpClient엔진을 연결
        factory.setReadTimeout(Duration.ofSeconds(60)); // 데이터 처리 시간 확인(60초 이내)

        // RestClient: RestTemplate상위 버전, 체이닝가능 / .builder(): 객체에 필요한 설정을 하나씩 추가하는 설정 모드
        this.restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .requestFactory(factory)
                .build(); // 객체 생성마무리
    }

    // 1. 이미지 분석
    @Override
    public IngredientAnalysisResponse analyzeIngredients(MultipartFile file, String preference, String type, String userId) {
        log.info("[prod] 파이썬 서버로 이미지 분석 요청을 보냅니다. (최대 60초 대기) User: {}", userId);

        // MultipartBodyBuilder: text, image등 여러 파일을 part별로 묶어서 보내는 객체
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());
        builder.part("preference", preference);
        builder.part("type", type);
        builder.part("userId", userId);

        try {
            return restClient.post()
                    .uri("/analyze-image") // @app.post("/analyze-image")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .retrieve() // 요청 전송, 응답 수신 둘 다 한 쓰레드에서 진행(Blocking방식)
                    .body(IngredientAnalysisResponse.class); // JSON 응답을 분석 DTO로 변환
        } catch (Exception e) {
            // 타임아웃 발생 시 혹은 네트워크 오류 시 로그를 남깁니다.
            log.error("이미지 분석 서버 통신 오류 (타임아웃 여부 확인 필요): {}", e.getMessage());
            return new IngredientAnalysisResponse(null, List.of("분석 실패 또는 시간 초과"), preference, "error");
        }
    }

    @Override
    public RecipeResponse generateRecipe(List<String> ingredients, String preference, String userId) {
        log.info("[prob] 파이썬 서버로 레시피 생성 요청을 보냅니다. User: {}", userId);

        try {
            return restClient.post()
                    .uri("/generate-recipe") // @app.post(파이썬에 미구현)
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