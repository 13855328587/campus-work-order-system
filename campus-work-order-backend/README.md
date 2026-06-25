# 校园智慧工单系统后端

更新日期：2026-06-25

本模块是校园智慧工单系统后端，提供用户认证、权限控制、工单流转、图片上传、用户管理、幂等控制、缓存会话、操作日志和统计接口。

## 1. 技术栈

- Java 17
- Spring Boot 3.3
- Spring Security
- JWT
- MyBatis Plus
- MySQL 8
- Redis
- RabbitMQ / Spring AMQP
- 阿里云 OSS
- Maven

## 2. 核心功能

### 认证与用户

- 登录、注册、验证码校验；
- JWT 鉴权；
- Redis 保存登录会话，支持退出登录后 token 失效；
- 角色权限控制：`SUPER_ADMIN`、`ADMIN`、`WORKER`、`STUDENT`；
- 注册身份可选择学生或维修人员；
- 管理端新增用户，默认密码 `123456`；
- 管理端修改用户信息、角色、状态；
- 管理端单个/批量重置密码；
- 管理端单个/批量逻辑删除用户；
- 用户个人信息修改；
- 用户修改密码；
- 头像上传。

### 工单业务

- 学生创建工单；
- 工单图片上传和查看；
- 我的工单查询；
- 管理员工单审核、驳回、分配；
- 维修人员接单、批量接单、拒绝接单、完成工单；
- 学生或转为维修人员后的原提交人可查看自己的历史工单；
- 学生或维修人员可取消自己提交的待审核/待处理工单；
- 管理员批量审核；
- 学生批量取消；
- 工单类别查询和展示；
- 工单详情、状态、图片、问题描述、处理结果、拒绝原因展示；
- 首页仪表盘统计数据，包含今日新增、状态分布、类别分布和优先级结构。

### 状态机与业务规则

工单主要状态：

| 状态 | 说明 |
|---|---|
| `PENDING_REVIEW` | 待审核 |
| `REJECTED` | 已驳回 |
| `PENDING_PROCESS` | 待处理/待分配：未分配时表示管理员待分配，已分配时表示维修人员待接单 |
| `WORKER_REJECTED` | 维修人员已拒绝 |
| `PROCESSING` | 处理中 |
| `COMPLETED` | 已完成 |
| `CANCELLED` | 已取消 |

关键规则：

- 新建工单默认进入待审核；
- 审核通过后进入待处理；
- 驳回只能从待审核进入已驳回；
- 待处理工单可分配维修人员，已分配但未接单时仍保持待处理状态；
- 维修人员可拒绝待处理工单并填写拒绝原因，工单进入被拒绝状态；
- 维修人员接单后进入处理中；
- 处理中工单可完成；
- 待审核、待处理、被拒绝的本人提交工单可取消；
- 删除、禁用维修人员，或将维修人员改为非维修角色时，其待处理/处理中的工单解除处理人并退回待分配/待处理状态；
- 已完成工单保持历史记录，不因用户角色变更、禁用或删除而修改。

## 3. 数据库初始化

初始化脚本：

```text
src/main/resources/sql/init.sql
```

可在 Navicat、MySQL Workbench 或命令行中执行。脚本包含：

- `sys_user`
- `work_order`
- `idempotency_record`
- `operation_log`
- 索引
- 初始账号
- 示例工单

## 4. 配置说明

主要配置文件：

```text
src/main/resources/application.yml
src/main/resources/application-dev.yml
src/main/resources/application-prod.yml
```

重点配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_work_order
    username: root
    password: 123456
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: campus
    password: campus123

aliyun:
  oss:
    endpoint: ${OSS_ENDPOINT:}
    bucket-name: ${OSS_BUCKET_NAME:}
    access-key-id: ${OSS_ACCESS_KEY_ID:}
    access-key-secret: ${OSS_ACCESS_KEY_SECRET:}
    url-prefix: ${OSS_URL_PREFIX:}
```

注意：不要把真实 OSS 密钥提交到 Git。

## 5. 启动后端

在 IDEA 中运行：

```text
WorkOrderApplication.java
```

或命令行：

```bash
mvn spring-boot:run
```

启动成功后：

```text
http://localhost:8080
```

## 6. Docker Compose 部署

项目根目录提供完整版本 `docker-compose.yml`，会同时启动：

- MySQL 8，并自动执行 `src/main/resources/sql/init.sql`；
- Redis，用于登录会话缓存；
- RabbitMQ，用于工单变更事件；
- Spring Boot 后端；
- Nginx 前端。

使用方式：

```bash
cd ..
cp .env.example .env
docker compose up -d --build
```

如果需要重新初始化数据库示例数据：

```bash
docker compose down -v
docker compose up -d --build
```

## 7. 默认账号

密码均为 `123456`。

| 角色 | 用户名 | 密码 |
|---|---|---|
| 超级管理员 | `superadmin` | `123456` |
| 管理员 | `admin` | `123456` |
| 维修人员 | `worker` | `123456` |
| 学生 | `student` | `123456` |

## 8. 常用接口

### 登录

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456",
  "captchaId": "验证码ID",
  "captchaCode": "验证码"
}
```

### 注册

公开注册只允许学生和维修人员：

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "student01",
  "password": "123456",
  "realName": "张三",
  "phone": "18800000000",
  "role": "STUDENT"
}
```

### 创建工单

```http
POST /api/work-orders
Authorization: Bearer <token>
Idempotency-Key: test-key-001
Content-Type: application/json

{
  "title": "宿舍灯坏了",
  "description": "宿舍灯无法打开",
  "location": "1号楼201",
  "category": "ELECTRIC",
  "priority": "MEDIUM",
  "imageUrls": []
}
```

### 我的工单

```http
GET /api/work-orders/my?pageNum=1&pageSize=10
Authorization: Bearer <token>
```

学生和维修人员均可访问。维修人员访问时展示自己曾经提交的工单。

### 管理端用户新增

```http
POST /api/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "worker02",
  "realName": "李四",
  "phone": "18800000002",
  "role": "WORKER"
}
```

新增用户默认密码为 `123456`。

## 8. 工程化能力

- Maven 构建；
- Dockerfile；
- Docker Compose；
- GitHub Actions CI/CD；
- 初始化 SQL；
- k6 压测脚本；
- Redis 登录会话缓存；
- 幂等记录表；
- 乐观锁版本号；
- 操作日志表；
- 业务状态机校验。
