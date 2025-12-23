package com.gzist.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体类
 * 对应数据库表：products
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("products")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 产品ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 产品编码（唯一）
     */
    @TableField("product_code")
    private String productCode;

    /**
     * 产品分类
     */
    @TableField("category")
    private String category;

    /**
     * 产品价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 库存数量
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 产品描述
     */
    @TableField("description")
    private String description;

    /**
     * 产品图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 状态（1:上架 0:下架）
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
