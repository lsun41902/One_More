package com.board.one_more_project.controller;

import com.board.one_more_project.dto.PreferenceResponse;
import com.board.one_more_project.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Master Data", description = "취향, 재료, 조미료 등 마스터 데이터 조회")
@Slf4j
@RestController // 이 클래스가 REST API의 엔드포인트임을 선언
@RequestMapping("/api/preferences") // 기본 URL 경로 설정
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService; // Service 의존성 주입

    @Operation(summary = "취향 목록 조회", description = "유저가 선택할 수 있는 모든 취향(STYLE, TASTE, CONDITION) 목록을 반환합니다.")
    @GetMapping // GET /api/preferences 요청을 처리
    public ResponseEntity<List<PreferenceResponse>> getAllPreferences() {
        log.info("GET /api/preferences 요청 수신");

        // 1. Service 계층 호출하여 DB 데이터 조회 및 DTO 변환
        List<PreferenceResponse> preferences = preferenceService.getAllPreferences();

        log.info("취향 데이터 {}개 조회 완료", preferences.size());

        // 2. HTTP 200 OK 상태 코드와 함께 DTO 리스트 반환
        return ResponseEntity.ok(preferences);
    }
}