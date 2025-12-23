package com.gzist.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzist.project.entity.Product;
import com.gzist.project.mapper.ProductMapper;
import com.gzist.project.service.IProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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

        // 构建查询条件
        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        // 产品名称模糊查询
        if (StringUtils.isNotBlank(productName)) {
            wrapper.like("product_name", productName);
        }

        // 产品分类精确查询
        if (StringUtils.isNotBlank(category)) {
            wrapper.eq("category", category);
        }

        // 价格区间查询
        if (minPrice != null) {
            wrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            wrapper.le("price", maxPrice);
        }

        // 按创建时间降序排序
        wrapper.orderByDesc("created_time");

        return productMapper.selectPage(page, wrapper);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean addProduct(Product product, Long userId) {
        // 检查产品编码是否已存在
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("product_code", product.getProductCode());
        if (productMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("产品编码已存在");
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
    public boolean updateProduct(Product product) {
        // 检查产品是否存在
        Product existProduct = productMapper.selectById(product.getId());
        if (existProduct == null) {
            throw new RuntimeException("产品不存在");
        }

        // 如果修改了产品编码，检查新编码是否已被使用
        if (!existProduct.getProductCode().equals(product.getProductCode())) {
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("product_code", product.getProductCode());
            wrapper.ne("id", product.getId());
            if (productMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("产品编码已存在");
            }
        }

        return this.updateById(product);
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
