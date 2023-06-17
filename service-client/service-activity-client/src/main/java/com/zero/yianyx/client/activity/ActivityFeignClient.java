package com.zero.yianyx.client.activity;

import com.zero.yianyx.model.activity.CouponInfo;
import com.zero.yianyx.model.order.CartInfo;
import com.zero.yianyx.vo.order.CartInfoVo;
import com.zero.yianyx.vo.order.OrderConfirmVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/13
 */
@FeignClient("service-activity")
public interface ActivityFeignClient {

    /**
     * 更新优惠券使用状态
     * @param couponId
     * @param userId
     * @param orderId
     * @return
     */
    @GetMapping("/api/activity/inner/updateCouponInfoUseStatus/{couponId}/{userId}/{orderId}")
    public Boolean updateCouponInfoUseStatus(@PathVariable("couponId")Long couponId,
                                             @PathVariable("userId")Long userId,
                                             @PathVariable("orderId")Long orderId);

    /**
     * 获取优惠券覆盖的商品sku列表
     * @param cartInfoList
     * @param couponId
     * @return
     */
    @PostMapping("/api/activity/inner/findRangeSkuIdList")
    public CouponInfo findRangeSkuIdList(@RequestBody List<CartInfo>cartInfoList,
                                         @PathVariable("couponId")Long couponId);

    /**
     * 获取购物车对应规则数据
     * @param cartInfoList
     * @return
     */
    @PostMapping("/api/activity/inner/findCartActivityList")
    public List<CartInfoVo> findCartActivityList(@RequestBody List<CartInfo> cartInfoList);

    /**
     * 获取购物车中满足条件的活动和优惠券信息
     * @param cartInfoList
     * @param userId
     * @return
     */
    @PostMapping("/api/activity/inner/findCartActivityAndCoupon/{userId}")
    public OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList, @PathVariable("userId") Long userId);

    /**
     * 根据skuId列表获取活动数据和优惠券数据
     * @param skuId
     * @param userId
     * @return
     */
    @GetMapping("/api/activity/inner/findActivityAndCoupon/{skuId}/{userId}")
    public Map<String, Object> findActivityAndCoupon(@PathVariable Long skuId,@PathVariable Long userId);

    /**
     * 根据skuId列表获取促销信息
     * @param skuIdList
     * @return
     */
    @PostMapping("/api/activity/inner/findActivity")
    public Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList);



}