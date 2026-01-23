from pydantic import BaseModel
from typing import List, Optional
import calc_ai
from fastapi import FastAPI, File, UploadFile,HTTPException, Form, Depends
from fastapi.middleware.cors import CORSMiddleware
from fastapi import Request
from dataclasses import dataclass



#region 전역변수 선언
#기본 셋팅
app = FastAPI()
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=["*"],  # 실제 운영 환경에서는 허용할 IP만 적는 것이 좋습니다.
#     allow_credentials=True,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )

ai = calc_ai.AnalyzeGenai()
# ai.get_rag_embedding()
#endregion

#region 클래스 생성
class Question(BaseModel):
    message: str

class Ingredients(BaseModel):
    ingredient:str
    quantity:str

class RequestRecipe(BaseModel):
    preferences: List[str]
    ingredients: List[Ingredients]
    spices: List[str]
    userId: str

@dataclass
class RequestAnalyze:
    files: Optional[List[UploadFile]] = File(None)
    preference: Optional[str] = Form(None)
    userId: Optional[str] = Form(None)
#endregion


#region 레시피 - 만개의 레시피
@app.post("/recipes-generate-real",
    tags=["레시피"],
    summary="만개의 레시피(3) 추천",
    description="만개의 레시피를 참고한 레시피 추천.")
async def analyze(request: RequestRecipe):
    ShowDebugForText(request)
    result = ai.get_chef_recipe(request.preferences, request.ingredients, request.spices)
    return {"result": result}
#endregion


#region 레시피 - 기본 레시피
@app.post("/recipes-generate-initial",
    tags=["레시피"],
    summary="기본 재료 레시피(1), 추가 재료 레시피(2) 추천",
    description="텍스트로 전달 받은 핵심 재료들을 분석하여 레시피 추천.")
async def analyze(request: RequestRecipe):
    ShowDebugForText(request)
    result = ai.get_recipe(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 레시피 - 다른 레시피
@app.post("/recipes-generate-basic",
    tags=["레시피"],
    summary="기본 재료 다른 레시피(3) 추천",
    description="방금전에 말한 레시피가 아닌 다른 레시피 추천 받기.")
async def analyze(request: RequestRecipe):
    ShowDebugForText(request)
    result = ai.get_another_recipe(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 레시피 - 재료가 더 있다면
@app.post("/recipes-generate-more",
    tags=["레시피"],
    summary="추가 재료 레시피(3) 추천",
    description="추가로 재료가 더 있다면 이런 음식도 가능해요!")
async def analyze(request: RequestRecipe):
    ShowDebugForText(request)
    result = ai.get_another_more_somthing(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 분석 - 사진 - 영수증
#이미지로 입력받은 재료 분석후 레시피 추천하기

@app.post("/analyze-image-receipts",
          tags=["AI분석"],
          summary="이미지 영수증 분석",
          description="영수증에서 재료들을 분석하여 재료를 확인."
          )
async def analyze_images_receipts(request: RequestAnalyze = Depends()):
    await ShowDebugForImage(request)
    try:
        # 클래스에 이미지 리스트 전달
        process_image = calc_ai.AnalyzeGenai(request.files, request.userId)
        result = process_image.calc_image_receipt()
        return {"result": result}
    except Exception as e:
        # 분석 중 에러 처리
        raise HTTPException(status_code=500, detail=f"AI 분석 중 오류 발생: {str(e)}")
#endregion

#region 분석 - 사진 - 재료
#이미지로 입력받은 재료 분석후 레시피 추천하기
@app.post("/analyze-image-ingredients",
          tags=["AI분석"],
          summary="이미지 재료 분석",
          description="이미지로 전달 받은 재료들을 분석하여 재료를 확인."
          )
async def analyze_images(request: RequestAnalyze = Depends()):
    await ShowDebugForImage(request)
    try:
        # 클래스에 이미지 리스트 전달
        process_image = calc_ai.AnalyzeGenai(request.files, request.userId)
        result = process_image.calc_image()
        return {"result": result}
    except Exception as e:
        # 분석 중 에러 처리
        raise HTTPException(status_code=500, detail=f"AI 분석 중 오류 발생: {str(e)}")

#endregion

#region Debug

@app.post("/remake-csv",
    tags=["기능"],
    summary="CSV 다시 만들기",
    description="CSV다시 만들기")
async def analyze(request: Question):
    result = ai.remake_csv()
    return {"result": result}

def ShowDebugForText(request: RequestRecipe):
    print("\n" + "=" * 50)
    print("★ [DEBUG] 요청 도달 성공!")
    print(f"userId: {request.userId}")
    print(f"preference (raw): {[t for t in request.preferences]}")
    inge_list = [f"{t.ingredient}({t.quantity})" for t in request.ingredients]
    print(f"ingredients (raw): {', '.join(inge_list)}")
    print(f"spices (raw): {request.spices}")

async def ShowDebugForImage(request:RequestAnalyze):
    # 콘솔(터미널)에 바로 찍힙니다.
    print("\n" + "=" * 50)
    print("★ [DEBUG] 요청 도달 성공!")
    print(f"userId: {request.userId}")
    print(f"preference (raw): {request.preference}")

    if request.files:
        print(f"파일 개수: {len(request.files)}")
        for f in request.files:
            print(f"파일명: {f.filename}")

    # 만약 preference가 JSON 문자열('[...]')로 왔을 경우 처리
    print(f"변환된 취향 리스트: {request.preference}")

    # 이미지 최대 크기
    max_file_size = 15 * 1024 * 1024  # 15MB (Spring보다 조금 더 여유 있게 설정)
    # 1. 파일 검증
    for file in request.files:
        # 파일을 한 번에 다 읽지 않고 크기만 확인
        content = await file.read()
        size = len(content)

        # 바이트(Byte)를 메가바이트(MB)로 환산
        size_in_mb = size / (1024 * 1024)

        if size > max_file_size:
            print(f"⚠️ 용량 초과 발생: {file.filename} ({size_in_mb:.2f} MB)")
            raise HTTPException(status_code=413, detail=f"파일이 너무 큽니다. ({size_in_mb:.2f} MB)")
        else:
            # 정상적인 경우 용량 출력
            print(f"✅ 이미지 수신: {file.filename} / 크기: {size_in_mb:.2f} MB")

        # 읽은 데이터를 AI에 넘기기 위해 커서를 다시 맨 앞으로!
        await file.seek(0)

@app.post("/debug",
          tags=["디버그"],
          summary="디버그 용",
          description="자료형 확인하기."
          )
async def debug(request: Request):
    print("\n" + "=" * 50)
    # Spring이 보낸 모든 Form 데이터를 강제로 추출
    form_data = await request.form()

    print("--- [Spring이 보낸 실제 데이터 목록] ---")
    for key in form_data.keys():
        # getlist를 써야 중복된 키(리스트)를 모두 볼 수 있습니다.
        values = form_data.getlist(key)
        print(f"Key: '{key}' | Values: {values}")

    print("=" * 50 + "\n")
    return {"result": "ok"}

#endregion

#메인
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app,host='0.0.0.0',port=8000)
