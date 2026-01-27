package com.board.one_more_project.domain.preference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreferenceRepository extends JpaRepository<Preference, Long> {

    /**
     * [쿼리 메서드]
     * Spring Data JPA의 핵심 기능. 메서드 이름만으로 SQL 쿼리를 자동으로 생성합니다.
     * 'SELECT * FROM preferences WHERE category = ? ORDER BY id ASC'와 동일한 기능입니다.
     */
    List<Preference> findByCategoryOrderByIdAsc(String category);

    /**
     * [쿼리 메서드]
     * 모든 취향 데이터를 카테고리별로 묶어 정렬합니다.
     * 'SELECT * FROM preferences ORDER BY category ASC, id ASC'
     */
    List<Preference> findAllByOrderByCategoryAscIdAsc();
}