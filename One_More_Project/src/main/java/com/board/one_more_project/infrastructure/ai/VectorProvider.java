package com.board.one_more_project.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * [VectorProvider]
 * 텍스트를 벡터 문자열로 변환하는 책임을 전담합니다.
 * 모든 서비스에서 중복되었던 임베딩 로직을 이 클래스 하나로 통합합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorProvider {

    private final EmbeddingModel embeddingModel;

    /**
     * 텍스트를 받아서 pgvector(PostgreSQL) 검색에 적합한 문자열 포맷으로 반환합니다.
     * @param text 변환할 문자열 (검색어 또는 재료명)
     * @return "[0.123, 0.456, ...]" 형태의 문자열
     */
    public String getVectorString(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("벡터 변환을 위한 텍스트가 비어있습니다.");
        }

        log.debug("텍스트 임베딩 변환 시작: {}", text);
        float[] embedding = embeddingModel.embed(text);

        // pgvector의 cast(:vector as vector) 구문에 맞는 문자열 포맷으로 변환
        return Arrays.toString(embedding);
    }
}