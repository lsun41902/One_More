-- ------------------------------------
-- 3. 조미료 마스터 (spices) 데이터 삽입
-- ------------------------------------

INSERT INTO spices (name) VALUES
('간장'), ('고추장'), ('된장'), ('쌈장'), ('소금'), ('설탕'), ('후추'),
('참기름'), ('식초'), ('올리브 오일'), ('맛술'), ('굴소스'), ('다진 마늘');


-- --------------------------------------------------
-- 4. 조미료 키워드 매핑 (spice_keywords) 데이터 삽입
-- (초성, 영문, 축약어 검색 테스트용)
-- --------------------------------------------------

-- 간장 (ID=1 가정)
INSERT INTO spice_keywords (spice_id, keyword) VALUES
(1, '간장'), (1, 'ㄱㅈ'), (1, 'soy sauce'), (1, '양조간장');

-- 고추장 (ID=2 가정)
INSERT INTO spice_keywords (spice_id, keyword) VALUES
(2, '고추장'), (2, 'ㄱㅊㅈ'), (2, 'gochujang'), (2, '태양초');

-- 소금 (ID=5 가정)
INSERT INTO spice_keywords (spice_id, keyword) VALUES
(5, '소금'), (5, '천일염'), (5, '맛소금'), (5, 'salt');

-- 올리브 오일 (ID=10 가정)
INSERT INTO spice_keywords (spice_id, keyword) VALUES
(10, '올리브'), (10, '올리브유'), (10, '오일'), (10, 'oil'), (10, '포도씨유');