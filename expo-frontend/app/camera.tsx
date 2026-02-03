// app/camera.tsx
import {useLocalSearchParams, useRouter} from "expo-router";
import React, {useState} from "react";
import {
  ActivityIndicator,
  Alert,
  Image,
  Modal,
  ScrollView,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import {useAppContent} from "../src/context/AppContext";
import {useImagePickerService} from "../src/hooks/useImagePickerService";
import {useMasterDataService} from "../src/hooks/useMasterDataService";
import {Colors, commonStyles} from "../src/styles/common.styles";

export default function CameraScreen() {
  const router = useRouter();
  const params = useLocalSearchParams();
  const analysisType = (params.type as "image" | "receipt") || "image";

  const {selectedImages} = useAppContent();
  const {pickImage, takePhoto, removeImage} = useImagePickerService();
  const {analyzeImages} = useMasterDataService();

  const [isAnalyzing, setIsAnalyzing] = useState(false);

  const handleAnalyze = async () => {
    if (selectedImages.length === 0) {
      Alert.alert("알림", "분석할 사진을 최소 1장 선택해주세요.");
      return;
    }

    setIsAnalyzing(true);
    const success = await analyzeImages(selectedImages, analysisType);
    setIsAnalyzing(false);

    if (success) {
      router.replace("/ingredients");
    }
  };

  return (
    // [수정] View 대신 ScrollView를 사용하여 요소가 겹치지 않게 함
    <ScrollView
      style={commonStyles.safeContainer}
      contentContainerStyle={commonStyles.contentContainer}
    >
      {/* 분석 중 로딩 모달 */}
      <Modal transparent={true} visible={isAnalyzing} animationType="fade">
        <View
          style={{
            flex: 1,
            backgroundColor: "rgba(0,0,0,0.7)",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <View
            style={{
              backgroundColor: Colors.white,
              padding: 30,
              borderRadius: 20,
              alignItems: "center",
            }}
          >
            <ActivityIndicator size="large" color={Colors.warning} />
            <Text
              style={{
                marginTop: 20,
                fontSize: 18,
                fontWeight: "bold",
                color: Colors.black,
              }}
            >
              {analysisType === "receipt"
                ? "영수증을 읽고 있어요..."
                : "식재료를 분석 중입니다..."}
            </Text>
          </View>
        </View>
      </Modal>

      <View>
        <Text style={commonStyles.mainTitle}>
          {analysisType === "receipt" ? "영수증 확인 🧾" : "사진 확인 📸"}
        </Text>
        <Text style={commonStyles.subTitle}>
          선택한 사진을 확인해주세요.{"\n"}최대 3장까지 분석할 수 있어요.
        </Text>

        {/* 사진 슬롯 영역 */}
        <View style={commonStyles.thumbnailRow}>
          {[0, 1, 2].map((index) => {
            const imageUri = selectedImages[index];
            if (imageUri) {
              return (
                <View key={index} style={commonStyles.thumbnailFilled}>
                  <Image
                    source={{uri: imageUri}}
                    style={commonStyles.thumbnailImage}
                    resizeMode="cover"
                  />
                  <TouchableOpacity
                    style={commonStyles.deleteBadge}
                    onPress={() => removeImage(index)}
                  >
                    <Text style={commonStyles.deleteText}>✕</Text>
                  </TouchableOpacity>
                </View>
              );
            } else {
              return (
                <TouchableOpacity
                  key={index}
                  style={commonStyles.thumbnailSlot}
                  onPress={() => {
                    Alert.alert(
                      "사진 추가",
                      "어떤 방식으로 추가하시겠습니까?",
                      [
                        {text: "앨범에서 선택", onPress: pickImage},
                        {text: "카메라 촬영", onPress: takePhoto},
                        {text: "취소", style: "cancel"},
                      ],
                    );
                  }}
                >
                  <View style={commonStyles.addButtonSmall}>
                    <Text style={commonStyles.plusText}>+</Text>
                  </View>
                </TouchableOpacity>
              );
            }
          })}
        </View>

        {/* 팁 섹션 */}
        <View style={commonStyles.sectionCard}>
          <Text style={commonStyles.sectionTitle}>💡 팁</Text>
          <Text style={{color: Colors.gray, lineHeight: 20}}>
            {analysisType === "receipt"
              ? "• 영수증의 글자가 잘 보이도록 찍어주세요.\n• 구겨진 영수증은 펴서 촬영해주세요."
              : "• 재료가 잘 보이도록 밝은 곳에서 찍어주세요.\n• 여러 재료를 한꺼번에 찍어도 됩니다."}
          </Text>
        </View>

        {/* [수정] 버튼 영역: flex: 1을 제거하고 marginTop을 주어 겹침 방지 */}
        <View style={{marginTop: 40, marginBottom: 40, gap: 10}}>
          <TouchableOpacity
            style={[
              commonStyles.nextButton,
              selectedImages.length === 0 && {
                backgroundColor: Colors.lightGray,
              },
            ]}
            onPress={handleAnalyze}
            disabled={selectedImages.length === 0 || isAnalyzing}
          >
            <Text style={commonStyles.nextButtonText}>
              {selectedImages.length > 0
                ? `${selectedImages.length}장 분석 시작하기 🚀`
                : "사진을 추가해주세요"}
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            onPress={() => router.back()}
            style={commonStyles.textButton}
            disabled={isAnalyzing}
          >
            <Text style={commonStyles.textButtonLabel}>이전 단계로</Text>
          </TouchableOpacity>
        </View>
      </View>
    </ScrollView>
  );
}
