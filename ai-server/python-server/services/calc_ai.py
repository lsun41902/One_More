import os
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from core.config import settings
from collections import deque
import shutil
import json

# 제미나이
from langchain_google_genai import ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings
import google.generativeai as genai
from PIL import Image
from google.api_core import exceptions
from langchain_core.documents import Document
from langchain_classic.chains import RetrievalQA
from sentence_transformers import SentenceTransformer, util, InputExample, losses
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_community.vectorstores.utils import DistanceStrategy  # 1. 임포트 추가
import torch

import pandas as pd
import traceback
import csv
import time
import re

genai_api_key = settings.GOOGLE_API_KEY
genai.configure(api_key=genai_api_key)

# [수정] 전역 변수에서 무거운 객체 생성을 제거하고 None으로 초기화합니다.
openai_llm = None
genai_embeddings = None
genai_vectorstore = None

TOP_FOLDER = ".."

recipe_titles = deque(maxlen=9)

model_candidates = [
    "gemini-2.5-flash-lite",
    "gemini-2.0-flash",
    "gemini-3-flash-preview",
    "gemini-1.5-flash"  # 가장 보수적이고 안전한 선택지
]

multimodel_candidates = [
    "gemini-3-flash-preview",  # 2026년 최신, 가장 뛰어난 추론 성능
    "gemini-2.5-flash",  # 가장 안정적인 고성능 모델
    "gemini-2.5-flash-lite",  # 속도와 비용 효율 최강
    "gemini-2.0-flash",  # 구형이지만 여전히 강력한 범용 모델
    "gemini-1.5-flash"  # 최후의 보루 (하루 1,500회 무료 한도)
]


def get_openai_llm():
    global openai_llm
    if openai_llm is None:
        from langchain_openai import ChatOpenAI
        openai_llm = ChatOpenAI(model="gpt-4o-mini")
    return openai_llm


def get_vectorstore():
    current_dir = os.path.dirname(os.path.abspath(__file__))
    model_path = os.path.join(current_dir, TOP_FOLDER, "my_food_model")
    index_path = os.path.join(current_dir, TOP_FOLDER, "faiss_my_food_model_index")

    embeddings = HuggingFaceEmbeddings(
        model_name=model_path,
        model_kwargs={'device': 'cuda'}
    )

    # load_local 할 때 distance_strategy를 명시적으로 지정합니다. ✅
    vectorstore = FAISS.load_local(
        index_path,
        embeddings,
        allow_dangerous_deserialization=True,
        distance_strategy=DistanceStrategy.COSINE  # 여기에 추가!
    )

    print("✅ 코사인 유사도 전략으로 인덱스를 성공적으로 로드했습니다!")
    return vectorstore


def get_real_recipe_ingredient(user_input):
    # 지금까지 학습된 중간 모델 로드
    model = SentenceTransformer('./my_food_model')

    # 내 레시피 데이터의 일부 (100개 정도만 테스트)
    current_dir = os.path.dirname(os.path.abspath(__file__))
    path = os.path.join(current_dir, TOP_FOLDER, "temp_data", "refined_recipe_data.csv")
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
    real_ingredient = []
    for score, idx in zip(top_results.values, top_results.indices):
        recipe_idx = idx.item()
        print(f"✅ 유사도 점수: {score:.4f}")
        # 보통 CKG_NM(요리명) 컬럼이 따로 있다면 그걸 출력하는 게 좋습니다.
        # 없다면 원문인 CKG_MTRL_CN을 보여줍니다.
        print(f"추천 요리 내용: {df.iloc[recipe_idx]['CKG_MTRL_CN']}...")
        print(f"매칭된 키워드: {df.iloc[recipe_idx]['ingredient']}")
        print("-" * 50)
        real_ingredient.append(df.iloc[recipe_idx]['CKG_MTRL_CN'])
    return real_ingredient


class AnalyzeOpenAi:
    def __init__(self):
        get_openai_llm()

    def get_recipe(self, msg):
        return openai_llm.invoke(msg)


