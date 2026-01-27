package com.board.one_more_project.domain.ingredient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor // final이 붙은 필드(Repository)를 자동으로 주입해주는 생성자를 만듦
@Transactional(readOnly = true) // 이 클래스 안에서는 데이터를 '읽기'만 하겠다고 선언 (성능 최적화)
public class IngredientServiceImpl implements IngredientService {

    // 창고지기(Repository)를 불러옵니다.
    private final IngredientRepository ingredientRepository;

    @Override
    public List<IngredientResponse> getAllIngredients() {
        log.info("모든 재료 데이터 조회 요청 시작");

        // 1. Repository에게 부탁해서 DB에 있는 모든 재료를 가져옵니다.
        // (아까 Repository에 만들어둔 정렬 기능 사용)
        return ingredientRepository.findAllByOrderByCategoryAscNameAsc()
                .stream() // 리스트를 하나씩 흘려보내는 파이프라인 시작

                // 2. Entity(창고 물건) -> DTO(포장된 상품) 변환
                // IngredientResponse 클래스에 만들어둔 static 메서드(from)를 사용합니다.
                .map(IngredientResponse::from)

                // 3. 다시 리스트로 묶어서 반환
                .toList();
    }
}