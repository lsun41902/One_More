import React from "react";
import { StyleSheet, Text, TouchableOpacity, View } from "react-native";

export default function HomeScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>나의 입맛</Text>

      {/* 컨테이너 */}
      <view style={styles.chipsContainer}>
        <TouchableOpacity style={styles.chip}>
          <Text style={styles.chipText}>한식</Text>
        </TouchableOpacity>

        <TouchableOpacity style={(styles.chip, styles.chipActive)}>
          <Text style={styles.chipText}>중식</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.chip}>
          <Text style={styles.chipText}>일식</Text>
        </TouchableOpacity>
      </view>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 60,
    alignItems: "center",
    backgroundColor: "#fff",
  },
  title: {
    fontSize: 18,
    fontWeight: "bold",
    marginBottom: "15",
  },
  chipsContainer: {
    flexDirection: "row",
    flexWrap: "wrap",
    justifyContent: "center",
    gap: 8,
  },
  chip: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: "#ddd",
    backgroundColor: "#f9f9f9",
  },
  chipActive: {
    backgroundColor: "#007AFF",
    borderColor: "#007AFF",
  },
  chipText: {
    color: "#333",
    fontSize: 14,
  },
  chipTextActive: {
    color: "#fff",
    fontWeight: "bold",
  },
});
