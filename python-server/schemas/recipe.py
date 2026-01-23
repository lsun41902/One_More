from pydantic import BaseModel
from typing import List, Optional
from fastapi import File, Form, UploadFile
from dataclasses import dataclass

#region 클래스 생성
class Question(BaseModel):
    message: str

class Ingredients(BaseModel):
    ingredient:str
    quantity:str

class RequestRecipe(BaseModel):
    preferences: List[str]
    ingredients: List[Ingredients]
    spices: List[str]
    userId: str

@dataclass
class RequestAnalyze:
    files: Optional[List[UploadFile]] = File(None)
    preference: Optional[str] = Form(None)
    userId: Optional[str] = Form(None)
#endregion