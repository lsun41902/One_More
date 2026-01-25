-- ----------------------------------------
-- 1. 취향 마스터 (preferences) 데이터 삽입
-- ----------------------------------------

-- TASTE (맛)
INSERT INTO preferences (category, name) VALUES
('TASTE', '매운 맛'),
('TASTE', '단 맛'),
('TASTE', '짠 맛'),
('TASTE', '고소한 맛'),
('TASTE', '상큼한 맛');

-- STYLE (요리 스타일)
INSERT INTO preferences (category, name) VALUES
('STYLE', '한식'),    
('STYLE', '일식'),    
('STYLE', '중식'),    
('STYLE', '양식'),  
('STYLE', '퓨전 요리'),
('STYLE', '베트남 요리');       

-- CONDITION (요리 컨디션/목적)
INSERT INTO preferences (category, name) VALUES
('CONDITION', '해장용'),
('CONDITION', '기력 회복'),
('CONDITION', '야식'),
('CONDITION', '도시락 반찬'),
('CONDITION', '포만감 높은');

-- ----------------------------------------------------
-- 2. 취향 키워드 매핑 (preference_keywords) 데이터 삽입
-- (동일한 preference_id를 여러 키워드에 연결하여 동의어 검색을 구현)
-- ----------------------------------------------------

-- TASTE: '매운 맛' (ID=1 가정) - 기존 유지
INSERT INTO preference_keywords (preference_id, keyword) VALUES
(1, '매운'), (1, '매콤한'), (1, '얼얼한'), (1, '아주매운'), (1, 'spicy'), (1, '화끈한');

-- TASTE: '단 맛' (ID=2 가정) - 기존 유지
INSERT INTO preference_keywords (preference_id, keyword) VALUES
(2, '단'), (2, '달콤한'), (2, '달달한'), (2, 'sweet'), (2, '디저트'), (2, '설탕');


-- STYLE: '한식' (ID=6 가정) - [추가]
INSERT INTO preference_keywords (preference_id, keyword) VALUES
(6, '한식'), (6, '한국'), (6, 'korean'), (6, '집밥');

-- STYLE: '일식' (ID=7 가정) - [추가]
INSERT INTO preference_keywords (preference_id, keyword) VALUES
(7, '일식'), (7, '일본'), (7, 'japanese'), (7, '스시'), (7, '튀김');

-- CONDITION: '포만감 높은' (ID=16 가정) - 기존 유지
INSERT INTO preference_keywords (preference_id, keyword) VALUES
(16, '든든한'), (16, '배부른'), (16, '양많은'), (16, '많이'), (16, '포만감');


