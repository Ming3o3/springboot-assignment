-- ====================================
-- 添加持久化登录表 (persistent_logins)
-- 用于修复 Spring Security Remember-Me 功能
-- 执行日期：2025-12-25
-- ====================================

USE product_management_system;

-- 创建持久化登录表（使用无意义的自增id作为主键）
CREATE TABLE IF NOT EXISTS `persistent_logins` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID（无业务意义）',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名（逻辑外键，关联users.username）',
    `series` VARCHAR(64) NOT NULL COMMENT '序列号（唯一标识）',
    `token` VARCHAR(64) NOT NULL COMMENT '令牌值',
    `last_used` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后使用时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_series` (`series`),
    KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='持久化登录令牌表（逻辑外键，由应用层保证数据完整性）';

-- 验证表是否创建成功
SELECT 'persistent_logins表创建成功！' AS message;
