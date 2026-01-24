-- ------------------------------------
-- 1. 재료 마스터 (ingredients) 데이터 삽입
-- ------------------------------------

INSERT INTO ingredients (name, category) VALUES
('돼지고기', '육류'),       -- ID=1
('삼겹살', '육류'),          -- ID=2
('앞다리살', '육류'),        -- ID=3
('김치', '채소'),           -- ID=4
('양파', '채소'),           -- ID=5
('두부', '가공품'),          -- ID=6
('달걀', '기타'),           -- ID=7
('명태', '수산물'),          -- ID=8
('쌀', '곡류');             -- ID=9


-- ----------------------------------------------------
-- 2. 재료 키워드 매핑 (ingredient_keywords) 데이터 삽입
-- (동의어, 초성, 포함 관계 검색 테스트용)
-- ----------------------------------------------------

-- 돼지고기 (ID=1)
INSERT INTO ingredient_keywords (ingredient_id, keyword) VALUES
(1, '돼지'), (1, '돈육'), (1, 'pork'), (1, 'ㄷㅈ'), (1, '목살'), (1, '뒷다리살');

-- 삼겹살 (ID=2)
INSERT INTO ingredient_keywords (ingredient_id, keyword) VALUES
(2, '삼겹살'), (2, '삼겹'), (2, '뱃살'), (2, '삼겹이'), (2, 'samyup');

-- 앞다리살 (ID=3)
INSERT INTO ingredient_keywords (ingredient_id, keyword) VALUES
(3, '앞다리'), (3, '전지'), (3, '앞다리살');

-- 김치 (ID=4)
INSERT INTO ingredient_keywords (ingredient_id, keyword) VALUES
(4, '김치'), (4, '배추김치'), (4, '신김치'), (4, 'kimchi');

-- 양파 (ID=5)
INSERT INTO ingredient_keywords (ingredient_id, keyword) VALUES
(5, '양파'), (5, 'onion'), (5, 'ㅇㅍ');

-- 두부 (ID=6)
INSERT INTO ingredient_keywords (ingredient_id, keyword) VALUES
(6, '두부'), (6, '순두부'), (6, '연두부'), (6, 'tofu');

-- 달걀 (ID=7)
INSERT INTO ingredient_keywords (ingredient_id, keyword) VALUES
(7, '달걀'), (7, '계란'), (7, '에그'), (7, 'egg');

-- 명태 (ID=8) - 예시: 다른 이름으로 불리는 생선
INSERT INTO ingredient_keywords (ingredient_id, keyword) VALUES
(8, '명태'), (8, '황태'), (8, '북어'), (8, '동태'), (8, '코다리');