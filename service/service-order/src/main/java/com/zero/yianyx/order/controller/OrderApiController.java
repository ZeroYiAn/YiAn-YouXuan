package com.zero.yianyx.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.yianyx.common.auth.AuthContextHolder;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.order.OrderInfo;
import com.zero.yianyx.order.service.OrderInfoService;
import com.zero.yianyx.vo.order.OrderSubmitVo;
import com.zero.yianyx.vo.order.OrderUserQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/15
 */
@RestController
@RequestMapping(value="/api/order")
public class OrderApiController {

    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation(value = "获取用户订单分页列表")
    @GetMapping("auth/findUserOrderPage/{page}/{limit}")
    public Result findUserOrderPage(@PathVariable Long page, @PathVariable Long limit,
                    OrderUserQueryVo orderUserQueryVo) {
        Long userId = AuthContextHolder.getUserId();
        orderUserQueryVo.setUserId(userId);
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = orderInfoService.getOrderInfoByUserIdPage(pageParam, orderUserQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("确认订单")
    @GetMapping("auth/confirmOrder")
    public Result confirm() {
        return Result.ok(orderInfoService.confirmOrder());
    }

    @ApiOperation("生成订单")
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderSubmitVo orderParamVo) {

        return Result.ok(orderInfoService.submitOrder(orderParamVo));
    }

    @ApiOperation("获取订单详情")
    @GetMapping("auth/getOrderInfoById/{orderId}")
    public Result getOrderInfoById(@PathVariable("orderId") Long orderId){
        return Result.ok(orderInfoService.getOrderInfoById(orderId));
    }

    @GetMapping("inner/getOrderInfo/{orderNo}")
    public OrderInfo getOrderInfo(@PathVariable("orderNo")String orderNo){
        OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        return orderInfo;
    }
}
