import React from 'react';
import { View, Text, StyleSheet, Button, Alert } from 'react-native';

export default function HomeScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>내 첫 번째 버튼</Text>
      
      {/* 여기에 버튼을 추가했습니다 */}
      <View style={styles.buttonContainer}>
        <Button
          title="여기를 눌러보세요"
          color="#007AFF" // 아이폰 기본 파란색
          onPress={() => Alert.alert('성공!', '버튼이 작동합니다.')}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  buttonContainer: {
    marginTop: 10,
    padding: 10,
    borderRadius: 8,
    backgroundColor: '#f0f0f0', // 버튼 배경을 살짝 구분
  },
});