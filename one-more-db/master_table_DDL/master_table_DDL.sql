-- [1단계] 기존 테이블 전체 삭제 SQL (초기화)
-- DROP TABLE IF EXISTS preference_keywords CASCADE;
-- DROP TABLE IF EXISTS ingredient_keywords CASCADE;
-- DROP TABLE IF EXISTS spice_keywords CASCADE;
-- DROP TABLE IF EXISTS preferences CASCADE;
-- DROP TABLE IF EXISTS ingredients CASCADE;
-- DROP TABLE IF EXISTS spices CASCADE;
-- DROP TABLE IF EXISTS recipes CASCADE;

-- [2단계] 마스터 데이터 테이블 설계

-- 2-1. 취향 마스터 테이블 (표준 이름)
CREATE TABLE preferences (
    id SERIAL PRIMARY KEY,
    category VARCHAR(20) NOT NULL,   -- 'STYLE', 'TASTE', 'CONDITION'
    name VARCHAR(50) NOT NULL UNIQUE,-- '매운 맛', '단 맛', '포만감'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2-2. 재료 마스터 테이블 (표준 이름)
CREATE TABLE ingredients (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),            -- '육류', '채소' 등
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2-3. 조미료 마스터 테이블 (표준 이름)
CREATE TABLE spices (
    id SERIAL PRIMARY KEY,
    category VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- [3단계] 키워드 매핑 테이블 설계 (검색 최적화)

-- 3-1. 취향 검색용 키워드 매핑 테이블
CREATE TABLE preference_keywords (
    id SERIAL PRIMARY KEY,
    preference_id INTEGER REFERENCES preferences(id) ON DELETE CASCADE,
    keyword VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_preference_keyword ON preference_keywords(keyword);

-- 3-2. 재료 검색용 키워드 매핑 테이블
CREATE TABLE ingredient_keywords (
    id SERIAL PRIMARY KEY,
    ingredient_id INTEGER REFERENCES ingredients(id) ON DELETE CASCADE,
    keyword VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ingredient_keyword ON ingredient_keywords(keyword);

-- 3-3. 조미료 검색용 키워드 매핑 테이블
CREATE TABLE spice_keywords (
    id SERIAL PRIMARY KEY,
    spice_id INTEGER REFERENCES spices(id) ON DELETE CASCADE,
    keyword VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_spice_keyword ON spice_keywords(keyword);

-- [4단계] 레시피 결과 저장 테이블 설계

CREATE TABLE recipes (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary TEXT,
    ingredients_list TEXT,
	more_list TEXT,
    recipe TEXT,
    tips TEXT,
	image_url TEXT,
	reference_link TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);