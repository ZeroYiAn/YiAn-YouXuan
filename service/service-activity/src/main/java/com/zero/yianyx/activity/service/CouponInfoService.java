package com.zero.yianyx.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.activity.CouponInfo;
import com.zero.yianyx.model.order.CartInfo;
import com.zero.yianyx.vo.activity.CouponRuleVo;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/11
 */
public interface CouponInfoService extends IService<CouponInfo> {


    /**
     * 优惠券分页查询
     * @param pageParam
     * @return
     */
    IPage<CouponInfo> selectPage(Page<CouponInfo> pageParam);

    /**
     * 根据优惠券id获取优惠券信息
     * @param id
     * @return
     */
    CouponInfo getCouponInfo(String id);

    /**
     * 根据优惠券id获取优惠券规则列表
     * @param id
     * @return
     */
    Object findCouponRuleList(Long id);

    /**
     * 新增优惠券规则
     * @param couponRuleVo
     */
    void saveCouponRule(CouponRuleVo couponRuleVo);

    /**
     * 根据关键字获取sku列表，活动使用
     * @param keyword
     * @return
     */
    Object findCouponByKeyword(String keyword);

    /**
     * 根据 skuId 和 userId 获取用户拥有的优惠券信息
     * @param skuId
     * @param userId
     * @return
     */
    List<CouponInfo> findCouponInfoList(Long skuId, Long userId);

    /**
     * 获取购物车可以使用的优惠券列表
     * @param cartInfoList
     * @param userId
     * @return
     */
    List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId);

    /**
     * 获取购物车对应的优惠卷
     * @param cartInfoList
     * @param couponId
     * @return
     */
    CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId);

    /**
     * 更新优惠券使用状态
     * @param couponId
     * @param userId
     * @param orderId
     */
    void updateCouponInfoUseStatus(Long couponId, Long userId, Long orderId);
}
