package com.zero.yianyx.cart.controller;

import com.zero.yianyx.cart.service.CartInfoService;
import com.zero.yianyx.client.activity.ActivityFeignClient;
import com.zero.yianyx.common.auth.AuthContextHolder;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.order.CartInfo;
import com.zero.yianyx.vo.order.OrderConfirmVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:  购物车相关功能处理器
 * @author: ZeroYiAn
 * @time: 2023/6/14
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {
    @Resource
    private CartInfoService cartInfoService;
    @Resource
    private ActivityFeignClient activityFeignClient;

    /**
     * 查询购物车列表
     */
    @GetMapping("cartList")
    public Result cartList() {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);
        return Result.ok(cartInfoList);
    }

    /**
     * 查询带活动以及优惠券的购物车
     */
    @GetMapping("activityCartList")
    public Result activityCartList() {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);
        //远程调用：获取购物车中满足条件的促销活动和优惠券信息
        OrderConfirmVo orderTradeVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);
        return Result.ok(orderTradeVo);
    }
    /**
     * 添加若干商品到购物车
     */
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum) {
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.addToCart(skuId, userId, skuNum);
        return Result.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId) {
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteCart(skuId, userId);
        return Result.ok();
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("deleteAllCart")
    public Result deleteAllCart(){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteAllCart(userId);
        return Result.ok();
    }

    /**
     * 批量删除购物车
     */
    @PostMapping("batchDeleteCart")
    public Result batchDeleteCart(@RequestBody List<Long> skuIdList){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchDeleteCart(skuIdList, userId);
        return Result.ok();
    }


    /**
     * 更新选中状态
     * @param skuId
     * @param isChecked
     * @return
     */
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable(value = "skuId") Long skuId,
                            @PathVariable(value = "isChecked") Integer isChecked) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        // 调用更新方法
        cartInfoService.checkCart(userId, isChecked, skuId);
        return Result.ok();
    }

    /**
     * 全部选中/不选中
     * @param isChecked
     * @return
     */
    @GetMapping("checkAllCart/{isChecked}")
    public Result checkAllCart(@PathVariable(value = "isChecked") Integer isChecked) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        // 调用更新方法
        cartInfoService.checkAllCart(userId, isChecked);
        return Result.ok();
    }

    /**
     * 批量选中
     * @param skuIdList
     * @param isChecked
     * @return
     */
    @PostMapping("batchCheckCart/{isChecked}")
    public Result batchCheckCart(@RequestBody List<Long> skuIdList, @PathVariable(value = "isChecked") Integer isChecked){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchCheckCart(skuIdList, userId, isChecked);
        return Result.ok();
    }

    /**
     * 获取当前用户选中的购物项
     * @param userId
     * @return
     */
    @GetMapping("inner/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId) {
        return cartInfoService.getCartCheckedList(userId);
    }
}
