import os
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from dotenv import load_dotenv
from collections import deque
import cv2
from ultralytics import YOLO
import shutil
import json

# 제미나이
from langchain_google_genai import ChatGoogleGenerativeAI
import google.generativeai as genai
from PIL import Image, ImageDraw, ImageFont
from google.api_core import exceptions

#image 가져오기
import get_image

load_dotenv()

openai_model = YOLO('yolov10n.pt')
openai_llm = ChatOpenAI(model="gpt-4o-mini")

genai_api_key = os.getenv("GOOGLE_API_KEY")
genai.configure(api_key=genai_api_key)

UPLOAD_DIR = "./uploads"

recipe_titles = deque(maxlen=10)

model_candidates = [
    "gemini-2.5-flash-lite",
    "gemini-2.0-flash",
    "gemini-3-flash-preview",
    "gemini-1.5-flash"  # 가장 보수적이고 안전한 선택지
]

multimodel_candidates = [
    "gemini-3-flash-preview",    # 2026년 최신, 가장 뛰어난 추론 성능
    "gemini-2.5-flash",          # 가장 안정적인 고성능 모델
    "gemini-2.5-flash-lite",     # 속도와 비용 효율 최강
    "gemini-2.0-flash",          # 구형이지만 여전히 강력한 범용 모델
    "gemini-1.5-flash"           # 최후의 보루 (하루 1,500회 무료 한도)
]


class Text_Question():
    def __init__(self):
        pass

    def get_recipe(self, msg):
        return openai_llm.invoke(msg)


