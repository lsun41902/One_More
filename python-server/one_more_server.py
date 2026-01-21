from pydantic import BaseModel
from typing import List, Annotated
import calc_ai
from fastapi import FastAPI, File, UploadFile,HTTPException, Form

#기본 셋팅
app = FastAPI()

# 이미지를 저장할 폴더 생성
UPLOAD_DIR = "./uploads"
#이미지 최대 크기
MAX_FILE_SIZE = 15 * 1024 * 1024  # 15MB (Spring보다 조금 더 여유 있게 설정)

#클래스 생성
class Question(BaseModel):
    message: str

class Preferences_Ingredient(BaseModel):
    preferences: List[str]
    ingredients: List[str]
    spices: List[str]

#region 텍스트 분석
@app.post("/test-rag",
    tags=["테스트"],
    summary="RAG 테스트",
    description="텍스트로 전달 받은 요리의 재료들을 분석하여 레시피 추천.")
async def analyze(request: Question):
    process_text = calc_ai.Analyze()
    result = process_text.test_langchain_rag(request.message)
    return {"result": result}

@app.post("/remake-csv",
    tags=["기능"],
    summary="CSV 다시 만들기",
    description="CSV다시 만들기")
async def analyze(request: Question):
    process_text = calc_ai.Analyze()
    result = process_text.remake_csv()
    return {"result": result}
#endregion


#region 텍스트 분석
@app.post("/recipes-generate-initial",
    tags=["레시피"],
    summary="기본 재료(1) 추가 재료 레시피(2) 추천",
    description="텍스트로 전달 받은 핵심 재료들을 분석하여 레시피 추천.")
async def analyze(request: Preferences_Ingredient):
    process_text = calc_ai.Analyze()
    result = process_text.get_recipe(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 다른 레시피 추천받기
@app.post("/recipes-generate-basic",
    tags=["레시피"],
    summary="기본 재료 다른 레시피(3) 추천",
    description="방금전에 말한 레시피가 아닌 다른 레시피 추천 받기.")
async def analyze(request: Preferences_Ingredient):
    process_text = calc_ai.Analyze()
    result = process_text.get_another_recipe(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 만약 다른 재료가 더 있다면!
@app.post("/recipes-generate-more",
    tags=["레시피"],
    summary="추가 재료 레시피(3) 추천",
    description="추가로 재료가 더 있다면 이런 음식도 가능해요!")
async def analyze(request: Preferences_Ingredient):
    process_text = calc_ai.Analyze()
    result = process_text.get_another_more_somthing(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 영수증 사진 분석
#이미지로 입력받은 재료 분석후 레시피 추천하기

@app.post("/analyze-image-receipts",
          tags=["AI분석"],
          summary="이미지 영수증 분석",
          description="영수증에서 재료들을 분석하여 재료를 확인."
          )
async def analyze_images_receipts(
    files: Annotated[List[UploadFile], File(description="여러 개의 이미지 파일을 선택하세요")]
):
    # 1. 파일 검증
    for file in files:
        # 파일을 한 번에 다 읽지 않고 크기만 확인
        content = await file.read()
        size = len(content)

        # 바이트(Byte)를 메가바이트(MB)로 환산
        size_in_mb = size / (1024 * 1024)

        if size > MAX_FILE_SIZE:
            print(f"⚠️ 용량 초과 발생: {file.filename} ({size_in_mb:.2f} MB)")
            raise HTTPException(status_code=413, detail=f"파일이 너무 큽니다. ({size_in_mb:.2f} MB)")
        else:
            # 정상적인 경우 용량 출력
            print(f"✅ 이미지 수신: {file.filename} / 크기: {size_in_mb:.2f} MB")

        # 읽은 데이터를 AI에 넘기기 위해 커서를 다시 맨 앞으로!
        await file.seek(0)
    try:
        # 클래스에 이미지 리스트 전달
        process_image = calc_ai.Analyze(files)
        result = process_image.calc_image_receipt()
        return {"result": result}
    except Exception as e:
        # 분석 중 에러 처리
        raise HTTPException(status_code=500, detail=f"AI 분석 중 오류 발생: {str(e)}")
#endregion

#region 음식 사진 분석
#이미지로 입력받은 재료 분석후 레시피 추천하기
@app.post("/analyze-image-ingredients",
          tags=["AI분석"],
          summary="이미지 재료 분석",
          description="이미지로 전달 받은 재료들을 분석하여 재료를 확인."
          )
async def analyze_images(
files: Annotated[List[UploadFile], File(description="여러 개의 이미지 파일을 선택하세요")],
preference: Annotated[List[str], Form()] = None,
userId: Annotated[str, Form()] = None
):
    # 1. 파일 검증
    for file in files:
        # 파일을 한 번에 다 읽지 않고 크기만 확인
        content = await file.read()
        size = len(content)

        # 바이트(Byte)를 메가바이트(MB)로 환산
        size_in_mb = size / (1024 * 1024)

        if size > MAX_FILE_SIZE:
            print(f"⚠️ 용량 초과 발생: {file.filename} ({size_in_mb:.2f} MB)")
            raise HTTPException(status_code=413, detail=f"파일이 너무 큽니다. ({size_in_mb:.2f} MB)")
        else:
            # 정상적인 경우 용량 출력
            print(f"✅ 이미지 수신: {file.filename} / 크기: {size_in_mb:.2f} MB")

        # 읽은 데이터를 AI에 넘기기 위해 커서를 다시 맨 앞으로!
        await file.seek(0)
    try:
        # 클래스에 이미지 리스트 전달
        process_image = calc_ai.Analyze(files)
        result = process_image.calc_image()
        return {"result": result}
    except Exception as e:
        # 분석 중 에러 처리
        raise HTTPException(status_code=500, detail=f"AI 분석 중 오류 발생: {str(e)}")

#endregion





#메인
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app,host='0.0.0.0',port=8000)
