from fastapi import APIRouter
from schemas.recipe import RequestRecipe
from services import calc_ai

router = APIRouter(prefix="/recipes-generate-",tags=["레시피"])
# ai.get_rag_embedding()

#region 레시피 - 만개의 레시피
@router.post("/real",
    tags=["레시피"],
    summary="만개의 레시피(3) 추천",
    description="만개의 레시피를 참고한 레시피 추천.")
async def analyze(request: RequestRecipe):
    ShowDebugForText(request)
    ai = calc_ai.AnalyzeGenai()
    result = ai.get_chef_recipe(request.preferences, request.ingredients, request.spices)
    return {"result": result}
#endregion


#region 레시피 - 기본 레시피
@router.post("/initial",
    tags=["레시피"],
    summary="기본 재료 레시피(1), 추가 재료 레시피(2) 추천",
    description="텍스트로 전달 받은 핵심 재료들을 분석하여 레시피 추천.")
async def analyze(request: RequestRecipe):
    ShowDebugForText(request)
    ai = calc_ai.AnalyzeGenai()
    result = ai.get_recipe(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 레시피 - 다른 레시피
@router.post("/basic",
    tags=["레시피"],
    summary="기본 재료 다른 레시피(3) 추천",
    description="방금전에 말한 레시피가 아닌 다른 레시피 추천 받기.")
async def analyze(request: RequestRecipe):
    ShowDebugForText(request)
    ai = calc_ai.AnalyzeGenai()
    result = ai.get_another_recipe(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region 레시피 - 재료가 더 있다면
@router.post("/more",
    tags=["레시피"],
    summary="추가 재료 레시피(3) 추천",
    description="추가로 재료가 더 있다면 이런 음식도 가능해요!")
async def analyze(request: RequestRecipe):
    ShowDebugForText(request)
    ai = calc_ai.AnalyzeGenai()
    result = ai.get_another_more_somthing(request.preferences,request.ingredients,request.spices)
    return {"result": result}
#endregion

#region Debug
def ShowDebugForText(request: RequestRecipe):
    print("\n" + "=" * 50)
    print("★ [DEBUG] 요청 도달 성공!")
    print(f"userId: {request.userId}")
    print(f"preference (raw): {[t for t in request.preferences]}")
    inge_list = [f"{t.ingredient}({t.quantity})" for t in request.ingredients]
    print(f"ingredients (raw): {', '.join(inge_list)}")
    print(f"spices (raw): {request.spices}")
#endregion