package com.board.one_more_project.domain.preference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel; // 채팅 모델 인터페이스
import org.springframework.ai.chat.prompt.Prompt; // 프롬프트 객체
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.one_more_project.domain.ingredient.Ingredient;
import com.board.one_more_project.domain.ingredient.IngredientRepository;
import com.board.one_more_project.domain.ingredient.IngredientResponse;
import com.board.one_more_project.domain.spice.Spice;
import com.board.one_more_project.domain.spice.SpiceRepository;
import com.board.one_more_project.domain.spice.SpiceResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreferenceServiceImpl implements PreferenceService {

    private final PreferenceRepository preferenceRepository;
    private final EmbeddingModel embeddingModel; // 벡터 변환기
    private final ChatModel chatModel;           // Exaone LLM 모델
    private final SpiceRepository spiceRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    public List<PreferenceResponse> getAllPreferences() {
        log.info("모든 취향 데이터 조회 요청 시작");
        return preferenceRepository.findAllByOrderByCategoryAscIdAsc()
                .stream()
                .map(PreferenceResponse::from)
                .toList();
    }

    /**
     * [RAG 방식의 추천 로직]
     * 1. 사용자의 취향을 LLM(Exaone)에게 전달
     * 2. LLM이 어울리는 재료/조미료 단어 리스트를 생성
     * 3. 생성된 단어들을 벡터로 변환하여 DB에서 실제 데이터 검색
     */
    @Override
    public PreferenceRecommendationResponse recommendRelatedKeywords(List<String> preferences) {
        log.info("RAG 기반 연관 키워드 추천 시작. 입력 취향: {}", preferences);

        // 1. LLM에게 보낼 질문(Prompt) 만들기
        // 초보자 주의: 프롬프트가 명확해야 LLM이 이상한 소리를 안 합니다.
        String userPreferences = String.join(", ", preferences);
        String instruction = "너는 전문 요리사야. 사용자의 취향 [" + userPreferences + "]에 어울리는 "
                + "식재료 5개와 조미료 3개를 추천해줘. "
                + "설명은 생략하고 단어만 쉼표(,)로 구분해서 한 줄로 말해줘. "
                + "예시: 돼지고기, 양파, 대파, 고추장, 간장";

        log.info("Exaone 모델에게 질문 전송 중...");

        // 2. Exaone 3.5 호출 및 답변 받기
        String llmResponse = chatModel.call(instruction);
        log.info("Exaone 답변 수신: {}", llmResponse);

        // 3. 답변 파싱 (쉼표로 잘라서 리스트 만들기)
        // 정규식을 사용해 쉼표 앞뒤 공백을 제거하고 리스트로 변환합니다.
        List<String> recommendedWords = Arrays.stream(llmResponse.split(","))
                .map(String::trim)
                .toList();

        List<IngredientResponse> finalIngredients = new ArrayList<>();
        List<SpiceResponse> finalSpices = new ArrayList<>();

        // 4. LLM이 추천한 단어들을 하나씩 DB에서 벡터 검색
        for (String word : recommendedWords) {
            log.debug("추천 단어 '{}'로 DB 검색 수행", word);

            // 단어를 벡터로 변환
            float[] vector = embeddingModel.embed(word);
            String vectorString = Arrays.toString(vector);

            // 재료 DB에서 가장 유사한 것 1개 찾기
            List<Ingredient> ingredients = ingredientRepository.findNearestIngredients(vectorString, 1);
            if (!ingredients.isEmpty()) {
                finalIngredients.add(IngredientResponse.from(ingredients.get(0)));
            }

            // 조미료 DB에서 가장 유사한 것 1개 찾기
            List<Spice> spices = spiceRepository.findNearestSpices(vectorString, 1);
            if (!spices.isEmpty()) {
                finalSpices.add(SpiceResponse.from(spices.get(0)));
            }
        }

        // 5. 중복 제거 (LLM이 비슷한 단어를 또 말했을 경우 대비)
        List<IngredientResponse> distinctIngredients = finalIngredients.stream().distinct().collect(Collectors.toList());
        List<SpiceResponse> distinctSpices = finalSpices.stream().distinct().collect(Collectors.toList());

        log.info("최종 추천 완료: 재료 {}건, 조미료 {}건", distinctIngredients.size(), distinctSpices.size());

        return new PreferenceRecommendationResponse(distinctIngredients, distinctSpices);
    }
}