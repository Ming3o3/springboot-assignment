# 产品管理系统

## 项目简介

这是一个基于 Spring Boot + MyBatis-Plus + Spring Security + Thymeleaf + Redis 的产品管理系统，实现了用户认证、产品管理、权限控制、数据缓存等功能。

## 技术栈

### 后端技术
- **Spring Boot 2.7.6**: 核心框架
- **Spring Security**: 安全框架，实现用户认证和权限控制
- **MyBatis-Plus 3.5.3.1**: 持久层框架
- **MySQL 8.0**: 关系型数据库
- **Redis**: 缓存数据库
- **Druid**: 数据库连接池
- **Lombok**: 简化JavaBean开发

### 前端技术
- **Thymeleaf**: 模板引擎
- **HTML5 + CSS3**: 页面结构和样式
- **JavaScript**: 前端交互逻辑

### 工具库
- **Hutool**: Java工具类库
- **FastJSON**: JSON处理
- **Spring AOP**: 面向切面编程（日志记录）

## 项目功能

### 1. 用户认证模块
- ✅ 用户注册（带前端JavaScript校验）
- ✅ 用户登录（Spring Security表单认证）
- ✅ 记住我功能（Cookie，有效期1天）
- ✅ Session管理
- ✅ 密码加密（BCrypt）

### 2. 产品管理模块
- ✅ 产品列表查询（支持分页）
- ✅ 多条件搜索（产品名称、分类、价格区间）
- ✅ 新增产品（仅管理员）
- ✅ 编辑产品（仅管理员）
- ✅ 删除产品（仅管理员，带确认提示）
- ✅ 数据缓存（Redis）

### 3. 权限控制模块
- ✅ 基于角色的访问控制（RBAC）
- ✅ 管理员角色（ROLE_ADMIN）
- ✅ 普通用户角色（ROLE_USER）
- ✅ 方法级权限控制（@PreAuthorize）

### 4. 日志记录模块
- ✅ AOP切面记录操作日志
- ✅ 记录用户操作、IP地址、操作时间
- ✅ 控制台和文件日志输出

### 5. 安全防护
- ✅ CSRF保护（Spring Security默认启用）
- ✅ SQL注入防护（Druid Filter）
- ✅ 密码加密存储

## 项目结构

```
springboot/
├── database/                          # 数据库相关
│   ├── schema.sql                     # 数据库建表SQL
│   └── ER图说明.md                     # ER图文档
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/gzist/project/
│   │   │       ├── SpringbootApplication.java    # 启动类
│   │   │       ├── aspect/                       # AOP切面
│   │   │       │   └── OperationLogAspect.java   # 操作日志切面
│   │   │       ├── common/                       # 公共类
│   │   │       │   └── Result.java               # 统一响应结果
│   │   │       ├── config/                       # 配置类
│   │   │       │   ├── MybatisPlusConfig.java    # MyBatis-Plus配置
│   │   │       │   ├── RedisConfig.java          # Redis配置
│   │   │       │   ├── SecurityConfig.java       # Spring Security配置
│   │   │       │   └── MyMetaObjectHandler.java  # 自动填充配置
│   │   │       ├── controller/                   # 控制器层
│   │   │       │   ├── AuthController.java       # 认证控制器
│   │   │       │   ├── IndexController.java      # 首页控制器
│   │   │       │   └── ProductController.java    # 产品控制器
│   │   │       ├── dto/                          # 数据传输对象
│   │   │       │   └── RegisterDTO.java          # 注册DTO
│   │   │       ├── entity/                       # 实体类
│   │   │       │   ├── User.java                 # 用户实体
│   │   │       │   ├── Role.java                 # 角色实体
│   │   │       │   ├── UserRole.java             # 用户角色关联实体
│   │   │       │   ├── Product.java              # 产品实体
│   │   │       │   └── OperationLog.java         # 操作日志实体
│   │   │       ├── mapper/                       # Mapper接口
│   │   │       │   ├── UserMapper.java
│   │   │       │   ├── RoleMapper.java
│   │   │       │   ├── UserRoleMapper.java
│   │   │       │   ├── ProductMapper.java
│   │   │       │   └── OperationLogMapper.java
│   │   │       └── service/                      # 服务层
│   │   │           ├── IUserService.java
│   │   │           ├── IProductService.java
│   │   │           └── impl/
│   │   │               ├── UserServiceImpl.java
│   │   │               └── ProductServiceImpl.java
│   │   └── resources/
│   │       ├── application.yml                   # 配置文件
│   │       └── templates/                        # Thymeleaf模板
│   │           ├── login.html                    # 登录页面
│   │           ├── register.html                 # 注册页面
│   │           └── product/
│   │               ├── list.html                 # 产品列表页
│   │               ├── add.html                  # 新增产品页
│   │               └── edit.html                 # 编辑产品页
│   └── test/
│       └── java/
│           └── com/gzist/project/
│               └── SpringbootApplicationTests.java
├── pom.xml                                       # Maven配置
└── README.md                                     # 项目说明
```

## 数据库设计

### 数据表说明

1. **users** - 用户表
   - 存储用户基本信息
   - 密码采用BCrypt加密

