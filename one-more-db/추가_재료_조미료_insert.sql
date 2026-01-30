-- 1. 임시 테이블 생성 (CSV를 통째로 들이붓기 위함)
CREATE TEMP TABLE temp_ingredients_batch (
    ing_name TEXT,
    spi_name TEXT
);

-- 2. CSV 파일을 임시 테이블로 고속 복사
-- 파일 경로를 공용경로로 지정해야함. 
COPY temp_ingredients_batch(ing_name, spi_name)
FROM 'C:\Users\Public\scan\cleaned_ingredients.csv'
WITH (FORMAT csv, HEADER true, ENCODING 'utf8');

-- 3. 임시 테이블에서 진짜 테이블로 데이터 분산 삽입
-- 중복 제거를 위해 DISTINCT를 사용하고, 빈 값('')은 넣지 않습니다.

-- [Ingredients 테이블로 삽입]
INSERT INTO ingredients (name)
SELECT DISTINCT ing_name 
FROM temp_ingredients_batch 
WHERE ing_name IS NOT NULL AND ing_name != ''
ON CONFLICT (name) DO NOTHING;

-- [Spices 테이블로 삽입]
INSERT INTO spices (name)
SELECT DISTINCT spi_name 
FROM temp_ingredients_batch 
WHERE spi_name IS NOT NULL AND spi_name != ''
ON CONFLICT (name) DO NOTHING;

-- 4. 임시 테이블 삭제
DROP TABLE temp_ingredients_batch;

