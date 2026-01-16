from pydantic import BaseModel
from typing import List
import calc_ai
from fastapi import FastAPI, File, UploadFile
import shutil
import os


app = FastAPI()
process = calc_ai.llm_question()
# 이미지를 저장할 폴더 생성
UPLOAD_DIR = "./uploads"

class Question(BaseModel):
    message: str

class IngredientRequest(BaseModel):
    ingredients: List[str]

@app.post("/ai")
async def ask_ai(item:Question):
    print(f"Spring 친구가 보낸 메시지:{item.message}")
    result = process.getQuestion(item.message)
    return {"reply": result}

@app.post("/analyze")
async def analyze(request: IngredientRequest):
    print(f"옆자리 자바 서버에서 온 재료: {request.ingredients}")
    return {"status": "success", "message": "잘 받았습니다!"}

@app.post("/analyze-image")
async def analyze_image(file: UploadFile = File(...)):
    # 1. 파일 저장 경로 설정
    print(f"접속됨")
    file_path = os.path.join(UPLOAD_DIR, file.filename)
    os.makedirs(UPLOAD_DIR, exist_ok=True)

    # 2. 파일을 로컬 디스크에 저장 (Option B 구현)
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    print(f"이미지 수신 완료: {file.filename} 저장됨")

    # 3. 나중에는 여기서 YOLO 모델을 돌리게 됩니다.
    return {
        "status": "success",
        "filename": file.filename,
        "message": "이미지가 서버에 성공적으로 저장되었습니다."
    }

#메인
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app,host='0.0.0.0',port=8000)