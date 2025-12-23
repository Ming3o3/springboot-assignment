package com.gzist.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gzist.project.entity.Product;

import java.math.BigDecimal;

/**
 * 产品Service接口
 *
 * @author GZIST
 * @since 2025-12-23
 */
public interface IProductService extends IService<Product> {

    /**
     * 分页查询产品（支持条件查询）
     *
     * @param current 当前页
     * @param size 每页大小
     * @param productName 产品名称（模糊查询）
     * @param category 产品分类
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 分页结果
     */
    IPage<Product> getProductPage(Integer current, Integer size, String productName,
                                   String category, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 新增产品
     *
     * @param product 产品对象
     * @param userId 创建人ID
     * @return 是否成功
     */
    boolean addProduct(Product product, Long userId);

    /**
     * 更新产品
     *
     * @param product 产品对象
     * @return 是否成功
     */
    boolean updateProduct(Product product);

    /**
     * 删除产品
     *
     * @param id 产品ID
     * @return 是否成功
     */
    boolean deleteProduct(Long id);

    /**
     * 批量删除产品
     *
     * @param ids 产品ID数组
     * @return 是否成功
     */
    boolean batchDeleteProducts(Long[] ids);
}
