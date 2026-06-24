CREATE DATABASE IF NOT EXISTS campus_work_order
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_work_order;

DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS idempotency_record;
DROP TABLE IF EXISTS work_order;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '加密密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) NULL COMMENT '手机号',
    avatar_url VARCHAR(500) NULL COMMENT '头像OSS地址',
    role VARCHAR(30) NOT NULL COMMENT '角色：STUDENT/ADMIN/WORKER',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='用户表';

CREATE TABLE work_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '工单编号',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    handler_id BIGINT NULL COMMENT '维修人员ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    description TEXT NOT NULL COMMENT '描述',
    location VARCHAR(100) NOT NULL COMMENT '地点',
    category VARCHAR(50) NOT NULL COMMENT '类型',
    priority VARCHAR(20) NOT NULL COMMENT '优先级',
    status VARCHAR(30) NOT NULL COMMENT '状态',
    reject_reason VARCHAR(255) NULL COMMENT '驳回原因',
    finish_result VARCHAR(255) NULL COMMENT '处理结果',
    image_urls TEXT NULL COMMENT '工单图片OSS地址JSON数组',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_creator_id (creator_id),
    INDEX idx_handler_id (handler_id),
    INDEX idx_status (status)
) COMMENT='工单表';

CREATE TABLE idempotency_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    idempotency_key VARCHAR(100) NOT NULL COMMENT '幂等Key',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT NULL COMMENT '工单ID',
    request_hash VARCHAR(100) NULL COMMENT '请求摘要',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_idempotency_key (idempotency_key),
    INDEX idx_user_id (user_id)
) COMMENT='幂等记录表';

CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '操作人ID',
    order_id BIGINT NULL COMMENT '工单ID',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    before_status VARCHAR(30) NULL COMMENT '操作前状态',
    after_status VARCHAR(30) NULL COMMENT '操作后状态',
    remark TEXT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) COMMENT='操作日志表';

-- 初始密码均为 123456，BCrypt 加密后保存
INSERT INTO sys_user (username, password, real_name, phone, role, status)
VALUES
('admin', '$2a$10$dXJ3SW6G7P50lGmUkg5QpeFvYhhTkm1d6hGhjRHb30xQoK8WGqWnK', '系统管理员', '18800000001', 'ADMIN', 1),
('worker', '$2a$10$dXJ3SW6G7P50lGmUkg5QpeFvYhhTkm1d6hGhjRHb30xQoK8WGqWnK', '维修人员', '18800000002', 'WORKER', 1),
('student', '$2a$10$dXJ3SW6G7P50lGmUkg5QpeFvYhhTkm1d6hGhjRHb30xQoK8WGqWnK', '学生用户', '18800000003', 'STUDENT', 1);

INSERT INTO work_order (order_no, creator_id, handler_id, title, description, location, category, priority, status)
VALUES
('WO202606230001', 3, 2, '宿舍空调无法启动', '宿舍空调开机无反应，需要维修。', '3号宿舍楼502', 'ELECTRIC', 'MEDIUM', 'PENDING_PROCESS'),
('WO202606230002', 3, NULL, '校园网无法连接', '宿舍网络无法连接，影响学习。', '5号宿舍楼301', 'NETWORK', 'HIGH', 'PENDING_REVIEW');
