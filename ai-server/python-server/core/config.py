import os
from dotenv import load_dotenv

load_dotenv() # 최상위 루트의 .env를 읽음

class Settings:
    PROJECT_NAME: str = "One More Project"
    GOOGLE_API_KEY: str = os.getenv("GOOGLE_API_KEY")
    OPENAI_API_KEY: str = os.getenv("OPENAI_API_KEY")
    DATABASE_URL: str = os.getenv("DATABASE_URL")

settings = Settings()