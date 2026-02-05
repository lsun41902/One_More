// app/input-method.tsx
import {useRouter} from "expo-router";
import React from "react";
import {Text, TouchableOpacity, View} from "react-native";
import SummaryHeader from "../src/components/SummaryHeader";
import {useAppContent} from "../src/context/AppContext";
import {useImagePickerService} from "../src/hooks/useImagePickerService";
import {Colors, commonStyles} from "../src/styles/common.styles";

export default function InputMethodScreen() {
  const router = useRouter();
  const {selections} = useAppContent();
  const {pickImage} = useImagePickerService();

  /**
   * [핵심 로직] handleGoBack
   * resetAll을 호출하지 않습니다!
   * 단순히 router.back()만 실행하여, 메모리에 저장된 취향(selections)을 유지한 채 화면만 이동합니다.
   */
  const handleGoBack = () => {
    router.back();
  };

  return (
    <View style={commonStyles.safeContainer}>
      <View style={commonStyles.contentContainer}>
        <SummaryHeader selections={selections} />
        <Text style={commonStyles.mainTitle}>재료를 어떻게 등록할까?</Text>

        <View style={{width: "100%", gap: 15}}>
          <TouchableOpacity
            style={commonStyles.menuButton}
            onPress={() =>
              router.push({pathname: "/camera", params: {type: "image"}})
            }
          >
            <Text style={commonStyles.menuButtonText}>📸 식재료 사진 찍기</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[commonStyles.menuButton, {backgroundColor: Colors.indigo}]}
            onPress={() =>
              router.push({pathname: "/camera", params: {type: "receipt"}})
            }
          >
            <Text style={commonStyles.menuButtonText}>🧾 영수증 촬영하기</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[commonStyles.menuButton, {backgroundColor: Colors.primary}]}
            onPress={() => pickImage()}
          >
            <Text style={commonStyles.menuButtonText}>
              🖼️ 앨범에서 선택하기
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[commonStyles.menuButton, {backgroundColor: Colors.warning}]}
            onPress={() => router.push("/ingredients")}
          >
            <Text style={commonStyles.menuButtonText}>
              ⌨️ 직접 키워드 고르기
            </Text>
          </TouchableOpacity>
        </View>

        {/* [수정] 이전으로 돌아가기 버튼 */}
        <TouchableOpacity
          onPress={handleGoBack}
          style={commonStyles.textButton}
        >
          <Text style={commonStyles.textButtonLabel}>
            이전으로 돌아가기 (선택 유지)
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
