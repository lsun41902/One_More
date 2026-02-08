// src/hooks/useMasterDataService.ts
import {useEffect, useState} from "react";
import {Alert, Platform} from "react-native";
import {useAppContent} from "../context/AppContext";
import {
  GroupedPreferences,
  IngredientAnalysisResponse,
  IngredientDto,
  MasterDataResponse,
  PreferenceRecommendationResponse,
  RecipeGenerationRequest,
} from "../types/recipe";

/**
 * [마스터 데이터 및 AI 통신 서비스 훅]
 * 백엔드(Spring Boot)와 통신하여 취향, 재료, 조미료 데이터를 관리합니다.
 */
export const useMasterDataService = () => {
  // Global Context에서 상태와 설정 함수들을 가져옵니다.
  const {
    selections,
    ingredients,
    setIngredients,
    spices,
    setRecipes,
    setRecommendedIngredients,
    setRecommendedSpices,
  } = useAppContent();

  // --- 로컬 상태 관리 ---
  // 1. 취향 마스터 데이터 (스타일, 맛, 상태별 그룹화)
  const [preferences, setPreferences] = useState<GroupedPreferences>({
    STYLE: [],
    TASTE: [],
    CONDITION: [],
  });

  // 2. 검색 결과 리스트 (사용자가 직접 검색창에 입력했을 때 담기는 곳)
  const [ingredientOptions, setIngredientOptions] = useState<string[]>([]);
  const [spiceOptions, setSpiceOptions] = useState<string[]>([]);

  // 3. 통신 중 로딩 상태
  const [isLoading, setIsLoading] = useState(false);

  // 백엔드 서버 주소 (실제 기기 테스트 시 PC의 IP 주소로 변경 확인 필수)
  // const SERVER_URL = "http://192.168.22.54:8080";

  // apk테스트를 위한, ngrok 무료 버전 주소.
  const SERVER_URL = "https://areolar-tad-unimplanted.ngrok-free.dev";

  // ngrok 경고페이지 우회용 공통 헤더 정의
  const COMMON_HEADERS = {
    "ngrok-skip-browser-warning": "69420",
  };

  /**
   * [기능 1] 마스터 데이터 초기 로드
   * 앱 시작 시 혹은 첫 화면 진입 시 취향 태그 목록을 가져옵니다.
   */
  const fetchAllMasterData = async () => {
    try {
      setIsLoading(true);
      const prefRes = await fetch(`${SERVER_URL}/api/preferences`, {
        // [수정] 헤더 추가
        headers: COMMON_HEADERS,
      });

      if (prefRes.ok) {
        const prefData = await prefRes.json();
        // 카테고리별(STYLE, TASTE, CONDITION)로 데이터를 분류합니다.
        const grouped = prefData.reduce(
          (acc: any, p: any) => {
            const key = p.category.toUpperCase();
            if (acc[key]) acc[key].push(p);
            return acc;
          },
          {STYLE: [], TASTE: [], CONDITION: []},
        );
        setPreferences(grouped);
      }
    } catch (e) {
      console.error("마스터 데이터 로드 실패:", e);
    } finally {
      setIsLoading(false);
    }
  };

  // 훅이 처음 호출될 때 데이터를 한 번 가져옵니다.
  useEffect(() => {
    fetchAllMasterData();
  }, []);

  /**
   * [기능 2] RAG 기반 취향 분석 (AI 추천)
   * 사용자가 선택한 취향 리스트를 백엔드에 보내 LLM(Exaone) 추천 결과를 받습니다.
   */
  const analyzePreferences = async (preferenceList: string[]) => {
    try {
      setIsLoading(true);
      const response = await fetch(`${SERVER_URL}/api/preferences/analyze`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...COMMON_HEADERS,
        },
        body: JSON.stringify(preferenceList),
      });

      if (!response.ok) throw new Error("취향 분석 통신 실패");

      const data: PreferenceRecommendationResponse = await response.json();

      // AI가 추천해준 재료와 조미료를 Context에 저장하여 화면에 표시합니다.
      setRecommendedIngredients(data.ingredientResponseList);
      setRecommendedSpices(data.spiceResponses);
    } catch (e) {
      console.error("AI 추천 로드 실패:", e);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * [기능 3-1] 재료 직접 검색 (Semantic Search)
   * 사용자가 검색창에 입력한 키워드로 DB에서 유사한 재료를 찾습니다.
   */
  const searchIngredients = async (query: string) => {
    if (!query.trim()) {
      setIngredientOptions([]); // 검색어가 없으면 결과창을 비웁니다.
      return;
    }
    try {
      const response = await fetch(
        `${SERVER_URL}/api/ingredients/search?q=${encodeURIComponent(query)}`,
        {
          headers: COMMON_HEADERS,
        },
      );
      if (response.ok) {
        const data: MasterDataResponse[] = await response.json();
        setIngredientOptions(data.map((d) => d.name));
      }
    } catch (e) {
      console.error("재료 검색 실패:", e);
    }
  };

  /**
   * [기능 3-2] 조미료 직접 검색 (Semantic Search)
   * 사용자가 검색창에 입력한 키워드로 DB에서 유사한 조미료를 찾습니다.
   */
  const searchSpices = async (query: string) => {
    if (!query.trim()) {
      setSpiceOptions([]);
      return;
    }
    try {
      const response = await fetch(
        `${SERVER_URL}/api/spices/search?q=${encodeURIComponent(query)}`,
        {
          headers: COMMON_HEADERS,
        },
      );
      if (response.ok) {
        const data: MasterDataResponse[] = await response.json();
        setSpiceOptions(data.map((d) => d.name));
      }
    } catch (e) {
      console.error("조미료 검색 실패:", e);
    }
  };

  /**
   * [기능 4] 이미지/영수증 분석 요청
   * 카메라로 찍은 사진을 백엔드로 전송하여 객체 인식 결과를 받습니다.
   */
  const analyzeImages = async (
    imageUris: string[],
    type: "image" | "receipt" = "image",
  ): Promise<boolean> => {
    setIsLoading(true);
    try {
      const formData = new FormData();

      // 이미지 파일들을 FormData에 담습니다.
      for (let i = 0; i < imageUris.length; i++) {
        const uri = imageUris[i];
        const fileName = uri.split("/").pop() || `photo_${i}.jpg`;
        const fileType = fileName.endsWith(".png") ? "image/png" : "image/jpeg";

        if (Platform.OS === "web") {
          const response = await fetch(uri);
          const blob = await response.blob();
          formData.append("files", blob, fileName);
        } else {
          formData.append("files", {
            uri: Platform.OS === "ios" ? uri.replace("file://", "") : uri,
            name: fileName,
            type: fileType,
          } as any);
        }
      }

      formData.append("type", type);
      formData.append("userId", "test_user_01");

      const response = await fetch(`${SERVER_URL}/api/recipe/analyze`, {
        method: "POST",
        headers: COMMON_HEADERS,
        body: formData,
      });

      if (!response.ok) throw new Error("이미지 분석 서버 에러");

      const data: IngredientAnalysisResponse[] = await response.json();

      // 분석된 재료들을 기존 장바구니 리스트에 추가합니다.
      const detectedList: IngredientDto[] = [];
      data.forEach((item) => {
        if (item.ingredients) detectedList.push(...item.ingredients);
      });

      // [수정] 기존 리스트(ingredients)와 새 리스트(detectedList)를 합칠 때 중복 제거
      const mergedList = [...ingredients];

      detectedList.forEach((newItem) => {
        // 이미 목록에 같은 이름의 재료가 있는지 확인
        const exists = mergedList.some(
          (existingItem) => existingItem.ingredient === newItem.ingredient,
        );

        // 없으면 추가 (있으면 무시)
        if (!exists) {
          mergedList.push(newItem);
        }
      });

      setIngredients(mergedList);
      return true;
    } catch (error: any) {
      Alert.alert("분석 실패", error.message);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * [기능 5] 최종 레시피 생성 요청
   * 모든 재료, 조미료, 취향을 통합하여 AI 레시피 생성을 요청합니다.
   */
  const generateRecipes = async (
    action: string = "initial",
  ): Promise<boolean> => {
    setIsLoading(true);
    try {
      const requestData: RecipeGenerationRequest = {
        userId: "test_user_01",
        action: action,
        ingredients: ingredients,
        spices: spices,
        preferences: [
          selections.style,
          selections.taste,
          selections.condition,
        ].filter(Boolean),
      };

      const response = await fetch(`${SERVER_URL}/api/recipe/generate`, {
        method: "POST",
        headers: {
          ...COMMON_HEADERS,
          "Content-Type": "application/json", // [필수] JSON 데이터임을 명시
        },
        body: JSON.stringify(requestData),
      });

      if (!response.ok) throw new Error("레시피 생성 실패");

      setRecipes(await response.json());
      return true;
    } catch (error: any) {
      Alert.alert("연동 실패", error.message);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  // 화면(Component)에서 사용할 수 있도록 함수와 상태들을 내보냅니다.
  return {
    preferences,
    ingredientOptions,
    spiceOptions,
    isLoading,
    generateRecipes,
    analyzePreferences,
    searchIngredients,
    searchSpices,
    analyzeImages,
  };
};
