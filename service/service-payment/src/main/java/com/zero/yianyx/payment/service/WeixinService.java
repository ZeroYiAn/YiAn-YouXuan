package com.zero.yianyx.payment.service;

import java.util.Map;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/16
 */

public interface WeixinService {
    /**
     * 根据订单号下单，生成支付链接
     * @param orderNo
     * @return
     */
    Map createJsapi(String orderNo);

    /**
     * 根据订单号去微信第三方查询支付状态
     * @param orderNo
     * @return
     */
    Map queryPayStatus(String orderNo);
}
