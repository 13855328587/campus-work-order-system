# 校园工单管理系统前端

## 1. 技术栈

- Vue 3
- Vite
- Element Plus
- Axios
- Vue Router
- Pinia

## 2. 页面功能

当前前端已经包含：

- 登录页
- 注册页
- 首页仪表盘
- 学生：新建工单、我的工单
- 管理员：工单管理、用户管理
- 维修人员：我的任务
- Token 保存
- 路由权限控制
- Axios 请求封装

## 3. 安装依赖

进入项目目录：

```bash
cd campus-work-order-frontend
npm install
```

## 4. 启动前端

```bash
npm run dev
```

启动后访问：

```text
http://localhost:5173
```

## 5. 后端地址

开发环境通过 Vite 代理访问后端：

```text
/api -> http://localhost:8080
```

请先启动 Spring Boot 后端。

## 6. 测试账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | admin | 123456 |
| 维修人员 | worker | 123456 |
| 学生 | student | 123456 |

## 7. 注意

当前前端和前面生成的后端骨架基本对应。维修人员页面如果无法查询到任务，是因为后端初版只给管理员开放了全部工单查询接口；后续可以给后端补充一个维修人员任务接口 `/api/work-orders/worker/tasks`。
