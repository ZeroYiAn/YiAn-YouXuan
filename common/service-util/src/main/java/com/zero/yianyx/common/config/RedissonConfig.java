package com.zero.yianyx.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/15
 */

@Configuration
public class RedissonConfig {

    /**
     * 自动装配
     *
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        //  配置host，port等参数
        SingleServerConfig serverConfig = config.useSingleServer()
                //redis://127.0.0.1:7181
                .setAddress("redis://192.168.200.155:6379")
                .setTimeout(3000)
                .setPingConnectionInterval(60000)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(10);
        serverConfig.setPassword("780415");
        return Redisson.create(config);
    }
}