class AnalyzeGenai:
    # region 생성자
    def __init__(self, images=None, user_id='user_1'):
        current_dir = os.path.dirname(os.path.abspath(__file__))
        UPLOAD_DIR = "uploads"
        self.image_path = os.path.join(current_dir, TOP_FOLDER, UPLOAD_DIR, user_id)
        os.makedirs(self.image_path, exist_ok=True)
        self.saved_file_paths = []
        if images:
            for image in images:
                file_path = os.path.join(self.image_path, image.filename)
                image.file.seek(0)
                # 2. 파일을 로컬 디스크에 저장 (Option B 구현)
                with open(file_path, "wb") as buffer:
                    shutil.copyfileobj(image.file, buffer)
                self.saved_file_paths.append(file_path)
        # [추천] 변수들을 여기서 미리 선언해두면 나중에 찾기 편합니다.
        self.get_json_temp_path()

    # region 기본 json형식 설정
    # 기본 json형식 설정
    def get_json_temp_path(self):
        current_path = os.path.dirname(os.path.abspath(__file__))
        temp_json_path = "temp_json"
        image_json_path = os.path.join(current_path, TOP_FOLDER, temp_json_path, "temp_for_image_json.txt")
        self.image_prompt_json = self.load_temp_json(image_json_path)

        default_recipe_json_path = os.path.join(current_path, TOP_FOLDER, temp_json_path,
                                                "temp_for_default_recipe_json.txt")
        self.default_recipe_prompt_json = self.load_temp_json(default_recipe_json_path)

        another_recipe_json_path = os.path.join(current_path, TOP_FOLDER, temp_json_path,
                                                "temp_for_another_recipe_json.txt")
        self.another_recipe_prompt_json = self.load_temp_json(another_recipe_json_path)

        more_recipe_json_path = os.path.join(current_path, TOP_FOLDER, temp_json_path, "temp_for_more_recipe_json.txt")
        self.more_recipe_prompt_json = self.load_temp_json(more_recipe_json_path)

    # temp json을 읽어오기
    def load_temp_json(self, path):
        with open(path, "r", encoding="utf-8") as f:
            image_prompt_json = f.read()
        return image_prompt_json

    # endregion
    # endregion

    # region Genai 모델
    def get_embedding(self, docs):
        batch_size = 1000
        for i in range(0, len(docs), batch_size):
            batch = docs[i: i + batch_size]
            try:
                # 한 번에 5개씩만 추가
                genai_vectorstore.add_documents(batch)
                print(f"✅ {i + len(batch)} / {len(docs)} 저장 완료")

            except Exception as e:
                print(f"❌ {i}번째 근처에서 에러 발생: {e}")
                print("10초 대기 후 다시 시도합니다...")
                time.sleep(10)
                # 실패한 배치를 다시 시도 (필요시 로직 추가)
        # 3. 검색기(Retriever) 만들기
        print("✨ 모든 데이터가 안전하게 저장되었습니다!")

        return None

    def get_text_model(self, prompt):
        for model_name in model_candidates:
            try:
                # 모델 선언 및 호출 (LangChain 기준)
                print("현재 모델 이름:", model_name)
                llm = ChatGoogleGenerativeAI(
                    model=model_name,
                    google_api_key=genai_api_key,
                    response_mime_type="application/json"
                )
                return llm.invoke(prompt)
            except exceptions.ResourceExhausted:
                # 횟수 초과(Quota) 오류 발생
                print(f"현재 모델 이름:{model_name} 한도 초과! 모델로 전환하여 다시 시도합니다.")
            except Exception as e:
                if "429" in str(e) or "quota" in str(e).lower():
                    print(f"{model_name} 한도 초과, 다음 모델로 시도합니다...")
                    continue
                else:
                    raise e
        print("모든 모델의 할당량이 소진되었습니다.")
        return None

    def get_image_model(self, content):
        for model_name in multimodel_candidates:
            try:
                # 모델 선언 및 호출 (LangChain 기준)
                print("현재 모델 이름:", model_name)
                genai_model = genai.GenerativeModel(model_name)
                return genai_model.generate_content(
                    content,
                    generation_config={"response_mime_type": "application/json"})
            except exceptions.ResourceExhausted:
                # 횟수 초과(Quota) 오류 발생
                print(f"현재 모델 이름:{model_name} 한도 초과! 모델로 전환하여 다시 시도합니다.")
                pass
            except Exception as e:
                if "429" in str(e) or "quota" in str(e).lower():
                    print(f"{model_name} 한도 초과, 다음 모델로 시도합니다...")
                    continue
                else:
                    raise e
        print("모든 모델의 할당량이 소진되었습니다.")
        return None

    # endregion

    # region langchain,rag 테스트
    # 파일 복원하기
    # region csv 한글 깨진거 복구하기
    def remake_csv(self):
        current_path = os.path.dirname(os.path.abspath(__file__))
        input_file = os.path.join(current_path, "temp_data", "TB_RECIPE_SEARCH-220701.csv")
        output_file = os.path.join(current_path, "temp_data", 'TB_RECIPE_SEARCH-220701_복원.csv')
        fixed_rows = []

        # 1. 파일을 바이트 단위로 직접 열어서 처리
        with open(input_file, 'rb') as f:
            for line_content in f:
                try:
                    # CP949로 시도하되, 오류가 나면 무시(ignore)하거나 대체(replace)합니다.
                    decoded_line = line_content.decode('cp949', errors='ignore')

                    # 읽어온 줄을 리스트에 저장 (CSV 형식을 유지하기 위해 strip 사용)
                    fixed_rows.append(decoded_line.strip().split(','))
                except Exception as e:
                    print(f"처리 중 오류 발생 줄 건너뜀: {e}")

        # 2. 복구된 내용을 UTF-8로 다시 저장
        with open(output_file, 'w', encoding='utf-8-sig', newline='') as f:
            writer = csv.writer(f)
            writer.writerows(fixed_rows)

        print(f"✅ 복구가 완료되었습니다! '{output_file}' 파일을 확인해 보세요.")

    # endregion
    # region json정리하기
    def extract_json(self, text):
        # ```json ... ``` 또는 ``` ... ``` 사이의 내용만 추출
        pattern = r"```(?:json)?\s*([\s\S]*?)\s*```"
        match = re.search(pattern, text)
        if match:
            return match.group(1).strip()
        return text.strip()

    # endregion
    # region 문서 읽어오기
    def get_docs(self):
        current_path = os.path.dirname(os.path.abspath(__file__))
        # temp_data_path = os.path.join(current_path,"temp_data","TB_RECIPE_SEARCH-20231130.csv")
        temp_data_path = os.path.join(current_path, "temp_data", "TB_RECIPE_SEARCH-22070.csv")
        try:
            # 에러가 예상되는 지점
            df = pd.read_csv(temp_data_path, encoding='utf-8', engine='python', on_bad_lines='skip')
            print("✅ 파일 로드 성공!")
            docs = []
            for i, row in df.iterrows():
                # AI가 학습할 '문장' 만들기 (컬럼명은 본인의 CSV에 맞게 수정)
                content = f"요리명: {row['CKG_NM']}\n레시피 재료: {row['CKG_MTRL_CN']}"
                # print(content)
                # Document 객체 생성
                doc = Document(
                    page_content=content,
                    metadata={
                        "id": row['RCP_SNO'],
                        "author": row.get('RGTR_ID', 'unknown')  # 작성자 정보 등
                    }
                )
                docs.append(doc)
            return docs
        except UnicodeDecodeError as e:
            print(f"❌ [인코딩 에러] 파일의 글자 형식(Encoding)이 맞지 않습니다.")
            print(f"에러 내용: {e}")
            print("해결책: pd.read_csv(path, encoding='cp949') 또는 'euc-kr'을 시도하세요.")

        except FileNotFoundError:
            print(f"❌ [파일 경로 에러] 파일을 찾을 수 없습니다.")
            print(f"확인된 경로: {temp_data_path}")

        except Exception as e:
            print(f"❌ [기타 에러 발생] 아래 로그를 확인하세요:")
            # 상세한 에러 추적 로그(Traceback)를 전체 출력합니다.
            traceback.print_exc()
        return None

    # endregion
    # region 임베딩 작업
    def get_rag_embedding(self):
        try:
            docs = self.get_docs()
            self.get_embedding(docs)
        except Exception as e:
            # 1. 에러의 종류와 메시지 출력
            print(f"에러 타입: {type(e).__name__}")
            print(f"에러 메시지: {e}")
            traceback.print_exc()

    # endregion
    def get_chef_recipe(self, preference, ingredients, spice, temp=""):
        # 벡터 DB 에서 검색 하기
        vectorstore = get_vectorstore()
        print(f"📊 현재 사용 중인 거리 전략: {vectorstore.distance_strategy}")
        retriever = vectorstore.as_retriever(
            search_type="mmr",
            search_kwargs={
                "k": 3,
                "fetch_k": 10,  # 먼저 후보 10개를 뽑은 뒤
                "lambda_mult": 0.5  # 0에 가까울수록 "다양성" 강조, 1에 가까울수록 "유사도" 강조
            }
        )
        try:
            search_query = ", ".join([item.ingredient for item in ingredients])
        except TypeError:
            # 혹시 몰라 대비하는 딕셔너리 방식 (에러나면 이쪽으로 실행)
            search_query = ", ".join([item['ingredient'] for item in ingredients])
        docs_and_scores = vectorstore.similarity_search_with_score(search_query, k=3)
        retrieved_docs = [doc for doc, score in docs_and_scores]

        print("\n=== 🎯 검색 결과 및 유사도 점수 ===")
        for i, (doc, score) in enumerate(docs_and_scores):
            print(f"[{i + 1}] 점수: {score:.4f}")  # 점수가 낮을수록(0에 가까울수록) 유사함
            print(f"내용: {doc.page_content}...")  # 앞부분만 살짝 출력
            print("-" * 30)
        for model_name in model_candidates:
            try:
                print("현재 모델 이름:", model_name)
                # 4. 드디어 'rag_chain' 만들기! (RetrievalQA 사용)
                llm_instance = ChatGoogleGenerativeAI(
                    model=model_name,
                    google_api_key=genai_api_key,  # 본인의 API 키 변수명 확인
                    temperature=0,
                    response_mime_type="application/json"
                )
                rag_chain = RetrievalQA.from_chain_type(
                    llm=llm_instance,
                    chain_type="stuff",  # 찾은 문서를 뭉쳐서(stuff) 전달
                    retriever=retriever,
                    return_source_documents=True  # 어떤 레시피를 참고했는지 알려달라고 설정
                )
                prompt = f"""
                {temp}
                {retrieved_docs}
                너는 한국의 요리사야. 내가 앞에 넣은 데이터는 다른 요리사가 사용한 재료와 요리결과물이야. (요리 이름은 simple하게 바꿔줘)
                [필요 재료]랑 [유저가 보유한 식재료, 조미료, 맛 취향] 을 비교해서
                유사한 [요리사가 사용한 재료]를 이용해 만들수 있는 요리 레시피를 3개 추천해줘.
                만약 정보에 없는 내용이라면 지어내지 말고 "모르겠습니다"라고 답해줘. 답변은 한글로 상세하게.
                Return the response in a VALID JSON format. 
                Do not include any conversational text or markdown code blocks (like ```json).
                
                [유저의 맛 취향]:
                {preference}
                
                [유저가 보유한 식재료]:
                {ingredients}
                
                [유저가 보유한 조미료]:
                {spice}
                
                Output Format:{self.another_recipe_prompt_json}
                JSON Output:"""
                result = rag_chain.invoke({"query": prompt})

                # 1. AI의 답변 출력
                print("=== AI 답변 ===")
                print(result.get("result"))
                # 2. 실제로 참조한 데이터(CSV 행) 출력
                print("\n=== 참조한 소스 문서 ===")
                for i, doc in enumerate(result.get('source_documents')):
                    print(f"[{i + 1}] {doc.page_content}")

                clean_json_str = self.extract_json(result.get("result"))
                result_json = json.loads(clean_json_str)
                self.save_recipe(result_json)
                return result_json
            except exceptions.ResourceExhausted:
                # 횟수 초과(Quota) 오류 발생
                print(f"현재 모델 이름:{model_name} 한도 초과! 모델로 전환하여 다시 시도합니다.")
                pass
            except json.decoder.JSONDecodeError:
                self.get_chef_recipe(preference, ingredients, spice, 'json형식이 안맞았어. 다시 확인해서 보내줘')

        return None

    # endregion

    # region 추천한것 외의 추천 레시피 3개
    def get_another_more_somthing(self, preference, ingredients, spice):
        prompt = f"""
                음.. 혹시 지금 내가 가지고 있는 재료들과 1~5개 정도의 재료 또는 조미료가 있다면 더 맛있는 음식이 가능할까?
                방금 추천한 {recipe_titles} 이 레시피들 말고 다른 레시피를 원해. 중복되지 않게 조심해. 
                나는 {preference} 이런 취향을 가지고 있고, {ingredients} 이런 재료들고 {spice} 이런 조미료를 가지고있어,
                다른 재료는 없으니 참고해. 사람들이 이미 만든적이 있는 레시피를 기반으로 3개 추천해줬으면 좋겠어. 꼭 3가지를 찾지 못해도 상관없어. 적어도 3가지를 추천해줘. 
                title 은 제목만 적어줘 예를 들어 (매콤달콤 돼지고기 김치볶음밥) 이런 품사를 제목에 섞지마,
                ingredients 는 [재료, 정량 얼마] 이런식으로 적어주고, 근거가 없는 요리는 추천하지 않도록 조심해줘. 예를 들면 김치찌게에 방울토마토를 넣기. 이런 음식은 아무도 선호하지 않아. 
                recipe는 손질 방법과, 정량 기준, 조리에 대한 상세한 시간을 꼭 언급해줘. 
                tip은 1~5개정도 알려주고 title,ingredients,summary,recipe,tip 는 한글로 알려줘,
                결과는 반드시 JSON 배열로만 응답해줘.
                응답은 반드시 아래 JSON 형식을 지켜줘:{self.more_recipe_prompt_json}

              
                """

        response = self.get_text_model(prompt)
        print(response.content)
        result_json = json.loads(response.content)
        self.save_recipe(result_json)
        return result_json

    # endregion

    # region 레시피 추천
    # 기본 레시피1개, 추천 레시피 2개
    def get_recipe(self, preference, ingredients, spice):
        prompt = f"""
                1번. 나는 지금 배가고파 요리 레시피를 대신좀 찾아줘. 
                나는 {preference} 이런 취향을 가지고 있고, {ingredients} 이런 재료들고 {spice} 이런 조미료를 가지고있어,
                가지고 있는 재료들로만 만들수 있는 요리 레피를 한가지 추천해줘. 
                
                2번. 음.. 혹시 지금 내가 가지고 있는 재료들과 1~5개 정도의 재료 또는 조미료가 있다면 더 맛있는 음식이 가능할까?
                나는 {preference} 이런 취향을 가지고 있고, {ingredients} 이런 재료들고 {spice} 이런 조미료를 가지고있어,
                다른 재료는 없으니 참고해. 사람들이 이미 만든적이 있는 레시피를 기반으로 2개 추천해줬으면 좋겠어.
                 
                그리고, title 은 제목만 적어줘 예를 들어 (매콤달콤 돼지고기 김치볶음밥) 이런 품사를 제목에 섞지마,
                ingredients 는 [재료, 정량 얼마] 이런식으로 적어주고, 근거가 없는 요리는 추천하지 않도록 조심해줘. 예를 들면 김치찌게에 방울토마토를 넣기. 이런 음식은 아무도 선호하지 않아. 
                recipe는 손질 방법과, 정량 기준, 조리에 대한 상세한 시간을 꼭 언급해줘. 
                tip은 1~5개정도 알려주고 title,ingredients,summary,recipe,tip 는 한글로 알려줘,
                결과는 반드시 JSON 배열로만 응답해줘.
                응답은 반드시 아래 JSON 형식을 지켜줘:{self.default_recipe_prompt_json}
                """

        response = self.get_text_model(prompt)
        print(response.content)
        result_json = json.loads(response.content)
        self.save_recipe(result_json)
        return result_json

    # 추천한것 외의 기본 레시피 3개
    def get_another_recipe(self, preference, ingredients, spice):
        prompt = f"""
                방금 추천한 {recipe_titles} 이 레시피들 말고 다른 레시피를 원해. 중복되지 않게 조심해. 
                나는 {preference} 이런 취향을 가지고 있고, {ingredients} 이런 재료들고 {spice} 이런 조미료를 가지고있어,
                다른 재료는 없으니 참고해. 사람들이 이미 만든적이 있는 레시피를 기반으로 3개 추천해줬으면 좋겠어. 꼭 3가지를 찾지 못해도 상관없어. 적어도 3가지를 추천해줘. 
                title 은 제목만 적어줘 예를 들어 (매콤달콤 돼지고기 김치볶음밥) 이런 품사를 제목에 섞지마,
                ingredients 는 [재료, 정량 얼마] 이런식으로 적어주고, 근거가 없는 요리는 추천하지 않도록 조심해줘. 예를 들면 김치찌게에 방울토마토를 넣기. 이런 음식은 아무도 선호하지 않아. 
                recipe는 손질 방법과, 정량 기준, 조리에 대한 상세한 시간을 꼭 언급해줘. 
                tip은 1~5개정도 알려주고 title,ingredients,summary,recipe,tip 는 한글로 알려줘,
                결과는 반드시 JSON 배열로만 응답해줘.
                응답은 반드시 아래 JSON 형식을 지켜줘:{self.another_recipe_prompt_json}
            
              
                """

        response = self.get_text_model(prompt)
        print(response.content)
        result_json = json.loads(response.content)
        self.save_recipe(result_json)
        return result_json

    def save_recipe(self, data):
        for recipe in data:
            title = recipe['title']
            if title not in recipe_titles:
                recipe_titles.append(title)  # set에 제목 등록
                print("이미 추천한 레시피:", title)

    # endregion

    # region 영수증 분석
    def calc_image_receipt(self):
        imgs = [Image.open(path) for path in self.saved_file_paths]
        num_images = len(imgs)
        prompt = f"""
            총 {num_images}장의 사진이 있어. 구매내역을 보고 요리에 사용되는 식재료만 따로 구분해서 알려줘.
            해당 물품이 요리에 사용되지 않는다면 빼도되.
            해당 물품의 label에 알려주고 수량을 quantity에 알려줘.
            ingredients, label의 재료는 한글로 알려줘, 결과는 반드시 image_index를 포함한 JSON 배열로만 응답해줘.
            응답은 반드시 아래 JSON 형식을 지켜줘:{self.image_prompt_json}
            """
        content = [prompt] + imgs
        response = self.get_image_model(content)
        return json.loads(response.text)

    # endregion

    # region 이미지 분석
    def calc_image(self):
        imgs = [Image.open(path) for path in self.saved_file_paths]
        num_images = len(imgs)
        prompt = f"""
            총 {num_images}장의 사진이 있어.
            ingredients, label 재료는 한글로 알려줘, 결과는 반드시 image_index를 포함한 JSON 배열로만 응답해줘.
            그리고 되도록이면 이미지에서 분석한 해당 재료를 세는 단위로 인식한 수량(계량정도)를 같이 알려줘
            응답은 반드시 아래 JSON 형식을 지켜줘:{self.image_prompt_json}
            """
        content = [prompt] + imgs
        response = self.get_image_model(content)
        return json.loads(response.text)

    # endregion
