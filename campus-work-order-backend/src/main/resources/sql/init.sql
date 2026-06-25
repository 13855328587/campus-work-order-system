SET NAMES utf8mb4;

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
    username VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名：3-20位字母、数字或下划线',
    password VARCHAR(100) NOT NULL COMMENT '加密密码',
    real_name VARCHAR(20) NOT NULL COMMENT '真实姓名：2-20位中文、字母或间隔点',
    phone VARCHAR(11) NULL COMMENT '手机号',
    avatar_url VARCHAR(500) NULL COMMENT '头像OSS地址',
    role VARCHAR(30) NOT NULL COMMENT '角色：SUPER_ADMIN/ADMIN/WORKER/STUDENT',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_deleted_role_status (deleted, role, status)
) COMMENT='用户表';

CREATE TABLE work_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '工单编号',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    handler_id BIGINT NULL COMMENT '维修人员ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    description TEXT NOT NULL COMMENT '描述',
    location VARCHAR(100) NOT NULL COMMENT '地点',
    category VARCHAR(50) NOT NULL COMMENT '类别：ELECTRIC/NETWORK/FURNITURE/OTHER',
    priority VARCHAR(20) NOT NULL COMMENT '优先级',
    status VARCHAR(30) NOT NULL COMMENT '状态：PENDING_REVIEW/REJECTED/PENDING_PROCESS/WORKER_REJECTED/PROCESSING/COMPLETED/CANCELLED',
    reject_reason VARCHAR(255) NULL COMMENT '驳回原因或维修人员拒绝原因',
    finish_result VARCHAR(255) NULL COMMENT '处理结果',
    image_urls TEXT NULL COMMENT '工单图片OSS地址JSON数组',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_creator_id (creator_id),
    INDEX idx_handler_id (handler_id),
    INDEX idx_status (status),
    INDEX idx_category_status (category, status),
    INDEX idx_status_created_at (status, created_at),
    INDEX idx_creator_status_created_at (creator_id, status, created_at),
    INDEX idx_handler_status_created_at (handler_id, status, created_at)
) COMMENT='工单表';

CREATE TABLE idempotency_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    idempotency_key VARCHAR(100) NOT NULL COMMENT '幂等Key',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT NULL COMMENT '工单ID',
    request_hash VARCHAR(100) NULL COMMENT '请求摘要',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_idempotency_key (user_id, idempotency_key),
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
('admin', '$2a$10$xZPPxpf1LIdTqzauT/PryOlKt/z2Ct99XcQuLsCfZpYWspoVHrfHm', '系统管理员', '18800000001', 'ADMIN', 1),
('worker', '$2a$10$xZPPxpf1LIdTqzauT/PryOlKt/z2Ct99XcQuLsCfZpYWspoVHrfHm', '维修人员', '18800000002', 'WORKER', 1),
('student', '$2a$10$xZPPxpf1LIdTqzauT/PryOlKt/z2Ct99XcQuLsCfZpYWspoVHrfHm', '学生用户', '18800000003', 'STUDENT', 1),
('superadmin', '$2a$10$xZPPxpf1LIdTqzauT/PryOlKt/z2Ct99XcQuLsCfZpYWspoVHrfHm', '超级管理员', '18800000004', 'SUPER_ADMIN', 1);

INSERT INTO work_order
(order_no, creator_id, handler_id, title, description, location, category, priority, status, reject_reason, finish_result, created_at, updated_at)
VALUES
('WO202606250001', 3, NULL, '校园网无法连接', '宿舍网络无法连接，影响学习。', '5号宿舍楼301', 'NETWORK', 'HIGH', 'PENDING_REVIEW', NULL, NULL, NOW(), NOW()),
('WO202606250002', 3, NULL, '教学楼投影无法开机', '教学楼投影仪无法开机，影响上课。', '教学楼A区203', 'ELECTRIC', 'MEDIUM', 'PENDING_PROCESS', NULL, NULL, NOW(), NOW()),
('WO202606250003', 3, 2, '宿舍空调无法启动', '宿舍空调开机无反应，需要维修。', '3号宿舍楼502', 'ELECTRIC', 'MEDIUM', 'PENDING_PROCESS', NULL, NULL, NOW(), NOW()),
('WO202606240001', 3, 2, '水龙头漏水', '洗手池水龙头持续漏水。', '实验楼1楼卫生间', 'FURNITURE', 'LOW', 'PROCESSING', NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('WO202606230001', 3, 2, '宿舍门锁松动', '宿舍门锁松动，存在安全隐患。', '2号宿舍楼408', 'FURNITURE', 'HIGH', 'COMPLETED', NULL, '已更换门锁并完成测试。', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('WO202606220001', 3, 2, '网络端口损坏', '墙面网络端口损坏，无法插入网线。', '图书馆三楼自习区', 'NETWORK', 'MEDIUM', 'WORKER_REJECTED', '现场需要网络中心专人处理，维修人员无法单独处理。', NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('WO202606210001', 3, NULL, '椅子损坏', '自习室椅子靠背断裂。', '教学楼B区105', 'FURNITURE', 'LOW', 'CANCELLED', NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY));
