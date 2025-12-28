package com.gzist.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 对应数据库表：operation_logs
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("operation_logs")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID（主键，无业务意义）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户名（关联users.username）
     */
    @TableField("username")
    private String username;

    /**
     * 操作类型
     */
    @TableField("operation")
    private String operation;

    /**
     * 操作方法
     */
    @TableField("method")
    private String method;

    /**
     * 请求参数
     */
    @TableField("params")
    private String params;

    /**
     * 操作IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 操作时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
