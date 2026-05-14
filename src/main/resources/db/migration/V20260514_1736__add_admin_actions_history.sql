-- Admin actions history table
CREATE TABLE admin_actions_history
(
    id             UUID PRIMARY KEY,
    admin_id       UUID                     NOT NULL,
    action_type    VARCHAR(50)              NOT NULL,
    target_type    VARCHAR(50)              NOT NULL,
    target_id      UUID,
    changed_fields JSONB,
    description    VARCHAR(500),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_admin_actions_history_admin
        FOREIGN KEY (admin_id) REFERENCES users (id)
);

CREATE INDEX idx_admin_actions_history_admin_created
    ON admin_actions_history (admin_id, created_at);

CREATE INDEX idx_admin_actions_history_target
    ON admin_actions_history (target_type, target_id);
