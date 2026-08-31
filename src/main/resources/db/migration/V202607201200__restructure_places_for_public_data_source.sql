-- 카카오 연동을 걷어내고 공공데이터(소상공인 등) 기반으로 장소 데이터 관리
-- 지번주소/도로명주소로 분리하고,공공데이터를 가져온(동기화한) 일시를 기록하는 컬럼을 추가
ALTER TABLE places
    DROP COLUMN kakao_id,
    DROP COLUMN category,
    DROP COLUMN address,
    ADD COLUMN external_id   VARCHAR(255) NOT NULL,
    ADD COLUMN source        VARCHAR(50)  NOT NULL,
    ADD COLUMN category_code VARCHAR(50)  NULL,
    ADD COLUMN category_name VARCHAR(50)  NULL,
    ADD COLUMN jibun_address VARCHAR(255) NOT NULL,
    ADD COLUMN road_address  VARCHAR(255) NULL,
    ADD COLUMN fetched_at    TIMESTAMP    NOT NULL;

ALTER TABLE places
    ADD CONSTRAINT uk_places_source_external_id UNIQUE (source, external_id);
