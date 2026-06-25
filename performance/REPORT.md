# 校园智慧工单系统性能测试报告

更新日期：2026-06-25

## 1. 测试目标

本测试用于验证系统在工单查询场景下是否满足非功能需求：

- 请求速率约 1000 次/分钟；
- 并发资源池 20～100 个虚拟用户；
- 查询接口在高频访问下保持较低失败率；
- P95 响应时间控制在 1000ms 以内。

## 2. 测试范围

| 项目 | 内容 |
|---|---|
| 测试工具 | k6 0.54.0 |
| 测试脚本 | `performance/work-order-read.js` |
| 封装脚本 | `performance/run-test.ps1` |
| 持续时间 | 1 分钟 |
| 目标速率 | 17 次/秒 |
| 默认接口 | `/api/work-orders/page?pageNum=1&pageSize=5` |
| 认证方式 | JWT Bearer Token |

可选测试接口：

| 角色 | 接口 |
|---|---|
| 管理员/超级管理员 | `/api/work-orders/page?pageNum=1&pageSize=5` |
| 学生 | `/api/work-orders/my?pageNum=1&pageSize=5` |
| 维修人员 | `/api/work-orders/worker/tasks?pageNum=1&pageSize=5` |

## 3. 测试环境

| 项目 | 内容 |
|---|---|
| 操作系统 | 待填写 |
| CPU | 待填写 |
| 内存 | 待填写 |
| JDK | 待填写 |
| MySQL | 待填写 |
| Redis | 待填写 |
| 后端启动方式 | 本地 / Docker Compose，待填写 |
| 前端启动方式 | 本地 / Docker Compose，待填写 |
| 测试时间 | 待填写 |

## 4. 运行命令

推荐：

```powershell
.\performance\run-test.ps1
```

或：

```powershell
.\performance\run-test.ps1 -AuthToken '粘贴登录Token'
```

指定接口：

```powershell
.\performance\run-test.ps1 `
  -AuthToken '粘贴登录Token' `
  -TestPath '/api/work-orders/my?pageNum=1&pageSize=5'
```

## 5. 验收标准与实测结果

| 指标 | 标准 | 实测值 | 结论 |
|---|---:|---:|---|
| 总请求量 | ≥ 1000 | 待填写 | 待执行 |
| HTTP 失败率 | < 1% | 待填写 | 待执行 |
| 业务失败率 | < 1% | 待填写 | 待执行 |
| P95 响应时间 | < 1000ms | 待填写 | 待执行 |
| 检查成功率 | > 99% | 待填写 | 待执行 |
| 最大虚拟用户数 | ≤ 100 | 待填写 | 待执行 |

## 6. 测试结论

尚未填写正式压测结果。完成测试后，应根据 k6 控制台输出和 `performance/results/summary.json` 填写实测数据。

结论模板：

```text
本次测试在 1 分钟内完成约 ____ 次请求，HTTP 失败率为 ____，业务失败率为 ____，P95 响应时间为 ____ ms，最大虚拟用户数为 ____。

结论：通过 / 不通过。
```

## 7. 优化建议记录

如测试未通过，可按以下方向排查：

- 数据库索引是否命中；
- 查询是否返回过大 pageSize；
- MySQL 连接池是否过小；
- Redis、MySQL 是否与应用在同一机器争抢资源；
- 是否开启了过多调试日志；
- Docker Desktop 分配的 CPU 和内存是否不足。
