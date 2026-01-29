import pandas as pd
import random
import os
from sentence_transformers import SentenceTransformer, util, InputExample,losses
import torch
from torch.utils.data import DataLoader

import re



def clean_ingredients():
    current_dir = os.path.dirname(os.path.abspath(__file__))
    # path = os.path.join(current_dir, "..", "temp_data", "TB_RECIPE_SEARCH-22070.csv")
    path = os.path.join(current_dir, "..", "temp_data", "real_recipe.csv")
    try:
        df = pd.read_csv(path, encoding='cp949')
    except UnicodeDecodeError:
        df = pd.read_csv(path, encoding='euc-kr')

    # 새로운 컬럼 생성 (apply 사용)
    df['ingredient'] = df['CKG_MTRL_CN'].apply(clean_recipe_text)
    # 파일 저장 경로 설정 (temp_data 폴더에 저장)
    output_path = os.path.join(current_dir, "..", "temp_data", "refined_recipe_data.csv")

    # CSV로 저장하기
    df.to_csv(output_path,
              index=False,  # 0, 1, 2... 같은 행 번호는 저장하지 않음
              encoding='utf-8-sig')  # 엑셀에서 열었을 때 한글이 깨지지 않게 함

    print(f"✅ 전처리가 완료된 파일이 저장되었습니다: {output_path}")



# 전처리 함수 정의
def clean_recipe_text(text):
    if pd.isna(text): return "" # 결측치 예외 처리
    # 1. [재료], [양념] 기준으로 쪼개기
    result = re.split(r'\[.*?\]', text)
    words=[]

    exclude_units = ['T', 't', 'g', 'ml', '개', '장', '마리', '움큼',
                     '봉지', '컵', '약간', '인분','큰술','적당량','많이',
                     '적게','조금','/','쪽','방울','&',
                     'kg','cm','그램','근','작은술','또는','중간것','정도','+','-',
                     '자세한','상세레시피를','얇은것','않은','손질되지','~','큰것','?',
                     '먹다','만큼','or','!','상세','레시피','설명','재료','내용','확인','보실','습니다',
                     '달달','하시구용','먹는','해서']

    clean_parts = []
    for text in result:
        parts = text.split('|')
        for text2 in parts:
            words = text2.split(' ')
            for w in words:
                w = w.strip()
                if not w: continue

                # 단어에 숫자가 포함되어 있거나, 단위 리스트 중 하나라도 포함되어 있으면 pass
                contains_unit = any(unit in w for unit in exclude_units)
                contains_digit = any(char.isdigit() for char in w)

                if not (contains_unit or contains_digit):
                    w = w.replace('(','').replace(')','')
                    clean_parts.append(w)
    return " ".join(clean_parts)


def dummy_test(user_input):
    # 지금까지 학습된 중간 모델 로드
    model = SentenceTransformer('./my_food_model')

    # 내 레시피 데이터의 일부 (100개 정도만 테스트)
    current_dir = os.path.dirname(os.path.abspath(__file__))
    path = os.path.join(current_dir, "..", "temp_data", "refined_recipe_data.csv")
    try:
        df = pd.read_csv(path, encoding='utf-8-sig')
    except UnicodeDecodeError:
        df = pd.read_csv(path, encoding='euc-kr')
    db_embeddings = model.encode(df['ingredient'].astype(str).tolist())

    # 사용자 입력과 유사도 체크
    user_vec = model.encode(user_input)

    # 1. 유사도 계산
    scores = util.cos_sim(user_vec, db_embeddings)[0]

    # 2. 가장 높은 점수 순으로 상위 3개 인덱스 추출
    top_results = torch.topk(scores, k=3)

    print(f"\n입력하신 재료: {user_input}")
    print("-" * 50)

    for score, idx in zip(top_results.values, top_results.indices):
        recipe_idx = idx.item()
        print(f"✅ 유사도 점수: {score:.4f}")
        # 보통 CKG_NM(요리명) 컬럼이 따로 있다면 그걸 출력하는 게 좋습니다.
        # 없다면 원문인 CKG_MTRL_CN을 보여줍니다.
        print(f"추천 요리 내용: {df.iloc[recipe_idx]['CKG_MTRL_CN']}...")
        print(f"매칭된 키워드: {df.iloc[recipe_idx]['ingredient']}")
        print("-" * 50)
