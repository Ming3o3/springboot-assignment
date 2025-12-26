package com.gzist.project.vo.request;

import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 产品保存请求VO（新增和编辑通用）
 *
 * @author GZIST
 * @since 2025-12-25
 */
@Data
public class ProductSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 产品ID（编辑时必填）
     */
    private Long id;

    /**
     * 产品编码
     */
    @NotBlank(message = "产品编码不能为空")
    @Size(max = 50, message = "产品编码长度不能超过50个字符")
    private String productCode;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空")
    @Size(max = 100, message = "产品名称长度不能超过100个字符")
    private String productName;

    /**
     * 产品分类
     */
    @NotBlank(message = "产品分类不能为空")
    @Size(max = 50, message = "产品分类长度不能超过50个字符")
    private String category;

    /**
     * 产品价格
     */
    @NotNull(message = "产品价格不能为空")
    @DecimalMin(value = "0.01", message = "产品价格必须大于0")
    private BigDecimal price;

    /**
     * 库存数量
     */
    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负数")
    private Integer stock;

    /**
     * 产品描述
     */
    @Size(max = 500, message = "产品描述长度不能超过500个字符")
    private String description;

    /**
     * 产品图片URL
     */
    @Size(max = 255, message = "图片URL长度不能超过255个字符")
    private String imageUrl;

    /**
     * 产品状态：0-下架，1-上架
     */
    @NotNull(message = "产品状态不能为空")
    private Integer status;
}
