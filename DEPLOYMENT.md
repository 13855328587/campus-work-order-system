# 校园智慧工单系统部署说明

更新日期：2026-06-25

本文档说明本项目在本地和 Docker Compose 环境下的构建、运行、部署与 CI/CD 使用方式。

## 1. 项目组成

| 模块 | 路径 | 说明 |
|---|---|---|
| 后端 | `campus-work-order-backend` | Spring Boot 3、Spring Security、MyBatis Plus、MySQL、Redis、RabbitMQ、OSS |
| 前端 | `campus-work-order-frontend/campus-work-order-frontend` | Vue 3、Vite、Element Plus、Pinia |
| 初始化 SQL | `campus-work-order-backend/src/main/resources/sql/init.sql` | 数据表、索引、初始账号、示例数据 |
| 消息队列 | `rabbitmq` | 工单变更事件发布与消费 |
| 压测脚本 | `performance` | k6 工单查询压测 |
| 部署脚本 | `scripts/deploy.sh` | 服务器 Docker Compose 部署脚本 |
| CI/CD | `.github/workflows/ci.yml` | 构建、测试、Docker 校验、可选部署 |

## 2. 环境要求

本地开发：

- JDK 17
- Maven 4 或 Maven 3.9+
- Node.js 18+
- MySQL 8
- Redis

容器运行：

- Docker
- Docker Compose

可选：

- k6 或 Docker 版 k6，用于性能测试
- 阿里云 OSS，用于头像和工单图片上传

## 3. 环境变量

复制环境变量模板：

```bash
cp .env.example .env
```

`.env` 用于 Docker Compose。至少需要确认：

```text
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=campus_work_order
JWT_SECRET=
REDIS_DATABASE=0
RABBITMQ_USERNAME=
RABBITMQ_PASSWORD=
OSS_ENDPOINT=
OSS_BUCKET_NAME=
OSS_ACCESS_KEY_ID=
OSS_ACCESS_KEY_SECRET=
OSS_URL_PREFIX=
```

注意：

- `.env` 不要提交到 Git。
- OSS 的 `access-key-id` 和 `access-key-secret` 不要写进公开仓库。
- `OSS_BUCKET_NAME` 应填写 Bucket 名称，不是 AccessKey。
- `OSS_URL_PREFIX` 通常填写可公网访问的 Bucket 域名，例如 `https://你的bucket.oss-cn-xxx.aliyuncs.com`，如果绑定了自定义域名则填自定义域名。

## 4. Docker Compose 启动

在项目根目录执行：

```bash
docker compose up -d --build
```

启动后访问：

- 前端：http://localhost
- 后端：http://localhost:8080
- MySQL：仅 Compose 内部网络使用
- Redis：仅 Compose 内部网络使用
- RabbitMQ：`localhost:5672`
- RabbitMQ 管理台：http://localhost:15672

首次创建 MySQL 数据卷时会自动执行：

```text
campus-work-order-backend/src/main/resources/sql/init.sql
```

已有数据卷不会重复初始化。如需重新初始化本地容器数据：

```bash
docker compose down -v
docker compose up -d --build
```

当前初始化 SQL 已覆盖完整版本核心流程数据，包括待审核、待分配、已分配待处理、处理中、已完成、被拒绝和已取消工单。

## 5. 本地开发启动

后端：

```bash
cd campus-work-order-backend
mvn spring-boot:run
```

前端：

```bash
cd campus-work-order-frontend/campus-work-order-frontend
npm install
npm run dev
```

开发访问：

- 前端：http://localhost:5173
- 后端：http://localhost:8080

## 6. 常用运维命令

```bash
# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f backend frontend

# 停止服务，保留数据库数据
docker compose down

# 停止服务，并删除 MySQL/Redis 数据卷
docker compose down -v

# 重新构建并启动
docker compose up -d --build
```

## 7. CI/CD 流程

GitHub Actions 工作流位于：

```text
.github/workflows/ci.yml
```

推送到 `main` / `master` 或提交 Pull Request 时会自动执行：

1. Maven 测试并打包后端；
2. npm 安装依赖并构建前端；
3. 校验 Docker Compose 配置；
4. 构建前后端容器镜像；
5. 上传后端 JAR 和前端 `dist` 构建产物。

### 可选生产部署

生产服务器需要提前安装 Git、Docker、Docker Compose，并克隆本仓库、配置好 `.env`。

在 GitHub 仓库 `Settings -> Secrets and variables -> Actions` 中配置：

| Secret | 说明 |
|---|---|
| `DEPLOY_HOST` | 服务器地址 |
| `DEPLOY_USER` | SSH 用户 |
| `DEPLOY_SSH_KEY` | SSH 私钥 |
| `DEPLOY_PATH` | 服务器项目绝对路径 |

进入 GitHub：

```text
Actions -> Build and Verify -> Run workflow
```

勾选部署选项后，流水线会在构建和测试通过后调用：

```text
scripts/deploy.sh
```

完成生产部署。

## 8. 默认账号

初始化 SQL 中提供了测试账号，密码均为 `123456`。

| 角色 | 用户名 | 密码 |
|---|---|---|
| 超级管理员 | `superadmin` | `123456` |
| 管理员 | `admin` | `123456` |
| 维修人员 | `worker` | `123456` |
| 学生 | `student` | `123456` |

## 9. 部署注意事项

- 修改后端代码后需要重新启动后端。
- 修改前端代码后需要重新启动前端开发服务，生产环境需要重新构建。
- 改过角色权限后，测试账号最好退出重新登录，因为 JWT 中包含登录时的角色信息。
- 逻辑删除用户不会物理删除数据库记录，避免历史工单失去关联。
- 删除、禁用或改变维修人员角色时，待处理/处理中的工单会解除维修人员并退回待分配/待处理状态。
