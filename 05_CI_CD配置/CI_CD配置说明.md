# CI/CD 配置说明

| 项目 | 内容 |
|---|---|
| 学校 | 安徽师范大学 |
| 指导老师 | 杜同春 |
| 专业 | 计算机技术 |
| 学号 | 2521022918 |
| 姓名 | 胡雪飞 |
| 日期 | 2026-06-26 |

## 1. 目录说明

本目录用于归档校园工单管理系统的 CI/CD、构建、测试、部署和 Docker 化配置说明。

本目录已归档项目中实际使用的 CI/CD 与部署相关文件副本：

```text
05_CI_CD配置/
├── CI_CD配置说明.md
├── 构建脚本与Dockerfile清单.md
├── github-actions/
│   └── ci.yml
├── docker/
│   ├── docker-compose.yml
│   ├── backend-Dockerfile
│   └── frontend-Dockerfile
├── scripts/
│   └── deploy.sh
├── performance/
│   ├── run-test.ps1
│   ├── README.md
│   └── REPORT.md
└── env/
    └── .env.example
```

其中：

- `github-actions/ci.yml`：GitHub Actions 自动化流程配置。
- `docker/docker-compose.yml`：本地或服务器 Docker Compose 一键部署配置。
- `docker/backend-Dockerfile`：Spring Boot 后端镜像构建配置。
- `docker/frontend-Dockerfile`：Vue 前端构建和 Nginx 部署配置。
- `scripts/deploy.sh`：部署脚本。
- `performance/run-test.ps1`：性能测试执行脚本。
- `performance/README.md`：性能测试说明。
- `performance/REPORT.md`：性能测试结果说明。
- `env/.env.example`：环境变量示例，不包含真实密钥。

## 2. CI/CD 流程概述

本项目的 CI/CD 流程主要包含以下阶段：

```text
代码提交到 GitHub
        ↓
触发 GitHub Actions
        ↓
后端 Maven 构建与测试
        ↓
前端 npm 安装与构建
        ↓
Docker 镜像构建检查
        ↓
部署脚本或 Docker Compose 部署
```

## 3. 构建阶段

### 3.1 后端构建

后端项目基于 Spring Boot，使用 Maven 构建。

常用命令：

```bash
cd campus-work-order-backend
mvn clean package
```

Docker 构建中使用多阶段构建：

1. 使用 Maven 镜像编译后端 Jar 包。
2. 使用 JRE 镜像运行最终 Jar 包。

### 3.2 前端构建

前端项目基于 Vue，使用 npm 构建。

常用命令：

```bash
cd campus-work-order-frontend/campus-work-order-frontend
npm install
npm run build
```

Docker 构建中使用多阶段构建：

1. 使用 Node 镜像构建前端静态资源。
2. 使用 Nginx 镜像托管前端页面。

## 4. 测试阶段

项目包含基础测试与性能测试能力。

### 4.1 后端测试

后端可通过 Maven 执行测试：

```bash
mvn test
```

### 4.2 性能测试

性能测试脚本位于：

```text
performance/run-test.ps1
performance/work-order-read.js
```

主要用于验证工单查询接口在并发场景下的响应时间、失败率和吞吐能力。

## 5. 部署阶段

项目支持 Docker Compose 一键部署，包含以下服务：

```text
frontend
backend
mysql
redis
rabbitmq
```

启动命令：

```bash
docker compose up -d --build
```

停止命令：

```bash
docker compose down
```

如需重新初始化数据库，可执行：

```bash
docker compose down -v --remove-orphans
docker compose up -d --build
```

注意：`down -v` 会删除 Docker 数据卷，会清空 Docker 环境中的数据库数据。

## 6. 环境变量管理

项目使用环境变量管理数据库、Redis、RabbitMQ、OSS 等配置。

示例配置文件：

```text
.env.example
```

本地真实配置文件：

```text
.env
```

注意：

- `.env` 不应提交到 GitHub。
- 包含真实 OSS AccessKey、数据库密码、Token 的配置文件不应直接公开。
- `application-dev.yml` 如果包含真实密钥，也不建议提交，应改为环境变量占位。

## 7. 访问地址

Docker Compose 启动后，默认访问地址如下：

```text
前端系统：http://localhost
后端接口：http://localhost:8080
RabbitMQ 管理页：http://localhost:15672
```

## 8. 小结

本项目已具备基本 CI/CD 工程化能力：

- 支持 GitHub Actions 自动构建与测试。
- 支持前后端 Docker 镜像构建。
- 支持 Docker Compose 一键部署完整系统。
- 支持 MySQL、Redis、RabbitMQ 等基础服务编排。
- 支持性能测试脚本和部署文档。
