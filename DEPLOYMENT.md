# Docker Compose 部署

## 1. 准备环境变量

复制环境变量模板：

```bash
cp .env.example .env
```

编辑 `.env`，至少修改 MySQL 密码、JWT 密钥及阿里云 OSS 配置。`.env` 已加入 `.gitignore`，不要提交真实密钥。

## 2. 构建并启动

```bash
docker compose up -d --build
```

启动后访问：

- 前端：http://localhost
- 后端：http://localhost:8080
- MySQL 和 Redis 仅在 Compose 内部网络开放

首次创建 MySQL 数据卷时会自动执行 `src/main/resources/sql/init.sql`。已有数据卷不会重复初始化。

## 3. 运维命令

```bash
# 查看服务状态
docker compose ps

# 查看实时日志
docker compose logs -f backend frontend

# 停止服务（保留数据库数据）
docker compose down

# 停止并删除数据库、Redis 数据卷
docker compose down -v
```

## 4. CI/CD

`.github/workflows/ci.yml` 在推送到 `main`/`master` 或提交 Pull Request 时自动执行：

1. Maven 测试并打包后端；
2. npm 安装依赖并构建前端；
3. 校验 Docker Compose；
4. 构建前后端容器镜像；
5. 上传后端 JAR 和前端 `dist` 构建产物。

### 生产部署配置

生产服务器需要预先安装 Git、Docker 和 Docker Compose，并克隆本仓库、配置好 `.env`。在 GitHub 仓库的 `Settings → Secrets and variables → Actions` 中配置：

- `DEPLOY_HOST`：服务器地址；
- `DEPLOY_USER`：SSH 用户；
- `DEPLOY_SSH_KEY`：SSH 私钥；
- `DEPLOY_PATH`：服务器中的项目绝对路径。

然后进入 GitHub 的 `Actions → Build and Verify → Run workflow`，勾选部署选项。流水线在所有构建和测试通过后，调用 `scripts/deploy.sh` 完成生产部署。
