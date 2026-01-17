# Project Context
- 프로젝트의 흐름
1. 사용자에게 web페이지에서 입력을 받는다. (이 입력은 사용자의 입력 텍스트들, 선택한 항목의 텍스트들, 촬영한 이미지, 촬영한 영수증이 될 수 있다.)
2. spring backend에서 입력을 받고, 이미지인지 텍스트인지 분류하고, python 서버로 분석을 위해 전송한다. 
3. python 서버는 전달받은 데이터가 
3-1 이미지일 경우, YOLO8을 모델을 통해 이미지 속 객체를 텍스트로 변환 후 LLM으로 전달한다.
3-2 영수증일 경우, OCR 모델을 통해 영수증 속 텍스트 객체를 텍스트로 변환 후 LLM으로 전달한다.
3-3 텍스트일 경우 LLM으로 전달한다.
4. LLM은 전달받은 텍스트를 작성된 prompt에 넣어서 정해진 형식의 레시피를 출력한다. 
5. LLM이 출력한 레시피를 파이선 서버에서 스프링서버로 전송한다. 
6. 전달받은 레시피를 사용자에게 전달한다. 

# Project properties 
1. 초반 단계에서는 DB를 만들지 않고, 서버간의 데이터 전송만으로 기능을 구현하고 있다. 
2. 기술스택은 대략적인 예정이고. 아직 완벽하게 사용중이지 않다. 만약 사용하고 싶은지 확인하고 싶다면, 직접 사용여부를 물어봐라.

# Detailed Technical Stack
## 1. Main Backend (Java)
- Framework: Spring Boot 4.0.1 (Java 17)
- ORM/DB: Spring Data JPA, QueryDSL (동적 필터링용), PostgreSQL + PostGIS (공간 데이터 연산)
- Security: Spring Security + JWT (Stateless)
- Infrastructure: Redis (할인 정보 캐싱), JUnit5 (테스트)

## 2. AI Service (Python)
- Framework: FastAPI (Asynchronous)
- Vision: YOLOv8 (식재료 객체 탐지), Google Cloud Vision API (영수증 OCR)
- Image Processing: OpenCV (전처리 및 신선도 분석)
- Deployment: Docker (AI 환경 격리)

## 3. Intelligence Layer (LLM)
- Model: OpenAI GPT-4o (또는 Claude 3.5)
- Library: LangChain (프롬프트 템플릿 및 체인 관리)
- Core: 자연어 기반 맥락 분석 및 영양 정보 리포트 요약


Get-ChildItem -Recurse -Include *.java, *.properties, *.yml, *.gradle, *.xml | ForEach-Object {
    "--- FILE: $($_.FullName) ---"
    Get-Content $_.FullName
    "`n"
} | Out-File -FilePath project_context.txt -Encoding utf8NoBOM


일단 코드를 추가해야돼. 기존 코드도 약간 다듬어야하고. 
지금 컨트롤러에서 이미지만 업로드한다고 메서드가 되어있는데. 
영수증 or 재료사진 둘 중 하나를 받는거라서. 파이썬 서버에서 영수증은 OCR이 분석하고, 재료사진은 yolo가 분석해야하거든.
백엔드에서 구분해서 보내줘야 파이썬에서 알맞은 모델에 전달할 수 있어.
dto, service, controller 수정해서 코드 보내줘. (수정할 필요 없으면 안건드려도 괜찮아.)
