package com.board.one_more_project.domain.preference;

import java.util.List;

/**
 * [취향 데이터 서비스 인터페이스]
 * 유저의 취향 선택지(Preference) 데이터를 조회하는 비즈니스 로직을 정의합니다.
 */
public interface PreferenceService {

    /**
     * 모든 취향 데이터를 카테고리별로 정렬하여 조회합니다.
     * @return 카테고리별로 정렬된 PreferenceResponse DTO 리스트
     */
    List<PreferenceResponse> getAllPreferences();
}