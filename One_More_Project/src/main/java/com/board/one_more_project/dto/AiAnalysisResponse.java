package com.board.one_more_project.dto;

import java.util.List;

/**
 * Python AI 서버(FastAPI)로부터 받을 응답 구조입니다.
 */
public record AiAnalysisResponse(
        List<String> keywords // AI가 이미지에서 추출한 식재료 리스트
) {}