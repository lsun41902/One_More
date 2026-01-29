import pandas as pd
from sentence_transformers import SentenceTransformer, InputExample, losses
from torch.utils.data import DataLoader
import os
import torch
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_core.documents import Document
from tqdm import tqdm # 진행 바 라이브러리
from langchain_community.vectorstores.utils import DistanceStrategy # 1. 임포트 추가



def study_embedding():
    # 1. GPU 장치 설정
    print(f"PyTorch 버전: {torch.__version__}")  # 2.6.0 이상인지 확인
    print(f"GPU 사용 가능 여부: {torch.cuda.is_available()}")
    device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"🚀 학습 장치: {device}")
    if torch.cuda.is_available():
        print(f"사용 중인 GPU: {torch.cuda.get_device_name(0)}")

    # 2. 모델 로드 시 device 지정 (가장 중요!)
    # RTX 4070을 제대로 인식시키기 위해 device 파라미터를 추가합니다.
    model = SentenceTransformer('jhgan/ko-sroberta-multitask', device=device)

    # 3. 데이터 로드
    current_dir = os.path.dirname(os.path.abspath(__file__))
    path = os.path.join(current_dir, "..", "temp_data", 'refined_recipe_data.csv')
    df = pd.read_csv(path)

    # 4. 학습 데이터셋 생성
    train_examples = []
    for i, row in df.iterrows():
        # query: 전처리된 재료 키워드 뭉치
        # answer: 실제 정답 (여기서는 'CKG_NM' 즉 요리명이 들어가는 것이 성능상 더 유리할 수 있습니다.)
        query = str(row['ingredient'])
        answer = str(row['CKG_MTRL_CN'])

        if query.strip() and answer.strip():
            train_examples.append(InputExample(texts=[query, answer]))

    # 5. DataLoader 설정
    # RTX 4070은 메모리가 넉넉하므로 batch_size를 64~128로 키우면 학습 속도가 훨씬 빨라집니다.
    train_dataloader = DataLoader(train_examples, shuffle=True, batch_size=128)

    # 6. Loss 함수 설정
    train_loss = losses.MultipleNegativesRankingLoss(model=model)

    # 7. 학습 시작 (Fine-tuning)
    model.fit(
        train_objectives=[(train_dataloader, train_loss)],
        epochs=5,
        warmup_steps=100,
        output_path='../my_food_model',
        show_progress_bar=True # 진행 상황을 보기 쉽게 표시
    )

    print("🎉 GPU를 활용한 모델 학습이 완료되었습니다!")


def study_embedding_indexing():
    # 1. 경로 설정 (안전하게 절대 경로로 계산)
    current_dir = os.path.dirname(os.path.abspath(__file__))

    # [수정] 학습 함수에서 저장한 위치와 동일하게 맞추는 것이 좋습니다.
    model_path = os.path.join(current_dir, "..", "my_food_model")

    print(f"⏳ 모델 로딩 중... (경로: {model_path})")
    try:
        embeddings_model = HuggingFaceEmbeddings(
            model_name=model_path,
            model_kwargs={'device': 'cuda'}
        )
    except Exception as e:
        print(f"❌ 모델 로드 실패: {e}")
        return

    # 2. 데이터 로드 (인코딩 추가)
    path = os.path.join(current_dir, "..", "temp_data", "refined_recipe_data.csv")
    df = pd.read_csv(path, encoding='utf-8-sig')
    bad_patterns = ["본문내용", "확인해 보실", "소개 해 드리겠습니다", "상세레시피", "확인해주세요"]
    documents = []
    for _, row in df.iterrows():
        # 데이터가 비어있는지(nan) 확인
        if pd.isna(row['CKG_MTRL_CN']) or pd.isna(row['ingredient']):
            continue
        title = str(row['RCP_TTL'])
        content = str(row['CKG_MTRL_CN'])
        ingredient_list = str(row['ingredient'])

        # 무의미한 안내 문구 필터링
        if any(pattern in content for pattern in bad_patterns):
            continue

        # 'nan'이라는 문자열 자체도 걸러냄
        if ingredient_list.lower() == 'nan':
            continue

        # 모든 조건을 통과한 깨끗한 데이터만 추가
        documents.append(
            Document(
                page_content=f"요리 이름:{title}\n요리사가 사용한 재료: {content}\n필요 재료: {ingredient_list}",
                metadata={'ingredient': ingredient_list}
            )
        )

    # 3. 인덱싱 진행
    batch_size = 256
    total_docs = len(documents)  # 이 줄을 추가하세요!
    initial_batch = documents[:batch_size]

    # [수정] 명시적으로 정규화가 지원되는 방식으로 생성
    vectorstore = FAISS.from_documents(
        initial_batch,
        embeddings_model,
        distance_strategy=DistanceStrategy.COSINE
    )

    # 핵심: 이미 생성된 인덱스에 데이터를 추가할 때도 전략이 유지되도록 함
    for i in tqdm(range(batch_size, total_docs, batch_size), desc="Indexing Recipes"):
        batch = documents[i: i + batch_size]
        vectorstore.add_documents(batch)

    output_path = os.path.join(current_dir, "..", "faiss_my_food_model_index")
    vectorstore.save_local(output_path)
    print(f"\n✅ DB 생성 완료! 저장 경로: {output_path}")