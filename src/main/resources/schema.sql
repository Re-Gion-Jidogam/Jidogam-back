-- Users table
CREATE TABLE users
(
    id                UUID PRIMARY KEY,
    nickname          VARCHAR(100)             NOT NULL UNIQUE,
    email             VARCHAR(50)              NOT NULL UNIQUE,
    password          TEXT                     NOT NULL,
    exp               BIGINT                   NOT NULL,
    role              VARCHAR(30)              NOT NULL,
    profile_image_url VARCHAR(512)             NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE,
    deleted_at        TIMESTAMP WITH TIME ZONE
);

-- Guidebooks table
CREATE TABLE guidebooks
(
    id                UUID PRIMARY KEY,
    author_id         UUID                     NOT NULL,
    title             VARCHAR(50)              NOT NULL,
    description       VARCHAR(512),
    thumbnail_url     VARCHAR(512),
    map_image_url     VARCHAR(512),
    emoji             VARCHAR(100),
    color             VARCHAR(50),
    is_published      BOOLEAN                  NOT NULL,
    published_date    TIMESTAMP WITH TIME ZONE,
    points            INTEGER                  NOT NULL,
    rating_sum        BIGINT                   NOT NULL,
    rating_count      INTEGER                  NOT NULL,
    participant_count INTEGER                  NOT NULL,
    total_place_count INTEGER                  NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE
);


-- Guidebook participants table
CREATE TABLE guidebook_participations
(
    id               UUID PRIMARY KEY,
    user_id          UUID                     NOT NULL,
    guidebook_id     UUID                     NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    last_activity_at TIMESTAMP WITH TIME ZONE,
    is_completed     BOOLEAN                  NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, guidebook_id)
);

-- Guidebook places table
CREATE TABLE guidebook_places
(
    id           UUID PRIMARY KEY,
    guidebook_id UUID                     NOT NULL,
    place_id     UUID                     NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (guidebook_id, place_id)
);

-- Guidebook reviews table
CREATE TABLE guidebook_reviews
(
    id           UUID PRIMARY KEY,
    guidebook_id UUID                     NOT NULL,
    author_id    UUID                     NOT NULL,
    content      TEXT                     NOT NULL,
    rating       DOUBLE PRECISION         NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE,
    UNIQUE (author_id, guidebook_id)
);

-- Stamps table
CREATE TABLE stamps
(
    id         UUID PRIMARY KEY,
    user_id    UUID                     NOT NULL,
    place_id   UUID                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (user_id, place_id)
);

-- Areas table
CREATE TABLE areas
(
    id           UUID PRIMARY KEY,
    sido         VARCHAR(50)              NOT NULL,
    sigungu      VARCHAR(50)              NOT NULL,
    weight       INTEGER                  NOT NULL,
    sigungu_code VARCHAR(10)              NOT NULL UNIQUE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE
);

