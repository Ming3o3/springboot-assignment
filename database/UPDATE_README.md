# 数据库更新说明

## 更新时间
2025-12-25

## 更新内容

### 1. persistent_logins表结构变更
将`persistent_logins`表的主键从`series`字段改为自增的`id`字段，以符合项目统一的主键设计规范。

**变更前**:
```sql
PRIMARY KEY (series)
```

**变更后**:
```sql
id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
UNIQUE KEY uk_series (series)
```

### 2. 代码同步更新
- 新增 `CustomJdbcTokenRepositoryImpl.java` - 自定义TokenRepository实现
- 修改 `SecurityConfig.java` - 使用自定义TokenRepository替代默认的JdbcTokenRepositoryImpl

## 执行步骤

### 如果表已存在（需要重建）

```sql
-- 1. 删除旧表（注意：会清除所有remember-me令牌）
USE product_management_system;
DROP TABLE IF EXISTS persistent_logins;

-- 2. 执行建表脚本
source add_persistent_logins.sql;
-- 或直接执行schema.sql
```

### 如果表不存在（首次创建）

```sql
-- 直接执行建表脚本
USE product_management_system;
source add_persistent_logins.sql;
```

## 注意事项

1. **数据迁移**: 如果已有旧的`persistent_logins`表数据需要保留，建议先备份：
   ```sql
   CREATE TABLE persistent_logins_backup AS SELECT * FROM persistent_logins;
   ```

2. **Remember-Me令牌失效**: 表结构变更后，所有用户的"记住我"令牌将失效，需要重新登录

3. **应用重启**: 更新表结构和代码后，需要重启Spring Boot应用

4. **自动建表**: 不建议在生产环境使用`setCreateTableOnStartup(true)`，应手动执行SQL脚本

## 设计优势

### 统一的主键设计
- 所有表使用无业务意义的自增`id`作为主键
- 业务字段（如`series`）作为唯一索引
- 便于后续系统扩展和维护

### 逻辑外键设计
- `username` 逻辑关联 `users.username`
- 不使用数据库物理外键约束
- 由应用层Service保证数据一致性
- 支持更灵活的分库分表架构

## 相关文件

- `schema.sql` - 完整的数据库创建脚本
- `add_persistent_logins.sql` - 单独的建表脚本
- `ER图说明.md` - 数据库设计文档
- `CustomJdbcTokenRepositoryImpl.java` - 自定义TokenRepository实现
- `SecurityConfig.java` - Spring Security配置

## 问题排查

如果遇到"Table doesn't exist"错误：
1. 检查数据库是否已创建
2. 确认表是否已执行创建脚本
3. 查看应用日志确认数据源配置正确

如果Remember-Me功能异常：
1. 检查`persistent_logins`表是否创建成功
2. 确认SecurityConfig中使用的是CustomJdbcTokenRepositoryImpl
3. 查看浏览器Cookie中是否存在remember-me令牌
