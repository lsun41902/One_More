package com.board.one_more_project.service;

import com.board.one_more_project.dto.IngredientAnalysisResponse;
import com.board.one_more_project.dto.RecipeResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

//[AI 통신 서비스 인터페이스]
//역할: 컨트롤러와 서비스 구현체 사이의 추상화 계층을 형성합니다.
//이 인터페이스를 통해 컨트롤러는 실제 로직이 무엇인지 몰라도 메서드를 호출할 수 있습니다.
public interface AiClientService {
    // 1단계: 이미지/영수증을 보내서 재료 분석 결과를 받아옴
    IngredientAnalysisResponse analyzeIngredients(MultipartFile file, String preference, String type, String userId);

    // 2단계: 확정된 재료 리스트를 보내서 최종 레시피를 받아옴 (텍스트 입력 포함)
    RecipeResponse generateRecipe(List<String> ingredients, String preference, String userId);
}