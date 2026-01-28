package com.board.one_more_project.domain.preference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final EmbeddingModel embeddingModel;
    private final SpiceRepository spiceRepository;
    private final IngredientRepository ingredientRepository;

    //유저 취향 키워드와 유사한 마스터 데이터를 검색
    @Override
    public List<PreferenceResponse> searchPreferences(String keyword) {
        log.info("취향 검색 시작 (Ollama): {}", keyword);

        float[] vector = embeddingModel.embed(keyword);

        List<Preference> results = preferenceRepository.findNearestPreferences(Arrays.toString(vector), 10);

        return results.stream()
                .map(PreferenceResponse::from)
                .toList();
    }

    // 모든 취향 데이터를 카테고리별로 정렬하여 조회
    @Override
    public List<PreferenceResponse> getAllPreferences() {
        log.info("모든 취향 데이터 조회 요청 시작");
        return preferenceRepository.findAllByOrderByCategoryAscIdAsc()
                .stream()
                .map(PreferenceResponse::from)
                .toList();
    }

    // 유저가 선택한 취향 리스트를 기반으로 연관된 재료와 조미료 추천
    @Override
    public PreferenceRecommendationResponse recommendRelatedKeywords(List<String> preferences) {
        log.info("취향 기반 연관 키워드 추쳔 요청 {}", preferences);

        List<IngredientResponse> recommendedIngredients = new ArrayList<>();
        List<SpiceResponse> recommendedSpices = new ArrayList<>();

        // 유저가 선택한 각 취향 키워드에 대해 유사한 재료/조미료 검색
        for (String preference : preferences) {
            float[] vector = embeddingModel.embed(preference);

            // [디버깅용 로그 추가] 벡터의 앞부분 5개만 찍어봅니다.
            log.info("키워드 '{}' 의 벡터값(앞 5개): [{}, {}, {}, {}, ...]",
                    preference, vector[0], vector[1], vector[2], vector[3], vector[4]);

            String vectorString = Arrays.toString(vector);

            // 유사한 재료 검색(상위 3개씩)
            List<Ingredient> ingredients = ingredientRepository.findNearestIngredients(vectorString, 5);
            recommendedIngredients.addAll(ingredients.stream().map(IngredientResponse::from).toList());

            // 유사하 조미료 검색(상위 3개씩)
            List<Spice> spiceResponses = spiceRepository.findNearestSpices(vectorString, 5);
            recommendedSpices.addAll(spiceResponses.stream().map(SpiceResponse::from).toList());
        }
        // 중복 제거 기능
        List<IngredientResponse> distinctIngredients = recommendedIngredients.stream().distinct().toList();
        List<SpiceResponse> distinctSpices = recommendedSpices.stream().distinct().toList();

        return new PreferenceRecommendationResponse(distinctIngredients, distinctSpices);

    }
}