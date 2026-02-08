# 🍳 레시피를 부탁해

> Gemini AI의 시각 분석 기술과 Ko-SBERT 기반의 레시피 추천 엔진을 결합하여, 냉장고 속 식재료에 딱 맞는 요리를 제안합니다.

---

## 🌟 주요 기능 (Main Features)

### 1️⃣ AI 식재료 및 영수증 분석
* Gemini API를 사용하여 식재료 사진이나 마트 영수증을 분석합니다.
* 이미지에서 식재료 명칭을 자동으로 추출하여 텍스트 데이터로 변환합니다.

### 2️⃣ 개인화된 AI 레시피 생성
* 분석된 식재료와 사용자가 보유한 조미료를 바탕으로 Gemini가 실시간 맞춤형 레시피를 추천합니다.
* 남은 식재료를 활용할 수 있는 효율적인 조리법을 제공합니다.

### 3️⃣ 만개의 레시피 기반 추천 엔진
* 만개의 레시피 출저: https://www.10000recipe.com/?srsltid=AfmBOopfi17OTAi0sdi7Q6Srb_TGzr0Cj2jOwYxm7lSB-3TRE8ITiPkx
* HuggingFace의 `jhgan/ko-sroberta-multitask` 모델을 활용했습니다.
* 만개의 레시피 데이터를 파인튜닝(Fine-tuning)하여, 사용자의 재료와 가장 연관성이 높은 실제 요리사들의 레시피를 추천합니다.

---

## 🛠 기술 스택 (Tech Stack)

### AI & Machine Learning
* Google Gemini: 이미지 분석 및 텍스트 레시피 생성
* HuggingFace: jhgan/ko-sroberta-multitask
* Sentence-Transformers: 레시피 유사도 검색 및 임베딩

### Data Processing
* Python 3.11
* Pandas: 데이터 전처리 및 관리
* Scikit-learn: 코사인 유사도(Cosine Similarity) 계산
* FAISS: 벡터 데이터 인덱싱 처리 검색 속도 향상

---

## 🏗 시스템 아키텍처 (Architecture)

1. 입력: 사용자가 식재료 사진 또는 영수증 이미지 업로드.
2. 분석: Gemini가 이미지 내 식재료 리스트 추출.
3. 합성: 추출된 식재료 + 기존 조미료 데이터를 조합.
4. 탐색: 파인튜닝된 모델을 통해 VectorDB 내 최적의 레시피 매칭.
5. 출력: AI 생성 레시피와 실제 추천 레시피를 제공.


# 필수 라이브러리 설치
pip install -r requirements.txt
