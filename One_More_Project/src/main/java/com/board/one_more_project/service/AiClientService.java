package com.board.one_more_project.service;

import com.board.one_more_project.dto.AiAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

/**
 * [실전용 AI 클라이언트 서비스]
 * 역할: Python FastAPI 서버에 이미지를 전송하고, 분석된 키워드 리스트를 받아옵니다.
 */
@Service
public class AiClientService {

    // Spring Boot의 최신 HTTP 클라이언트인 RestClient를 사용합니다.
    private final RestClient restClient;

    /**
     * 생성자에서 RestClient를 초기화합니다.
     * @param aiServerUrl application.yml에 정의된 ai-server.url 값을 가져옵니다.
     */
    public AiClientService(@Value("${ai-server.url:http://localhost:8000}") String aiServerUrl) {
        this.restClient = RestClient.create(aiServerUrl);
    }

    /**
     * [핵심 메서드] 사용자가 업로드한 이미지를 Python 서버로 보냅니다.
     * @param file 사용자가 보낸 이미지 파일
     * @return AiAnalysisResponse (Python이 분석한 키워드 리스트가 담긴 객체)
     */
    public AiAnalysisResponse sendImageForAnalysis(MultipartFile file) {

        // 1. Spring에서 제공하는 MultipartBodyBuilder를 사용하여 파일을 포장합니다.
        // Python 서버(FastAPI)가 "file"이라는 이름으로 받기로 약속했다면 이름을 맞춰야 합니다.
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());

        try {
            // 2. Python 서버의 /analyze-image 엔드포인트로 POST 요청을 보냅니다.
            return restClient.post()
                    .uri("/analyze-image")
                    .contentType(MediaType.MULTIPART_FORM_DATA) // 파일을 보낼 때는 반드시 이 형식을 지정해야 합니다.
                    .body(builder.build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (request, response) -> {
                        // 서버 에러 발생 시 로그를 남기거나 예외를 던질 수 있습니다.
                        throw new RuntimeException("AI 서버 응답 에러: " + response.getStatusCode());
                    })
                    .body(AiAnalysisResponse.class); // 중요: 응답 JSON을 AiAnalysisResponse 객체로 자동 변환합니다.

        } catch (Exception e) {
            // 통신 실패 시 에러 로그를 출력하고 빈 리스트를 가진 객체를 반환하여 시스템이 멈추지 않게 합니다.
            System.err.println("AI 서버와 통신 중 예외 발생: " + e.getMessage());
            return new AiAnalysisResponse(java.util.List.of());
        }
    }
}