from pydantic import BaseModel
from typing import List, Annotated
import calc_ai
from fastapi import FastAPI, File, UploadFile

#기본 셋팅
app = FastAPI()

# 이미지를 저장할 폴더 생성
UPLOAD_DIR = "./uploads"

#클래스 생성
class Question(BaseModel):
    message: str

class Preferences_Ingredient(BaseModel):
    preferences: List[str]
    ingredients: List[str]
    spices: List[str]


#region 텍스트 분석
@app.post("/analyze-text",
    tags=["분석 - 텍스트"],
    summary="텍스트 재료 분석",
    description="텍스트로 전달 받은 핵심 재료들을 분석하여 레시피 추천.")
async def analyze(request: Preferences_Ingredient):
    process_text = calc_ai.Analyze()
    result = process_text.get_recipe(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 다른 레시피 추천받기
@app.post("/another-recipe",
    tags=["분석 - 텍스트"],
    summary="다른 레시피 추천",
    description="방금전에 말한 레시피가 아닌 다른 레시피 추천 받기.")
async def analyze(request: Preferences_Ingredient):
    process_text = calc_ai.Analyze()
    result = process_text.get_another_recipe(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 만약 다른 재료가 더 있다면!
@app.post("/more-somthing",
    tags=["분석 - 텍스트"],
    summary="추가 재료가 있다면",
    description="추가로 재료가 더 있다면 이런 음식도 가능해요!")
async def analyze(request: Preferences_Ingredient):
    process_text = calc_ai.Analyze()
    result = process_text.get_more_something(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 영수증 사진 분석
#이미지로 입력받은 재료 분석후 레시피 추천하기
@app.post(
    "/analyze-image-receipt",
    tags=["분석 - 영수증"],
    summary="영수증 재료 분석",
    description="영수증에서 재료들을 분석하여 재료를 확인."
)
async def analyze_image(file: UploadFile = File(...)):
    # 단일 파일도 리스트로 감싸서 보내면 Image_Material을 공통으로 쓸 수 있네.
    process_image = calc_ai.Analyze([file])
    result = process_image.calc_image_receipt()
    return {"result": result}

@app.post("/analyze-multiple-receipt",
          tags=["분석 - 영수증"],
          summary="영수증 재료 분석",
          description="영수증에서 재료들을 분석하여 재료를 확인."
          )
async def analyze_multiple_images(
    files: Annotated[List[UploadFile], File(description="여러 개의 이미지 파일을 선택하세요")]
):
    # 3. AI 클래스에 이미지 리스트 전달
    process_image = calc_ai.Analyze(files)
    result = process_image.calc_image_receipt()
    return {"result": result}
#endregion

#region 음식 사진 분석
#이미지로 입력받은 재료 분석후 레시피 추천하기
@app.post(
    "/analyze-image",
    tags=["분석 - 이미지"],
    summary="이미지 재료 분석",
    description="이미지로 전달 받은 재료들을 분석하여 재료를 확인."
)
async def analyze_image(file: UploadFile = File(...)):
    # 단일 파일도 리스트로 감싸서 보내면 Image_Material을 공통으로 쓸 수 있네.
    process_image = calc_ai.Analyze([file])
    result = process_image.calc_image()
    return {"result": result}

@app.post("/analyze-multiple",
          tags=["분석 - 이미지"],
          summary="이미지 재료 분석",
          description="이미지로 전달 받은 재료들을 분석하여 재료를 확인."
          )
async def analyze_multiple_images(
    files: Annotated[List[UploadFile], File(description="여러 개의 이미지 파일을 선택하세요")]
):
    # 3. AI 클래스에 이미지 리스트 전달
    process_image = calc_ai.Analyze(files)
    result = process_image.calc_image()
    return {"result": result}
#endregion





#메인
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app,host='0.0.0.0',port=8000)