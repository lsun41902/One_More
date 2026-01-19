package com.board.one_more_project.config;

import com.board.one_more_project.dto.RecipeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice // 모든 컨트롤러에서 발생하는 예외를 여기서 잡습니다.
public class GlobalExceptionHandler {

    // IllegalArgumentException(잘못된 인자 값)이 발생했을 때 실행됩니다.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RecipeResponse> handleValidationException(IllegalArgumentException e) {
        log.warn("검증 실패: {}", e.getMessage());

        // 에러 내용을 RecipeResponse 형식에 맞춰서 반환합니다.
        RecipeResponse errorResponse = new RecipeResponse(
                "입력 오류",
                List.of(),
                List.of(e.getMessage()),
                "다시 확인 후 입력해주세요."
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }
}