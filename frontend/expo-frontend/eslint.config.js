// https://docs.expo.dev/guides/using-eslint/
const {defineConfig} = require("eslint/config");
const expoConfig = require("eslint-config-expo/flat");
const reactNative = require("eslint-plugin-react-native");

module.exports = defineConfig([
  ...expoConfig, // expoConfig는 배열이므로 전개 연산자(...)를 사용하는 것이 안전해
  {
    plugins: {
      "react-native": reactNative,
    },
    rules: {
      // <View> 안에 생으로 글자를 쓰는 실수를 잡아주는 규칙
      "react-native/no-raw-text": [
        "error",
        {
          skip: ["CustomText"], // 예외로 둘 컴포넌트가 있다면 추가
        },
      ],
      // 추가하면 관리하기 편한 규칙들
      "react-native/no-unused-styles": "warn",
      "react-native/no-inline-styles": "warn",
    },
  },
  {
    ignores: ["dist/*"],
  },
]);
