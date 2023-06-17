package com.zero.yianyx.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.WXPayUtil;
import com.zero.yianyx.model.order.PaymentInfo;
import com.zero.yianyx.payment.service.WeixinService;
import com.zero.yianyx.payment.utils.ConstantPropertiesUtils;
import com.zero.yianyx.payment.utils.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/16
 */
@Service
@Slf4j
public class WeixinServiceImpl implements WeixinService {
    @Resource
    private PaymentInfoServiceImpl paymentService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 调用微信支付系统，生成预付单
     * 根据订单号下单，生成支付链接
     * @param orderNo
     * @return
     */
    @Override
    public Map<String, String> createJsapi(String orderNo) {
        try {

            PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderNo);
            if(null == paymentInfo) {
                //添加支付记录
                paymentInfo = paymentService.savePaymentInfo(orderNo);
            }

            //封装微信支付系统所需要的参数
            Map<String, String> paramMap = new HashMap();
            //1、设置参数
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("body", paymentInfo.getSubject());
            paramMap.put("out_trade_no", paymentInfo.getOrderNo());
            int totalFee = paymentInfo.getTotalAmount().multiply(new BigDecimal(100)).intValue();
            paramMap.put("total_fee", String.valueOf(totalFee));
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", ConstantPropertiesUtils.NOTIFYURL);
            paramMap.put("trade_type", "JSAPI");

            //从redis中获取openid
//            UserLoginVo userLoginVo = (UserLoginVo)redisTemplate.opsForValue().get("user:login:" + paymentInfo.getUserId());
//            if(null != userLoginVo && !StringUtils.isEmpty(userLoginVo.getOpenId())) {
//                paramMap.put("openid", userLoginVo.getOpenId());
//            } else {
//                paramMap.put("openid", "oD7av4igt-00GI8PqsIlg5FROYnI");
//            }

            paramMap.put("openid", "oD7av4igt-00GI8PqsIlg5FROYnI");

            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");

            //client设置参数 xml格式
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            //发送请求
            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            log.info("微信订单支付返回结果：{}", JSON.toJSONString(resultMap));

            //4、再次封装参数
            Map<String, String> parameterMap = new HashMap<>();
            String prepayId = String.valueOf(resultMap.get("prepay_id"));
            String packages = "prepay_id=" + prepayId;
            parameterMap.put("appId", ConstantPropertiesUtils.APPID);
            parameterMap.put("nonceStr", resultMap.get("nonce_str"));
            parameterMap.put("package", packages);
            parameterMap.put("signType", "MD5");
            parameterMap.put("timeStamp", String.valueOf(new Date().getTime()));
            //生成签名
            String sign = WXPayUtil.generateSignature(parameterMap, ConstantPropertiesUtils.PARTNERKEY);

            //返回结果
            Map<String, String> result = new HashMap();
            result.put("timeStamp", parameterMap.get("timeStamp"));
            result.put("nonceStr", parameterMap.get("nonceStr"));
            result.put("signType", "MD5");
            result.put("paySign", sign);
            result.put("package", packages);
            if(null != resultMap.get("result_code")) {
                //微信支付二维码2小时过期，可采取2小时未支付取消订单
                redisTemplate.opsForValue().set(orderNo, result, 120, TimeUnit.MINUTES);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public Map queryPayStatus(String orderNo) {
        try {
            //1、封装参数
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderNo);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());

            //2、设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //6、转成Map
            //7、返回
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
