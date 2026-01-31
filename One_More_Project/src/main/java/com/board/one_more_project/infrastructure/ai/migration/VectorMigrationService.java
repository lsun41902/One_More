package com.board.one_more_project.infrastructure.ai.migration;

import com.board.one_more_project.domain.ingredient.IngredientRepository;
import com.board.one_more_project.domain.preference.PreferenceRepository;
import com.board.one_more_project.domain.spice.SpiceRepository;
import com.board.one_more_project.infrastructure.ai.VectorProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorMigrationService {

    private final IngredientRepository ingredientRepository;
    private final SpiceRepository spiceRepository;
    private final PreferenceRepository preferenceRepository;
    private final VectorProvider vectorProvider;

    @Transactional
    public void migrateAll() {
        log.info("=== 벡터 마이그레이션 시작 (VectorProvider 활용) ===");

        // 1. 재료 임베딩
        ingredientRepository.findAll().forEach(item -> {
            String vector = vectorProvider.getVectorString(item.getName());
            ingredientRepository.updateEmbedding(item.getId(), vector);
        });

        // 2. 조미료 임베딩
        spiceRepository.findAll().forEach(item -> {
            String vector = vectorProvider.getVectorString(item.getName());
            spiceRepository.updateEmbedding(item.getId(), vector);
        });

        // 3. 취향 임베딩
        preferenceRepository.findAll().forEach(item -> {
            String vector = vectorProvider.getVectorString(item.getName());
            preferenceRepository.updateEmbedding(item.getId(), vector);
        });

        log.info("=== 모든 벡터 마이그레이션 완료 ===");
    }
}