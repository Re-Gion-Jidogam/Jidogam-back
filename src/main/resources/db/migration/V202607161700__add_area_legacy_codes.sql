-- 지역 코드가 재배정된 경우, 예전 코드로도 현재 Area를 찾을 수 있도록 매핑 테이블 추가
CREATE TABLE area_legacy_codes
(
    id          UUID PRIMARY KEY,
    legacy_code VARCHAR(10)              NOT NULL UNIQUE,
    legacy_name VARCHAR(50),
    area_id     UUID                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE area_legacy_codes
    ADD CONSTRAINT fk_area_legacy_codes_area_id
        FOREIGN KEY (area_id) REFERENCES areas (id);
