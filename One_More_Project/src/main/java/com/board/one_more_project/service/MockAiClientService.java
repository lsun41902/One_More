package com.board.one_more_project.service;

import com.board.one_more_project.dto.RecipeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//[가짜 AI 서버 통신 구현체]
//@Profile("dev"): 개발 환경(dev) 프로파일이 활성화될 때만 스프링 빈으로 등록됩니다.
//파이썬 서버 없이도 즉시 가짜 데이터를 반환하여 프론트엔드나 컨트롤러 테스트를 돕습니다.
@Slf4j
@Service
@Profile("dev")
public class MockAiClientService implements AiClientService {

    @Override
    public RecipeResponse sendImageForRecipe(MultipartFile file, String preference, String type) {
        log.info("[Mock] 가짜 이미지 분석 로직을 실행합니다. (입력 파일: {})", file.getOriginalFilename());

        // 실제 통신 없이 즉시 가짜 데이터를 생성하여 반환합니다.
        return new RecipeResponse(
                "[Mock] 이미지 분석 요리: 스테이크",
                List.of("소고기", "아스파라거스", "마늘"),
                List.of("1. 고기에 소금을 뿌린다.", "2. 팬에 굽는다.", "3. 맛있게 먹는다."),
                "Mock 데이터이므로 실제 분석 결과가 아닙니다."
        );
    }

    @Override
    public RecipeResponse sendTextForRecipe(List<String> ingredients, String preference) {
        log.info("[Mock] 가짜 텍스트 레시피 로직을 실행합니다. (입력 재료 수: {})", ingredients.size());

        return new RecipeResponse(
                "[Mock] 텍스트 기반 요리: 볶음밥",
                ingredients, // 사용자가 입력한 재료를 그대로 보여줌
                List.of("1. 재료를 다진다.", "2. 밥과 함께 볶는다."),
                "취향 반영 결과: " + preference
        );
    }
}