# One_More_Project: AI 기반 개인화 레시피 추천 서비스

## **1. 프로젝트 개요 (Project Overview)**

- 기획 배경: 1인 가구의 증가와 함께 '집밥'에 대한 관심은 높아졌지만, 식재료 관리의 어려움으로 인해 배달 음식이나 레토르트 식품에 의존하는 경향이 여전합니다. 식재료를 소분하고 관리하지 못해 발생하는 음식물 쓰레기는 경제적 손실뿐만 아니라 요리에 대한 심리적 장벽을 높이는 원인이 됩니다.
- 프로젝트 목표: 이러한 문제를 해결하기 위해 AI 기반의 스마트 레시피 추천 서비스를 기획했습니다. 사용자가 냉장고 속 재료를 일일이 입력할 필요 없이 사진 촬영이나 영수증 업로드만으로 간편하게 등록하고, 사용자의 취향(매운맛, 한식 등)이 반영된 최적의 레시피를 제공하여 건강한 식생활과 잔반 감소를 돕고자 합니다.
  

## **2. 기술 스택 (Tech Stack)**

- Backend

| Category | Detail (java) |
| --- | --- |
| **BackEnd** | **Java 21**, **Spring Boot 3.4.1,**  |
| **OS** | Windows |
| **Library & API** | **Spring AI**, **Ollama** (Model: `bge-m3`, `exaone3.5`), **Lombok**, Spring Data JPA,  **RestTemplate** (Server-to-Server Comm) |
| **IDE** | IntelliJ IDEA (Java), VS Code |
| **Server** | Apache Tomcat (Spring Boot Embedded), Uvicorn (FastAPI Server) |
| **Document** | **Swagger** (SpringDoc OpenAPI 3.0) |
| **CI** | **Gradle** (Build Tool) |
| **DataBase** | **PostgreSQL** (Relation + **pgvector** Extension) |
- AI-server

| Category | Detail (Python) |
| --- | --- |
| **BackEnd** | **Python3.11, FastAPI** |
| **OS** | **Windows 11** |
| **Library & API** | **AI/LLM(**Google GenAi, LangChain), **Embedding &** **Vector DB (**FAISS, HuggingFace (`jhgan/ko-sroberta-multitask`), **Model Training**(Sentence-Transformers(PyTorch)) |
| **IDE** | **Pycham** |
| **Server** | **Uvicorn** (FastAPI Server) |
| **Document** | **Swagger** (SpringDoc OpenAPI 3.0) |
| **CI** | **pip** |
- Frontend

| Category | Detail (TypeScript) |
| --- | --- |
| **FrontEnd** | **React Native / Expo** |
| **OS** | **Windows 11** |
| **Library & API** | **Expo Router, React Navigation, React Native Reanimated, Expo Image Picker** |
| **IDE** | **VSCode** |
| **Server** | **Node.js** |
| **CI** | **npm** |


## **3. 시스템 아키텍처 & 데이터 흐름**

3-1 서비스 데이터 플로우:
<div align="center">
  <img src="./assets/3-1_서비스_데이터_플로우.png" width="80%" />
</div>

3-2 시스템 인프라 아키텍처:
<div align="center">
  <img src="./assets/3-2_시스템_인프라_아키텍처.png" width="80%" />
</div>


## 4. 주요 기능 (Key Features)
---
#### 1. 취향 선택
<div align="center">
  <img src="./assets/select_preferences.gif" width="35%" />
</div>

---
#### 2. 식재료 이미지, 영수증 분석
<div align="center">
  <img src="./assets/analyse_image.gif" width="35%" />
  <img src="./assets/analyse_receipt.gif" width="35%" />
</div>

---
#### 3-A. 분석 재료 수정 /  3-B. 직접 식재료 선택
<div align="center">
  <img src="./assets/modify_ingredients.gif" width="35%" />
  <img src="./assets/direct_select_ingredients.gif" width="35%" />
</div>

---
#### 4. 조미료 선택 /  5. 재료, 조미료 확인/레시피 생성
<div align="center">
  <img src="./assets/modify_spices.gif" width="35%" />
  <img src="./assets/generate_recipy.gif" width="35%" />
</div>

---
#### 6-A 기본 방식 레시피 생성
<div align="center">
  <img src="./assets/recipy_basic_detail.gif" width="35%" />
</div>

---
#### 6-B 재료 추가 방식 레시피 생성 /  6-C '만개의 레시피' 레시피 생성
<div align="center">
  <img src="./assets/recipy_more_detail.gif" width="35%" />
  <img src="./assets/recipy_real_detail.gif" width="35%" />
</div>

---
