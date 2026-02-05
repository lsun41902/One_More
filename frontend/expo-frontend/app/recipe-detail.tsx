// app/recipe-detail.tsx
import {useRouter} from "expo-router";
import React from "react";
import {Image, ScrollView, Text, TouchableOpacity, View} from "react-native";
import SummaryHeader from "../src/components/SummaryHeader";
import {useAppContent} from "../src/context/AppContext";
import {Colors, commonStyles} from "../src/styles/common.styles";

export default function RecipeDetailScreen() {
  const router = useRouter();
  // Global Context에서 데이터 추출
  const {selectedRecipe, selections, resetAll} = useAppContent();

  // 홈으로 이동하며 상태 초기화 (전체 프로세스 리셋)
  const handleGoHome = () => {
    resetAll();
    router.dismissAll();
    router.replace("/");
  };

  // 예외 처리: 데이터가 없을 경우 (Deep Link 등으로 진입 시 발생 가능)
  if (!selectedRecipe) {
    return (
      <View style={commonStyles.loaderContainer}>
        <Text style={{color: Colors.gray}}>
          레시피 정보를 찾을 수 없습니다.
        </Text>
        <TouchableOpacity
          onPress={() => router.back()}
          style={commonStyles.textButton}
        >
          <Text style={commonStyles.textButtonLabel}>뒤로 가기</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <ScrollView
      style={commonStyles.safeContainer}
      contentContainerStyle={commonStyles.contentContainer}
    >
      {/* 1. 상단 요약 헤더: 현재 요리 설정(스타일, 맛, 상태) 표시 */}
      <SummaryHeader selections={selections} />

      {/* 2. 레시피 타이틀 및 요약 섹션 */}
      <View style={{marginBottom: 20}}>
        <Text style={commonStyles.mainTitle}>{selectedRecipe.title}</Text>
        <View
          style={[
            commonStyles.sectionCard,
            {
              backgroundColor: "#F2F2F7",
              borderLeftWidth: 5,
              borderLeftColor: Colors.success,
            },
          ]}
        >
          <Text
            style={{
              fontSize: 16,
              color: "#555",
              fontStyle: "italic",
              lineHeight: 24,
            }}
          >
            {/* 
              [Linter 에러 해결 포인트] 
              따옴표(")를 직접 쓰지 않고 문자열 결합을 통해 이스케이프(\") 처리.
              이렇게 하면 react/no-unescaped-entities 에러가 발생하지 않습니다.
            */}
            {'"' + selectedRecipe.summary + '"'}
          </Text>
        </View>
      </View>

      {/* 3. AI 생성 이미지 (있을 경우에만 렌더링) */}
      {selectedRecipe.image && (
        <Image
          source={{uri: selectedRecipe.image}}
          style={{
            width: "100%",
            height: 220,
            borderRadius: 20,
            marginBottom: 20,
          }}
          resizeMode="cover"
        />
      )}

      {/* 4. 필요 재료 섹션: 보유 재료 + 추가 필요 재료(more) */}
      <View style={commonStyles.sectionCard}>
        <Text style={commonStyles.sectionTitle}>🛒 필요 재료</Text>
        {selectedRecipe.ingredients.map((ing, idx) => (
          <View
            key={`ing-${idx}`}
            style={{
              flexDirection: "row",
              justifyContent: "space-between",
              paddingVertical: 8,
              borderBottomWidth: 1,
              borderBottomColor: "#F2F2F7",
            }}
          >
            <Text style={{fontSize: 16, color: Colors.black}}>
              • {ing.ingredient}
            </Text>
            <Text
              style={{fontSize: 16, fontWeight: "bold", color: Colors.primary}}
            >
              {ing.quantity}
            </Text>
          </View>
        ))}

        {/* 추가 재료(more) 리스트가 존재할 경우 경고 문구와 함께 렌더링 */}
        {selectedRecipe.more && selectedRecipe.more.length > 0 ? (
          <View
            style={{
              marginTop: 15,
              paddingTop: 15,
              borderTopWidth: 1,
              borderTopColor: Colors.lightGray,
            }}
          >
            <Text
              style={{
                fontSize: 14,
                color: Colors.warning,
                fontWeight: "bold",
                marginBottom: 10,
              }}
            >
              ⚠️ 추가로 필요한 재료 (장보기가 필요해요)
            </Text>
            {selectedRecipe.more.map((ing, idx) => (
              <View
                key={`more-${idx}`}
                style={{
                  flexDirection: "row",
                  justifyContent: "space-between",
                  paddingVertical: 5,
                }}
              >
                <Text style={{fontSize: 15, color: "#666"}}>
                  + {ing.ingredient}
                </Text>
                <Text style={{fontSize: 15, color: Colors.gray}}>
                  {ing.quantity}
                </Text>
              </View>
            ))}
          </View>
        ) : null}
      </View>

      {/* 5. 조리 순서 섹션 (Array Map 순회) */}
      <View style={commonStyles.sectionCard}>
        <Text style={commonStyles.sectionTitle}>🍳 조리 순서</Text>
        {selectedRecipe.recipe.map((step, index) => (
          <View key={index} style={{flexDirection: "row", marginBottom: 20}}>
            <View style={[commonStyles.badge, {marginRight: 12, marginTop: 2}]}>
              <Text style={commonStyles.badgeText}>{index + 1}</Text>
            </View>
            <Text
              style={{
                flex: 1,
                fontSize: 16,
                color: Colors.black,
                lineHeight: 24,
              }}
            >
              {step}
            </Text>
          </View>
        ))}
      </View>

      {/* 6. 하단 네비게이션 액션 버튼 */}
      <View style={{gap: 10, marginTop: 10}}>
        <TouchableOpacity
          style={[commonStyles.nextButton, {backgroundColor: Colors.black}]}
          onPress={() => router.back()}
        >
          <Text style={commonStyles.nextButtonText}>목록으로 돌아가기</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={commonStyles.textButton}
          onPress={handleGoHome}
        >
          <Text style={commonStyles.textButtonLabel}>
            처음부터 다시 하기 (전체 초기화)
          </Text>
        </TouchableOpacity>
      </View>

      {/* 하단 여백 확보 */}
      <View style={{height: 50}} />
    </ScrollView>
  );
}
