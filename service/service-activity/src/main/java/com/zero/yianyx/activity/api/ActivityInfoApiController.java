package com.zero.yianyx.activity.api;

import com.zero.yianyx.activity.service.ActivityInfoService;
import com.zero.yianyx.activity.service.CouponInfoService;
import com.zero.yianyx.model.activity.CouponInfo;
import com.zero.yianyx.model.order.CartInfo;
import com.zero.yianyx.vo.order.CartInfoVo;
import com.zero.yianyx.vo.order.OrderConfirmVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @description: 促销与优惠券接口
 * @author: ZeroYiAn
 * @time: 2023/6/13
 */

@RestController
@RequestMapping("/api/activity")
public class ActivityInfoApiController {
    @Resource
    private ActivityInfoService activityInfoService;
    @Resource
    private CouponInfoService couponInfoService;

    /**
     * 根据skuId列表获取活动数据和优惠券数据
     * @param skuId
     * @param userId
     * @return
     */
    @GetMapping("inner/findActivityAndCoupon/{skuId}/{userId}")
    public Map<String, Object> findActivityAndCoupon(@PathVariable Long skuId,@PathVariable Long userId) {
        return activityInfoService.findActivityAndCoupon(skuId,userId);
    }

    /**
     * 获取 购物车中满足条件的促销与优惠券信息
     * @param cartInfoList
     * @param userId
     * @return
     */
    @PostMapping("inner/findCartActivityAndCoupon/{userId}")
    public OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList, @PathVariable("userId") Long userId) {
        return activityInfoService.findCartActivityAndCoupon(cartInfoList, userId);
    }


    /**
     * 根据skuId列表获取促销活动信息
     * @param skuIdList
     * @return
     */
    @PostMapping("inner/findActivity")
    public Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList) {
        return activityInfoService.findActivity(skuIdList);
    }

    @PostMapping("inner/findCartActivityList")
    public List<CartInfoVo> findCartActivityList(@RequestBody List<CartInfo> cartInfoList){
        return activityInfoService.findCartActivityList(cartInfoList);
    }

    /**
     * 获取购物车对应优惠券数据
     * @param cartInfoList
     * @param couponId
     * @return
     */
    @PostMapping("inner/findRangeSkuIdList")
    public CouponInfo findRangeSkuIdList(@RequestBody List<CartInfo>cartInfoList,
                                         @PathVariable("couponId")Long couponId){
        return couponInfoService.findRangeSkuIdList(cartInfoList,couponId);
    }

    /**
     * 更新优惠券使用状态
     */
    @GetMapping("inner/updateCouponInfoUseStatus/{couponId}/{userId}/{orderId}")
    public Boolean updateCouponInfoUseStatus(@PathVariable("couponId")Long couponId,
                                             @PathVariable("userId")Long userId,
                                             @PathVariable("orderId")Long orderId){
        if(null != couponId){
            couponInfoService.updateCouponInfoUseStatus(couponId,userId,orderId);
        }
        return true;
    }







}