-- Places table
CREATE TABLE places
(
    id              UUID PRIMARY KEY,
    kakao_id        VARCHAR(255)             NOT NULL UNIQUE,
    area_id         UUID                     NOT NULL,
    name            VARCHAR(255)             NOT NULL,
    x               DECIMAL(18, 14)          NOT NULL,
    y               DECIMAL(17, 14)          NOT NULL,
    address         VARCHAR(255)             NOT NULL,
    category        VARCHAR(50)              NULL,
    points          INTEGER                  NOT NULL,
    guidebook_count INTEGER                  NOT NULL,
    stamp_count     INTEGER                  NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_place_coordinates ON places (y, x);

-- Refresh token table
CREATE TABLE refresh_tokens
(
    id            UUID PRIMARY KEY,
    user_id       UUID                     NOT NULL,
    refresh_token TEXT                     NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Email auth code table
CREATE TABLE email_auth_codes
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(50)              NOT NULL UNIQUE,
    code       TEXT                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used       BOOLEAN                  NOT NULL DEFAULT FALSE
);

-- Email send failure logs table
CREATE TABLE email_send_failure_logs
(
    id               UUID PRIMARY KEY,
    email            VARCHAR(255)             NOT NULL,
    masked_auth_code VARCHAR(50),
    error_message    VARCHAR(1000),
    retry_count      INT                      NOT NULL DEFAULT 0,
    failed_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Guidebook area ratios table
CREATE TABLE guidebook_area_ratios
(
    id                UUID PRIMARY KEY,
    guidebook_id      UUID                     NOT NULL UNIQUE,
    first_area_id     UUID                     NOT NULL,
    first_area_ratio  DOUBLE PRECISION         NOT NULL,
    second_area_id    UUID                     NULL,
    second_area_ratio DOUBLE PRECISION         NULL,
    third_area_id     UUID                     NULL,
    third_area_ratio  DOUBLE PRECISION         NULL,
    is_primary_area   BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Password reset code table
CREATE TABLE password_reset_tokens
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(50)              NOT NULL UNIQUE,
    token      TEXT                     NOT NULL,
    used       BOOLEAN                  NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Retry failure log
CREATE TABLE retry_failure_log
(
    id                 UUID PRIMARY KEY,
    failure_type       VARCHAR(50)              NOT NULL,
    target_identifier  VARCHAR(500)             NOT NULL,
    context            JSONB,
    error_message      VARCHAR(1000),
    retry_count        INTEGER                  NOT NULL,
    max_retry_attempts INTEGER                  NOT NULL,
    failed_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    last_retry_at      TIMESTAMP WITH TIME ZONE,
    resolved_at        TIMESTAMP WITH TIME ZONE,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITH TIME ZONE
);

CREATE TABLE place_change_histories
(
    id             UUID PRIMARY KEY,
    place_id       UUID                     NOT NULL,
    kakao_id       VARCHAR(255)             NOT NULL,
    changed_fields JSONB                    NOT NULL,
    source         VARCHAR(50)              NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Foreign Key Constraints
ALTER TABLE places
    ADD CONSTRAINT fk_places_area_id
        FOREIGN KEY (area_id) REFERENCES areas (id);

ALTER TABLE guidebook_participations
    ADD CONSTRAINT fk_guidebook_participants_guidebook_id
        FOREIGN KEY (guidebook_id) REFERENCES guidebooks (id);

ALTER TABLE guidebook_participations
    ADD CONSTRAINT fk_guidebook_participants_user_id
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE guidebook_places
    ADD CONSTRAINT fk_guidebook_places_guidebook_id
        FOREIGN KEY (guidebook_id) REFERENCES guidebooks (id);

ALTER TABLE guidebook_places
    ADD CONSTRAINT fk_guidebook_places_place_id
        FOREIGN KEY (place_id) REFERENCES places (id);

ALTER TABLE guidebook_reviews
    ADD CONSTRAINT fk_guidebook_reviews_guidebook_id
        FOREIGN KEY (guidebook_id) REFERENCES guidebooks (id);

ALTER TABLE guidebook_reviews
    ADD CONSTRAINT fk_guidebook_reviews_user_id
        FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE stamps
    ADD CONSTRAINT fk_stamps_place_id
        FOREIGN KEY (place_id) REFERENCES places (id);

ALTER TABLE stamps
    ADD CONSTRAINT fk_stamps_user_id
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_user_id
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE guidebook_area_ratios
    ADD CONSTRAINT fk_guidebook_area_ratios_guidebook
        FOREIGN KEY (guidebook_id) REFERENCES guidebooks (id);

ALTER TABLE guidebook_area_ratios
    ADD CONSTRAINT fk_guidebook_area_ratios_first_area
        FOREIGN KEY (first_area_id) REFERENCES areas (id);

ALTER TABLE guidebook_area_ratios
    ADD CONSTRAINT fk_guidebook_area_ratios_second_area
        FOREIGN KEY (second_area_id) REFERENCES areas (id);

ALTER TABLE guidebook_area_ratios
    ADD CONSTRAINT fk_guidebook_area_ratios_third_area
        FOREIGN KEY (third_area_id) REFERENCES areas (id);