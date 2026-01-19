package com.board.one_more_project.config;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * [레시피 검증 전문가 클래스]
 * @Component: 이 어노테이션이 있어야 스프링이 서버 기동 시 이 클래스를 '빈(Bean)'으로 등록합니다.
 * 별도의 @Configuration 설정 없이도 스프링이 자동으로 관리하는 부품이 됩니다.
 */
@Component
public class RecipeValidator {

    // 1단계: 이미지 분석 요청 시 파일과 취향을 검증합니다.
    public void validate(MultipartFile file, String preference) {
        // 파일이 아예 없거나 내용이 비어있는지 확인합니다.
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }
        // 공통 검증 로직 호출
        commonCheck(preference);
    }

    // 2단계: 레시피 생성 요청 시 재료 리스트와 취향을 검증합니다.
    public void validate(List<String> ingredients, String preference) {
        // 리스트 자체가 null이거나 비어있는지 확인합니다.
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("입력된 재료가 없습니다.");
        }
        // 데이터 정제: 사용자가 실수로 입력한 빈 문자열을 제거합니다.
        ingredients.removeIf(String::isEmpty);
        // 정제 후에도 재료가 하나도 없다면 에러를 던집니다.
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("유효한 재료가 하나도 없습니다.");
        }
        // 공통 검증 로직 호출
        commonCheck(preference);
    }

    // 공통 검증: 취향 텍스트의 길이를 제한합니다. (멘티님의 보안 로직)
    private void commonCheck(String preference) {
        // 취향 텍스트가 100자를 넘어가면 예외를 발생시킵니다.
        if (preference != null && preference.length() >= 100) {
            throw new IllegalArgumentException("요리 취향은 100자 이내로 입력해주세요.");
        }
    }
}