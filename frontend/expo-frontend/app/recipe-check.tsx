// app/recipe-check.tsx
import {useRouter} from "expo-router";
import React, {useState} from "react";
import {
  ActivityIndicator,
  ScrollView,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import SummaryHeader from "../src/components/SummaryHeader"; // 공통 헤더 도입
import {useAppContent} from "../src/context/AppContext";
import {useMasterDataService} from "../src/hooks/useMasterDataService";
import {Colors, commonStyles} from "../src/styles/common.styles"; // 공통 스타일 도입

export default function RecipeCheckScreen() {
  const router = useRouter();
  const {
    selections,
    ingredients,
    spices,
    setSelections,
    setIngredients,
    setSpices,
  } = useAppContent();

  const {generateRecipes} = useMasterDataService();
  const [isGenerating, setIsGenerating] = useState(false);

  // 레시피 생성 실행 로직
  const handleGenerate = async () => {
    setIsGenerating(true);
    const success = await generateRecipes();
    if (success) {
      router.replace("/recipe-list");
    } else {
      setIsGenerating(false);
    }
  };

  // 데이터 초기화 및 홈 이동
  const handleReset = () => {
    setSelections({style: "", taste: "", condition: ""});
    setIngredients([]);
    setSpices([]);
    router.dismissAll();
    router.replace("/");
  };

  // AI 생성 중 로딩 화면 (디자인 통일)
  if (isGenerating) {
    return (
      <View style={commonStyles.loaderContainer}>
        <ActivityIndicator size="large" color={Colors.success} />
        <Text style={commonStyles.loadingText}>
          AI 셰프가 레시피를 생각 중입니다...
        </Text>
        <Text style={{marginTop: 10, color: Colors.gray}}>
          잠시만 기다려주세요
        </Text>
      </View>
    );
  }

  return (
    <ScrollView
      style={commonStyles.safeContainer}
      contentContainerStyle={commonStyles.contentContainer}
    >
      {/* 1. 상단 요약 헤더 (일관성 유지) */}
      <SummaryHeader selections={selections} />

      <Text style={commonStyles.mainTitle}>준비 완료!</Text>

      {/* 2. 선택 재료 섹션 (카드 스타일) */}
      <View style={commonStyles.sectionCard}>
        <Text style={commonStyles.sectionTitle}>
          선택한 재료 ({ingredients.length})
        </Text>
        <View style={commonStyles.chipWrapper}>
          {ingredients.map((item) => (
            <View key={item.ingredient} style={commonStyles.chip}>
              <Text style={commonStyles.chipText}>
                {item.ingredient} ({item.quantity})
              </Text>
            </View>
          ))}
          {ingredients.length === 0 && (
            <Text style={{color: Colors.placeholder}}>
              선택된 재료가 없습니다.
            </Text>
          )}
        </View>
      </View>

      {/* 3. 선택 조미료 섹션 (카드 스타일) */}
      <View style={commonStyles.sectionCard}>
        <Text style={commonStyles.sectionTitle}>
          선택한 조미료 ({spices.length})
        </Text>
        <View style={commonStyles.chipWrapper}>
          {spices.map((item) => (
            <View key={item} style={commonStyles.chip}>
              <Text style={commonStyles.chipText}>{item}</Text>
            </View>
          ))}
          {spices.length === 0 && (
            <Text style={{color: Colors.placeholder}}>
              선택된 조미료가 없습니다.
            </Text>
          )}
        </View>
      </View>

      {/* 4. 하단 액션 버튼 */}
      <TouchableOpacity
        style={commonStyles.nextButton}
        onPress={handleGenerate}
      >
        <Text style={commonStyles.nextButtonText}>레시피 생성하기</Text>
      </TouchableOpacity>

      <TouchableOpacity
        onPress={handleReset}
        style={{alignItems: "center", marginTop: 20, padding: 10}}
      >
        <Text style={{color: Colors.gray, textDecorationLine: "underline"}}>
          처음부터 다시 하기 ↺
        </Text>
      </TouchableOpacity>

      <View style={{height: 50}} />
    </ScrollView>
  );
}
