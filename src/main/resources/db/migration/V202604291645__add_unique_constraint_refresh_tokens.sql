ALTER TABLE refresh_tokens
    ADD CONSTRAINT uk_refresh_tokens_user_id UNIQUE (user_id);