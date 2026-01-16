package com.board.one_more_project.service;

import com.board.one_more_project.dto.RecipeResponse;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class RecipeService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper; // JSON 문자열을 객체로 변환하기 위함

    @Value("${openai.api.key}")
    private String apiKey;

    public RecipeService(ObjectMapper objectMapper) {
        // OpenAI API 주소로 RestClient를 초기화합니다.
        this.restClient = RestClient.create("https://api.openai.com/v1/chat/completions");
        this.objectMapper = objectMapper;
    }

    /**
     * 키워드 리스트를 받아 LLM에게 레시피 생성을 요청합니다.
     */
    public RecipeResponse generateRecipe(List<String> keywords, String preference) {
        // 1. 프롬프트 조립
        String ingredientsText = String.join(", ", keywords);
        String prompt = String.format(
                "냉장고에 있는 재료는 [%s]이야. 이 재료들을 활용해서 [%s] 스타일의 요리 레시피를 하나 만들어줘. " +
                        "답변은 반드시 아래의 JSON 형식으로만 대답해줘. 다른 설명은 하지마.\n" +
                        "{\n" +
                        "  \"title\": \"요리제목\",\n" +
                        "  \"ingredients\": [\"재료1\", \"재료2\"],\n" +
                        "  \"steps\": [\"1단계 설명\", \"2단계 설명\"],\n" +
                        "  \"tip\": \"꿀팁 내용\"\n" +
                        "}", ingredientsText, preference
        );

        // 2. OpenAI API 호출 (POST 요청)
        try {
            String responseBody = restClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", "gpt-4o", // 사용할 AI 모델
                            "messages", List.of(
                                    Map.of("role", "system", "content", "너는 친절하고 전문적인 요리사야."),
                                    Map.of("role", "user", "content", prompt)
                            ),
                            "response_format", Map.of("type", "json_object") // JSON 응답 강제 설정
                    ))
                    .retrieve()
                    .body(String.class);

            // 3. OpenAI의 복잡한 응답 JSON에서 우리가 원하는 "content" 부분만 추출하여 RecipeResponse로 변환
            // (실제로는 JSON 파싱 로직이 더 들어가야 하지만, 이해를 돕기 위해 핵심 흐름만 작성)
            var rootNode = objectMapper.readTree(responseBody);
            String contentJson = rootNode.path("choices").get(0).path("message").path("content").asText();

            return objectMapper.readValue(contentJson, RecipeResponse.class);

        } catch (Exception e) {
            // 에러 발생 시 로그를 찍고 null이나 예외를 던집니다.
            System.err.println("레시피 생성 중 오류 발생: " + e.getMessage());
            return null;
        }
    }
}