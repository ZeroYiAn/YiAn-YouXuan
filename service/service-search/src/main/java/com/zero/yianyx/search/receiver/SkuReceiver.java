package com.zero.yianyx.search.receiver;


import com.rabbitmq.client.Channel;
import com.zero.yianyx.mq.constant.MqConst;
import com.zero.yianyx.search.service.SkuService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @description: 接收MQ消息,ES进行商品上架、下架动作
 * @author: ZeroYiAn
 * @time: 2023/6/10
 */
@Component
public class SkuReceiver {
    @Resource
    private SkuService skuService;

    /**
     * 商品上架：从消息队列中接受到消息，得到上架商品的skuId
     * 然后调用upperSku(),即通过远程调用，得到商品信息，最终将商品信息同步到ES中
     * @param skuId
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_GOODS_DIRECT),
            key = {MqConst.ROUTING_GOODS_UPPER}
    ))
    public void upperSku(Long skuId, Message message, Channel channel) throws IOException {
        if (null != skuId) {
            skuService.upperSku(skuId);
        }
        /**
         * 手动确认
         * 第一个参数：表示收到的消息的标号
         * 第二个参数：如果为true表示可以签收多个消息
         */
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 商品下架：从消息队列中接受到消息，得到下架商品的skuId
     * 然后调用lowerSku(),将商品信息从ES中删除即可
     * @param skuId
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_GOODS_DIRECT),
            key = {MqConst.ROUTING_GOODS_LOWER}
    ))
    public void lowerSku(Long skuId, Message message, Channel channel) throws IOException {
        if (null != skuId) {
            skuService.lowerSku(skuId);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


}
