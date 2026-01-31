package com.board.one_more_project.domain.common;

import java.util.List;

/**
 * [MasterDataService]
 * 재료, 조미료, 취향 등 모든 마스터 데이터의 조회 및 검색을 담당하는 통합 인터페이스입니다.
 */
public interface MasterDataService {

    /**
     * 특정 타입의 모든 마스터 데이터를 조회합니다.
     */
    List<MasterDataResponse> getAllData(MasterDataResponse.MasterDataType type);

    /**
     * 특정 타입 내에서 키워드와 유사한 데이터를 검색합니다.
     */
    List<MasterDataResponse> searchData(MasterDataResponse.MasterDataType type, String keyword);
}