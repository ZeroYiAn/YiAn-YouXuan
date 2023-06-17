package com.zero.yianyx.mq.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: RabbitMq消息发送服务类
 * @author: ZeroYiAn
 * @time: 2023/6/10
 */
@Service
@Slf4j
public class RabbitService {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param exchange      交换机
     * @param routingKey    路由键
     * @param message       消息
     * @return
     */
    public boolean sendMessage(String exchange,String routingKey, Object message){
        //  调用发送数据的方法
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
        log.info("发送MQ消息:{}", JSON.toJSONString(message));
        return true;
    }

}
