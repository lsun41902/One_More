// app/spices.tsx
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

export default function SpicesScreen() {
  const router = useRouter();
  const {spices, setSpices, selections, recommendedSpices} = useAppContent();
  const {spiceOptions, isLoading, searchSpices} = useMasterDataService();
  const [searchText, setSearchText] = useState("");

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => searchSpices(searchText), 400);
    return () => clearTimeout(delayDebounceFn);
  }, [searchText]);

  const toggleSpice = (value: string) => {
    setSpices(
      spices.includes(value)
        ? spices.filter((s) => s !== value)
        : [...spices, value],
    );
  };

  return (
    <ScrollView
      style={commonStyles.safeContainer}
      contentContainerStyle={commonStyles.contentContainer}
    >
      <SummaryHeader selections={selections} />

      <Text style={commonStyles.mainTitle}>조미료 선택</Text>
      <Text style={commonStyles.subTitle}>
        AI가 추천한 조미료와 직접 검색한 조미료입니다.
      </Text>

      <View style={commonStyles.searchBar}>
        <TextInput
          style={commonStyles.searchInput}
          placeholder="조미료를 직접 검색해보세요"
          value={searchText}
          onChangeText={setSearchText}
          placeholderTextColor={Colors.placeholder}
        />
      </View>

      {/* 확정된 조미료 목록 */}
      {spices.length > 0 && (
        <View
          style={[
            commonStyles.sectionCard,
            {
              backgroundColor: "#E6F4FE",
              borderColor: Colors.primary,
              borderWidth: 1,
            },
          ]}
        >
          <Text style={[commonStyles.sectionTitle, {color: Colors.primary}]}>
            🛒 담은 조미료 ({spices.length})
          </Text>
          <View style={commonStyles.chipWrapper}>
            {spices.map((name) => (
              <TouchableOpacity
                key={name}
                style={[commonStyles.chip, commonStyles.chipSelected]}
                onPress={() => toggleSpice(name)}
              >
                <Text style={commonStyles.chipTextSelected}>{name} ✕</Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>
      )}

      {/* AI 추천 섹션 */}
      <View style={[commonStyles.sectionCard, commonStyles.aiBorder]}>
        <Text style={[commonStyles.sectionTitle, commonStyles.aiTitle]}>
          👨‍🍳 취향 맞춤 추천 조미료
        </Text>
        <View style={commonStyles.chipWrapper}>
          {recommendedSpices.map((item) => {
            const isSelected = spices.includes(item.name);
            return (
              <TouchableOpacity
                key={item.id}
                style={[
                  commonStyles.aiChip,
                  isSelected && commonStyles.aiChipSelected,
                ]}
                onPress={() => toggleSpice(item.name)}
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

      {/* 검색 결과 섹션 */}
      {searchText.length > 0 && (
        <View style={commonStyles.sectionCard}>
          <Text style={commonStyles.sectionTitle}>🔍 검색 결과</Text>
          {isLoading ? (
            <ActivityIndicator size="small" color={Colors.primary} />
          ) : (
            <View style={commonStyles.chipWrapper}>
              {spiceOptions.map((name) => (
                <TouchableOpacity
                  key={name}
                  style={[
                    commonStyles.chip,
                    spices.includes(name) && commonStyles.chipSelected,
                  ]}
                  onPress={() => toggleSpice(name)}
                >
                  <Text
                    style={[
                      commonStyles.chipText,
                      spices.includes(name) && commonStyles.chipTextSelected,
                    ]}
                  >
                    {name}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          )}
        </View>
      )}

      {/* 하단 액션 버튼들 */}
      <View style={{marginTop: 20, gap: 10}}>
        <TouchableOpacity
          style={commonStyles.nextButton}
          onPress={() => router.push("/recipe-check")}
        >
          <Text style={commonStyles.nextButtonText}>
            조미료 선택 완료 (최종 확인) ▼
          </Text>
        </TouchableOpacity>

        {/* [추가] 이전으로 돌아가기: router.back()을 통해 재료 선택 화면으로 이동 */}
        <TouchableOpacity
          onPress={() => router.back()}
          style={commonStyles.textButton}
        >
          <Text style={commonStyles.textButtonLabel}>
            이전 단계로 (재료 다시 고르기)
          </Text>
        </TouchableOpacity>
      </View>

      <View style={{height: 50}} />
    </ScrollView>
  );
}
