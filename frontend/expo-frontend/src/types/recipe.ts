// src/types/recipe.ts

// 1. 재료 DTO (Java: IngredientDto)
export interface IngredientDto {
  ingredient: string;
  quantity: string;
}

// 2. 레시피 응답 DTO (Java: RecipeResponse)
export interface RecipeResponse {
  title: string;
  summary: string;
  ingredients: IngredientDto[];
  more?: IngredientDto[]; // Java: nullable = true
  recipe: string[]; // 조리 순서
  tip: string[]; // 요리 팁
  image?: string; // Java: nullable = true
  reference?: string; // Java: nullable = true
}

// 3. 레시피 생성 요청 DTO (Java: RecipeGenerationRequest)
export interface RecipeGenerationRequest {
  ingredients: IngredientDto[];
  spices: string[];
  preferences: string[];
  userId: string;
  action: string; // "initial", "basic", "more"
}

// 4. 마스터 데이터 응답 (취향, 재료, 조미료 공통)
export interface MasterDataResponse {
  id: number;
  name: string;
  category?: string;
}

// 5. 취향 그룹화 타입
export interface GroupedPreferences {
  STYLE: MasterDataResponse[];
  TASTE: MasterDataResponse[];
  CONDITION: MasterDataResponse[];
}

// 6. 취향 기반 AI 추천 응답 타입
export interface PreferenceRecommendationResponse {
  ingredientResponseList: MasterDataResponse[];
  spiceResponses: MasterDataResponse[];
}

// 7. 이미지 분석 응답 타입 (Java: IngredientAnalysisResponse)
// 백엔드에서 List<IngredientAnalysisResponse> 형태로 반환됨
export interface IngredientAnalysisResponse {
  image_index: number;
  ingredients: IngredientDto[];
}
