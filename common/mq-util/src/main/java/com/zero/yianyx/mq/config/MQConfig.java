package com.zero.yianyx.mq.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 默认是字符串转换器,设置后可以把对象转成json格式对象
 * @author: ZeroYiAn
 * @time: 2023/6/10
 */

@Configuration
public class MQConfig {

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
