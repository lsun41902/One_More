// src/components/RecipeCard.tsx
import React from "react";
import { StyleSheet, Text, View } from "react-native";
import { RecipeResponse } from "../types/recipe"; // DTO import

// Props 정의: 이 컴포넌트는 recipe 데이터와 index 번호가 필요함
interface RecipeCardProps {
  recipe: RecipeResponse;
  index: number;
}

export default function RecipeCard({ recipe, index }: RecipeCardProps) {
  return (
    <View style={styles.card}>
      <Text style={styles.index}>#{index + 1}번 레시피</Text>

      <Text style={styles.label}>[제목]</Text>
      <Text style={styles.valueTitle}>{recipe.title}</Text>

      <Text style={styles.label}>[한줄 요약]</Text>
      <Text style={styles.value}>{recipe.summary}</Text>

      <Text style={styles.label}>[필요 재료]</Text>
      {recipe.ingredients.map((ing, i) => (
        <Text key={i} style={styles.value}>
          - {ing.ingredient} ({ing.quantity})
        </Text>
      ))}

      {recipe.more && recipe.more.length > 0 && (
        <>
          <Text style={styles.label}>[추가 필요 재료]</Text>
          {recipe.more.map((ing, i) => (
            <Text key={i} style={styles.value}>
              + {ing.ingredient} ({ing.quantity})
            </Text>
          ))}
        </>
      )}

      <Text style={styles.label}>[조리 순서]</Text>
      {recipe.recipe.map((step, i) => (
        <Text key={i} style={styles.value}>
          {step}
        </Text>
      ))}

      <Text style={styles.label}>[요리 팁]</Text>
      {recipe.tip.map((t, i) => (
        <Text key={i} style={styles.value}>
          * {t}
        </Text>
      ))}
    </View>
  );
}

// 카드 전용 스타일은 여기에 둡니다 (응집도 향상)
const styles = StyleSheet.create({
  card: {
    backgroundColor: "#fff",
    borderRadius: 10,
    padding: 15,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: "#ddd",
    elevation: 3,
  },
  index: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#007AFF",
    marginBottom: 10,
    borderBottomWidth: 1,
    borderBottomColor: "#eee",
    paddingBottom: 5,
  },
  label: {
    fontSize: 14,
    fontWeight: "bold",
    color: "#888",
    marginTop: 10,
    marginBottom: 2,
  },
  valueTitle: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#333",
  },
  value: {
    fontSize: 15,
    color: "#333",
    lineHeight: 22,
    marginLeft: 5,
  },
});
