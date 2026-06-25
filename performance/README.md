# 工单查询压力测试

更新日期：2026-06-25

该目录用于验证校园智慧工单系统的查询接口性能，重点覆盖“约 1000 次/分钟”的非功能需求。

## 1. 测试目标

- 请求规模：约 1000 次/分钟；
- 请求速率：17 次/秒；
- 持续时间：1 分钟；
- 并发资源池：从 20 个虚拟用户开始，最大允许扩展到 100 个；
- 核心接口：工单分页查询。

## 2. 文件说明

| 文件 | 说明 |
|---|---|
| `work-order-read.js` | k6 压测脚本 |
| `run-test.ps1` | Windows PowerShell 封装脚本 |
| `REPORT.md` | 压测报告模板 |
| `results/summary.json` | 运行后生成的 k6 汇总结果 |

## 3. 运行条件

1. 后端已启动；
2. 数据库和 Redis 正常；
3. 使用有权限的账号登录系统；
4. 从浏览器开发者工具中复制登录后的 Bearer Token；
5. 本机安装 Docker，或直接安装 k6。

## 4. 推荐运行方式

在项目根目录执行：

```powershell
.\performance\run-test.ps1
```

脚本会提示粘贴 Token。Token 不会保存到项目文件。

也可以直接传参：

```powershell
.\performance\run-test.ps1 -AuthToken '粘贴登录Token'
```

默认测试管理员工单分页接口：

```text
/api/work-orders/page?pageNum=1&pageSize=5
```

## 5. 指定测试接口

不同角色可选择不同查询接口：

| 角色 | TEST_PATH |
|---|---|
| 管理员/超级管理员 | `/api/work-orders/page?pageNum=1&pageSize=5` |
| 学生 | `/api/work-orders/my?pageNum=1&pageSize=5` |
| 维修人员 | `/api/work-orders/worker/tasks?pageNum=1&pageSize=5` |

示例：

```powershell
.\performance\run-test.ps1 `
  -AuthToken '粘贴登录Token' `
  -TestPath '/api/work-orders/my?pageNum=1&pageSize=5'
```

如果使用 Docker 直接运行：

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

## 6. 通过标准

| 指标 | 标准 |
|---|---:|
| 总请求量 | ≥ 1000 |
| HTTP 失败率 | < 1% |
| 业务失败率 | < 1% |
| 检查成功率 | > 99% |
| P95 响应时间 | < 1000ms |
| 最大虚拟用户数 | ≤ 100 |

## 7. 结果填写

运行结束后：

1. 查看控制台输出；
2. 查看 `performance/results/summary.json`；
3. 将实测数据填写到 `performance/REPORT.md`；
4. 不要用估算值替代真实结果。

## 8. 常见问题

### 401 未登录

Token 错误、过期或没有携带 `Bearer`。重新登录后复制新的 Token。

### 403 无权限

账号角色和测试接口不匹配。例如维修人员不能压测管理员分页接口。

### 连接失败

确认后端是否运行在：

```text
http://localhost:8080
```

Docker 运行 k6 时，脚本默认使用：

```text
http://host.docker.internal:8080
```
