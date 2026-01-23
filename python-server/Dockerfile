# 1. 파이썬 3.11 슬림 버전 사용 (리눅스 기반)
FROM python:3.11-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 라이브러리 설치 (캐시 활용을 위해 먼저 복사)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. 소스 코드 복사
COPY . .

# 5. 포트 설정 (FastAPI 기본 포트 8000)
EXPOSE 8000

# 6. 서버 실행
CMD ["python", "one_more_server.py"]