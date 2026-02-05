// src/styles/home.styles.ts
import { StyleSheet } from "react-native";

export const homeStyles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f0f0f0",
    paddingTop: 60,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: "bold",
    textAlign: "center",
    marginBottom: 20,
  },
  buttonContainer: {
    paddingHorizontal: 20,
    marginBottom: 10,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  loadingText: {
    marginTop: 15,
    textAlign: "center",
    color: "#666",
  },
  scrollView: {
    paddingHorizontal: 15,
  },
  emptyText: {
    textAlign: "center",
    marginTop: 50,
    color: "#999",
    fontSize: 16,
  },

  preferenceContainer: {
    width: "90%",
    maxHeight: 150, // 최대 높이 설정
    backgroundColor: "#e9f7ef", // 연한 녹색 배경
    padding: 10,
    borderRadius: 8,
    marginBottom: 15,
    borderWidth: 1,
    borderColor: "#d4edda",
  },
  preferenceText: {
    fontSize: 14,
    lineHeight: 20,
    color: "#333",
  },
});
