// app/recipe-list.tsx
import {useRouter} from "expo-router";
import React, {useState} from "react";
import {
  ActivityIndicator,
  Alert,
  ScrollView,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import {useAppContent} from "../src/context/AppContext";
import {useMasterDataService} from "../src/hooks/useMasterDataService";
import {Colors, commonStyles} from "../src/styles/common.styles";
import {RecipeResponse} from "../src/types/recipe";

export default function RecipeListScreen() {
  const router = useRouter();
  // Global Context에서 레시피 목록과 초기화 함수를 가져옵니다.
  const {recipes, setSelectedRecipe, resetAll} = useAppContent();
  const {generateRecipes} = useMasterDataService();

  // 레시피 재생성(재요청) 중 로딩 상태 관리
  const [isRegenerating, setIsRegenerating] = useState(false);

  // 레시피 상세 페이지로 이동하는 함수
  const handlePressRecipe = (recipe: RecipeResponse) => {
    setSelectedRecipe(recipe);
    router.push("/recipe-detail");
  };

  /**
   * [로직] 레시피 재생성 요청
   * @param type "basic" | "more" | "real" (만개의 레시피 추가)
   */
  const handleReGenerate = async (type: "basic" | "more" | "real") => {
    setIsRegenerating(true);
    const success = await generateRecipes(type);
    if (success) {
      Alert.alert("생성 완료", "새로운 레시피를 가져왔습니다.");
    }
    setIsRegenerating(false);
  };

  // 모든 메모리를 비우고 홈으로 이동
  const handleGoHome = () => {
    resetAll();
    router.dismissAll();
    router.replace("/");
  };

  // 재생성 중일 때 보여줄 로딩 화면
  if (isRegenerating) {
    return (
      <View style={commonStyles.loaderContainer}>
        <ActivityIndicator size="large" color={Colors.warning} />
        <Text style={commonStyles.loadingText}>
          새로운 레시피를 가져오고 있습니다...
        </Text>
      </View>
    );
  }

  return (
    <ScrollView
      style={commonStyles.safeContainer}
      contentContainerStyle={commonStyles.contentContainer}
    >
      <Text style={commonStyles.mainTitle}>추천 레시피 🍽️</Text>
      <Text style={commonStyles.subTitle}>결과가 마음에 드시나요?</Text>

      <View style={{gap: 15}}>
        {recipes.map((recipe, index) => (
          <TouchableOpacity
            key={`recipe-${index}`}
            style={commonStyles.sectionCard}
            onPress={() => handlePressRecipe(recipe)}
          >
            {/* 메뉴 번호 배지 */}
            <View style={commonStyles.badge}>
              <Text style={commonStyles.badgeText}>MENU {index + 1}</Text>
            </View>

            {/* 요리 제목 */}
            <Text
              style={{
                fontSize: 20,
                fontWeight: "bold",
                color: Colors.black,
                marginBottom: 8,
              }}
            >
              {recipe.title}
            </Text>

            {/* 요리 요약 설명 */}
            <Text
              style={{
                fontSize: 15,
                color: "#555",
                lineHeight: 22,
                marginBottom: 15,
              }}
            >
              {recipe.summary}
            </Text>

            {/* 재료 개수 정보 섹션 */}
            <View
              style={{
                flexDirection: "row",
                justifyContent: "space-between",
                borderTopWidth: 1,
                borderTopColor: Colors.lightGray,
                paddingTop: 15,
              }}
            >
              <Text style={{fontSize: 13, color: Colors.gray}}>
                {/* 기본 재료 개수 출력 */}
                필요 재료: {recipe.ingredients ? recipe.ingredients.length : 0}
                개
                {/* 
                  [핵심 로직] 추가 재료(+n개) 표시 
                  - basic, more, real 어떤 타입이든 recipe.more에 데이터가 있으면 표시합니다.
                  - Unexpected text node 에러 방지를 위해 삼항 연산자(? :)를 사용합니다.
                */}
                {recipe.more && recipe.more.length > 0 ? (
                  <Text style={{color: Colors.warning, fontWeight: "bold"}}>
                    {` (+${recipe.more.length}개)`}
                  </Text>
                ) : null}
              </Text>
              <Text
                style={{
                  fontSize: 14,
                  color: Colors.success,
                  fontWeight: "bold",
                }}
              >
                상세보기 ➔
              </Text>
            </View>
          </TouchableOpacity>
        ))}
      </View>

      {/* 재요청 버튼 섹션 */}
      <View
        style={[
          commonStyles.sectionCard,
          {marginTop: 20, alignItems: "center"},
        ]}
      >
        <Text
          style={{
            fontSize: 18,
            fontWeight: "bold",
            color: Colors.black,
            marginBottom: 20,
          }}
        >
          다른 레시피가 필요하신가요?
        </Text>

        <View style={{width: "100%", gap: 10}}>
          {/* 기본 재료만 활용 */}
          <TouchableOpacity
            style={[
              commonStyles.menuButton,
              {backgroundColor: Colors.lightGray, padding: 15},
            ]}
            onPress={() => handleReGenerate("basic")}
          >
            <Text
              style={{fontSize: 15, fontWeight: "bold", color: Colors.black}}
            >
              🏠 장보기 귀찮아 (기본 재료)
            </Text>
          </TouchableOpacity>

          {/* 추가 재료 포함 (응용) */}
          <TouchableOpacity
            style={[
              commonStyles.menuButton,
              {
                backgroundColor: "#FFF4E5",
                borderColor: Colors.warning,
                borderWidth: 1,
                padding: 15,
              },
            ]}
            onPress={() => handleReGenerate("more")}
          >
            <Text
              style={{fontSize: 15, fontWeight: "bold", color: Colors.warning}}
            >
              ✨ 추가 재료 활용 (응용 요리)
            </Text>
          </TouchableOpacity>

          {/* 만개의 레시피 기반 (RAG) */}
          <TouchableOpacity
            style={[
              commonStyles.menuButton,
              {
                backgroundColor: "#E8F5E9",
                borderColor: Colors.success,
                borderWidth: 1,
                padding: 15,
              },
            ]}
            onPress={() => handleReGenerate("real")}
          >
            <Text
              style={{fontSize: 15, fontWeight: "bold", color: Colors.success}}
            >
              🍳 만개의 레시피 (검증된 맛)
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* 전체 초기화 및 홈 이동 버튼 */}
      <TouchableOpacity
        style={{marginTop: 20, alignItems: "center", padding: 15}}
        onPress={handleGoHome}
      >
        <Text style={{color: Colors.gray, textDecorationLine: "underline"}}>
          처음으로 돌아가기 (전체 초기화)
        </Text>
      </TouchableOpacity>

      {/* 하단 여백 */}
      <View style={{height: 50}} />
    </ScrollView>
  );
}
