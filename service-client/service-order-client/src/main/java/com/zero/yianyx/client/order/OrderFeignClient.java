package com.zero.yianyx.client.order;

import com.zero.yianyx.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/16
 */
@FeignClient("service-order")
public interface OrderFeignClient {
    @GetMapping("/api/order/inner/getOrderInfo/{orderNo}")
    public OrderInfo getOrderInfo(@PathVariable("orderNo")String orderNo);
}
