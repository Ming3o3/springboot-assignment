# 数据库ER图设计说明

## 1. 数据库概述

**数据库名称**: `product_management_system`  
**字符集**: utf8mb4  
**排序规则**: utf8mb4_unicode_ci

**设计原则**:
- **无意义主键**: 所有表均使用自增BIGINT类型的`id`字段作为主键，与业务逻辑解耦
- **逻辑外键**: 不使用数据库物理外键约束，采用逻辑外键设计，由应用层Service保证数据完整性
- **事务保证**: 使用`@Transactional`注解确保关联数据的一致性操作
- **索引优化**: 保留外键字段的索引以提升查询性能

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
| user_id | BIGINT(20) | NOT NULL | 用户ID（逻辑外键，关联users.id） |
| role_id | BIGINT(20) | NOT NULL | 角色ID（逻辑外键，关联roles.id） |
| created_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: (`user_id`, `role_id`)
- KEY: `user_id`, `role_id`

**外键约束**:
- 本表使用逻辑外键设计，不在数据库层面定义物理外键
- `user_id` 逻辑关联 `users(id)`，由应用层保证级联删除
- `role_id` 逻辑关联 `roles(id)`，由应用层保证级联删除

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
| created_by | BIGINT(20) | NULL | 创建人ID（逻辑外键，关联users.id） |
| created_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `product_code`
- KEY: `category`, `status`, `created_by`

**外键约束**:
- 本表使用逻辑外键设计,不在数据库层面定义物理外键
- `created_by` 逻辑关联 `users(id)`，删除用户时由应用层Service将created_by设为NULL或处理关联数据

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

### 2.6 持久化登录表 (persistent_logins)

**表名**: `persistent_logins`  
**说明**: Spring Security Remember-Me功能的令牌存储表

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 主键ID（无业务意义） |
| username | VARCHAR(64) | NOT NULL | 用户名（逻辑外键，关联users.username） |
| series | VARCHAR(64) | NOT NULL, UNIQUE | 序列号（唯一标识一个令牌） |
| token | VARCHAR(64) | NOT NULL | 令牌值（每次使用后会更新） |
| last_used | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 最后使用时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `series`
- KEY: `username`

**外键约束**:
- 本表使用逻辑外键设计，不在数据库层面定义物理外键
- `username` 逻辑关联 `users(username)`，由应用层保证数据一致性

**说明**:
- 本表用于Spring Security的"记住我"功能
- 主键`id`为无业务意义的自增字段
- `series`字段作为唯一索引，每个用户可以有多个有效的remember-me令牌
- `token`值在每次使用后会自动更新，防止令牌被盗用
- `last_used`记录令牌最后使用时间，用于过期清理

---

## 3. 表关系说明

### 3.1 用户与角色关系（多对多）

```
users (1) ----< user_roles >---- (N) roles
```

- 一个用户可以拥有多个角色
- 一个角色可以分配给多个用户
- 通过中间表 `user_roles` 实现多对多关系
- **逻辑外键设计**：删除用户或角色时，由应用层Service自动删除关联关系

### 3.2 用户与产品关系（一对多）

```
users (1) ----< (N) products
```

- 一个用户可以创建多个产品
- 一个产品只能由一个用户创建
- 通过 `products.created_by` 逻辑外键关联
- **逻辑外键设计**：删除用户时，由应用层Service将产品的 `created_by` 字段设为 NULL 或根据业务需求处理

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
- 所有表均使用 `BIGINT` 类型自增主键 `id`（无业务意义）
- 确保数据唯一性和高效查询
- 主键id与业务逻辑解耦，便于系统扩展和维护

### 5.2 唯一约束
- `users.username`: 保证用户名唯一
- `users.email`: 保证邮箱唯一
- `roles.role_code`: 保证角色代码唯一
- `products.product_code`: 保证产品编码唯一
- `user_roles(user_id, role_id)`: 防止重复分配角色

### 5.3 逻辑外键约束（应用层保证）
- **设计原则**：不使用数据库物理外键约束，采用逻辑外键设计
- **优点**：
  - 提高系统灵活性，避免数据库层面锁定
  - 更好的水平扩展能力和分库分表支持
  - 简化数据迁移和备份恢复操作
  - 业务逻辑集中在应用层，更易维护
- **关联字段**：
  - `user_roles.user_id` → `users.id`
  - `user_roles.role_id` → `roles.id`
  - `products.created_by` → `users.id`
- **数据完整性保证**：
  - Service层使用 `@Transactional` 注解保证事务一致性
  - 删除用户时，先删除 `user_roles` 关联数据，再删除用户
  - 删除角色时，先删除 `user_roles` 关联数据，再删除角色
  - 删除用户时，将 `products.created_by` 设为 NULL 或根据业务需求处理

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
- 逻辑外键设计，由应用层Service保证数据一致性
- 操作日志记录用户操作行为
- 用户状态控制账户启用/禁用
- 使用事务管理保证数据操作的原子性

---

## 10. 扩展性设计

- 使用 `BIGINT` 主键支持海量数据
- 预留扩展字段（如avatar、image_url）
- 支持软删除设计（status字段）
- 时间戳字段记录创建和更新时间

---

**文档版本**: v2.0  
**创建日期**: 2025-12-23  
**更新日期**: 2025-12-25  
**更新说明**: 
- v2.0: 将所有表改为使用无业务意义的自增id作为主键
- v2.0: 所有外键关联改为逻辑外键，由应用层保证数据完整性
- v2.0: 添加persistent_logins表并适配自定义TokenRepository实现
