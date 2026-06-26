# 构建脚本与 Dockerfile 清单

| 项目 | 内容 |
|---|---|
| 学校 | 安徽师范大学 |
| 指导老师 | 杜同春 |
| 专业 | 计算机技术 |
| 学号 | 2521022918 |
| 姓名 | 胡雪飞 |
| 日期 | 2026-06-26 |

## 1. CI/CD 文件清单

| 类型 | 文件路径 | 作用 |
|---|---|---|
| GitHub Actions | `github-actions/ci.yml` | 自动执行构建、测试等 CI 流程 |
| Docker Compose | `docker/docker-compose.yml` | 编排前端、后端、MySQL、Redis、RabbitMQ |
| 后端 Dockerfile | `docker/backend-Dockerfile` | 构建 Spring Boot 后端镜像 |
| 前端 Dockerfile | `docker/frontend-Dockerfile` | 构建 Vue + Nginx 前端镜像 |
| 部署脚本 | `scripts/deploy.sh` | Linux 环境部署脚本 |
| 性能测试脚本 | `performance/run-test.ps1` | Windows PowerShell 性能测试启动脚本 |
| 环境变量示例 | `env/.env.example` | Docker 和部署所需环境变量示例 |
| 性能测试说明 | `performance/README.md` | k6 压测说明文档 |
| 性能测试报告 | `performance/REPORT.md` | 性能测试结果说明 |

## 2. 后端 Dockerfile 说明

后端 Dockerfile 归档副本位于：

```text
05_CI_CD配置/docker/backend-Dockerfile
```

主要流程：

1. 使用 Maven 镜像下载依赖并编译项目。
2. 生成 Spring Boot 可执行 Jar。
3. 使用 JRE 镜像运行 Jar。
4. 对外暴露后端服务端口。

该方式可以减少最终镜像体积，并避免在运行镜像中保留完整 Maven 环境。

## 3. 前端 Dockerfile 说明

前端 Dockerfile 归档副本位于：

```text
05_CI_CD配置/docker/frontend-Dockerfile
```

主要流程：

1. 使用 Node 镜像安装依赖。
2. 执行前端构建命令生成 `dist`。
3. 使用 Nginx 镜像托管静态资源。
4. 通过 Nginx 配置转发后端接口请求。

## 4. docker-compose.yml 说明

`docker/docker-compose.yml` 用于启动完整系统，服务包括：

| 服务 | 说明 |
|---|---|
| mysql | 存储用户、工单、日志、幂等记录等业务数据 |
| redis | 缓存、登录辅助、临时数据存储 |
| rabbitmq | 消息队列服务 |
| backend | Spring Boot 后端服务 |
| frontend | Vue 前端页面和 Nginx 代理 |

启动命令：

```bash
docker compose up -d --build
```

查看状态：

```bash
docker compose ps
```

停止服务：

```bash
docker compose down
```

清空数据并重新初始化：

```bash
docker compose down -v --remove-orphans
docker compose up -d --build
```

## 5. GitHub Actions 说明

CI 配置文件归档副本位于：

```text
05_CI_CD配置/github-actions/ci.yml
```

该文件用于在代码推送到 GitHub 后自动执行构建和测试任务，体现项目具备持续集成能力。

典型流程包括：

- 检出代码
- 设置 JDK
- 设置 Node.js
- 后端 Maven 构建
- 前端 npm 构建
- 执行测试或构建校验

## 6. 性能测试脚本说明

性能测试相关文件位于：

```text
performance/
```

主要文件：

```text
performance/run-test.ps1
performance/work-order-read.js
performance/README.md
performance/REPORT.md
```

用途：

- 使用 k6 对工单查询接口进行压测。
- 检查接口失败率、响应时间、吞吐能力。
- 为“性能优化实践”或“工程化测试”提供材料支撑。

## 7. 安全说明

以下文件不应直接提交真实敏感信息：

```text
.env
application-dev.yml
application-prod.yml
```

推荐做法：

- 使用 `.env.example` 提供变量示例。
- 使用环境变量注入真实密码和 OSS 密钥。
- GitHub 仓库中不要出现真实 AccessKey、Token、数据库密码。

## 8. 是否满足材料要求

从当前项目情况看，已经具备以下内容：

| 要求 | 当前状态 |
|---|---|
| 构建脚本 | 已具备，前后端均可构建 |
| Dockerfile | 已具备，前后端各一个 |
| Docker Compose | 已具备，可一键启动完整系统 |
| CI/CD 配置 | 已具备 `.github/workflows/ci.yml` |
| 部署脚本 | 已具备 `scripts/deploy.sh` |
| 性能测试脚本 | 已具备 `performance/run-test.ps1` |
| Markdown 说明 | 已补充本目录两个说明文档 |
