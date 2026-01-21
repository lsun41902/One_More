package com.board.one_more_project.config;

import com.board.one_more_project.dto.RecipeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice // 모든 컨트롤러에서 발생하는 예외를 감지하는 센서
public class GlobalExceptionHandler {

    // IllegalArgumentException(잘못된 입력) 발생 시 실행
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RecipeResponse> handleValidationException(IllegalArgumentException e) {
        log.warn("검증 실패(User Input Error): {}", e.getMessage());

        // 변경된 RecipeResponse(필드 8개) 구조에 맞춰서 에러 객체를 생성합니다.
        RecipeResponse errorResponse = new RecipeResponse(
                "입력 오류 발생",               // title
                e.getMessage(),                 // summary (여기에 에러 이유를 넣습니다)
                Collections.emptyList(),        // ingredients (빈 리스트)
                null,                           // more (없음)
                List.of("입력하신 정보를 다시 확인해주세요."), // recipe (안내 문구)
                List.of("재료나 취향 선택이 올바른지 확인해보세요."), // tip (안내 문구)
                null,                           // image
                null                            // reference
        );

        // 400 Bad Request 상태 코드와 함께 에러 정보를 반환합니다.
        return ResponseEntity.badRequest().body(errorResponse);
    }
}