2. **roles** - 角色表
   - 系统角色定义
   - 包含ROLE_ADMIN和ROLE_USER

3. **user_roles** - 用户角色关联表
   - 实现用户与角色的多对多关系

4. **products** - 产品表
   - 存储产品信息
   - 包含价格、库存、分类等

5. **operation_logs** - 操作日志表
   - 记录用户操作行为

详细设计请查看：[database/ER图说明.md](database/ER图说明.md)

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <项目地址>
   cd springboot
   ```

2. **创建数据库**
   ```sql
   -- 执行 database/schema.sql 文件
   mysql -u root -p < database/schema.sql
   ```

3. **修改配置文件**
   
   编辑 `src/main/resources/application.yml`：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/product_management_system
       username: root        # 修改为你的数据库用户名
       password: root        # 修改为你的数据库密码
     redis:
       host: localhost       # 修改为你的Redis地址
       port: 6379
   ```

4. **启动Redis**
   ```bash
   redis-server
   ```

5. **编译运行**
   ```bash
   mvn clean package
   java -jar target/project-0.0.1-SNAPSHOT.jar
   ```

6. **访问系统**
   
   浏览器访问：http://localhost:8080

### 默认账户

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin  | admin123 | 管理员 |
| user   | admin123 | 普通用户 |

## 核心功能说明

### 1. 注册功能
- 前端JavaScript校验：
  - 用户名长度3-20字符
  - 密码长度6-20字符
  - 两次密码一致性校验
  - 邮箱格式验证
  - 实时检查用户名和邮箱是否已存在
- 后端校验：
  - 使用@Valid注解进行参数验证
  - 密码BCrypt加密存储
  - 自动分配ROLE_USER角色

### 2. 登录功能
- Spring Security表单认证
- 支持"记住我"功能（Cookie，1天有效）
- Session防止非法访问
- 登录失败提示
- 退出登录清除Cookie和Session

### 3. 产品查询
- MyBatis-Plus分页插件
- 支持多条件查询：
  - 产品名称模糊搜索
  - 产品分类精确查询
  - 价格区间查询
- Redis缓存提升性能

### 4. 产品增删改
- 仅管理员可操作（@PreAuthorize("hasRole('ADMIN')")）
- 新增/修改后自动清除Redis缓存（@CacheEvict）
- 删除前弹出确认提示
- 支持批量删除（可选）

### 5. 日志记录
- AOP切面自动记录操作
- 记录内容：
  - 用户名
  - 操作类型（登录/注册/新增/修改/删除/查询）
  - 操作方法
  - 请求参数
  - IP地址
  - 操作时间
- 控制台和文件双输出

## 代码规范

### 1. 包结构规范
- `entity`: 实体类
- `dto`: 数据传输对象
- `mapper`: 数据访问层
- `service`: 业务逻辑层
- `controller`: 控制器层
- `config`: 配置类
- `aspect`: AOP切面
- `common`: 公共类

### 2. 命名规范
- 类名：大驼峰命名法（PascalCase）
- 方法名/变量名：小驼峰命名法（camelCase）
- 常量：全大写，下划线分隔
- 包名：全小写

### 3. 注解规范
- 实体类：@Data, @TableName, @TableId, @TableField
- Service：@Service, @Transactional
- Controller：@Controller, @RestController, @RequestMapping
- 权限控制：@PreAuthorize
- 缓存：@Cacheable, @CacheEvict

### 4. 注释规范
- 类注释：说明类的作用、作者、日期
- 方法注释：说明方法功能、参数、返回值
- 复杂逻辑：添加行内注释

## 评分点对照

| 评分点 | 实现情况 | 说明 |
|--------|---------|------|
| 数据表设计 | ✅ | 5张表，主外键完整，ER图清晰 |
| 界面设计 | ✅ | Thymeleaf模板，登录/注册/产品管理页面完整，CSS美化 |
| JavaBean映射 | ✅ | MyBatis-Plus注解完整，Lombok简化代码 |
| 注册功能 | ✅ | JS前端校验完整，密码加密 |
| 登录功能 | ✅ | Spring Security认证，记住我功能，Session管理 |
| 查询功能 | ✅ | 分页查询，多条件搜索，模糊查询 |
| 增删改功能 | ✅ | 完整CRUD，权限控制，操作提示 |
| 其他功能 | ✅ | Thymeleaf表达式，Redis缓存，CSRF防护，AOP日志 |

## 注意事项

1. **首次启动前需要**：
   - 创建数据库并执行SQL脚本
   - 启动Redis服务
   - 修改配置文件中的数据库和Redis连接信息

2. **Remember-Me功能**：
   - 首次启动需要在SecurityConfig中启用建表：`tokenRepository.setCreateTableOnStartup(true)`
   - 创建表后注释掉该行代码

3. **缓存清理**：
   - 产品数据修改后自动清除缓存
   - 也可手动清理Redis缓存：`redis-cli FLUSHDB`

## 开发者

- **作者**: GZIST
- **日期**: 2025-12-23
- **项目**: 高级架构技术期末大作业

## 许可证

MIT License
