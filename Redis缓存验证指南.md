# Redis缓存验证指南

## 当前缓存状态分析

### 您的日志解读

**日志内容**:
```log
==> Preparing: INSERT INTO operation_logs (username, operation, method, params, ip, created_time) VALUES (?, ?, ?, ?, ?, ?)
==> Parameters: admin(String), 查询(String), com.gzist.project.controller.ProductController.listPage(String), 
                [{"current":1,"size":10,"productName":"","category":"","minPrice":null,"maxPrice":null}](String), 
                0:0:0:0:0:0:0:1(String), 2025-12-26T10:54:10.394925900(LocalDateTime)
<== Updates: 1
```

### ✅ 缓存已生效的证据

**1. 没有产品查询SQL**

如果缓存未生效，您应该看到：
```sql
-- ❌ 这些SQL没有出现，说明走了缓存！
==> Preparing: SELECT id, product_name, product_code, category, price, stock, 
                description, image_url, status, created_by, created_time, updated_time 
                FROM products 
                WHERE (deleted = 0) 
                ORDER BY created_time DESC 
                LIMIT ?, ?
```

**2. 只有操作日志SQL**

您看到的SQL是AOP切面记录操作日志，**每次请求都会执行**：
- ✅ 第一次查询：操作日志SQL + 产品查询SQL
- ✅ 第二次查询：操作日志SQL（产品数据从Redis读取）← **您当前的状态**

**3. Redis中的缓存数据**

从您的截图可以看到Redis中存在缓存：
```
Key: product-system:products:page:1:10:::null:null
TTL: 1794秒（约30分钟）
Size: 2.99KB
```

这说明：
- ✅ 缓存已成功写入Redis
- ✅ 缓存key生成正确
- ✅ 缓存数据包含完整的分页结果

## 如何100%确认缓存生效

### 方法1: 查看完整日志对比

**重新测试流程**:

1. **清除所有缓存**
```bash
redis-cli
> FLUSHDB  # 清空当前数据库
> KEYS *   # 确认为空
```

2. **第一次访问** `http://localhost:8080/product/list`

**日志输出**（缓存未命中）:
```log
2025-12-26 11:00:00 [http-nio-8080-exec-1] INFO  ProductServiceImpl - 【缓存未命中】从数据库查询产品列表 - current: 1, size: 10, name: '', category: '', minPrice: null, maxPrice: null

Creating a new SqlSession
==> Preparing: SELECT ... FROM products ORDER BY created_time DESC LIMIT ?, ?
==> Parameters: 0(Long), 10(Long)
<== Total: 8

==> Preparing: INSERT INTO operation_logs (...) VALUES (...)
<== Updates: 1
```

3. **第二次访问** `http://localhost:8080/product/list`（立即刷新）

**日志输出**（缓存命中）:
```log
# 没有 "【缓存未命中】从数据库查询产品列表" 日志 ✅
# 没有 "SELECT ... FROM products" SQL ✅

==> Preparing: INSERT INTO operation_logs (...) VALUES (...)  # 只有操作日志
<== Updates: 1

2025-12-26 11:00:01 [http-nio-8080-exec-2] INFO  OperationLogAspect - 操作日志 - 用户: admin, 操作: 查询, 方法: com.gzist.project.controller.ProductController.listPage, IP: ...
```

**对比说明**:
- 第一次：有数据库查询日志 + 产品查询SQL
- 第二次：无数据库查询日志，无产品查询SQL ← **缓存生效**

### 方法2: 查看响应时间

**浏览器开发者工具（F12）→ Network面板**:

| 请求次数 | 响应时间 | 说明 |
|---------|---------|------|
| 第1次 | 150-200ms | 数据库查询 + 缓存写入 |
| 第2次 | **5-10ms** | Redis缓存读取 ✅ |
| 第3次 | **5-10ms** | Redis缓存读取 ✅ |

如果第2次、第3次响应时间明显缩短（<20ms），说明缓存生效。

### 方法3: Redis监控命令

**打开两个终端**:

**终端1 - 监控Redis命令**:
```bash
redis-cli
> MONITOR
```

**终端2 - 访问页面**:
```bash
# 第一次访问
curl http://localhost:8080/product/list
```

**终端1会显示**:
```
# 第一次访问（缓存未命中）
1640512800.123456 [0 127.0.0.1:xxxxx] "GET" "product-system:products:page:1:10:null:null:null:null"
# 返回 (nil) - 缓存不存在

1640512800.234567 [0 127.0.0.1:xxxxx] "SET" "product-system:products:page:1:10:null:null:null:null" "[\"com.baomidou.mybatisplus.extension.plugins.pagination.Page\",{...}]" "EX" "1800"
# 写入缓存，过期时间1800秒

# 第二次访问（缓存命中）
1640512801.345678 [0 127.0.0.1:xxxxx] "GET" "product-system:products:page:1:10:null:null:null:null"
# 返回缓存数据 ✅ 不再执行SET命令
```

### 方法4: 应用程序日志级别

