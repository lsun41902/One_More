package com.board.one_more_project.domain.spice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service // Spring Context에 Bean으로 등록
@RequiredArgsConstructor // 생성자 주입(Constructor Injection) 방식을 위한 Lombok 어노테이션
@Transactional(readOnly = true) // 트랜잭션 범위를 설정하며, 조회 최적화를 위해 읽기 전용으로 설정
public class SpiceServiceImpl implements SpiceService {

    // 의존성 주입 (Dependency Injection): Repository Bean을 주입받습니다.
    private final SpiceRepository spiceRepository;

    @Override
    public List<SpiceResponse> getAllSpices() {
        log.info("모든 조미료 데이터 조회 요청(Transaction Start)");

        return spiceRepository.findAllByOrderByNameAsc() // 1. Persistence Context를 통해 Entity 조회
                .stream()
                .map(SpiceResponse::from) // 2. Entity -> DTO 매핑 (데이터 캡슐화)
                .toList();
    }
}