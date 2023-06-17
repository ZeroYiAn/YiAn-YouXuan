package com.zero.yianyx.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.client.order.OrderFeignClient;
import com.zero.yianyx.common.exception.YianyxException;
import com.zero.yianyx.common.result.ResultCodeEnum;
import com.zero.yianyx.enums.PaymentStatus;
import com.zero.yianyx.enums.PaymentType;
import com.zero.yianyx.model.order.OrderInfo;
import com.zero.yianyx.model.order.PaymentInfo;
import com.zero.yianyx.mq.constant.MqConst;
import com.zero.yianyx.mq.service.RabbitService;
import com.zero.yianyx.payment.mapper.PaymentInfoMapper;
import com.zero.yianyx.payment.service.PaymentInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/16
 */
@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    @Resource
    private OrderFeignClient orderFeignClient;

    @Resource
    private RabbitService rabbitService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PaymentInfo savePaymentInfo(String orderNo) {
        //通过远程调用，根据orderNo查询订单信息
        OrderInfo order = orderFeignClient.getOrderInfo(orderNo);
        if(null == order) {
            //没有订单数据
            throw new YianyxException(ResultCodeEnum.DATA_ERROR);
        }
        // 保存交易记录,封装到PaymentInfo对象
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(PaymentType.WEIXIN);
        paymentInfo.setUserId(order.getUserId());
        paymentInfo.setOrderNo(order.getOrderNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        String subject = "userID"+order.getUserId()+"支付订单";
        paymentInfo.setSubject(subject);
        //TODO 为了测试
        paymentInfo.setTotalAmount(new BigDecimal("0.01"));
        //调用方法实现添加
        baseMapper.insert(paymentInfo);
        return paymentInfo;
    }

    @Override
    public PaymentInfo getPaymentInfo(String orderNo) {
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOrderNo, orderNo);
        return baseMapper.selectOne(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void paySuccess(String orderNo,Map<String,String> paramMap) {
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOrderNo, orderNo);
        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);
        if (paymentInfo.getPaymentStatus() != PaymentStatus.UNPAID) {
            return;
        }

        PaymentInfo paymentInfoUpd = new PaymentInfo();
        paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
        String tradeNo = paramMap.get("ransaction_id");
        paymentInfoUpd.setTradeNo(tradeNo);
        paymentInfoUpd.setCallbackTime(new Date());
        paymentInfoUpd.setCallbackContent(paramMap.toString());
        baseMapper.update(paymentInfoUpd, new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderNo, orderNo));
        // 表示交易成功！
        //整合rabbitMQ异步实现实现，①发送消息到order模块修改订单记录为已经支付；② order模块修改完再发送MQ消息通知product模块扣减库存
        //发送消息: 传递订单号orderNo
        rabbitService.sendMessage(MqConst.EXCHANGE_PAY_DIRECT, MqConst.ROUTING_PAY_SUCCESS, orderNo);
    }
}
