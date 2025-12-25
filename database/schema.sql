-- ====================================
-- 数据库创建脚本
-- 项目名称：SpringBoot 产品管理系统
-- 数据库：MySQL 8.0+
-- 创建日期：2025-12-23
-- ====================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS product_management_system 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE product_management_system;

-- ====================================
-- 1. 用户表 (users)
-- 说明：存储系统用户基本信息
-- ====================================
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱（唯一）',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态（1:启用 0:禁用）',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ====================================
-- 2. 角色表 (roles)
-- 说明：存储系统角色信息
-- ====================================
CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID（主键）',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称（ADMIN/USER等）',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色代码（唯一）',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ====================================
-- 3. 用户角色关联表 (user_roles)
-- 说明：用户与角色的多对多关系
-- ====================================
CREATE TABLE IF NOT EXISTS `user_roles` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID（逻辑外键，关联users.id）',
    `role_id` BIGINT(20) NOT NULL COMMENT '角色ID（逻辑外键，关联roles.id）',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表（逻辑外键，由应用层保证数据完整性）';

-- ====================================
-- 4. 产品表 (products)
-- 说明：存储产品信息
-- ====================================
CREATE TABLE IF NOT EXISTS `products` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '产品ID（主键）',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `product_code` VARCHAR(50) NOT NULL COMMENT '产品编码（唯一）',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '产品分类',
    `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '产品价格',
    `stock` INT(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
    `description` TEXT DEFAULT NULL COMMENT '产品描述',
    `image_url` VARCHAR(255) DEFAULT NULL COMMENT '产品图片URL',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态（1:上架 0:下架）',
    `created_by` BIGINT(20) DEFAULT NULL COMMENT '创建人ID（逻辑外键，关联users.id）',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_code` (`product_code`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表（逻辑外键，由应用层保证数据完整性）';

-- ====================================
-- 5. 操作日志表 (operation_logs)
-- 说明：记录系统操作日志
-- ====================================
CREATE TABLE IF NOT EXISTS `operation_logs` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID（主键）',
    `user_id` BIGINT(20) DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作类型（如：登录、新增、修改、删除）',
    `method` VARCHAR(255) DEFAULT NULL COMMENT '操作方法',
    `params` TEXT DEFAULT NULL COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ====================================
-- 6. 持久化登录表 (persistent_logins)
-- 说明：Spring Security Remember-Me功能的令牌存储表
-- ====================================
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

-- ====================================
-- 初始化数据
-- ====================================

-- 插入默认角色
INSERT INTO `roles` (`role_name`, `role_code`, `description`) VALUES
('管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限'),
('普通用户', 'ROLE_USER', '普通用户，拥有基本查看权限');

-- 插入默认管理员账户
-- 密码: admin123 (BCrypt加密后的值)
INSERT INTO `users` (`username`, `password`, `email`, `real_name`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '系统管理员', 1),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user@example.com', '测试用户', 1);

-- 分配角色
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
(1, 1), -- admin 用户分配 ADMIN 角色
(2, 2); -- user 用户分配 USER 角色

-- 插入示例产品数据
INSERT INTO `products` (`product_name`, `product_code`, `category`, `price`, `stock`, `description`, `created_by`, `status`) VALUES
('MacBook Pro 16', 'PROD001', '电脑', 18999.00, 50, 'Apple MacBook Pro 16英寸笔记本电脑', 1, 1),
('iPhone 15 Pro', 'PROD002', '手机', 8999.00, 100, 'Apple iPhone 15 Pro 256GB', 1, 1),
('AirPods Pro', 'PROD003', '耳机', 1999.00, 200, 'Apple AirPods Pro 第二代', 1, 1),
('iPad Air', 'PROD004', '平板', 4799.00, 80, 'Apple iPad Air 10.9英寸', 1, 1),
('Apple Watch', 'PROD005', '智能手表', 3199.00, 150, 'Apple Watch Series 9', 1, 1);
