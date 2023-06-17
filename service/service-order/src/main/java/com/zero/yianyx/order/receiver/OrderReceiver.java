package com.zero.yianyx.order.receiver;

import com.rabbitmq.client.Channel;
import com.zero.yianyx.mq.constant.MqConst;
import com.zero.yianyx.order.service.OrderInfoService;
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
 * @description: mq消息接收端
 * @author: ZeroYiAn
 * @time: 2023/6/16
 */

@Component
public class OrderReceiver {

    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 订单支付完成，更改订单状态与通知扣减库存
     * @param orderNo
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER_PAY, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_PAY_DIRECT),
            key = {MqConst.ROUTING_PAY_SUCCESS}
    ))
    public void orderPay(String orderNo, Message message, Channel channel) throws IOException {
        if (!StringUtils.isEmpty(orderNo)){
            // 支付成功！ 修改订单状态为已支付
            orderInfoService.orderPay(orderNo);
        }
        //消费的手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
