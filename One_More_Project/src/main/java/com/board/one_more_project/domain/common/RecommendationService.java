package com.board.one_more_project.domain.common;

import com.board.one_more_project.domain.preference.PreferenceRecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ChatModel chatModel; // Ollama (Llama-3 / EXAONE)
    private final MasterDataService masterDataService;

    /**
     * [추천 핵심 로직]
     * 1. LLM에게 유저 취향에 맞는 검색 키워드 추출 요청 (Query Expansion)
     * 2. 추출된 키워드들로 Vector DB에서 실제 재료/조미료 매칭
     * 이 서비스는 **"유저 취향 -> LLM 키워드 확장 -> Vector DB 검색"**으로 이어지는 전체 추천 파이프라인을 관리합니다.
     * 기존에 PreferenceServiceImpl에 흩어져 있던 로직을 고도화하여 통합합니다.
     */
    public PreferenceRecommendationResponse recommend(List<String> preferences) {
        log.info("추천 파이프라인 시작: 취향={}", preferences);

        // 1. LLM을 통한 키워드 확장 (프롬프트 엔지니어링 적용)
        String promptMsg = String.format(
                "유저의 취향 %s를 분석하여 한식 요리에 어울리는 식재료 5개와 조미료 5개를 추천해줘. " +
                        "설명 없이 '재료: 단어, 단어... / 조미료: 단어, 단어...' 형식으로만 답해줘.",
                preferences.toString()
        );

        String llmResponse = chatModel.call(promptMsg);
        log.info("LLM 추출 키워드: {}", llmResponse);

        // 2. LLM 응답 파싱 (간단한 파싱 로직 예시)
        List<String> ingredientKeywords = parseKeywords(llmResponse, "재료:");
        List<String> spiceKeywords = parseKeywords(llmResponse, "조미료:");

        // 3. 통합 마스터 데이터 서비스를 통해 DB 검색 (Vector Search)
        List<MasterDataResponse> ingredients = ingredientKeywords.stream()
                .flatMap(k -> masterDataService.searchData(MasterDataResponse.MasterDataType.INGREDIENT, k).stream())
                .distinct().limit(15).toList();

        List<MasterDataResponse> spices = spiceKeywords.stream()
                .flatMap(k -> masterDataService.searchData(MasterDataResponse.MasterDataType.SPICE, k).stream())
                .distinct().limit(15).toList();

        return new PreferenceRecommendationResponse(ingredients, spices);
    }

    private List<String> parseKeywords(String response, String prefix) {
        try {
            String section = response.split(prefix)[1].split("/")[0];
            return Arrays.stream(section.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("LLM 응답 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }
}