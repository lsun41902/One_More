package com.board.one_more_project.domain.spice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Spring Bean으로 등록하여 DI(의존성 주입) 가능하게 설정
public interface SpiceRepository extends JpaRepository<Spice, Long> {

    /**
     * [Derived Query Method]
     * 메서드 이름을 분석하여 JPQL을 자동 생성합니다.
     * SQL: SELECT * FROM spices ORDER BY name ASC;
     * * @return 이름 오름차순으로 정렬된 Spice 엔티티 리스트
     */
    List<Spice> findAllByOrderByNameAsc();
}