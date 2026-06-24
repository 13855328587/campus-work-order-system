# 校园工单管理系统后端

## 1. 技术栈

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- MyBatis Plus
- MySQL 8
- Maven

## 2. 项目功能

当前后端骨架已经包含：

- 用户注册
- 用户登录
- JWT 鉴权
- RBAC 角色权限
- 创建工单
- 我的工单
- 管理员审核、驳回、分配工单
- 维修人员接单、完成工单
- 学生取消工单
- 工单状态机校验
- Idempotency-Key 幂等提交
- MyBatis Plus 乐观锁
- SQL 初始化脚本

## 3. 初始化数据库

在 Navicat 或 MySQL Workbench 中执行：

```sql
src/main/resources/sql/init.sql
```

或者复制其中内容执行。

## 4. 修改数据库密码

打开：

```text
src/main/resources/application.yml
```

把下面的密码改成你自己的 MySQL root 密码：

```yaml
spring:
  datasource:
    username: root
    password: 123456
```

## 5. 启动项目

在 IDEA 中打开本项目，然后运行：

```text
WorkOrderApplication.java
```

启动成功后，后端地址为：

```text
http://localhost:8080
```

## 6. 测试账号

初始账号密码如下：

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | admin | 123456 |
| 维修人员 | worker | 123456 |
| 学生 | student | 123456 |

## 7. 登录接口

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

## 8. 创建工单接口

```http
POST http://localhost:8080/api/work-orders
Authorization: Bearer <token>
Idempotency-Key: test-key-001
Content-Type: application/json

{
  "title": "宿舍灯坏了",
  "description": "宿舍灯无法打开",
  "location": "1号楼201",
  "category": "ELECTRIC",
  "priority": "MEDIUM"
}
```