**修改 application.yml**:
```yaml
logging:
  level:
    com.gzist.project.service.impl.ProductServiceImpl: DEBUG
    org.springframework.cache: DEBUG
```

**重启后查看日志**:
```log
# 缓存命中时会看到
DEBUG o.s.cache.interceptor.CacheInterceptor - Cache hit for key 'product-system:products:page:1:10:null:null:null:null' on cache 'products'

# 缓存未命中时会看到
DEBUG o.s.cache.interceptor.CacheInterceptor - Cache miss for key 'product-system:products:page:1:10:null:null:null:null' on cache 'products'
```

## 当前代码优化点

### 已完成的优化

**1. 空字符串处理**

**优化前**:
```java
key = "'page:' + #queryRequest.productName + ..."
// productName="" 时，key = "page:::..."（三个冒号）
```

**优化后**:
```java
key = "(#queryRequest.productName != null && #queryRequest.productName != '' 
       ? #queryRequest.productName 
       : 'null')"
// productName="" 时，key = "page:null:..." ✅ 统一格式
```

**2. 缓存条件优化**

添加了 `unless` 属性：
```java
@Cacheable(value = "products", 
           key = "...",
           unless = "#result == null || #result.records.size() == 0")
// 不缓存空结果，避免缓存穿透
```

**3. 日志优化**

```java
log.info("【缓存未命中】从数据库查询产品列表 - ...");
// 只有缓存未命中时才会输出，方便监控
```

### 代码分层架构验证

**完整的请求流程**:

```
┌─────────────────────────────────────────────────────────────┐
│  1. 浏览器请求                                               │
│     GET /product/list?current=1&size=10                     │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────┐
│  2. Controller层 (ProductController)                        │
│     - Spring MVC自动绑定参数到ProductQueryRequest           │
│     - 调用: productService.getProductPage(queryRequest)     │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────┐
│  3. AOP切面 (OperationLogAspect)                            │
│     - 拦截方法调用                                           │
│     - 记录操作日志到数据库 ← 您看到的SQL                     │
│     - 过滤Model等框架对象                                    │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────┐
│  4. Spring Cache拦截 (@Cacheable)                           │
│     - 生成缓存key: product-system:products:page:1:10:...   │
│     - 查询Redis: GET key                                    │
│     ├─ 缓存命中 → 直接返回 ✅ 跳过步骤5                      │
│     └─ 缓存未命中 → 继续执行步骤5                           │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────┐
│  5. Service层 (ProductServiceImpl)                          │
│     - 输出日志: "【缓存未命中】从数据库查询..."             │
│     - 构建查询条件                                           │
│     - 调用Mapper层                                          │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────┐
│  6. Mapper层 (ProductMapper)                                │
│     - MyBatis-Plus执行SQL                                   │
│     - 查询数据库: SELECT ... FROM products                  │
│     - 返回IPage<Product>                                    │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────┐
│  7. 缓存写入 (@Cacheable)                                    │
│     - 将查询结果序列化为JSON                                │
│     - 存入Redis: SET key value EX 1800                      │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────┐
│  8. 返回视图                                                 │
│     - Controller返回: "product/list"                        │
│     - Thymeleaf渲染页面                                     │
└─────────────────────────────────────────────────────────────┘
```

**关键点**:
- **第一次请求**: 执行步骤1-8全部流程
- **第二次请求**: 执行步骤1-4，在步骤4缓存命中后跳过5-7 ← **您当前的状态**

## 验证步骤总结

### 快速验证（推荐）

**1. 清空日志文件** 或 记录当前时间

**2. 访问页面两次**:
- 第一次: http://localhost:8080/product/list
- 等待2秒
- 第二次: http://localhost:8080/product/list（刷新）

**3. 查看日志，对比两次请求**:

**第一次应该看到**:
```log
INFO  ProductServiceImpl - 【缓存未命中】从数据库查询产品列表 - current: 1, size: 10, ...
SELECT ... FROM products ...
INSERT INTO operation_logs ...
```

**第二次应该看到**:
```log
# 没有 "【缓存未命中】" 日志 ✅
# 没有 "SELECT ... FROM products" ✅
INSERT INTO operation_logs ...  # 只有这个
```

**如果第二次没有看到产品查询日志和SQL，说明缓存100%生效！** ✅

## 常见问题排查

### Q1: 为什么还看到SQL？
**A**: 您看到的是`INSERT INTO operation_logs`，这是AOP记录操作日志，与缓存无关。

### Q2: 如何区分缓存命中和未命中？
**A**: 查看日志中是否有 `【缓存未命中】从数据库查询产品列表` 这行日志。

### Q3: 缓存多久过期？
**A**: 30分钟（1800秒），在CacheConfig中配置。

### Q4: 如何手动清除缓存？
**A**: 
- 方法1: 执行任何新增/修改/删除产品操作
- 方法2: Redis命令 `FLUSHDB`
- 方法3: 重启Redis服务

---

**文档创建时间**: 2025-12-26  
**缓存状态**: ✅ 已生效  
**验证方法**: 查看日志中是否有产品查询SQL
