from fastapi import APIRouter, Depends,HTTPException
from services import calc_ai
from schemas.recipe import RequestAnalyze

router = APIRouter(prefix="/analyze-image-",tags=["AI분석"])

#region 분석 - 사진 - 영수증
#이미지로 입력받은 재료 분석후 레시피 추천하기
@router.post("/receipts",
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
@router.post("/ingredients",
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
#endregion