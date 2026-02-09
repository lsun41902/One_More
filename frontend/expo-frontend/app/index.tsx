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
  const {selections, setSelections, resetAll} = useAppContent();
  const [localStep, setLocalStep] = useState(0);
  const {preferences, isLoading, analyzePreferences} = useMasterDataService();

  // 질문 데이터 구조 정의 (마스터 데이터 기반)
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

  // 선택 핸들러: 선택 시 다음 질문 단계(localStep)로 인덱스 증가
  const handleSelect = async (key: string, value: string) => {
    const updatedSelections = {...selections, [key]: value};
    setSelections(updatedSelections);
    if (localStep < questions.length - 1) {
      setLocalStep(localStep + 1);
    }
  };

  // 다음 단계 이동: 선택된 취향 리스트를 분석 API로 전송 후 화면 이동
  const handleNextStep = () => {
    const preferenceList = [
      selections.style,
      selections.taste,
      selections.condition,
    ].filter(Boolean);
    analyzePreferences(preferenceList);
    router.push("/input-method");
  };

  // 초기화 핸들러: Global Context와 로컬 스텝 모두 리셋
  const handleReset = () => {
    resetAll();
    setLocalStep(0);
  };

  const isAllSelected =
    selections.style && selections.taste && selections.condition;

  // 초기 로딩 처리 (마스터 데이터 fetch 중일 때)
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
      {/* 상단 요약 헤더: 선택된 값이 실시간으로 반영됨 */}
      <SummaryHeader selections={selections} />

      <Text style={commonStyles.mainTitle}>취향 선택</Text>

      {/* 
          [로직 설명] 질문 리스트 렌더링 루프
          questions 배열을 순회하며 현재 단계(localStep)에 맞는 질문만 화면에 노출합니다.
      */}
      {questions.map((q, index) => {
        // 1. 아직 도달하지 않은 미래의 질문은 렌더링하지 않음 (Skip)
        if (index > localStep) return null;

        // 2. [수정 부분] 이미 선택이 완료된 이전 단계의 질문(History) 처리
        // 기존에는 여기서 TouchableOpacity를 리턴하여 선택 내용을 보여줬으나,
        // 요청하신 대로 '없애버리기' 위해 null을 리턴하도록 수정했습니다.
        if (index < localStep) {
          return null;
        }

        // 3. 현재 활성화된 질문 단계 (Active Step)
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

      {/* 모든 질문이 완료되었을 때 나타나는 하단 액션 영역 */}
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

      {/* 하단 스크롤 여백 확보 */}
      <View style={{height: 50}} />
    </ScrollView>
  );
}
