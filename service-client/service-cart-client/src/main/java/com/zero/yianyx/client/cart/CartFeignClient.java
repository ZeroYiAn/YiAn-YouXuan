package com.zero.yianyx.client.cart;

import com.zero.yianyx.model.order.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/15
 */
@FeignClient(value = "service-cart")
public interface CartFeignClient {
    /**
     * 根据用户Id 查询购物车列表
     * @param userId
     */
    @GetMapping("/api/cart/inner/getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId);
}