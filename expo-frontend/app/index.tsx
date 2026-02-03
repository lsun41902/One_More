// app/index.tsx
import {useRouter} from "expo-router";
import React, {useState} from "react";
import {
  ActivityIndicator,
  ScrollView,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import SummaryHeader from "../src/components/SummaryHeader";
import {useAppContent} from "../src/context/AppContext";
import {useMasterDataService} from "../src/hooks/useMasterDataService";
import {Colors, commonStyles} from "../src/styles/common.styles";

export default function HomeScreen() {
  const router = useRouter();
  const {selections, setSelections, resetAll} = useAppContent(); // [수정] resetAll 가져오기
  const [localStep, setLocalStep] = useState(0);
  const {preferences, isLoading, analyzePreferences} = useMasterDataService();

  const questions = [
    {
      key: "style",
      label: "스타일",
      title: "어떤 스타일을 선호해?",
      options: preferences.STYLE?.map((p) => p.name) || [],
    },
    {
      key: "taste",
      label: "맛",
      title: "어떤 맛을 원해?",
      options: preferences.TASTE?.map((p) => p.name) || [],
    },
    {
      key: "condition",
      label: "상태",
      title: "지금 어떤 상태야?",
      options: preferences.CONDITION?.map((p) => p.name) || [],
    },
  ];

  const handleSelect = async (key: string, value: string) => {
    const updatedSelections = {...selections, [key]: value};
    setSelections(updatedSelections);
    if (localStep < questions.length - 1) {
      setLocalStep(localStep + 1);
    }
  };

  const handleNextStep = () => {
    const preferenceList = [
      selections.style,
      selections.taste,
      selections.condition,
    ].filter(Boolean);
    analyzePreferences(preferenceList);
    router.push("/input-method");
  };

  //  [수정] handleReset
  const handleReset = () => {
    resetAll(); // 1. Global Context의 모든 데이터 삭제 (Reset All)
    setLocalStep(0); // 2. 현재 화면의 질문 단계 초기화
  };

  const isAllSelected =
    selections.style && selections.taste && selections.condition;

  if (isLoading && localStep === 0) {
    return (
      <View style={commonStyles.loaderContainer}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  return (
    <ScrollView
      style={commonStyles.safeContainer}
      contentContainerStyle={commonStyles.contentContainer}
    >
      <SummaryHeader selections={selections} />
      <Text style={commonStyles.mainTitle}>취향 선택</Text>

      {questions.map((q, index) => {
        if (index > localStep) return null;
        if (index < localStep) {
          return (
            <TouchableOpacity
              key={q.key}
              onPress={() => setLocalStep(index)}
              style={{
                flexDirection: "row",
                alignItems: "center",
                backgroundColor: "#F2F2F7",
                padding: 15,
                borderRadius: 10,
                marginBottom: 10,
              }}
            >
              <Text
                style={{
                  fontSize: 14,
                  color: "#666",
                  width: 60,
                  fontWeight: "600",
                }}
              >
                {q.label}
              </Text>
              <Text
                style={{
                  fontSize: 16,
                  color: "#333",
                  fontWeight: "bold",
                  flex: 1,
                }}
              >
                {selections[q.key as keyof typeof selections]}
              </Text>
              <Text style={{fontSize: 16, color: Colors.success}}>✔</Text>
            </TouchableOpacity>
          );
        }
        if (index === localStep) {
          return (
            <View key={q.key} style={commonStyles.sectionCard}>
              <Text
                style={[commonStyles.sectionTitle, {color: Colors.primary}]}
              >
                {q.title}
              </Text>
              <View style={commonStyles.chipWrapper}>
                {q.options.map((option) => {
                  const isSelected =
                    selections[q.key as keyof typeof selections] === option;
                  return (
                    <TouchableOpacity
                      key={option}
                      style={[
                        commonStyles.chip,
                        isSelected && commonStyles.chipSelected,
                      ]}
                      onPress={() => handleSelect(q.key, option)}
                    >
                      <Text
                        style={[
                          commonStyles.chipText,
                          isSelected && commonStyles.chipTextSelected,
                        ]}
                      >
                        {option}
                      </Text>
                    </TouchableOpacity>
                  );
                })}
              </View>
            </View>
          );
        }
        return null;
      })}

      {isAllSelected && (
        <View style={{marginTop: 20}}>
          <TouchableOpacity
            style={commonStyles.nextButton}
            onPress={handleNextStep}
          >
            <Text style={commonStyles.nextButtonText}>
              다음 단계로 (재료 입력 방식 선택) ▼
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[commonStyles.textButton, {marginTop: 15}]}
            onPress={handleReset}
          >
            <Text style={[commonStyles.textButtonLabel, {color: Colors.gray}]}>
              취향 다시 고르기
            </Text>
          </TouchableOpacity>
        </View>
      )}
      <View style={{height: 50}} />
    </ScrollView>
  );
}
