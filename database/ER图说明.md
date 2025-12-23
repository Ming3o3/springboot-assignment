# 数据库ER图设计说明

## 1. 数据库概述

**数据库名称**: `product_management_system`  
**字符集**: utf8mb4  
**排序规则**: utf8mb4_unicode_ci

## 2. 数据表设计

### 2.1 用户表 (users)

**表名**: `users`  
**说明**: 存储系统用户的基本信息

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 用户ID（主键） |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码（BCrypt加密） |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 邮箱 |
| phone | VARCHAR(20) | NULL | 手机号 |
| real_name | VARCHAR(50) | NULL | 真实姓名 |
| avatar | VARCHAR(255) | NULL | 头像URL |
| status | TINYINT(1) | NOT NULL, DEFAULT 1 | 状态（1:启用 0:禁用） |
| created_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| last_login_time | DATETIME | NULL | 最后登录时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `username`, `email`
- KEY: `status`

---

### 2.2 角色表 (roles)

**表名**: `roles`  
**说明**: 存储系统角色信息

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 角色ID（主键） |
| role_name | VARCHAR(50) | NOT NULL | 角色名称 |
| role_code | VARCHAR(50) | NOT NULL, UNIQUE | 角色代码（如ROLE_ADMIN） |
| description | VARCHAR(255) | NULL | 角色描述 |
| created_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `role_code`

---

### 2.3 用户角色关联表 (user_roles)

**表名**: `user_roles`  
**说明**: 用户与角色的多对多关系映射表

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| user_id | BIGINT(20) | NOT NULL, FOREIGN KEY | 用户ID（外键） |
| role_id | BIGINT(20) | NOT NULL, FOREIGN KEY | 角色ID（外键） |
| created_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: (`user_id`, `role_id`)
- KEY: `user_id`, `role_id`

**外键约束**:
- `fk_user_roles_user`: `user_id` REFERENCES `users(id)` ON DELETE CASCADE
- `fk_user_roles_role`: `role_id` REFERENCES `roles(id)` ON DELETE CASCADE

---

### 2.4 产品表 (products)

**表名**: `products`  
**说明**: 存储产品信息

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 产品ID（主键） |
| product_name | VARCHAR(100) | NOT NULL | 产品名称 |
| product_code | VARCHAR(50) | NOT NULL, UNIQUE | 产品编码 |
| category | VARCHAR(50) | NULL | 产品分类 |
| price | DECIMAL(10, 2) | NOT NULL, DEFAULT 0.00 | 产品价格 |
| stock | INT(11) | NOT NULL, DEFAULT 0 | 库存数量 |
| description | TEXT | NULL | 产品描述 |
| image_url | VARCHAR(255) | NULL | 产品图片URL |
| status | TINYINT(1) | NOT NULL, DEFAULT 1 | 状态（1:上架 0:下架） |
| created_by | BIGINT(20) | NULL, FOREIGN KEY | 创建人ID |
| created_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `product_code`
- KEY: `category`, `status`, `created_by`

**外键约束**:
- `fk_products_user`: `created_by` REFERENCES `users(id)` ON DELETE SET NULL

---

### 2.5 操作日志表 (operation_logs)

**表名**: `operation_logs`  
**说明**: 记录系统操作日志

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 日志ID（主键） |
| user_id | BIGINT(20) | NULL | 操作用户ID |
| username | VARCHAR(50) | NULL | 操作用户名 |
| operation | VARCHAR(100) | NOT NULL | 操作类型 |
| method | VARCHAR(255) | NULL | 操作方法 |
| params | TEXT | NULL | 请求参数 |
| ip | VARCHAR(50) | NULL | 操作IP |
| created_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 操作时间 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `user_id`, `created_time`

---

## 3. 表关系说明

### 3.1 用户与角色关系（多对多）

```
users (1) ----< user_roles >---- (N) roles
```

- 一个用户可以拥有多个角色
- 一个角色可以分配给多个用户
- 通过中间表 `user_roles` 实现多对多关系
- 级联删除：删除用户或角色时，自动删除关联关系

