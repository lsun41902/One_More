package com.board.one_more_project.domain.common;

import com.board.one_more_project.domain.ingredient.IngredientRepository;
import com.board.one_more_project.domain.preference.PreferenceRepository;
import com.board.one_more_project.domain.spice.SpiceRepository;
import com.board.one_more_project.infrastructure.ai.VectorProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MasterDataServiceImpl implements MasterDataService {

    private final IngredientRepository ingredientRepository;
    private final SpiceRepository spiceRepository;
    private final PreferenceRepository preferenceRepository;
    private final VectorProvider vectorProvider;

    @Override
    public List<MasterDataResponse> getAllData(MasterDataResponse.MasterDataType type) {
        log.info("{} 타입의 모든 데이터 조회 시작", type);

        return switch (type) {
            case INGREDIENT -> ingredientRepository.findAllByOrderByNameAsc().stream()
                    .map(i -> MasterDataResponse.of(i.getId(), i.getName(), type, null)).toList();
            case SPICE -> spiceRepository.findAllByOrderByNameAsc().stream()
                    .map(s -> MasterDataResponse.of(s.getId(), s.getName(), type, null)).toList();
            case PREFERENCE -> preferenceRepository.findAllByOrderByCategoryAscIdAsc().stream()
                    .map(p -> MasterDataResponse.of(p.getId(), p.getName(), type, p.getCategory())).toList();
        };
    }

    @Override
    public List<MasterDataResponse> searchData(MasterDataResponse.MasterDataType type, String keyword) {
        log.info("{} 타입 내 유사도 검색 시작: {}", type, keyword);

        // 공통 로직: 검색어를 벡터 문자열로 변환
        String queryVector = vectorProvider.getVectorString(keyword);

        return switch (type) {
            case INGREDIENT -> ingredientRepository.findNearestIngredients(queryVector, 10).stream()
                    .map(i -> MasterDataResponse.of(i.getId(), i.getName(), type, null)).toList();
            case SPICE -> spiceRepository.findNearestSpices(queryVector, 10).stream()
                    .map(s -> MasterDataResponse.of(s.getId(), s.getName(), type, null)).toList();
            case PREFERENCE -> preferenceRepository.findNearestPreferences(queryVector, 10).stream()
                    .map(p -> MasterDataResponse.of(p.getId(), p.getName(), type, p.getCategory())).toList();
        };
    }
}