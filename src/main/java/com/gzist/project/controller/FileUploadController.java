package com.gzist.project.controller;

import com.gzist.project.common.Result;
import com.gzist.project.service.IFileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 * 负责处理文件上传、删除等操作
 * 遵循RESTful设计，所有方法返回JSON格式数据
 *
 * @author GZIST
 * @since 2025-12-25
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    @Autowired
    private IFileUploadService fileUploadService;

    /**
     * 上传产品图片
     * 支持jpg、jpeg、png、gif、webp格式，最大5MB
     */
    @PostMapping("/upload/product-image")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileUploadService.uploadProductImage(file);
        return Result.success(imageUrl);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        fileUploadService.deleteFileWithValidation(fileUrl);
        return Result.success("文件删除成功");
    }
}
