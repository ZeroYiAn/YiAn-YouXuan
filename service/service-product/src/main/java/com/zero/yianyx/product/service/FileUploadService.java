package com.zero.yianyx.product.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */

public interface FileUploadService {
    /**
     * 文件上传
     * @param file
     * @return
     * @throws Exception
     */
    String fileUpload(MultipartFile file)throws Exception;


}