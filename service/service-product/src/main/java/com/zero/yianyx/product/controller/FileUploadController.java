package com.zero.yianyx.product.controller;

import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.product.service.FileUploadService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @description: 文件上传处理器 : 注意这里要跨域
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */


@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class FileUploadController {

    @Resource
    private FileUploadService fileUploadService;

    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception{
        String url = fileUploadService.fileUpload(file);
        return Result.ok(url);
    }
}