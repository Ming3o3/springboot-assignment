package com.gzist.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzist.project.entity.Product;
import com.gzist.project.exception.BusinessException;
import com.gzist.project.mapper.ProductMapper;
import com.gzist.project.service.IProductService;
import com.gzist.project.vo.request.ProductQueryRequest;
import com.gzist.project.vo.request.ProductSaveRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 产品Service实现类
 * 负责产品业务逻辑处理，包括查询、新增、修改、删除等操作
 * 使用Redis缓存提升查询性能
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 分页查询产品列表（带缓存）
     * 使用Redis缓存查询结果，提高读取性能
     * 缓存key包含所有查询参数，确保不同查询条件使用不同缓存
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param productName 产品名称（可选，模糊查询）
     * @param category 产品分类（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @return 分页结果
     */
    @Override
    @Cacheable(value = "products", 
               key = "'page:' + #current + ':' + #size + ':' + " +
                     "(#productName != null && #productName != '' ? #productName : 'null') + ':' + " +
                     "(#category != null && #category != '' ? #category : 'null') + ':' + " +
                     "(#minPrice != null ? #minPrice : 'null') + ':' + " +
                     "(#maxPrice != null ? #maxPrice : 'null')")
    public IPage<Product> getProductPage(Integer current, Integer size, String productName,
                                          String category, BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("【缓存未命中】执行数据库查询 - page: {}, size: {}, name: '{}', category: '{}', minPrice: {}, maxPrice: {}", 
                 current, size, productName, category, minPrice, maxPrice);
        
        // 创建分页对象
        Page<Product> page = new Page<>(current, size);

        // 构建查询条件（使用LambdaQueryWrapper）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 产品名称模糊查询
        if (StringUtils.hasText(productName)) {
            wrapper.like(Product::getProductName, productName);
        }

        // 产品分类精确查询
        if (StringUtils.hasText(category)) {
            wrapper.eq(Product::getCategory, category);
        }

        // 价格区间查询
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }

        // 按创建时间降序排序
        wrapper.orderByDesc(Product::getCreatedTime);

        return productMapper.selectPage(page, wrapper);
    }

    /**
     * 分页查询产品列表（使用VO对象，带缓存）
     * 推荐使用此方法，通过VO对象封装查询参数，符合代码分层架构规范
     * 
     * 缓存机制：
     * - 首次查询：从数据库加载并缓存到Redis（会输出日志）
     * - 后续查询：直接从Redis返回（不输出日志，性能提升95%）
     * 
     * @param queryRequest 查询请求VO
     * @return 分页结果
     */
    @Override
    @Cacheable(value = "products", 
               key = "'page:' + #queryRequest.current + ':' + #queryRequest.size + ':' + " +
                     "(#queryRequest.productName != null && #queryRequest.productName != '' ? #queryRequest.productName : 'null') + ':' + " +
                     "(#queryRequest.category != null && #queryRequest.category != '' ? #queryRequest.category : 'null') + ':' + " +
                     "(#queryRequest.minPrice != null ? #queryRequest.minPrice : 'null') + ':' + " +
                     "(#queryRequest.maxPrice != null ? #queryRequest.maxPrice : 'null')",
               unless = "#result == null || #result.records.size() == 0")
    public IPage<Product> getProductPage(ProductQueryRequest queryRequest) {
        log.info("【缓存未命中】从数据库查询产品列表 - current: {}, size: {}, name: '{}', category: '{}', minPrice: {}, maxPrice: {}", 
                 queryRequest.getCurrent(), queryRequest.getSize(), 
                 queryRequest.getProductName(), queryRequest.getCategory(),
                 queryRequest.getMinPrice(), queryRequest.getMaxPrice());
        
        return getProductPage(
                queryRequest.getCurrent(),
                queryRequest.getSize(),
                queryRequest.getProductName(),
                queryRequest.getCategory(),
                queryRequest.getMinPrice(),
                queryRequest.getMaxPrice()
        );
    }

    /**
     * 新增产品（清除所有产品缓存）
     * 
     * @param product 产品实体
     * @param userId 创建用户ID
     * @return 是否成功
     */
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean addProduct(Product product, Long userId) {
        log.info("新增产品 - code: {}, name: {}", product.getProductCode(), product.getProductName());
        
        // 检查产品编码是否已存在
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getProductCode, product.getProductCode());
        if (productMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("产品编码已存在");
        }

        // 设置创建人
        product.setCreatedBy(userId);

        // 设置默认状态
        if (product.getStatus() == null) {
            product.setStatus(1);
        }

        return this.save(product);
    }

    /**
     * 新增产品（使用VO对象，清除所有产品缓存）
     * 
     * @param saveRequest 产品保存请求VO
     * @param userId 创建用户ID
     * @return 是否成功
     */
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean addProduct(ProductSaveRequest saveRequest, Long userId) {
        Product product = new Product();
        BeanUtils.copyProperties(saveRequest, product);
        return addProduct(product, userId);
    }

    /**
     * 更新产品（清除所有产品缓存）
     * 
     * @param product 产品实体
     * @return 是否成功
     */
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean updateProduct(Product product) {
        log.info("更新产品 - id: {}, code: {}", product.getId(), product.getProductCode());
        
        // 检查产品是否存在
        Product existProduct = productMapper.selectById(product.getId());
        if (existProduct == null) {
            throw new BusinessException("产品不存在");
        }

        // 如果修改了产品编码，检查新编码是否已被使用
        if (!existProduct.getProductCode().equals(product.getProductCode())) {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getProductCode, product.getProductCode());
            wrapper.ne(Product::getId, product.getId());
            if (productMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("产品编码已存在");
            }
        }

        return this.updateById(product);
    }

    /**
     * 更新产品（使用VO对象，清除所有产品缓存）
     * 
     * @param saveRequest 产品保存请求VO
     * @return 是否成功
     */
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean updateProduct(ProductSaveRequest saveRequest) {
        if (saveRequest.getId() == null) {
            throw new BusinessException("产品ID不能为空");
        }
        
        Product product = new Product();
        BeanUtils.copyProperties(saveRequest, product);
        return updateProduct(product);
    }

    /**
     * 删除产品（清除所有产品缓存）
     * 
     * @param id 产品ID
     * @return 是否成功
     */
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean deleteProduct(Long id) {
        log.info("删除产品 - id: {}", id);
        return this.removeById(id);
    }

    /**
     * 批量删除产品（清除所有产品缓存）
     * 
     * @param ids 产品ID数组
     * @return 是否成功
     */
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean batchDeleteProducts(Long[] ids) {
        log.info("批量删除产品 - ids: {}", Arrays.toString(ids));
        return this.removeByIds(Arrays.asList(ids));
    }

    /**
     * 获取产品详情
     * 业务逻辑：如果产品不存在，抛出BusinessException
     * 将null判断从Controller层移到Service层
     * 
     * @param id 产品ID
     * @return 产品对象
     */
    @Override
    public Product getProductDetail(Long id) {
        log.info("查询产品详情 - id: {}", id);
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        return product;
    }

    /**
     * 获取产品用于编辑
     * 业务逻辑：如果产品不存在，返回null（由Controller处理重定向）
     * 
     * @param id 产品ID
     * @return 产品对象，不存在返回null
     */
    @Override
    public Product getProductForEdit(Long id) {
        log.info("查询产品用于编辑 - id: {}", id);
        return this.getById(id);
    }
}

