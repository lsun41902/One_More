package com.board.one_more_project.service;

import com.board.one_more_project.dto.IngredientResponse;
import java.util.List;

/**
 * [재료 서비스 인터페이스]
 * 비즈니스 로직의 껍데기(설계도)입니다.
 * 나중에 로직이 바뀌어도 Controller 코드는 건드리지 않기 위해 사용합니다.
 */
public interface IngredientService {

    /**
     * 모든 재료 데이터를 카테고리별로 정렬해서 가져옵니다.
     * * @return 가공된 재료 목록 (DTO 리스트)
     */
    List<IngredientResponse> getAllIngredients();
}