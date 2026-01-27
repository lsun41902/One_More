package com.board.one_more_project.domain.spice;

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
@RestController // @Controller + @ResponseBody: Return 값을 JSON으로 직렬화(Jackson 라이브러리 사용)
@RequestMapping("/api/spices") // 엔드포인트 URI 매핑
@RequiredArgsConstructor
public class SpiceController {

    // 의존성 주입: 비즈니스 로직을 수행할 Service Bean 주입
    private final SpiceService spiceService;

    @Operation(summary = "조미료 목록 조회", description = "모든 조미료(Spices) 목록을 이름순으로 반환합니다.")
    @GetMapping // HTTP GET Method 매핑
    public ResponseEntity<List<SpiceResponse>> getAllSpices() {
        log.info("GET /api/spices 요청 수신");

        // Service 계층 호출
        List<SpiceResponse> spices = spiceService.getAllSpices();

        log.info("조미료 데이터 {}개 반환", spices.size());

        // ResponseEntity를 사용하여 HTTP Status 200(OK)와 함께 응답
        return ResponseEntity.ok(spices);
    }
}