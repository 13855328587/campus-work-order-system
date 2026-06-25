/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : campus_work_order

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 25/06/2026 21:20:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for idempotency_record
-- ----------------------------
DROP TABLE IF EXISTS `idempotency_record`;
CREATE TABLE `idempotency_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `idempotency_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '幂等Key',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '工单ID',
  `request_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求摘要',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_idempotency_key`(`idempotency_key` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '幂等记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of idempotency_record
-- ----------------------------
INSERT INTO `idempotency_record` VALUES (20, '0ef70614-7e0e-4a3c-9118-60e71ec38063', 3, 22, '592429786', '2026-06-25 20:29:55');
INSERT INTO `idempotency_record` VALUES (21, '1e75e1ff-3f5a-4326-acba-c9b720dceed3', 4, 23, '-716740440', '2026-06-25 20:34:16');
INSERT INTO `idempotency_record` VALUES (22, '34ff8600-f7bc-4056-a57e-eeee6d1d574e', 5, 24, '234567790', '2026-06-25 20:38:27');
INSERT INTO `idempotency_record` VALUES (23, '939e9b0c-d336-4cc8-88b8-3e44a7e1cd0e', 6, 25, '207150063', '2026-06-25 20:42:51');
INSERT INTO `idempotency_record` VALUES (24, '4c0691a6-eb8e-4afb-b7b9-a0a5d915f732', 7, 26, '-1133878174', '2026-06-25 20:45:21');
INSERT INTO `idempotency_record` VALUES (25, '8c0256de-4928-47d4-93ce-e7a45c5d57ed', 8, 27, '-218783686', '2026-06-25 20:51:37');
INSERT INTO `idempotency_record` VALUES (26, '92b05154-31c5-468b-952a-0a2bcfc6437e', 3, 28, '-1850419468', '2026-06-25 20:55:53');
INSERT INTO `idempotency_record` VALUES (27, '8cd69619-f55f-4e1e-a73f-07ec89c93c7f', 3, 29, '-486660909', '2026-06-25 20:56:39');
INSERT INTO `idempotency_record` VALUES (28, '5e1162b2-3b9c-49ea-b2ab-9d904a72b298', 3, 30, '358983028', '2026-06-25 20:57:40');
INSERT INTO `idempotency_record` VALUES (29, 'b1d5bd53-725a-4236-84e1-37a387c62465', 3, 31, '-1401988417', '2026-06-25 20:58:03');
INSERT INTO `idempotency_record` VALUES (30, 'e5839f4d-2508-4582-85f9-9cf4513cf1c5', 3, 32, '392250663', '2026-06-25 20:58:43');

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '操作人ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '工单ID',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `before_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作前状态',
  `after_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作后状态',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 93 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_log
-- ----------------------------
INSERT INTO `operation_log` VALUES (65, 3, 22, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:29:55');
INSERT INTO `operation_log` VALUES (66, 4, 23, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:34:16');
INSERT INTO `operation_log` VALUES (67, 5, 24, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:38:27');
INSERT INTO `operation_log` VALUES (68, 6, 25, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:42:51');
INSERT INTO `operation_log` VALUES (69, 7, 26, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:45:21');
INSERT INTO `operation_log` VALUES (70, 8, 27, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:51:37');
INSERT INTO `operation_log` VALUES (71, 3, 28, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:55:53');
INSERT INTO `operation_log` VALUES (72, 3, 29, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:56:39');
INSERT INTO `operation_log` VALUES (73, 3, 30, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:57:40');
INSERT INTO `operation_log` VALUES (74, 3, 31, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:58:03');
INSERT INTO `operation_log` VALUES (75, 3, 32, 'CREATE', NULL, 'PENDING_REVIEW', '创建工单', '2026-06-25 20:58:43');
INSERT INTO `operation_log` VALUES (76, 1, 32, 'APPROVE', 'PENDING_REVIEW', 'PENDING_PROCESS', '审核通过', '2026-06-25 20:59:50');
INSERT INTO `operation_log` VALUES (77, 1, 32, 'ASSIGN', 'PENDING_PROCESS', 'PENDING_PROCESS', '分配维修人员：2', '2026-06-25 20:59:58');
INSERT INTO `operation_log` VALUES (78, 1, 31, 'APPROVE', 'PENDING_REVIEW', 'PENDING_PROCESS', '审核通过', '2026-06-25 21:00:00');
INSERT INTO `operation_log` VALUES (79, 1, 30, 'APPROVE', 'PENDING_REVIEW', 'PENDING_PROCESS', '审核通过', '2026-06-25 21:00:03');
INSERT INTO `operation_log` VALUES (80, 1, 29, 'APPROVE', 'PENDING_REVIEW', 'PENDING_PROCESS', '审核通过', '2026-06-25 21:00:05');
INSERT INTO `operation_log` VALUES (81, 1, 30, 'ASSIGN', 'PENDING_PROCESS', 'PENDING_PROCESS', '分配维修人员：9', '2026-06-25 21:00:10');
INSERT INTO `operation_log` VALUES (82, 1, 28, 'REJECT', 'PENDING_REVIEW', 'REJECTED', '没问题', '2026-06-25 21:00:25');
INSERT INTO `operation_log` VALUES (83, 1, 27, 'REJECT', 'PENDING_REVIEW', 'REJECTED', '没问题', '2026-06-25 21:00:27');
INSERT INTO `operation_log` VALUES (84, 1, 26, 'APPROVE', 'PENDING_REVIEW', 'PENDING_PROCESS', '审核通过', '2026-06-25 21:00:59');
INSERT INTO `operation_log` VALUES (85, 1, 25, 'APPROVE', 'PENDING_REVIEW', 'PENDING_PROCESS', '审核通过', '2026-06-25 21:01:03');
INSERT INTO `operation_log` VALUES (86, 1, 31, 'ASSIGN', 'PENDING_PROCESS', 'PENDING_PROCESS', '分配维修人员：10', '2026-06-25 21:01:09');
INSERT INTO `operation_log` VALUES (87, 1, 29, 'ASSIGN', 'PENDING_PROCESS', 'PENDING_PROCESS', '分配维修人员：11', '2026-06-25 21:01:12');
INSERT INTO `operation_log` VALUES (88, 2, 32, 'ACCEPT', 'PENDING_PROCESS', 'PROCESSING', '维修人员接单', '2026-06-25 21:01:34');
INSERT INTO `operation_log` VALUES (89, 2, 26, 'ACCEPT', 'PENDING_PROCESS', 'PROCESSING', '维修人员接单', '2026-06-25 21:01:43');
INSERT INTO `operation_log` VALUES (90, 2, 32, 'FINISH', 'PROCESSING', 'COMPLETED', '已完成', '2026-06-25 21:02:38');
INSERT INTO `operation_log` VALUES (91, 2, 25, 'WORKER_REJECT', 'PENDING_PROCESS', 'WORKER_REJECTED', '没问题', '2026-06-25 21:05:53');
INSERT INTO `operation_log` VALUES (92, 1, 24, 'APPROVE', 'PENDING_REVIEW', 'PENDING_PROCESS', '审核通过', '2026-06-25 21:06:22');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `role` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色：STUDENT/ADMIN/WORKER',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像地址',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_deleted_role_status`(`deleted` ASC, `role` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$zkgplrBk9cr7.vBF6c3TqeUQlnLXQi1zA9S.5p5WvgYJtDdHh861C', '胡雪飞', '13855328587', 'SUPER_ADMIN', 1, '2026-06-23 18:17:52', '2026-06-25 18:49:30', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/1f0f5540-0b7d-4c86-9546-d9f270a47801.png', 0);
INSERT INTO `sys_user` VALUES (2, 'csl', '$2a$10$YxKmEV2aToD8XyEwbB.QkeyyQpnnWMRYiFAJs65naYIxVdOrARd/y', '陈帅龙', '18755338718', 'WORKER', 1, '2026-06-23 18:17:52', '2026-06-25 18:50:22', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/f3aa1c9f-5d39-466b-8758-1f1e51976443.png', 0);
INSERT INTO `sys_user` VALUES (3, 'zsl', '$2a$10$YxKmEV2aToD8XyEwbB.QkeyyQpnnWMRYiFAJs65naYIxVdOrARd/y', '邾少林', '13855333918', 'STUDENT', 1, '2026-06-23 18:17:52', '2026-06-25 18:50:57', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/3536327c-8679-4827-96ab-f342977c44f5.png', 0);
INSERT INTO `sys_user` VALUES (4, 'dnn', '$2a$10$YxKmEV2aToD8XyEwbB.QkeyyQpnnWMRYiFAJs65naYIxVdOrARd/y', '董宁宁', '13637136298', 'STUDENT', 1, '2026-06-23 18:26:40', '2026-06-25 18:51:50', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/4652937b-5731-4314-9158-5d571a3542ab.png', 0);
INSERT INTO `sys_user` VALUES (5, 'wq', '$2a$10$YxKmEV2aToD8XyEwbB.QkeyyQpnnWMRYiFAJs65naYIxVdOrARd/y', '王勤', '18855321518', 'STUDENT', 1, '2026-06-24 14:23:08', '2026-06-25 18:56:10', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/1152014a-966e-4438-9d49-d23843b01d4d.png', 0);
INSERT INTO `sys_user` VALUES (6, 'wjw', '$2a$10$IuuEttGijKFzH5DsS8O2rOv/zy3On29HB8C6bFAqtt89XoQMaZxoi', '汪健伟', '15178099725', 'STUDENT', 1, '2026-06-24 14:23:37', '2026-06-25 18:59:04', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/2b3f3964-7b2e-4442-8902-07c760abaf33.png', 0);
INSERT INTO `sys_user` VALUES (7, 'zyy', '$2a$10$3lGUu5imDIrrChKb27hHYeFyN2gpL3UHeX3ua3WmG1DKrFMZML3Ie', '周莹莹', '13855328586', 'STUDENT', 1, '2026-06-25 17:29:57', '2026-06-25 18:59:33', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/a9cb9980-68c0-4357-ba79-9786df4fc6f2.png', 0);
INSERT INTO `sys_user` VALUES (8, 'hhd', '$2a$10$r7QQr8JF.oCfenfhmV9Vxufh.txJcDlRYVoToS4r8PYf592sufJCi', '韩华冬', '13855328589', 'STUDENT', 1, '2026-06-25 17:36:32', '2026-06-25 18:59:58', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/f8029f67-4416-48d5-a28c-386fa9e6ef37.png', 0);
INSERT INTO `sys_user` VALUES (9, 'pjh', '$2a$10$CgOTMJNo4wrBFMYmVSuBJeRc7V50m6XNjBLaCw41u4G9E/fZRPty2', '彭俊弘', '13637136294', 'WORKER', 1, '2026-06-25 17:37:11', '2026-06-25 19:00:59', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/40de2e8d-41a0-4ebe-b175-1c9c88ec01c4.png', 0);
INSERT INTO `sys_user` VALUES (10, 'bjj', '$2a$10$xZf9kwEKiy79oI8gbzpn7.hO4DzO2YsfmBPdoSpZT48eULu7ZDZ1i', '卞俊杰', '15178099746', 'WORKER', 1, '2026-06-25 17:40:13', '2026-06-25 19:01:24', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/74422109-baaf-444b-be10-b6477c67c57b.png', 0);
INSERT INTO `sys_user` VALUES (11, 'cjj', '$2a$10$40M7z0zrrxk5LAqLf1kBR.XomJ3BM7qmvQruGK6x2QELMNYjMzIXO', '曹杰', '15178099726', 'WORKER', 1, '2026-06-25 17:41:02', '2026-06-25 19:47:52', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/f369606c-12c1-4066-a4bb-def3e1f716cb.png', 0);
INSERT INTO `sys_user` VALUES (12, 'ldn', '$2a$10$HCWQIm9/oDcWH/RvmGG2WOhUUxtDgGhpDSW06/3SD3roGSP8D9oh2', '刘丹妮', '13637135425', 'WORKER', 1, '2026-06-25 17:41:32', '2026-06-25 19:49:46', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/b4177ae0-7956-4272-9846-3d8901839ea4.png', 0);
INSERT INTO `sys_user` VALUES (13, 'wxx', '$2a$10$jEfIFfzAgsLMJLMLNdgWYeRAE4af7LysIq1VNcecFW7ElmVm.ebVy', '王璇', '13855328587', 'ADMIN', 1, '2026-06-25 17:41:58', '2026-06-25 19:50:20', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/22c16d2d-0e13-4f00-acde-550c6b48155e.png', 0);
INSERT INTO `sys_user` VALUES (14, 'tjj', '$2a$10$aNScvl6WmsjmW2YtKnt4oOdS4WOoUa4Fi8MFq15HQy1v6MBtsaYbO', '汤鉴', '13637136258', 'ADMIN', 1, '2026-06-25 17:42:37', '2026-06-25 19:50:51', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/3006ebdd-1d98-4478-a244-757dbe6e500f.png', 0);
INSERT INTO `sys_user` VALUES (15, 'hyc', '$2a$10$lkOmZVSlAG2xo1zQxTbeauTwvcfBPzDTCrseenB2hC4qmrBuTmjPK', '胡雨辰', '15178099456', 'ADMIN', 1, '2026-06-25 17:42:55', '2026-06-25 19:51:20', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/dcb0ec8b-ff48-4f44-873e-dd0ce9f4c688.png', 0);
INSERT INTO `sys_user` VALUES (16, 'zyss', '$2a$10$g2FufktVlXbas4gX0Bcf8uB1q0uj2q7SNCNWCyk0Si3QqgLxp3lJS', '朱毅', '13637134565', 'ADMIN', 1, '2026-06-25 17:43:13', '2026-06-25 19:54:00', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/705bcfa0-b3b6-40c2-8b8d-f01251cd342c.png', 0);
INSERT INTO `sys_user` VALUES (17, 'jxx', '$2a$10$QTMGKsb48XEhsN2xbcbpa.Cs9/LjNTxQm6eGMjlE917.qBffIb7Ii', '金雪', '13637135468', 'WORKER', 1, '2026-06-25 17:43:42', '2026-06-25 17:45:28', NULL, 0);
INSERT INTO `sys_user` VALUES (18, 'zgt', '$2a$10$XyV/AiGr6l6O0.ztkHw8GeMasoaEkEuzoaO7MarYRhpB1sLd.nwuy', '张桂婷', '14554687829', 'ADMIN', 1, '2026-06-25 17:44:09', '2026-06-25 19:54:27', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/0d4d9687-eec9-4250-9e46-f64fce235855.png', 0);
INSERT INTO `sys_user` VALUES (19, 'yml', '$2a$10$2VpAG/KVQWUbxoeilBDx2Ogt3H0vvhKRGv.luuwlaE2gGXZpZz0m.', '杨梦林', '13638549546', 'ADMIN', 1, '2026-06-25 17:45:22', '2026-06-25 19:55:07', 'https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/uploads/2026-06-25/9a9c1f9b-1b61-4e7f-b822-0ff552916f0e.png', 0);

-- ----------------------------
-- Table structure for work_order
-- ----------------------------
DROP TABLE IF EXISTS `work_order`;
CREATE TABLE `work_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工单编号',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '维修人员ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '描述',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地点',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优先级',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '驳回原因',
  `finish_result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理结果',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '工单图片OSS地址JSON数组',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_handler_id`(`handler_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_order
-- ----------------------------
INSERT INTO `work_order` VALUES (22, 'WO20260625594761', 3, NULL, '宿舍插座异常，存在用电安全隐患', '宿舍墙面插座出现异常，插电后设备无法正常充电，插座附近有轻微焦味或接触不良现象，存在一定用电安全隐患。目前影响手机、电脑等设备正常充电，请求维修人员到现场检查插座线路并进行处理。', '花津校区学生宿舍 3 号楼 502', 'ELECTRIC', 'LOW', 'PENDING_REVIEW', NULL, NULL, '[\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/32989941-297a-41e5-bd8a-22ebd2ca25d3.png\"]', 0, '2026-06-25 20:29:55', '2026-06-25 20:31:02');
INSERT INTO `work_order` VALUES (23, 'WO20260625856143', 4, NULL, '宿舍网口无网络信号，无法正常上网学习', '宿舍墙面网口插入网线后，电脑无法连接网络，更换网线和设备测试后仍然没有信号。目前网络完全中断，影响正常上网学习、提交作业和查阅资料，请求尽快排查线路故障。', '花津校区学生宿舍 2 号楼 202', 'NETWORK', 'MEDIUM', 'PENDING_REVIEW', NULL, NULL, '[\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/4a920e0b-364f-4bb8-93a1-3097dd404388.png\",\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/a83dc59b-0a32-42dd-b54c-dd2345523d9f.png\"]', 0, '2026-06-25 20:34:16', '2026-06-25 20:34:16');
INSERT INTO `work_order` VALUES (24, 'WO20260625106806', 5, NULL, '宿舍座椅损坏，靠背松动无法正常使用', '宿舍学习座椅出现松动，靠背倾斜，椅腿连接处螺丝脱落或不稳，坐下时有明显晃动，存在安全隐患，无法正常用于学习。已拍摄三张现场图片，分别展示座椅整体状态、松动细节及损坏位置，请维修人员到现场检查并进行加固或更换处理。', '花津校区学生宿舍 3 号楼 403', 'FURNITURE', 'HIGH', 'PENDING_PROCESS', NULL, NULL, '[\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/45c902c2-7970-47ea-8fa2-c0d15cc45910.png\",\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/dc6fc269-bfdd-4044-b14f-b340f46b84fb.png\",\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/b0c87fe8-9ff2-4843-b19e-e7260313255c.png\"]', 1, '2026-06-25 20:38:27', '2026-06-25 20:38:27');
INSERT INTO `work_order` VALUES (25, 'WO20260625371384', 6, 2, '宿舍门锁故障', '宿舍门锁出现松动，门把手下垂，锁舌与门框错位，开关门时不顺畅，无法正常锁门，存在一定安全隐患。已拍摄四张现场图片，分别展示门锁整体状态、把手松动细节、锁舌错位情况和门口现场环境，请维修人员尽快检查并加固维修。', '花津校区学生宿舍 1 号楼 403', 'OTHER', 'URGENT', 'WORKER_REJECTED', '没问题', NULL, '[\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/30cb49d4-015a-434c-9b23-31bb28403a83.png\",\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/e1012eb6-cb8d-48db-9ee0-865362000c40.png\",\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/85fa7455-8dd7-4eb3-b37d-ae6679e46df3.png\",\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/c1aaeb8b-6d0d-4984-9d4d-ad8bc9e6afca.png\"]', 2, '2026-06-25 20:42:51', '2026-06-25 20:42:51');
INSERT INTO `work_order` VALUES (26, 'WO20260625520764', 7, 2, '宿舍座椅损坏', '宿舍学习座椅出现明显损坏，靠背松动后倾，椅腿连接处螺丝松动，坐垫边缘有磨损和塌陷情况，坐下时不稳定，存在安全隐患，无法正常用于学习。已拍摄五张现场图片，分别展示座椅整体状态、螺丝松动、靠背连接部位、坐垫损坏及现场摆放情况，请维修人员到现场检查并进行加固或更换处理。', '花津校区学生宿舍 3 号楼 403', 'FURNITURE', 'MEDIUM', 'PROCESSING', NULL, NULL, '[\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/cdeba010-8f72-4f3a-b065-e6636d05cfcd.png\",\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/9dd16b72-318e-45e0-a5ab-4eed86ecce9b.png\",\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/98401a3c-5c09-431e-b624-aaa0ef8c62c8.png\",\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/0386b5c7-d769-41f0-ae94-c3faeff76e5b.png\",\"https://campus-work-order-xuefei.oss-cn-beijing.aliyuncs.com/work-orders/2026-06-25/8b9a664b-6f44-4dbd-bb44-88eea530a6c3.png\"]', 2, '2026-06-25 20:45:21', '2026-06-25 20:45:21');
INSERT INTO `work_order` VALUES (27, 'WO20260625897374', 8, NULL, '花津校区 2 号楼 608 宿舍室内顶灯开关失灵通电无亮光，夜间无法照明影响日常学习休息申请上门检修', '宿舍顶灯开关按下无任何反应，灯具完全不发光，夜晚室内无照明，严重影响日常学习、起居，麻烦维修人员前来排查线路与灯具故障。', '花津校区学生宿舍 2 号楼 608', 'ELECTRIC', 'MEDIUM', 'REJECTED', '没问题', NULL, NULL, 1, '2026-06-25 20:51:37', '2026-06-25 20:51:37');
INSERT INTO `work_order` VALUES (28, 'WO20260625152843', 3, NULL, '宿舍书桌墙面网口损坏，完全无法连接有线网络', '本宿舍书桌墙面的有线网络网口出现故障已有三天时间，最开始只是偶尔断连，插拔网线后能够短暂恢复网络，昨天开始彻底无法识别网线，更换三根不同完好网线、两台笔记本电脑分别测试后，网口依旧无任何网络信号，电脑端始终提示未接入以太网，排除网线、电脑设备自身故障问题。\n宿舍日常需要依靠有线网络完成课程论文撰写、线上课程学习、专业文献下载、科研资料查阅以及线上作业提交，当前校园无线网络信号在宿舍区域稳定性较差，高峰期网速卡顿严重，视频会议、大文件传输完全无法正常进行，仅靠无线网已经严重阻碍正常学习进度。\n目前临近课程作业、论文提交截止时间，频繁的网络卡顿与完全失效的有线网口给日常学习带来极大困扰，无法稳定开展线上学习相关工作。我自行检查了网口外观，网口内部弹片存在松动、轻微变形的情况，没有私自拆卸线路，担心线路短路引发安全隐患，不敢自行处理。\n现申请维修工作人员上门对墙面网口、楼道分线线路进行全面排查检修，修复损坏网口，恢复宿舍有线网络正常使用，保障我们能够稳定完成各项学习任务，麻烦相关工作人员尽快安排上门处理。', '花津校区学生宿舍 2 号楼 608', 'NETWORK', 'MEDIUM', 'REJECTED', '没问题', NULL, NULL, 1, '2026-06-25 20:55:53', '2026-06-25 20:55:53');
INSERT INTO `work_order` VALUES (29, 'WO20260625198903', 3, 11, '花津校区宿舍卫生间水龙头漏水，持续渗水损耗水资源', '安徽师范大学花津校区学生公寓 2 号宿舍楼六层 608 寝室，进门左手边独立卫生间洗漱台区域，室内靠墙安装的冷水水龙头位置，楼栋位于校区南侧生活区，临近第二食堂，宿舍楼正门进入直走第二个楼梯间上楼即为本宿舍房间。', '宿舍卫生间冷水龙头阀芯损坏，关闭阀门后依旧持续滴水，地面长期积水潮湿，容易打滑存在摔倒风险，也浪费大量水资源，多次尝试拧紧开关无改善，请求上门更换水龙头配件。', 'ELECTRIC', 'MEDIUM', 'PENDING_PROCESS', NULL, NULL, NULL, 2, '2026-06-25 20:56:39', '2026-06-25 20:56:39');
INSERT INTO `work_order` VALUES (30, 'WO20260625260172', 3, 9, '宿舍书桌抽屉滑轨断裂，储物无法正常开合使用', '安徽师范大学花津校区学生公寓 4 号宿舍楼五层 512 寝室，进门右侧靠墙学习书桌区域，书桌下方左侧储物抽屉位置，楼栋位于校园西南生活区，靠近图书馆，从宿舍楼北门进入走东侧楼梯直达五层对应宿舍。', '书桌抽屉滑轨完全断裂脱落，推拉卡顿卡死，无法正常存放书本、生活用品，收纳功能完全失效，日常学习物品无处规整，影响宿舍正常起居，麻烦维修人员上门更换滑轨。', 'FURNITURE', 'MEDIUM', 'PENDING_PROCESS', NULL, NULL, NULL, 2, '2026-06-25 20:57:40', '2026-06-25 20:57:40');
INSERT INTO `work_order` VALUES (31, 'WO20260625282501', 3, 10, '教室墙面网口无网络，线上授课无法正常开展', '安徽师范大学花津校区博文楼三楼 306 公共教学教室，教室后排靠窗第二张课桌下方墙面网线接口处，教学楼位于校园教学区中部，正门进入三楼东侧走廊尽头即为该授课教室。', '教室有线网口插入网线无网络信号，更换网线、电脑测试均无效，近期课程需多媒体线上教学、投屏课件，断网严重阻碍课堂授课进度，恳请尽快检修线路故障。', 'NETWORK', 'MEDIUM', 'PENDING_PROCESS', NULL, NULL, NULL, 2, '2026-06-25 20:58:03', '2026-06-25 20:58:03');
INSERT INTO `work_order` VALUES (32, 'WO20260625323223', 3, 2, '宿舍入户门把手松动下垂，锁合不畅存在安全隐患', '安徽师范大学花津校区学生公寓 1 号宿舍楼四层 407 寝室，宿舍入户正门金属门把手位置，楼栋坐落于校区北侧生活区，紧邻大操场，从宿舍楼南门进入西侧步梯上四层，走廊中段房间即为报修宿舍。', '房门把手内部螺丝脱落，按压时大幅晃动下垂，关门后锁舌无法完全咬合，夜间休息无法稳妥锁门，财产安全无法保障，自行紧固螺丝无效果，申请维修人员上门加固或更换门锁配件。', 'OTHER', 'MEDIUM', 'COMPLETED', NULL, '已完成', NULL, 4, '2026-06-25 20:58:43', '2026-06-25 20:58:43');

SET FOREIGN_KEY_CHECKS = 1;
