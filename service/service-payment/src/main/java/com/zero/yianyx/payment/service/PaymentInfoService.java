package com.zero.yianyx.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.order.PaymentInfo;

import java.util.Map;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/16
 */
public interface PaymentInfoService extends IService<PaymentInfo> {
    /**
     * 保存交易记录
     * @param orderNo
     */
    PaymentInfo savePaymentInfo(String orderNo);

    PaymentInfo getPaymentInfo(String orderNo);

    //支付成功
    void paySuccess(String orderNo, Map<String,String> paramMap);
}
