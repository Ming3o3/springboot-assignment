package com.gzist.project.vo.request;

import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 产品查询请求VO
 *
 * @author GZIST
 * @since 2025-12-25
 */
@Data
public class ProductQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    @Min(value = 1, message = "页码最小值为1")
    private Integer current = 1;

    /**
     */
    @Min(value = 1, message = "每页大小最小值为1")
    @Max(value = 100, message = "每页大小最大值为100")
    private Integer size = 10;

    /**
     * 产品名称（模糊查询）
     */
    private String productName;

    /**
     * 产品分类
     */
    private String category;

    /**
     * 最低价格
     */
    @DecimalMin(value = "0.00", message = "价格不能为负数")
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    @DecimalMin(value = "0.00", message = "价格不能为负数")
    private BigDecimal maxPrice;
}
