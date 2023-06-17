package com.zero.yianyx.payment.controller;

import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.common.result.ResultCodeEnum;
import com.zero.yianyx.payment.service.PaymentInfoService;
import com.zero.yianyx.payment.service.WeixinService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @description: 微信支付接口
 * @author: ZeroYiAn
 * @time: 2023/6/16
 */
@RestController
@RequestMapping("/api/payment/weixin")
@Slf4j
public class WeixinController {
    @Resource
    private WeixinService weixinPayService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @ApiOperation(value = "下单 小程序支付")
    @GetMapping("/createJsapi/{orderNo}")
    public Result createJsapi(@PathVariable("orderNo") String orderNo) {
        return Result.ok(weixinPayService.createJsapi(orderNo));
    }

    @ApiOperation(value = "查询支付状态")
    @GetMapping("/queryPayStatus/{orderNo}")
    public Result queryPayStatus(@PathVariable("orderNo") String orderNo) {
        //1.调用微信支付系统接口查询订单支付系统
        Map<String, String> resultMap = weixinPayService.queryPayStatus(orderNo);
        //2.返回值为null，支付失败
        if (resultMap == null) {
            return Result.build(null, ResultCodeEnum.PAYMENT_FAIL.getCode(),ResultCodeEnum.PAYMENT_FAIL.getMessage());
        }
        //3.如果支付成功，修改支付记录表状态：已经支付   并 扣减库存
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {
            //更改订单状态，处理支付结果
            String out_trade_no = resultMap.get("out_trade_no");
            paymentInfoService.paySuccess(out_trade_no, resultMap);
            return Result.build(null, ResultCodeEnum.PAYMENT_SUCCESS.getCode(),ResultCodeEnum.PAYMENT_SUCCESS.getMessage());
        }
        //4.支付中，等待
        return Result.build(null, ResultCodeEnum.PAYMENT_WAITING.getCode(),ResultCodeEnum.PAYMENT_WAITING.getMessage());
    }
}