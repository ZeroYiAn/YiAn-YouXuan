package com.zero.yianyx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @description: 权限管理模块启动类
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */


@SpringBootApplication
@EnableDiscoveryClient
public class ServiceAclApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAclApplication.class, args);
    }

}
