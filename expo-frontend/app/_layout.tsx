import { Stack } from "expo-router";
import { AppProvider } from "../src/context/AppContext";

export default function RootLayout() {
  return (
    <AppProvider>
      <Stack>
        {/* 메인 화면 (취향 선택) */}
        <Stack.Screen name="index" options={{ headerShown: false }} />
        {/* 입력 방식 선택 화면 */}
        <Stack.Screen
          name="input-method"
          options={{ title: "등록 방법 선택" }}
        />
        {/* 카메라 촬영 화면 */}
        <Stack.Screen
          name="camera"
          options={{ title: "사진 촬영", headerShown: false }}
        />
      </Stack>
    </AppProvider>
  );
}
