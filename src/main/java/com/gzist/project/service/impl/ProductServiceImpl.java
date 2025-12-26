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
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Cacheable(value = "products", key = "#current + '-' + #size + '-' + #productName + '-' + #category + '-' + #minPrice + '-' + #maxPrice")
    public IPage<Product> getProductPage(Integer current, Integer size, String productName,
                                          String category, BigDecimal minPrice, BigDecimal maxPrice) {
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

    @Override
    @Cacheable(value = "products", key = "#queryRequest.current + '-' + #queryRequest.size + '-' + #queryRequest.productName + '-' + #queryRequest.category + '-' + #queryRequest.minPrice + '-' + #queryRequest.maxPrice")
    public IPage<Product> getProductPage(ProductQueryRequest queryRequest) {
        return getProductPage(
                queryRequest.getCurrent(),
                queryRequest.getSize(),
                queryRequest.getProductName(),
                queryRequest.getCategory(),
                queryRequest.getMinPrice(),
                queryRequest.getMaxPrice()
        );
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean addProduct(Product product, Long userId) {
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

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean addProduct(ProductSaveRequest saveRequest, Long userId) {
        Product product = new Product();
        BeanUtils.copyProperties(saveRequest, product);
        return addProduct(product, userId);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean updateProduct(Product product) {
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

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean deleteProduct(Long id) {
        return this.removeById(id);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean batchDeleteProducts(Long[] ids) {
        return this.removeByIds(Arrays.asList(ids));
    }
}
