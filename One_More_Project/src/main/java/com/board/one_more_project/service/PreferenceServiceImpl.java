package com.board.one_more_project.service;

import com.board.one_more_project.dto.PreferenceResponse;
import com.board.one_more_project.repository.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor // final 필드(Repository)를 주입받는 생성자를 자동 생성 (Lombok)
@Transactional(readOnly = true) // 조회 전용 트랜잭션 설정 (성능 최적화)
public class PreferenceServiceImpl implements PreferenceService {

    private final PreferenceRepository preferenceRepository; // Repository 의존성 주입

    /**
     * 모든 취향 데이터를 조회하고 DTO로 변환합니다.
     */
    @Override
    public List<PreferenceResponse> getAllPreferences() {
        log.info("모든 취향 데이터 조회 요청 시작");

        // 1. Repository를 통해 DB에서 모든 Entity를 조회 (카테고리, ID 순 정렬)
        // Repository에서 정의한 findAllByOrderByCategoryAscIdAsc() 메서드 사용
        return preferenceRepository.findAllByOrderByCategoryAscIdAsc()
                .stream()
                // 2. Entity(Preference)를 DTO(PreferenceResponse)로 변환
                .map(PreferenceResponse::from)
                // 3. 최종 List로 수집하여 반환
                .toList();
    }
}