// src/styles/common.styles.ts
import {StyleSheet} from "react-native";

// [Group 1: Design Tokens]
export const Colors = {
  background: "#F2F2F7",
  white: "#FFFFFF",
  black: "#1C1C1E",
  gray: "#8E8E93",
  lightGray: "#E5E5EA",
  placeholder: "#C7C7CC",
  primary: "#007AFF",
  success: "#34C759",
  warning: "#FF9500",
  indigo: "#5856D6",
  error: "#FF3B30", // 삭제 버튼용 레드
  overlay: "rgba(0,0,0,0.5)", // 배경 어둡게
};

export const commonStyles = StyleSheet.create({
  // [Group 2: Base Layouts]
  safeContainer: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  contentContainer: {
    paddingTop: 60,
    paddingHorizontal: 20,
  },
  loaderContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    padding: 40,
  },

  // [Group 3: Typography]
  mainTitle: {
    fontSize: 24,
    fontWeight: "bold",
    marginBottom: 10,
    color: Colors.black,
  },
  subTitle: {
    fontSize: 16,
    color: Colors.gray,
    marginBottom: 25,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: "bold",
    color: Colors.gray,
    marginBottom: 15,
  },
  aiTitle: {
    color: Colors.warning,
  },
  loadingText: {
    marginTop: 20,
    fontSize: 18,
    fontWeight: "bold",
    color: Colors.black,
  },
  emptyText: {
    color: Colors.placeholder,
    fontSize: 14,
    textAlign: "center",
    marginVertical: 10,
  },

  // [Group 4: Interactive - Search Bar]
  searchBar: {
    backgroundColor: Colors.white,
    borderRadius: 12,
    paddingHorizontal: 16,
    height: 50,
    justifyContent: "center",
    marginBottom: 20,
    borderWidth: 1,
    borderColor: Colors.lightGray,
    elevation: 2,
    shadowColor: "#000",
    shadowOffset: {width: 0, height: 2},
    shadowOpacity: 0.05,
    shadowRadius: 4,
  },
  searchInput: {
    fontSize: 16,
    color: Colors.black,
  },

  // [Group 5: Interactive - Chips]
  chipWrapper: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 8,
  },
  chip: {
    backgroundColor: Colors.background,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: Colors.lightGray,
  },
  chipSelected: {
    backgroundColor: Colors.primary,
    borderColor: Colors.primary,
  },
  chipText: {
    color: Colors.black,
    fontSize: 14,
    fontWeight: "500",
  },
  chipTextSelected: {
    color: Colors.white,
    fontWeight: "bold",
  },
  aiChip: {
    backgroundColor: "#FFF4E5",
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: Colors.warning,
  },
  aiChipSelected: {
    backgroundColor: Colors.warning,
    borderColor: Colors.warning,
  },
  aiChipText: {
    color: Colors.warning,
    fontSize: 14,
    fontWeight: "bold",
  },
  aiChipTextSelected: {
    color: Colors.white,
    fontWeight: "bold",
  },

  // [Group 6: Interactive - Buttons]
  nextButton: {
    backgroundColor: Colors.success,
    padding: 18,
    borderRadius: 16,
    alignItems: "center",
    marginTop: 10,
    elevation: 3,
  },
  nextButtonText: {
    color: Colors.white,
    fontSize: 17,
    fontWeight: "bold",
  },
  menuButton: {
    backgroundColor: Colors.success,
    padding: 22,
    borderRadius: 15,
    alignItems: "center",
    marginBottom: 15,
    elevation: 2,
  },
  menuButtonText: {
    color: Colors.white,
    fontSize: 18,
    fontWeight: "bold",
  },
  textButton: {
    marginTop: 20,
    alignItems: "center",
  },
  textButtonLabel: {
    color: Colors.gray,
    fontSize: 16,
    textDecorationLine: "underline",
  },

  // [Group 7: Information Display]
  sectionCard: {
    backgroundColor: Colors.white,
    borderRadius: 20,
    padding: 20,
    marginBottom: 20,
    elevation: 1,
  },
  aiBorder: {
    borderWidth: 1.5,
    borderColor: Colors.warning,
  },
  badge: {
    backgroundColor: "#E6F4FE",
    alignSelf: "flex-start",
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 8,
    marginBottom: 8,
  },
  badgeText: {
    color: Colors.primary,
    fontWeight: "bold",
    fontSize: 12,
  },

  // [Group 8: Image Thumbnails]
  thumbnailRow: {
    flexDirection: "row",
    justifyContent: "flex-start", // [수정] 왼쪽 정렬
    gap: 15, // [수정] 아이템 간 간격 고정
    marginBottom: 30,
  },
  // [신규] 이미지가 들어있는 슬롯의 컨테이너 (크기 고정 필수)
  thumbnailFilled: {
    width: 100,
    height: 100,
    borderRadius: 12,
    position: "relative", // 삭제 배지를 위해 relative 설정
  },
  thumbnailSlot: {
    width: 100,
    height: 100,
    borderRadius: 12,
    backgroundColor: Colors.background,
    borderWidth: 1,
    borderColor: Colors.lightGray,
    justifyContent: "center",
    alignItems: "center",
    borderStyle: "dashed",
  },
  thumbnailImage: {
    width: "100%",
    height: "100%",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: Colors.lightGray,
  },
  deleteBadge: {
    position: "absolute",
    top: -8,
    right: -8,
    backgroundColor: Colors.error,
    width: 24,
    height: 24,
    borderRadius: 12,
    justifyContent: "center",
    alignItems: "center",
    borderWidth: 2,
    borderColor: Colors.white,
    zIndex: 1,
  },
  deleteText: {
    color: Colors.white,
    fontWeight: "bold",
    fontSize: 12,
  },
  addButtonSmall: {
    width: "100%",
    height: "100%",
    justifyContent: "center",
    alignItems: "center",
  },
  plusText: {
    fontSize: 30,
    color: Colors.placeholder,
    fontWeight: "300",
  },
});
