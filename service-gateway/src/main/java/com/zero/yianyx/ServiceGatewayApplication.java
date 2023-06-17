package com.zero.yianyx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/11
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceGatewayApplication.class, args);
    }
}
