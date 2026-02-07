// src/styles/common.styles.ts
import {StyleSheet} from "react-native";

// [Group 1: Design Tokens]
export const Colors = {
  background: "#F2F2F7",
  white: "#FFFFFF",
  black: "#000000",
  gray: "#8E8E93",
  lightGray: "#E5E5EA",
  placeholder: "#C7C7CC",
  primary: "#007AFF",
  success: "#34C759",
  warning: "#FF9500",
  indigo: "#5856D6",
  error: "#FF3B30",
  overlay: "rgba(0, 0, 0, 0.5)",
};

export const commonStyles = StyleSheet.create({
  // [Group 2: Base Layouts]
  safeContainer: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  contentContainer: {
    paddingTop: 50, // 60 -> 50 (상단 여백 축소)
    paddingHorizontal: 16, // 20 -> 16 (좌우 여백 축소)
  },
  loaderContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    padding: 40,
  },

  // [Group 3: Typography]
  mainTitle: {
    fontSize: 22, // 24 -> 22
    fontWeight: "bold",
    marginBottom: 8,
    color: Colors.black,
  },
  sectionTitle: {
    fontSize: 15, // 16 -> 15
    fontWeight: "bold",
    color: Colors.gray,
    marginBottom: 10, // 15 -> 10
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
    borderRadius: 10, // 12 -> 10
    paddingHorizontal: 12,
    height: 44, // 50 -> 44 (높이 축소)
    justifyContent: "center",
    marginBottom: 15, // 20 -> 15
    borderWidth: 1,
    borderColor: Colors.lightGray,
    elevation: 2,
    shadowColor: "#000",
    shadowOffset: {width: 0, height: 2},
    shadowOpacity: 0.05,
    shadowRadius: 4,
  },
  searchInput: {
    fontSize: 15, // 16 -> 15
    color: Colors.black,
  },

  // [Group 5: Interactive - Chips] (여기가 핵심 수정 부분)
  chipWrapper: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 6, // 8 -> 6 (간격 축소)
  },
  chip: {
    backgroundColor: Colors.background,
    paddingHorizontal: 10, // 14 -> 10 (좌우 여백 축소)
    paddingVertical: 6, // 10 -> 6 (상하 여백 대폭 축소)
    borderRadius: 8, // 10 -> 8
    borderWidth: 1,
    borderColor: Colors.lightGray,
  },
  chipSelected: {
    backgroundColor: Colors.primary,
    borderColor: Colors.primary,
  },
  chipText: {
    color: Colors.black,
    fontSize: 13, // 14 -> 13 (글자 크기 축소)
    fontWeight: "500",
  },
  chipTextSelected: {
    color: Colors.white,
    fontWeight: "bold",
  },
  aiChip: {
    backgroundColor: "#FFF4E5",
    paddingHorizontal: 10, // 14 -> 10
    paddingVertical: 6, // 10 -> 6
    borderRadius: 8, // 10 -> 8
    borderWidth: 1,
    borderColor: Colors.warning,
  },
  aiChipSelected: {
    backgroundColor: Colors.warning,
    borderColor: Colors.warning,
  },
  aiChipText: {
    color: Colors.warning,
    fontSize: 13, // 14 -> 13
    fontWeight: "bold",
  },
  aiChipTextSelected: {
    color: Colors.white,
    fontWeight: "bold",
  },

  // [Group 6: Interactive - Buttons]
  nextButton: {
    backgroundColor: Colors.success,
    padding: 16, // 18 -> 16
    borderRadius: 14,
    alignItems: "center",
    marginTop: 10,
    elevation: 3,
  },
  nextButtonText: {
    color: Colors.white,
    fontSize: 16, // 17 -> 16
    fontWeight: "bold",
  },
  menuButton: {
    backgroundColor: Colors.success,
    padding: 18, // 22 -> 18
    borderRadius: 12,
    alignItems: "center",
    marginBottom: 12, // 15 -> 12
    elevation: 2,
  },
  menuButtonText: {
    color: Colors.white,
    fontSize: 16, // 18 -> 16
    fontWeight: "bold",
  },
  textButton: {
    marginTop: 15, // 20 -> 15
    alignItems: "center",
  },
  textButtonLabel: {
    color: Colors.gray,
    fontSize: 14, // 16 -> 14
    textDecorationLine: "underline",
  },

  // [Group 7: Information Display]
  sectionCard: {
    backgroundColor: Colors.white,
    borderRadius: 16, // 20 -> 16
    padding: 15, // 20 -> 15 (카드 내부 여백 축소)
    marginBottom: 15, // 20 -> 15
    elevation: 1,
  },
  aiBorder: {
    borderWidth: 1.5,
    borderColor: Colors.warning,
  },
  badge: {
    backgroundColor: "#E6F4FE",
    alignSelf: "flex-start",
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
    marginBottom: 6,
  },
  badgeText: {
    color: Colors.primary,
    fontWeight: "bold",
    fontSize: 11,
  },

  // [Group 8: Image Thumbnails]
  thumbnailRow: {
    flexDirection: "row",
    justifyContent: "flex-start",
    gap: 10, // 15 -> 10
    marginBottom: 20,
  },
  thumbnailFilled: {
    width: 90, // 100 -> 90
    height: 90, // 100 -> 90
    borderRadius: 10,
    position: "relative",
  },
  thumbnailSlot: {
    width: 90, // 100 -> 90
    height: 90, // 100 -> 90
    borderRadius: 10,
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
    borderRadius: 10,
    borderWidth: 1,
    borderColor: Colors.lightGray,
  },
  deleteBadge: {
    position: "absolute",
    top: -6,
    right: -6,
    backgroundColor: Colors.error,
    width: 22,
    height: 22,
    borderRadius: 11,
    justifyContent: "center",
    alignItems: "center",
    borderWidth: 2,
    borderColor: Colors.white,
    zIndex: 1,
  },
  deleteText: {
    color: Colors.white,
    fontWeight: "bold",
    fontSize: 10,
  },
  addButtonSmall: {
    width: "100%",
    height: "100%",
    justifyContent: "center",
    alignItems: "center",
  },
  plusText: {
    fontSize: 24,
    color: Colors.placeholder,
    fontWeight: "300",
  },
});
