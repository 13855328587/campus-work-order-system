---
name: work-order-load-test
description: Run and analyze the campus work-order system k6 load test. Use when asked to pressure test work-order APIs, verify the 1000-requests-per-minute requirement, compare latency or failure-rate thresholds, or generate an evidence-based performance result for this project.
---

# Work Order Load Test

归档信息：

| 项目 | 内容 |
|---|---|
| 学校 | 安徽师范大学 |
| 指导老师 | 杜同春 |
| 专业 | 计算机技术 |
| 学号 | 2521022918 |
| 姓名 | 胡雪飞 |
| 日期 | 2026-06-26 |

Execute the repository's existing k6 scenario consistently and report only measured results.

## Workflow

1. Confirm the backend is available on `http://localhost:8080` and Docker is running.
2. Ask for a current login token when none was supplied. Obtain it from browser DevTools: Application -> Session Storage -> `token`. Never save or print the token.
3. Select the role matching the token: `admin`, `student`, or `worker`.
4. Run the bundled launcher from the repository root:

```powershell
.\.codex\skills\work-order-load-test\scripts\run.ps1 -Role admin
```

Pass `-AuthToken 'token-value'` only when the user explicitly accepts putting the token in terminal history. Otherwise use the interactive prompt.

5. Analyze an existing result without rerunning the test when requested:

```powershell
.\.codex\skills\work-order-load-test\scripts\analyze-result.ps1
```

6. Report request count, request rate, HTTP failure rate, check success rate, business failure rate, average latency, P95 latency, and the final pass/fail decision.

## Acceptance Criteria

- At least 1000 requests during the one-minute scenario.
- HTTP failure rate below 1%.
- Business failure rate below 1%.
- Check success rate above 99%.
- P95 response time below 1000 ms.

Do not invent a result when `performance/results/summary.json` is missing. State that the test has not been executed.

## Failure Handling

- For HTTP 401 or failed business checks, obtain a new token and ensure its role matches `-Role`.
- For connection refusal, start the local backend on port 8080.
- For a missing `docker` command, start Docker Desktop and use a terminal with Docker on `PATH`.
- When a threshold fails, preserve the JSON result and report the failed metric instead of describing the run as successful.