class Analyze():
    # region 생성자
    def __init__(self, images=None, user_id='user1'):
        self.image_path = os.path.join(UPLOAD_DIR, user_id)
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
    # endregion

    # region Genai 모델
    def get_text_model(self,prompt):
        for model_name in model_candidates:
            try:
                # 모델 선언 및 호출 (LangChain 기준)
                print("현재 모델 이름:",model_name)
                llm = ChatGoogleGenerativeAI(
                    model=model_name,
                    google_api_key=genai_api_key,
                    model_kwargs={"response_mime_type": "application/json"}
                )
                return llm.invoke(prompt)
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

    def get_image_model(self,content):
        for model_name in multimodel_candidates:
            try:
                # 모델 선언 및 호출 (LangChain 기준)
                print("현재 모델 이름:",model_name)
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

    #region 추천한것 외의 추천 레시피 3개
    def get_another_more_somthing(self, preference, ingredients, spice):
        prompt = f"""
                음.. 혹시 지금 내가 가지고 있는 재료들과 1~5개 정도의 재료 또는 조미료가 있다면 더 맛있는 음식이 가능할까?
                방금 추천한 {recipe_titles} 이 레시피들 말고 다른 레시피를 원해. 중복되지 않게 조심해. 
                나는 {preference} 이런 취향을 가지고 있고, {ingredients} 이런 재료들고 {spice} 이런 조미료를 가지고있어,
                다른 재료는 없으니 참고해. 사람들이 이미 만든적이 있는 레시피를 기반으로 3개 추천해줬으면 좋겠어. 꼭 3가지를 찾지 못해도 상관없어. 적어도 3가지를 추천해줘. 
                title 은 제목만 적어줘 예를 들어 (매콤달콤 돼지고기 김치볶음밥) 이런 품사를 제목에 섞지마,
                ingredients 는 [재료, 정량 얼마] 이런식으로 적어주고, 근거가 없는 요리는 추천하지 않도록 조심해줘. 예를 들면 김치찌게에 방울토마토를 넣기. 이런 음식은 아무도 선호하지 않아. 
                recipe는 손질 방법과, 정량 기준, 조리에 대한 상세한 시간을 꼭 언급해줘. 
                image는 실존하지 않거나 접근이 불가능한 이미지 URL 대신, Google 이미지 검색이 가능한 레시피 사이트의 정확한 URL을 알려줘.
                reference는 Google 검색이 가능한 레시피 사이트의 정확한 URL을 알려줘.
                tip은 1~5개정도 알려주고 title,ingredients,summary,recipe,tip 는 한글로 알려줘,
                결과는 반드시 JSON 배열로만 응답해줘.
                응답은 반드시 아래 JSON 형식을 지켜줘:

              {{
                "title": "2번 요리 이름 (예: 김치볶음밥)",
                "ingredients": [["재료1","정량 얼마"], ["재료2","정량 얼마"], ["재료1","정량 얼마"]],
                "more" [["필요한재료1","정량 얼마"], ["필요한재료2","정량 얼마"], ["필요한재료3","정량 얼마"]],
                "summary": "해당 요리에 대한 50자 이내의 간략한 설명",
                "image": image,
                "reference": 인터넷 주소URL,
                "recipe": ["step1: 재료 손질 방법과 재료의 정량 기준을 알려주기(밥 숫가락으로 한숟갈, 티 스푼으로 한 숟갈, 종이컵 반컵)"],["step2: 냄비에 넣고 강불로 5분간 끓여주세요"],
                "tip":["tip1: 닭을 손질할때 우유를 넣어서 잡내를 없애면 좋아요!","tip2: 일반 소금 대신 맛소금을 사용하면 더 맛있어요!"]
              }},
              {{
                "title": "2번 요리 이름 (예: 김치볶음밥)",
                "ingredients": [["재료1","정량 얼마"], ["재료2","정량 얼마"], ["재료1","정량 얼마"]],
                "more" [["필요한재료1","정량 얼마"], ["필요한재료2","정량 얼마"], ["필요한재료3","정량 얼마"]],
                "summary": "해당 요리에 대한 50자 이내의 간략한 설명",
                "image": image,
                "reference": 인터넷 주소URL,
                "recipe": ["step1: 재료 손질 방법과 재료의 정량 기준을 알려주기(밥 숫가락으로 한숟갈, 티 스푼으로 한 숟갈, 종이컵 반컵)"],["step2: 냄비에 넣고 강불로 5분간 끓여주세요"],
                "tip":["tip1: 닭을 손질할때 우유를 넣어서 잡내를 없애면 좋아요!","tip2: 일반 소금 대신 맛소금을 사용하면 더 맛있어요!"]
              }}
                """

        response = self.get_text_model(prompt)
        print(response.content)
        result_json = json.loads(response.content)
        self.save_recipe(result_json)
        return result_json
    #endregion

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
                image는 실존하지 않거나 접근이 불가능한 이미지 URL 대신, Google 이미지 검색이 가능한 레시피 사이트의 정확한 URL을 알려줘.
                reference는 Google 검색이 가능한 레시피 사이트의 정확한 URL을 알려줘.
                tip은 1~5개정도 알려주고 title,ingredients,summary,recipe,tip 는 한글로 알려줘,
                결과는 반드시 JSON 배열로만 응답해줘.
                응답은 반드시 아래 JSON 형식을 지켜줘:

              {{
                "title": "1번 요리 이름 (예: 김치볶음밥)",
                "ingredients": [["재료1","정량 얼마"], ["재료2","정량 얼마"], ["재료1","정량 얼마"]],
                "summary": "해당 요리에 대한 50자 이내의 간략한 설명",
                "image": image,
                "reference": 인터넷 주소URL,
                "recipe": ["step1: 재료 손질 방법과 재료의 정량 기준을 알려주기(밥 숫가락으로 한숟갈, 티 스푼으로 한 숟갈, 종이컵 반컵)"],["step2: 냄비에 넣고 강불로 5분간 끓여주세요"],
                "tip":["tip1: 닭을 손질할때 우유를 넣어서 잡내를 없애면 좋아요!","tip2: 일반 소금 대신 맛소금을 사용하면 더 맛있어요!"]
              }},
            
              {{
                "title": "2번 요리 이름 (예: 김치볶음밥)",
                "ingredients": [["재료1","정량 얼마"], ["재료2","정량 얼마"], ["재료1","정량 얼마"]],
                "more" [["필요한재료1","정량 얼마"], ["필요한재료2","정량 얼마"], ["필요한재료3","정량 얼마"]],
                "summary": "해당 요리에 대한 50자 이내의 간략한 설명",
                "image": image,
                "reference": 인터넷 주소URL,
                "recipe": ["step1: 재료 손질 방법과 재료의 정량 기준을 알려주기(밥 숫가락으로 한숟갈, 티 스푼으로 한 숟갈, 종이컵 반컵)"],["step2: 냄비에 넣고 강불로 5분간 끓여주세요"],
                "tip":["tip1: 닭을 손질할때 우유를 넣어서 잡내를 없애면 좋아요!","tip2: 일반 소금 대신 맛소금을 사용하면 더 맛있어요!"]
              }},
              
              {{
                "title": "3번 요리 이름 (예: 김치볶음밥)",
                "ingredients": [["재료1","정량 얼마"], ["재료2","정량 얼마"], ["재료1","정량 얼마"]],
                "more" [["필요한재료1","정량 얼마"], ["필요한재료2","정량 얼마"], ["필요한재료3","정량 얼마"]],
                "summary": "해당 요리에 대한 50자 이내의 간략한 설명",
                "image": image,
                "reference": 인터넷 주소URL,
                "recipe": ["step1: 재료 손질 방법과 재료의 정량 기준을 알려주기(밥 숫가락으로 한숟갈, 티 스푼으로 한 숟갈, 종이컵 반컵)"],["step2: 냄비에 넣고 강불로 5분간 끓여주세요"],
                "tip":["tip1: 닭을 손질할때 우유를 넣어서 잡내를 없애면 좋아요!","tip2: 일반 소금 대신 맛소금을 사용하면 더 맛있어요!"]
              }},
            
                """

        response = self.get_text_model(prompt)
        print(response.content)
        result_json = json.loads(response.content)
        self.save_recipe(result_json)
        return result_json

    # 추천한것 외의 기본 레시피 3개
    def get_another_recipe(self, preference, ingredients,spice):
        prompt = f"""
                방금 추천한 {recipe_titles} 이 레시피들 말고 다른 레시피를 원해. 중복되지 않게 조심해. 
                나는 {preference} 이런 취향을 가지고 있고, {ingredients} 이런 재료들고 {spice} 이런 조미료를 가지고있어,
                다른 재료는 없으니 참고해. 사람들이 이미 만든적이 있는 레시피를 기반으로 3개 추천해줬으면 좋겠어. 꼭 3가지를 찾지 못해도 상관없어. 적어도 3가지를 추천해줘. 
                title 은 제목만 적어줘 예를 들어 (매콤달콤 돼지고기 김치볶음밥) 이런 품사를 제목에 섞지마,
                ingredients 는 [재료, 정량 얼마] 이런식으로 적어주고, 근거가 없는 요리는 추천하지 않도록 조심해줘. 예를 들면 김치찌게에 방울토마토를 넣기. 이런 음식은 아무도 선호하지 않아. 
                recipe는 손질 방법과, 정량 기준, 조리에 대한 상세한 시간을 꼭 언급해줘. 
                image는 실존하지 않거나 접근이 불가능한 이미지 URL 대신, Google 이미지 검색이 가능한 레시피 사이트의 정확한 URL을 알려줘.
                reference는 Google 검색이 가능한 레시피 사이트의 정확한 URL을 알려줘.
                tip은 1~5개정도 알려주고 title,ingredients,summary,recipe,tip 는 한글로 알려줘,
                결과는 반드시 JSON 배열로만 응답해줘.
                응답은 반드시 아래 JSON 형식을 지켜줘:
            
              {{
                "title": "요리 이름 (예: 김치볶음밥)",
                "ingredients": [["재료1","정량 얼마"], ["재료2","정량 얼마"], ["재료1","정량 얼마"]],
                "summary": "해당 요리에 대한 50자 이내의 간략한 설명",
                "image": image,
                "reference": 인터넷 주소URL,
                "recipe": ["step1: 재료 손질 방법과 재료의 정량 기준을 알려주기(밥 숫가락으로 한숟갈, 티 스푼으로 한 숟갈, 종이컵 반컵)"],["step2: 냄비에 넣고 강불로 5분간 끓여주세요"],
                "tip":["tip1: 닭을 손질할때 우유를 넣어서 잡내를 없애면 좋아요!","tip2: 일반 소금 대신 맛소금을 사용하면 더 맛있어요!"]
              }},
              {{
                "title": "요리 이름 (예: 김치볶음밥)",
                "ingredients": [["재료1","정량 얼마"], ["재료2","정량 얼마"], ["재료1","정량 얼마"]],
                "summary": "해당 요리에 대한 50자 이내의 간략한 설명",
                "image": image,
                "reference": 인터넷 주소URL,
                "recipe": ["step1: 재료 손질 방법과 재료의 정량 기준을 알려주기(밥 숫가락으로 한숟갈, 티 스푼으로 한 숟갈, 종이컵 반컵)"],["step2: 냄비에 넣고 강불로 5분간 끓여주세요"],
                "tip":["tip1: 닭을 손질할때 우유를 넣어서 잡내를 없애면 좋아요!","tip2: 일반 소금 대신 맛소금을 사용하면 더 맛있어요!"]
              }}
                """

        response = self.get_text_model(prompt)
        print(response.content)
        result_json = json.loads(response.content)
        self.save_recipe(result_json)
        return result_json

    def save_recipe(self, data):
        for recipe in data:
            title = recipe['title']
            # get_image.getUrlInfo_homepage1(recipe['reference'])
            if title not in recipe_titles:
                recipe_titles.append(title)  # set에 제목 등록
                print("이미 추천한 레시피:",title)
    # endregion

    # region 영수증 분석
    def calc_image_receipt(self):
        imgs = [Image.open(path) for path in self.saved_file_paths]
        num_images = len(imgs)
        prompt = f"""
            총 {num_images}장의 사진이 있어. 구매내역을 보고 구매 물품을 label에 알려주고 수량을 count에 알려줘.
            ingredients, label의 재료는 한글로 알려줘, 결과는 반드시 image_index를 포함한 JSON 배열로만 응답해줘.
            응답은 반드시 아래 JSON 형식을 지켜줘:
        {{
        "ingredients": ["재료1", "재료2"],
        "detections": [{{"label": "Apple", "count":2개}},{{"label": "Milk" ,"count":2개}}]
        }}
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
            응답은 반드시 아래 JSON 형식을 지켜줘:
        {{
        "ingredients": ["재료1", "재료2"],
        "detections": [{{"label": "Apple", "weighing":"1개"}},{{"label": "Milk","weighing":"1컵"}}]
        }}
            """
        content = [prompt] + imgs
        response = self.get_image_model(content)
        return json.loads(response.text)

    def draw_boxes(self, data):
        images_results = {}

        for image_result in data:  # data 자체가 리스트이므로 바로 순회합니다.
            idx = image_result['image_index']
            # 해당 이미지의 detections 리스트를 가져와 저장합니다.
            images_results[idx] = image_result['detections']

        # 1. 저장된 경로 리스트를 순회
        for i, path in enumerate(self.saved_file_paths):
            # 이미지 열기
            img = Image.open(path)
            draw = ImageDraw.Draw(img)

            # 2. 폰트 설정
            try:
                font = ImageFont.truetype("arial.ttf", 20)
            except:
                font = ImageFont.load_default()

            # 3. 각 탐지 결과에 대해 그리기 (해당 이미지에 맞는 데이터인지 확인 필요)
            for det in images_results[i]:
                # 주의: 여러 장의 이미지 결과가 섞여 있다면
                # det에 있는 image_index와 현재 path의 순서가 맞는지 체크 로직이 필요할 수 있습니다.
                label = det['label']
                box = det['box']
                draw.rectangle([box[1], box[0], box[3], box[2]], outline="red", width=5)
                draw.text((box[1], box[0] - 25), label, fill="red", font=font)

            # 4. 파일명 추출 및 저장 경로 생성
            # 예: D:/data/food.jpg -> 디렉토리: D:/data, 파일명: food, 확장자: .jpg
            directory, filename = os.path.split(path)
            name, ext = os.path.splitext(filename)

            # 새로운 저장 경로: D:/data/food_label.jpg
            output_path = os.path.join(directory, f"{name}_label{ext}")

            img.save(output_path)
            print(f"저장 완료: {output_path}")

        return data
    # endregion
