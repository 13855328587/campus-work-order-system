# 工单查询压力测试

该脚本验证非功能需求中的两个指标：

- 请求规模约 1000 次/分钟（17 次/秒，持续 1 分钟）；
- 并发资源池从 20 个虚拟用户开始，最大允许扩展到 100 个。

## 运行条件

1. 系统已启动；
2. 使用管理员账号正常登录，并从浏览器开发者工具中复制 Bearer Token；
3. 本机安装 k6，或使用 Docker 运行 k6。

## Docker 运行

PowerShell：

推荐直接执行封装脚本：

```powershell
.\performance\run-test.ps1
```

根据提示粘贴 Token 即可，Token 不会保存到项目文件。也可以直接传参：

```powershell
.\performance\run-test.ps1 -AuthToken '粘贴登录Token'
```

下面是脚本内部对应的完整 Docker 命令：

```powershell
$env:AUTH_TOKEN='粘贴登录Token'
docker run --rm `
  -e BASE_URL=http://host.docker.internal:8080 `
  -e AUTH_TOKEN=$env:AUTH_TOKEN `
  -v "${PWD}/performance:/scripts" `
  grafana/k6:0.54.0 run `
  --summary-export=/scripts/results/summary.json `
  /scripts/work-order-read.js
```

默认测试管理员分页接口。也可以通过 `TEST_PATH` 改为当前角色有权访问的查询接口：

- 学生：`/api/work-orders/my?pageNum=1&pageSize=5`
- 维修人员：`/api/work-orders/worker/tasks?pageNum=1&pageSize=5`

## 通过标准

- HTTP 失败率小于 1%；
- 业务失败率小于 1%；
- 检查成功率大于 99%；
- 95% 请求响应时间小于 1000ms。

运行结束后，将控制台指标填写到 `REPORT.md`，原始 JSON 保存在 `performance/results/summary.json`。
