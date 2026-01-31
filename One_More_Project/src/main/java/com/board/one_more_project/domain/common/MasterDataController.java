package com.board.one_more_project.domain.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "0. Master Data", description = "재료 및 조미료 마스터 데이터 관리")
@Slf4j
@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataService masterDataService;

    @Operation(summary = "재료 목록 조회", description = "모든 재료 목록을 이름순으로 반환합니다.")
    @GetMapping("/ingredients")
    public ResponseEntity<List<MasterDataResponse>> getAllIngredients() {
        return ResponseEntity.ok(masterDataService.getAllData(MasterDataResponse.MasterDataType.INGREDIENT));
    }

    @Operation(summary = "재료 검색", description = "의미론적 검색을 통해 유사한 재료를 찾습니다.")
    @GetMapping("/ingredients/search")
    public ResponseEntity<List<MasterDataResponse>> searchIngredients(@RequestParam("q") String keyword) {
        return ResponseEntity.ok(masterDataService.searchData(MasterDataResponse.MasterDataType.INGREDIENT, keyword));
    }

    @Operation(summary = "조미료 목록 조회", description = "모든 조미료 목록을 이름순으로 반환합니다.")
    @GetMapping("/spices")
    public ResponseEntity<List<MasterDataResponse>> getAllSpices() {
        return ResponseEntity.ok(masterDataService.getAllData(MasterDataResponse.MasterDataType.SPICE));
    }

    @Operation(summary = "조미료 검색", description = "의미론적 검색을 통해 유사한 조미료를 찾습니다.")
    @GetMapping("/spices/search")
    public ResponseEntity<List<MasterDataResponse>> searchSpices(@RequestParam("q") String keyword) {
        return ResponseEntity.ok(masterDataService.searchData(MasterDataResponse.MasterDataType.SPICE, keyword));
    }
}