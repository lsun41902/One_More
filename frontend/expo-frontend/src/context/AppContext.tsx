// src/context/AppContext.tsx
import React, {createContext, useContext, useState} from "react";
import {
  IngredientDto,
  MasterDataResponse,
  RecipeResponse,
} from "../types/recipe"; // IngredientDto 추가

interface AppState {
  selections: {style: string; taste: string; condition: string};
  setSelections: (data: {
    style: string;
    taste: string;
    condition: string;
  }) => void;

  // [변경] string[] -> IngredientDto[]
  ingredients: IngredientDto[];
  setIngredients: (list: IngredientDto[]) => void;

  spices: string[];
  setSpices: (list: string[]) => void;

  //  선택된 이미지 URI 리스트 (최대 3장)
  selectedImages: string[];
  setSelectedImages: (list: string[]) => void;

  recommendedIngredients: MasterDataResponse[];
  setRecommendedIngredients: (list: MasterDataResponse[]) => void;
  recommendedSpices: MasterDataResponse[];
  setRecommendedSpices: (list: MasterDataResponse[]) => void;

  recipes: RecipeResponse[];
  setRecipes: (list: RecipeResponse[]) => void;
  selectedRecipe: RecipeResponse | null;
  setSelectedRecipe: (recipe: RecipeResponse | null) => void;

  resetAll: () => void;
}

const AppContext = createContext<AppState | undefined>(undefined);

export const AppProvider = ({children}: {children: React.ReactNode}) => {
  const [selections, setSelections] = useState({
    style: "",
    taste: "",
    condition: "",
  });

  // [변경] 초기값 빈 배열
  const [ingredients, setIngredients] = useState<IngredientDto[]>([]);
  const [spices, setSpices] = useState<string[]>([]);

  // 이미지 상태 초기화
  const [selectedImages, setSelectedImages] = useState<string[]>([]);

  const [recommendedIngredients, setRecommendedIngredients] = useState<
    MasterDataResponse[]
  >([]);
  const [recommendedSpices, setRecommendedSpices] = useState<
    MasterDataResponse[]
  >([]);
  const [recipes, setRecipes] = useState<RecipeResponse[]>([]);
  const [selectedRecipe, setSelectedRecipe] = useState<RecipeResponse | null>(
    null,
  );

  const resetAll = () => {
    setSelections({style: "", taste: "", condition: ""}); // 취향 초기화
    setIngredients([]); // 선택 재료 초기화
    setSpices([]); // 선택 조미료 초기화
    setSelectedImages([]); // 사진 리스트 초기화
    setRecommendedIngredients([]); // AI 추천 재료 초기화
    setRecommendedSpices([]); // AI 추천 조미료 초기화
    setRecipes([]); // 생성된 레시피 리스트 초기화
    setSelectedRecipe(null); // 선택된 상세 레시피 초기화
  };

  return (
    <AppContext.Provider
      value={{
        selections,
        setSelections,
        ingredients,
        setIngredients, // 변경된 타입 반영
        spices,
        setSpices,
        recipes,
        setRecipes,
        selectedRecipe,
        setSelectedRecipe,
        recommendedIngredients,
        setRecommendedIngredients,
        recommendedSpices,
        setRecommendedSpices,
        selectedImages,
        setSelectedImages,
        resetAll,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useAppContent = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error("AppProvider를 먼저 씌워야 해!");
  return context;
};
