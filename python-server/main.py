from fastapi import FastAPI
from router import analysis, recipes, test

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
#endregion

app.include_router(recipes.router)
app.include_router(analysis.router)
app.include_router(test.router)

@app.get("/")
async def root():
    return {"message":"Welcome to One More Project AI server"}


#메인
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app",host='0.0.0.0',port=8000,reload=True)
