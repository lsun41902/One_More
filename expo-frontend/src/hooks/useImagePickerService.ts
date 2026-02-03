// src/hooks/useImagePickerService.ts
import * as ImagePicker from "expo-image-picker";
import {useRouter} from "expo-router";
import {Alert, Platform} from "react-native";
import {useAppContent} from "../context/AppContext";

export const useImagePickerService = () => {
  const router = useRouter();
  const {selectedImages, setSelectedImages} = useAppContent();

  // 최대 선택 가능 장수
  const MAX_IMAGES = 3;

  // 1. 갤러리에서 이미지 선택 (PC/Mobile 공용)
  const pickImage = async () => {
    // 권한 요청 (필요한 경우만 동작)
    const {status} = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== "granted") {
      alert("사진첩 접근 권한이 필요합니다.");
      return;
    }

    // 현재 이미지가 3장 꽉 찼는지 확인
    if (selectedImages.length >= MAX_IMAGES) {
      alert(`이미지는 최대 ${MAX_IMAGES}장까지만 선택할 수 있습니다.`);
      return;
    }

    // 갤러리 실행
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images, // 이미지만 (동영상 제외)
      allowsMultipleSelection: true, // 다중 선택 허용
      selectionLimit: MAX_IMAGES - selectedImages.length, // 남은 개수만큼만 선택 가능
      quality: 0.8, // 용량 최적화를 위해 80% 압축
      base64: false, // 바이너리 전송을 위해 base64 변환 안 함
    });

    if (!result.canceled) {
      const newUris = result.assets.map((asset) => asset.uri);
      // 기존 리스트에 추가
      const updatedList = [...selectedImages, ...newUris].slice(0, MAX_IMAGES);
      setSelectedImages(updatedList);

      // 선택 후 카메라 화면(프리뷰 화면)으로 이동하여 결과 확인
      router.push("/camera");
    }
  };

  // 2. 카메라 촬영 (Mobile 전용)
  const takePhoto = async () => {
    // [PC 방어 로직] 웹 환경에서는 카메라 실행 불가 -> 갤러리로 유도
    if (Platform.OS === "web") {
      const ok = window.confirm(
        "PC에서는 바로 촬영할 수 없습니다. 앨범에서 사진을 선택하시겠습니까?",
      );
      if (ok) {
        pickImage();
      }
      return;
    }

    // 권한 요청
    const {status} = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== "granted") {
      Alert.alert("권한 부족", "카메라 사용 권한이 필요합니다.");
      return;
    }

    if (selectedImages.length >= MAX_IMAGES) {
      Alert.alert(
        "개수 초과",
        `이미지는 최대 ${MAX_IMAGES}장까지만 가능합니다.`,
      );
      return;
    }

    // 카메라 실행
    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.8,
    });

    if (!result.canceled) {
      const newUri = result.assets[0].uri;
      setSelectedImages([...selectedImages, newUri]);

      // 촬영 후 카메라 화면(프리뷰 화면)으로 이동
      router.push("/camera");
    }
  };

  // 3. 이미지 삭제 함수 (프리뷰에서 X 버튼 누를 때 사용)
  const removeImage = (indexToRemove: number) => {
    setSelectedImages(
      selectedImages.filter((_, index) => index !== indexToRemove),
    );
  };

  return {
    selectedImages,
    pickImage,
    takePhoto,
    removeImage,
  };
};
