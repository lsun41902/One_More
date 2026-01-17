package com.board.one_more_project.service;

import com.board.one_more_project.dto.RecipeResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

//[AI 통신 서비스 인터페이스]
//역할: 컨트롤러와 서비스 구현체 사이의 추상화 계층을 형성합니다.
//이 인터페이스를 통해 컨트롤러는 실제 로직이 무엇인지 몰라도 메서드를 호출할 수 있습니다.
public interface AiClientService {
    // 이미지 기반 레시피 요청 메서드 규격
    RecipeResponse sendImageForRecipe(MultipartFile file, String preference, String type);

    // 텍스트 기반 레시피 요청 메서드 규격
    RecipeResponse sendTextForRecipe(List<String> ingredients, String preference);
}