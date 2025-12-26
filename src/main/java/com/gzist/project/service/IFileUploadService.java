package com.gzist.project.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传Service接口
 *
 * @author GZIST
 * @since 2025-12-25
 */
public interface IFileUploadService {

    /**
     * 上传产品图片
     *
     * @param file 上传的文件
     * @return 文件访问URL
     */
    String uploadProductImage(MultipartFile file);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 删除文件（带验证）
     * 业务逻辑：如果删除失败，抛出BusinessException
     *
     * @param fileUrl 文件URL
     */
    void deleteFileWithValidation(String fileUrl);
}
