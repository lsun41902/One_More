import React, { useState } from 'react';
import axios from 'axios';

// 1. Java DTO와 동일한 구조의 타입을 정의 (설계의 시작)
interface RecipeResponse {
    title: string;
    ingredients: string[];
    steps: string[];
    tip: string;
}

const RecipeGenerator: React.FC = () => {
    // 2. 상태(State) 정의: 화면에 변하는 데이터를 저장하는 메모리 공간
    const [inputText, setInputText] = useState<string>(''); // 사용자의 입력값
    const [recipe, setRecipe] = useState<RecipeResponse | null>(null); // 서버에서 받을 레시피 데이터
    const [isLoading, setIsLoading] = useState<boolean>(false); // 로딩 상태

    // 3. 비동기 통신 함수: 버튼 클릭 시 실행
    const handleGenerateRecipe = async () => {
        setIsLoading(true); // 로딩 시작 (화면이 자동으로 반응함)
        try {
            // 멘티님이 만든 Java 컨트롤러 주소로 요청을 보냄
            const response = await axios.post<RecipeResponse>('/api/recipe/text',
                [inputText], // RequestBody
                { params: { preference: '맛있게' } } // RequestParam
            );

            // 서버에서 받은 JSON 데이터를 상태(State)에 저장
            setRecipe(response.data);
        } catch (error) {
            console.error("통신 에러 발생", error);
        } finally {
            setIsLoading(false); // 로딩 종료
        }
    };

    // 4. UI 렌더링: 데이터(State)를 어떻게 보여줄지 정의 (JSX)
    return (
        <div style={{ padding: '20px' }}>
            <h1>AI 레시피 생성기</h1>

            {/* 입력창: 값이 바뀔 때마다 setInputText를 통해 '상태'를 업데이트함 */}
            <input
                type="text"
                value={inputText}
                onChange={(e) => setInputText(e.target.value)}
                placeholder="재료를 입력하세요"
            />

            <button onClick={handleGenerateRecipe} disabled={isLoading}>
                {isLoading ? '생성 중...' : '레시피 만들기'}
            </button>

            {/* 조건부 렌더링: recipe 데이터가 있을 때만 아래 내용을 화면에 그림 */}
            {recipe && (
                <div style={{ marginTop: '20px', border: '1px solid #ccc' }}>
                    <h2>{recipe.title}</h2>
                    <ul>
                        {recipe.ingredients.map((item, index) => <li key={index}>{item}</li>)}
                    </ul>
                    <p><strong>팁:</strong> {recipe.tip}</p>
                </div>
            )}
        </div>
    );
};

export default RecipeGenerator;