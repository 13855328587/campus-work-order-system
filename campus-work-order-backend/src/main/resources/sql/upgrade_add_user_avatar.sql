-- Existing databases only: execute once before deploying avatar support.
ALTER TABLE sys_user
    ADD COLUMN avatar_url VARCHAR(500) NULL COMMENT '头像OSS地址' AFTER phone;
