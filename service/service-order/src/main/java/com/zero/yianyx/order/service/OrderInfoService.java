package com.zero.yianyx.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.order.OrderInfo;
import com.zero.yianyx.vo.order.OrderConfirmVo;
import com.zero.yianyx.vo.order.OrderSubmitVo;
import com.zero.yianyx.vo.order.OrderUserQueryVo;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/15
 */
public interface OrderInfoService extends IService<OrderInfo> {
    /**
     * 确认订单
     */
    OrderConfirmVo confirmOrder();

    /**
     * 生成订单
     * @param orderParamVo
     * @return
     */
    Long submitOrder(OrderSubmitVo orderParamVo);

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfoById(Long orderId);

    /**
     * 根据订单号查询订单信息
     * @param orderNo
     * @return
     */
    OrderInfo getOrderInfoByOrderNo(String orderNo);

    /**
     * 订单支付，更改订单状态并通知扣减库存
     * @param orderNo
     */
    void orderPay(String orderNo);

    /**
     * 获取用户订单页面信息
     * @param pageParam
     * @param orderUserQueryVo
     * @return
     */
    IPage<OrderInfo> getOrderInfoByUserIdPage(Page<OrderInfo> pageParam, OrderUserQueryVo orderUserQueryVo);
}