### 3.2 用户与产品关系（一对多）

```
users (1) ----< (N) products
```

- 一个用户可以创建多个产品
- 一个产品只能由一个用户创建
- 通过 `products.created_by` 外键关联
- 删除用户时，产品的 `created_by` 字段设为 NULL

---

## 4. ER图示意

```
┌─────────────┐
│   users     │
├─────────────┤
│ id (PK)     │───┐
│ username    │   │
│ password    │   │
│ email       │   │
│ ...         │   │
└─────────────┘   │
                  │ 1
                  │
                  │ N
           ┌──────┴──────┐
           │ user_roles  │
           ├─────────────┤
           │ id (PK)     │
           │ user_id (FK)│
           │ role_id (FK)│
           └──────┬──────┘
                  │ N
                  │
                  │ 1
           ┌──────┴──────┐
           │   roles     │
           ├─────────────┤
           │ id (PK)     │
           │ role_name   │
           │ role_code   │
           │ ...         │
           └─────────────┘


┌─────────────┐
│   users     │
├─────────────┤
│ id (PK)     │───┐
│ ...         │   │ 1
└─────────────┘   │
                  │
                  │ N
           ┌──────┴──────────┐
           │   products      │
           ├─────────────────┤
           │ id (PK)         │
           │ product_name    │
           │ product_code    │
           │ price           │
           │ created_by (FK) │
           │ ...             │
           └─────────────────┘
```

---

## 5. 数据完整性设计

### 5.1 主键设计
- 所有表均使用 `BIGINT` 类型自增主键 `id`
- 确保数据唯一性和高效查询

### 5.2 唯一约束
- `users.username`: 保证用户名唯一
- `users.email`: 保证邮箱唯一
- `roles.role_code`: 保证角色代码唯一
- `products.product_code`: 保证产品编码唯一
- `user_roles(user_id, role_id)`: 防止重复分配角色

### 5.3 外键约束
- 使用外键约束保证参照完整性
- 合理设置级联操作（CASCADE、SET NULL）

### 5.4 数据类型规范
- 日期字段使用 `DATETIME` 类型
- 金额字段使用 `DECIMAL(10, 2)` 类型
- 状态字段使用 `TINYINT(1)` 类型
- 大文本使用 `TEXT` 类型

### 5.5 默认值设计
- 时间字段设置默认值 `CURRENT_TIMESTAMP`
- 状态字段设置合理默认值（如 `status=1`）
- 数值字段设置默认值（如 `price=0.00`, `stock=0`）

---

## 6. 索引设计

### 6.1 主键索引
- 所有表的 `id` 字段作为主键索引

### 6.2 唯一索引
- 业务唯一字段（username、email、product_code等）

### 6.3 普通索引
- 外键字段（user_id、role_id、created_by等）
- 查询条件字段（category、status等）
- 排序字段（created_time等）

---

## 7. 初始数据

### 7.1 角色数据
- `ROLE_ADMIN`: 管理员角色
- `ROLE_USER`: 普通用户角色

### 7.2 用户数据
- `admin`: 管理员账户（密码: admin123）
- `user`: 测试用户账户（密码: admin123）

### 7.3 产品数据
- 预置5条产品示例数据

---

## 8. 数据冗余处理

本设计遵循数据库范式，避免明显数据冗余：
- **第一范式（1NF）**: 所有字段均为原子性
- **第二范式（2NF）**: 消除部分函数依赖
- **第三范式（3NF）**: 消除传递函数依赖

适度冗余设计（提高查询性能）：
- `operation_logs.username`: 冗余用户名，避免频繁JOIN查询

---

## 9. 安全性设计

- 密码采用 BCrypt 加密存储
- 外键约束保证数据一致性
- 操作日志记录用户操作行为
- 用户状态控制账户启用/禁用

---

## 10. 扩展性设计

- 使用 `BIGINT` 主键支持海量数据
- 预留扩展字段（如avatar、image_url）
- 支持软删除设计（status字段）
- 时间戳字段记录创建和更新时间

---

**文档版本**: v1.0  
**创建日期**: 2025-12-23
