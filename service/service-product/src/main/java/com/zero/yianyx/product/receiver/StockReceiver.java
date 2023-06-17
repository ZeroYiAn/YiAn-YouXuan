package com.zero.yianyx.product.receiver;

import com.rabbitmq.client.Channel;
import com.zero.yianyx.mq.constant.MqConst;
import com.zero.yianyx.product.service.SkuInfoService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/16
 */

@Component
public class StockReceiver {

    @Resource
    private SkuInfoService skuInfoService;

    /**
     * 扣减库存成功，更新订单状态
     * @param orderNo
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MINUS_STOCK, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_ORDER_DIRECT),
            key = {MqConst.ROUTING_MINUS_STOCK}
    ))
    public void minusStock(String orderNo, Message message, Channel channel) throws IOException {
        if (!StringUtils.isEmpty(orderNo)){
            //扣减库存
            skuInfoService.minusStock(orderNo);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}