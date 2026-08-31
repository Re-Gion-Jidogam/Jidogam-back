-- Area 테이블: 시도/시군구를 계층형(자기참조) 구조로 재설계
-- no real users: 기존 데이터는 전부 비우고 AreaInitService 재실행으로 다시 적재
TRUNCATE TABLE areas CASCADE;

ALTER TABLE areas
    DROP COLUMN sido,
    DROP COLUMN sigungu,
    DROP CONSTRAINT areas_sigungu_code_key,
    DROP COLUMN sigungu_code;

ALTER TABLE areas
    ADD COLUMN name                 VARCHAR(50) NOT NULL,
    ADD COLUMN code                 VARCHAR(10) NOT NULL,
    ADD COLUMN administrative_level VARCHAR(20) NOT NULL,
    ADD COLUMN parent_id            UUID;

ALTER TABLE areas
    ADD CONSTRAINT uk_areas_parent_code UNIQUE (parent_id, code);

ALTER TABLE areas
    ADD CONSTRAINT fk_areas_parent_id
        FOREIGN KEY (parent_id) REFERENCES areas (id);

-- 시도(SIDO) row는 parent_id가 NULL이라 위 복합 유니크로 중복이 안 걸러짐(NULL은 서로 달리 취급됨)
-- 그래서 parent_id가 NULL인 row(=시도)에 한해 code 유니크를 별도로 강제
CREATE UNIQUE INDEX uk_areas_code_when_root ON areas (code) WHERE parent_id IS NULL;

-- 소외지역 분류라는 의미에 맞게 컬럼명 변경
ALTER TABLE areas
    RENAME COLUMN type TO population_decline_category;

-- 시도(SIDO) row는 소외지역 분류/가중치 대상이 아니므로 nullable로 전환
ALTER TABLE areas
    ALTER COLUMN weight DROP NOT NULL,
    ALTER COLUMN population_decline_category DROP NOT NULL,
    ALTER COLUMN weight_updated_at DROP NOT NULL;
