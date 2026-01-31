package com.board.one_more_project.infrastructure.ai.migration;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/migration")
@RequiredArgsConstructor
public class MigrationController {

    private final VectorMigrationService migrationService;

    @Operation(summary = "데이터 임베딩 초기화", description = "DB의 모든 텍스트를 벡터로 변환하여 저장합니다. (최초 1회 실행)")
    @PostMapping("/vectors")
    public String runMigration() {
        // 별도 스레드에서 실행하는 것이 좋으나, 단순 구현을 위해 직접 호출
        migrationService.migrateAll();
        return "마이그레이션이 시작되었습니다. 서버 로그를 확인하세요.";
    }
}