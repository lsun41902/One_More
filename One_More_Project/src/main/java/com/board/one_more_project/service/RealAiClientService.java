package com.board.one_more_project.service;

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

    // 생성자 주입을 통해 application.yml의 url을 가져와 RestClient를 초기화합니다.
    public RealAiClientService(@Value("${ai-server.url}") String aiServerUrl) {
        this.restClient = RestClient.create(aiServerUrl);
    }

    @Override
    public RecipeResponse sendImageForRecipe(MultipartFile file, String preference, String type) {
        log.info("[Real] 파이썬 서버로 이미지 분석 요청을 보냅니다.");
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());
        builder.part("preference", preference);
        builder.part("type", type);

        try {
            return restClient.post()
                    .uri("/analyze-image")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .retrieve()
                    .body(RecipeResponse.class);
        } catch (Exception e) {
            log.error("Python 서버 통신 중 오류 발생: {}", e.getMessage());
            return createErrorResponse("AI 분석 서버와 통신 중 오류가 발생했습니다.");
        }
    }

    @Override
    public RecipeResponse sendTextForRecipe(List<String> ingredients, String preference) {
        log.info("[Real] 파이썬 서버로 텍스트 레시피 요청을 보냅니다.");
        try {
            return restClient.post()
                    .uri("/analyze-text")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "ingredients", ingredients,
                            "preference", preference,
                            "type", "text"
                    ))
                    .retrieve()
                    .body(RecipeResponse.class);
        } catch (Exception e) {
            // log.error를 사용하되, e.getMessage()만 찍어서 눈이 편하게 만듭니다.
            // 만약 전체 에러 원인이 궁금하면 log.error("에러 발생", e); 처럼 e를 뒤에 붙이면 됩니다.
            log.error("Python 서버 통신 에러 발생: {}", e.getMessage());
            return createErrorResponse("레시피 생성 중 오류가 발생했습니다.");
        }
    }

    private RecipeResponse createErrorResponse(String message) {
        return new RecipeResponse("서비스 일시 중단", List.of(), List.of(message), "잠시 후 다시 시도해주세요.");
    }
}