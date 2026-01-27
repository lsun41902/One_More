from fastapi import APIRouter
import os
import pandas as pd
import re

router = APIRouter(prefix="/recipes-generate-",tags=["레시피"])

@router.post("test2",
    tags=["Test"],
    summary="조미료 분류",
    description="조미료 걸러내기")
async def analyze():
    df = None
    encodings = ['utf-8', 'cp949', 'utf-8-sig', 'euc-kr']
    path1 = os.path.join("./", "temp_data", 'recipe_materials2.csv')
    path2 = os.path.join("./", "temp_data", 'recipe_materials3.csv')
    paths = [path1, path2]
    only_seasonings = set()

    for file_name in paths:
        for enc in encodings:
            try:
                column_names = ['조미료', '속성1', '속성2', '속성3', '비고']
                df = pd.read_csv(
                    file_name,
                    encoding=enc,
                    names=column_names,
                    header=None,
                    index_col=False,
                    on_bad_lines='skip'  # 만약 5칸을 넘어가는 줄이 있어도 에러 없이 건너뜀
                )
                # 1차 시도: 일반적인 한글 인코딩(cp949)으로 읽기
                print(f"✅ 성공: {enc} 인코딩으로 파일을 읽었습니다.")
                seasoning_list = df['조미료'].tolist()
                only_seasonings = seasoning(seasoning_list)
                break
            except UnicodeDecodeError as e:
                print(f"❌ 인코딩 에러 발생: {e}")
                print(f"💡 해결책: {enc}이 이코딩이 아닙니다.")

            except pd.errors.ParserError as e:
                print(f"❌ 데이터 구조 에러 발생: {e}")
                print("💡 해결책: 구분자(sep)가 맞지 않거나, 특정 줄의 칸 수가 다릅니다. on_bad_lines='skip'을 추가해 보세요.")

            except FileNotFoundError:
                print(f"❌ 파일을 찾을 수 없습니다:")
                print("💡 해결책: 파일 경로와 확장자(.csv)가 정확한지 확인하세요.")

            except Exception as e:
                print(f"❌ 예상치 못한 기타 에러 발생: {type(e).__name__}")
                print(f"상세 내용: {e}")

    result_list = sorted(list(only_seasonings))
    df_result = pd.DataFrame(result_list, columns=['조미료'])

    # 폴더가 없으면 생성
    save_dir = "./temp_data"
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)

    save_path = os.path.join(save_dir, 'recipe_seasoning.csv')

    # 엑셀에서 깨지지 않도록 utf-8-sig 사용
    df_result.to_csv(save_path, index=False, encoding='utf-8-sig')

    print(f"추출된 재료: {result_list}")
    print(f"총 {len(result_list)}개의 재료가 '{save_path}'에 저장되었습니다.")

@router.post("test",
    tags=["Test"],
    summary="재료 분류",
    description="재료 걸러내기")
async def analyze():
    df = None
    encodings = ['utf-8', 'cp949', 'utf-8-sig', 'euc-kr']
    path1 = os.path.join("./", "temp_data", 'recipe_materials2.csv')
    path2 = os.path.join("./", "temp_data", 'recipe_materials3.csv')
    paths = [path1,path2]
    only_seasonings = set()
    material_set = set()
    for file_name in paths:
        for enc in encodings:
            try:
                column_names = ['재료명', '속성1', '속성2', '속성3', '비고']
                df = pd.read_csv(
                    file_name,
                    encoding=enc,
                    names=column_names,
                    header=None,
                    index_col=False,
                    on_bad_lines='skip'  # 만약 5칸을 넘어가는 줄이 있어도 에러 없이 건너뜀
                )
                # 1차 시도: 일반적인 한글 인코딩(cp949)으로 읽기
                print(f"✅ 성공: {enc} 인코딩으로 파일을 읽었습니다.")
                if df is not None:
                    material_list = df['재료명'].tolist()
                    only_seasonings = seasoning(material_list)
                    for text in material_list:
                        # nan(결측치) 방지 및 문자열 확인
                        if isinstance(text, str) and text not in only_seasonings and text.strip():
                            material_set.add(text)
                break
            except UnicodeDecodeError as e:
                print(f"❌ 인코딩 에러 발생: {e}")
                print(f"💡 해결책: {enc}이 이코딩이 아닙니다.")

            except pd.errors.ParserError as e:
                print(f"❌ 데이터 구조 에러 발생: {e}")
                print("💡 해결책: 구분자(sep)가 맞지 않거나, 특정 줄의 칸 수가 다릅니다. on_bad_lines='skip'을 추가해 보세요.")

            except FileNotFoundError:
                print(f"❌ 파일을 찾을 수 없습니다:")
                print("💡 해결책: 파일 경로와 확장자(.csv)가 정확한지 확인하세요.")

            except Exception as e:
                print(f"❌ 예상치 못한 기타 에러 발생: {type(e).__name__}")
                print(f"상세 내용: {e}")




    # 4. CSV 저장
    # 정렬된 리스트로 변환
    result_list = sorted(list(material_set))
    df_result = pd.DataFrame(result_list, columns=['재료명'])

    # 폴더가 없으면 생성
    save_dir = "./temp_data"
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)

    save_path = os.path.join(save_dir, 'recipe_materials.csv')

    # 엑셀에서 깨지지 않도록 utf-8-sig 사용
    df_result.to_csv(save_path, index=False, encoding='utf-8-sig')

    print(f"추출된 재료: {result_list}")
    print(f"총 {len(result_list)}개의 재료가 '{save_path}'에 저장되었습니다.")
    return {"result": "test"}


def extract_pure_ingredient(text):
    if not isinstance(text, str): return ""

    # 1. 숫자([0-9]) 또는 특수문자([^가-힣a-zA-Z\s])를 만나는 지점에서 쪼개기
    # [^가-힣]은 한글이 아닌 모든 문자를 의미합니다.
    parts = re.split(r'[0-9!@#$%^&*()_+\-=\[\]{};\':"\\|,.<>\/?]', text)

    # 2. 쪼개진 것 중 첫 번째 덩어리만 선택 후 양 끝 공백 제거
    result = parts[0].strip()

    return result

def seasoning(refined_list):
    # 조미료를 판단하는 핵심 키워드 리스트
    seasoning_keywords = [
        '소금', '설탕', '후추', '간장', '고추장', '된장', '식초', '액젓', '미원', '다시다',
    '고춧가루', '시럽', '올리고당', '물엿', '꿀', '참기름', '들기름', '식용유', '버터',
    '마요네즈', '케첩', '머스타드', '소스', '드레싱', '시즈닝', '와사비', '겨자', '소다',
    '베이킹파우더', '맛술', '청주', '굴소스', '춘장', '카레가루', '전분', '밀가루'
    ]

    # 10만 개 데이터(refined_list)에서 조미료만 골라내기
    only_seasonings = {
        item for item in refined_list
        if any(key in item for key in seasoning_keywords)
    }

    # 2. '핵심 키워드 리스트'에 있는 조미료가 추출된 결과에 없다면 강제로 추가
    for key in seasoning_keywords:
        if key not in only_seasonings:
            only_seasonings.add(key)  # 목록에 없으면 추가

    print(f"💡 조미료 데이터 건수: {len(only_seasonings)}건")
    return only_seasonings

