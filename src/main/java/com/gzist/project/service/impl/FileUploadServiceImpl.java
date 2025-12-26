package com.gzist.project.service.impl;

import com.gzist.project.config.FileUploadConfig;
import com.gzist.project.exception.BusinessException;
import com.gzist.project.service.IFileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * 文件上传Service实现类
 *
 * @author GZIST
 * @since 2025-12-25
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements IFileUploadService {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Override
    public String uploadProductImage(MultipartFile file) {
        // 1. 校验文件
        validateFile(file);

        // 2. 生成文件存储路径（按日期分目录）
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uploadPath = fileUploadConfig.getPath() + fileUploadConfig.getProductImagePath() + datePath + "/";

        // 3. 创建目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 4. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;

        // 5. 保存文件
        try {
            Path filePath = Paths.get(uploadPath + newFileName);
            Files.write(filePath, file.getBytes());
            log.info("文件上传成功: {}", filePath);

            // 6. 返回访问URL
            return "/uploads/" + fileUploadConfig.getProductImagePath() + datePath + "/" + newFileName;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return false;
        }

        try {
            // 从URL转换为实际文件路径
            String filePath = fileUrl.replace("/uploads/", "");
            Path path = Paths.get(fileUploadConfig.getPath() + filePath);
            
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("文件删除成功: {}", path);
                return true;
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}", fileUrl, e);
        }
        return false;
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > fileUploadConfig.getMaxSize()) {
            throw new BusinessException("文件大小不能超过" + (fileUploadConfig.getMaxSize() / 1024 / 1024) + "MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(fileUploadConfig.getAllowedTypes()).contains(contentType)) {
            throw new BusinessException("不支持的文件类型，仅支持图片格式（jpg, png, gif, webp）");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
