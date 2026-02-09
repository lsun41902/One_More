import React from "react";
import {StyleSheet, Text, View} from "react-native";

// 컴포넌트가 받을 데이터 타입 정의
interface SummaryHeaderProps {
  selections: {style: string; taste: string; condition: string};
}

export default function SummaryHeader({selections}: SummaryHeaderProps) {
  // 값이 없을 때 보여줄 텍스트 처리
  const renderItem = (value: string, label: string) => (
    <View style={styles.itemWrapper}>
      <Text style={styles.label}>{label}</Text>
      <Text style={[styles.value, !value && styles.placeholder]}>
        {value || "미선택"}
      </Text>
    </View>
  );

  return (
    <View style={styles.container}>
      <Text style={styles.title}>내 요리 설정</Text>
      <View style={styles.row}>
        {renderItem(selections.style, "스타일")}
        {renderItem(selections.taste, "맛")}
        {renderItem(selections.condition, "상태")}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: "#34C759", // 다크 그레이 (시스템 색상)
    padding: 20,
    borderRadius: 16,
    marginBottom: 10,
    width: "100%",
  },
  title: {
    color: "#000000",
    fontSize: 16,
    fontWeight: "bold",
    marginBottom: 12,
  },
  row: {flexDirection: "row", justifyContent: "space-between"},
  itemWrapper: {flex: 1},
  label: {color: "#FFFFFF", fontSize: 12, marginBottom: 4, fontWeight: "bold"},
  value: {color: "#000000", fontSize: 16, fontWeight: "bold"},
  placeholder: {color: "#48484A"}, // 선택 전에는 흐릿하게 표시
});
