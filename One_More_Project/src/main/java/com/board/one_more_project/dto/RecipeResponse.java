package com.board.one_more_project.dto;

import java.util.List;

/**
 * LLM으로부터 받을 레시피 응답 구조입니다.
 * 자바의 record 기능을 사용하면 코드가 매우 간결해집니다. (Java 14 이상)
 */
public record RecipeResponse(
        String title,           // 요리 제목
        List<String> ingredients, // 사용된 재료 리스트
        List<String> steps,      // 조리 순서 리스트
        String tip              // 요리 꿀팁
) {}