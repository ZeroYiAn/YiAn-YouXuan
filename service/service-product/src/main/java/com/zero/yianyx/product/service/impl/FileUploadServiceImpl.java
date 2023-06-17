package com.zero.yianyx.product.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.zero.yianyx.product.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {
    @Value("${aliyun.endpoint}")
    private String endPoint;
    @Value("${aliyun.keyid}")
    private String accessKey;
    @Value("${aliyun.keysecret}")
    private String secretKey;
    @Value("${aliyun.bucketname}")
    private String bucketName;
    @Override
    public String fileUpload(MultipartFile file) throws Exception {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endPoint, accessKey, secretKey);
        try {
            // 上传文件输入流
            InputStream inputStream = file.getInputStream();
            //上传文件路径+名称
            String fileName = file.getOriginalFilename();
            //生成随机唯一值，使用uuid，并去掉生成的uuid里面的“-”，添加到文件名称里面
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            fileName = uuid+fileName;
            //按照当前日期，创建文件夹，上传到创建文件夹里面
            //  2021/02/02/01.jpg
            String timeUrl = new DateTime().toString("yyyy/MM/dd");
            fileName = timeUrl+"/"+fileName;
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);
            //设置该属性可以返回response，如果不设置，则返回的response为空
            putObjectRequest.setProcess("true");
            //调用方法实现上传
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            //如果上传成功返回200
            log.info("文件上传返回状态码:"+result.getResponse().getStatusCode());
            log.info("文件上传错误信息：{}",result.getResponse().getErrorResponseAsString());
            log.info("文件上传路径：{}",result.getResponse().getUri());

            // 关闭OSSClient。
            ossClient.shutdown();
            //上传之后文件路径
            // https://zero-yianyx.oss-cn-shanghai.aliyuncs.com/01.jpg
            //返回
            return "https://"+bucketName+"."+endPoint+"/"+fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
