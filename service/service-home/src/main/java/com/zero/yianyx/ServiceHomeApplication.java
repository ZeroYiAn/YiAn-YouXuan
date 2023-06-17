package com.zero.yianyx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */
/**取消数据源自动配置
 * @author ZeroYiAn*/
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceHomeApplication.class, args);
    }
}