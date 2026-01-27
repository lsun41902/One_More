package com.board.one_more_project.domain.ingredient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // 스프링에게 "이건 DB 접근용 클래스야"라고 알려줍니다.
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    /**
     * [쿼리 메서드]
     * 모든 재료를 가져오되, 카테고리 이름 순(ㄱ~ㅎ) -> 재료 이름 순(ㄱ~ㅎ)으로 정렬합니다.
     * SQL: SELECT * FROM ingredients ORDER BY category ASC, name ASC;
     */
    List<Ingredient> findAllByOrderByCategoryAscNameAsc();
}