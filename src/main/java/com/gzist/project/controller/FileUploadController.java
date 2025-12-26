package com.gzist.project.controller;

import com.gzist.project.common.Result;
import com.gzist.project.service.IFileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 *
 * @author GZIST
 * @since 2025-12-25
 */
@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    @Autowired
    private IFileUploadService fileUploadService;

    /**
     * 上传产品图片
     */
    @PostMapping("/upload/product-image")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileUploadService.uploadProductImage(file);
        Result<String> result = new Result<>(200, "图片上传成功", imageUrl, true);
        return result;
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        boolean success = fileUploadService.deleteFile(fileUrl);
        if (success) {
            return Result.success("文件删除成功");
        }
        return Result.error("文件删除失败");
    }
}
