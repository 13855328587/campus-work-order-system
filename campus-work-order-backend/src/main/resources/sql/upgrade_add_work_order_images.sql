-- Existing databases only: execute once before deploying this version.
ALTER TABLE work_order
    ADD COLUMN image_urls TEXT NULL COMMENT '工单图片OSS地址JSON数组' AFTER finish_result;
