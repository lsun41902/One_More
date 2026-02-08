// app/ingredients.tsx
import {useRouter} from "expo-router";
import React, {useEffect, useState} from "react";
import {
  ActivityIndicator,
  ScrollView,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from "react-native";
import SummaryHeader from "../src/components/SummaryHeader";
import {useAppContent} from "../src/context/AppContext";
import {useMasterDataService} from "../src/hooks/useMasterDataService";
import {Colors, commonStyles} from "../src/styles/common.styles";

// 수량 선택 옵션 데이터
const QUANTITY_OPTIONS = [
  "약간",
  "적당히",
  "많이",
  "50g",
  "100g",
  "200g",
  "300g",
  "500g",
  "1근",
  "1봉지",
  "1조각",
  "1모",
  "1알",
];

export default function IngredientsScreen() {
  const router = useRouter();
  const {ingredients, setIngredients, selections, recommendedIngredients} =
    useAppContent();
  const {isLoading, searchIngredients, ingredientOptions} =
    useMasterDataService();

  const [searchText, setSearchText] = useState("");
  const [activeIngredientName, setActiveIngredientName] = useState<
    string | null
  >(null);

  // 검색어 입력 시 0.4초 뒤에 서버 통신
  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      searchIngredients(searchText);
    }, 400);
    return () => clearTimeout(delayDebounceFn);
  }, [searchText]);

  /**
   * [로직 1] 재료 추가/수정 토글
   */
  const toggleIngredient = (name: string) => {
    const existingIndex = ingredients.findIndex((i) => i.ingredient === name);
    if (existingIndex > -1) {
      setActiveIngredientName(name);
    } else {
      setIngredients([...ingredients, {ingredient: name, quantity: "적당히"}]);
      setActiveIngredientName(name);
    }
  };

  /**
   * [로직 2] 재료 삭제 (X 버튼 전용)
   */
  const removeIngredient = (name: string) => {
    setIngredients(ingredients.filter((i) => i.ingredient !== name));
    if (activeIngredientName === name) setActiveIngredientName(null);
  };

  /**
   * [로직 3] 수량 업데이트
   */
  const updateQuantity = (qty: string) => {
    if (!activeIngredientName) return;
    const newList = ingredients.map((item) =>
      item.ingredient === activeIngredientName
        ? {...item, quantity: qty}
        : item,
    );
    setIngredients(newList);
    setActiveIngredientName(null);
  };

  return (
    <ScrollView
      style={commonStyles.safeContainer}
      contentContainerStyle={commonStyles.contentContainer}
    >
      {/* 1. 내 요리 설정 요약 */}
      <SummaryHeader selections={selections} />

      <Text style={commonStyles.mainTitle}>재료 확인</Text>

      {/* 2. 검색창 */}
      <View style={commonStyles.searchBar}>
        <TextInput
          style={commonStyles.searchInput}
          placeholder="원하는 재료를 직접 검색하세요"
          value={searchText}
          onChangeText={setSearchText}
          placeholderTextColor={Colors.placeholder}
        />
      </View>

      {/* 3. 확정된 재료 목록 (장바구니) */}
      {ingredients.length > 0 && (
        <View
          style={[
            commonStyles.sectionCard,
            {borderColor: Colors.primary, borderWidth: 1},
          ]}
        >
          <Text style={[commonStyles.sectionTitle, {color: Colors.primary}]}>
            확정된 재료 목록 ({ingredients.length})
          </Text>
          <View style={commonStyles.chipWrapper}>
            {ingredients.map((item, index) => (
              <View
                key={`${item.ingredient}-${index}`}
                style={[
                  commonStyles.chip,
                  commonStyles.chipSelected,
                  // [수정] paddingRight를 줄여서 전체 길이 축소
                  {flexDirection: "row", alignItems: "center", paddingRight: 2},
                ]}
              >
                {/* [수정] 왼쪽 영역: 이름과 수량 클릭 시 수량 변경 */}
                <TouchableOpacity
                  onPress={() => setActiveIngredientName(item.ingredient)}
                  style={{
                    paddingRight: 2, // 10 -> 8
                  }}
                >
                  <Text style={commonStyles.chipTextSelected}>
                    {item.ingredient} [{item.quantity}]
                  </Text>
                </TouchableOpacity>

                {/* [수정] 오른쪽 영역: X 버튼 클릭 시 삭제 */}
                <TouchableOpacity
                  onPress={() => removeIngredient(item.ingredient)}
                  // [수정] paddingVertical을 제거하여 칩 높이가 커지는 것 방지
                  style={{paddingLeft: 2, paddingRight: 4, paddingVertical: 0}}
                >
                  <Text style={[commonStyles.chipTextSelected, {fontSize: 14}]}>
                    ✕
                  </Text>
                </TouchableOpacity>
              </View>
            ))}
          </View>
        </View>
      )}

      {/* 4. 취향 맞춤 추천 재료 (AI RAG) */}
      <View style={[commonStyles.sectionCard, commonStyles.aiBorder]}>
        <Text style={[commonStyles.sectionTitle, commonStyles.aiTitle]}>
          👨‍🍳 취향 맞춤 추천 재료
        </Text>
        <View style={commonStyles.chipWrapper}>
          {recommendedIngredients.map((item) => {
            const isSelected = ingredients.some(
              (i) => i.ingredient === item.name,
            );
            return (
              <TouchableOpacity
                key={item.name}
                style={[
                  commonStyles.aiChip,
                  isSelected && commonStyles.aiChipSelected,
                ]}
                onPress={() => toggleIngredient(item.name)}
              >
                <Text
                  style={[
                    commonStyles.aiChipText,
                    isSelected && commonStyles.aiChipTextSelected,
                  ]}
                >
                  {isSelected ? `✓ ${item.name}` : item.name}
                </Text>
              </TouchableOpacity>
            );
          })}
        </View>
      </View>

      {/* 5. 수량 선택 (재료를 선택했을 때만 나타남) */}
      {activeIngredientName && (
        <View
          style={[
            commonStyles.sectionCard,
            {borderColor: Colors.warning, borderWidth: 1},
          ]}
        >
          <Text style={[commonStyles.sectionTitle, {color: Colors.warning}]}>
            ⚖️ [{activeIngredientName}] 수량 선택
          </Text>
          <View style={commonStyles.chipWrapper}>
            {QUANTITY_OPTIONS.map((qty) => (
              <TouchableOpacity
                key={qty}
                style={commonStyles.chip}
                onPress={() => updateQuantity(qty)}
              >
                <Text style={commonStyles.chipText}>{qty}</Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>
      )}

      {/* 6. 검색 결과 섹션 */}
      {searchText.length > 0 && (
        <View style={commonStyles.sectionCard}>
          <Text style={commonStyles.sectionTitle}>🔍 검색 결과</Text>
          {isLoading ? (
            <ActivityIndicator size="small" color={Colors.primary} />
          ) : (
            <View style={commonStyles.chipWrapper}>
              {ingredientOptions.map((name) => {
                const isSelected = ingredients.some(
                  (i) => i.ingredient === name,
                );
                return (
                  <TouchableOpacity
                    key={name}
                    style={[
                      commonStyles.chip,
                      isSelected && commonStyles.chipSelected,
                    ]}
                    onPress={() => toggleIngredient(name)}
                  >
                    <Text
                      style={[
                        commonStyles.chipText,
                        isSelected && commonStyles.chipTextSelected,
                      ]}
                    >
                      {isSelected ? `✓ ${name}` : name}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>
          )}
        </View>
      )}

      {/* 7. 하단 액션 버튼들 */}
      <View style={{marginTop: 1, gap: 10}}>
        <TouchableOpacity
          style={commonStyles.nextButton}
          onPress={() => router.push("/spices")}
        >
          <Text style={commonStyles.nextButtonText}>
            조미료 선택하러 가기 ▼
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => router.back()}
          style={commonStyles.textButton}
        >
          <Text style={commonStyles.textButtonLabel}>
            이전 단계로 (입력 방식 변경)
          </Text>
        </TouchableOpacity>
      </View>

      <View style={{height: 50}} />
    </ScrollView>
  );
}
