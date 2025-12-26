package com.gzist.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * 文件上传配置类
 *
 * @author GZIST
 * @since 2025-12-25
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /**
     * 文件上传根路径
     */
    private String path = "uploads/";

    /**
     * 允许上传的文件类型
     */
    private String[] allowedTypes = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    /**
     * 单个文件最大大小（字节）默认5MB
     */
    private Long maxSize = 5 * 1024 * 1024L;

    /**
     * 产品图片存储路径
     */
    private String productImagePath = "products/";
}
