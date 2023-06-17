package com.zero.yianyx.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.activity.ActivityInfo;
import com.zero.yianyx.model.activity.ActivityRule;
import com.zero.yianyx.model.order.CartInfo;
import com.zero.yianyx.model.product.SkuInfo;
import com.zero.yianyx.vo.activity.ActivityRuleVo;
import com.zero.yianyx.vo.order.CartInfoVo;
import com.zero.yianyx.vo.order.OrderConfirmVo;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/11
 */
public interface ActivityInfoService extends IService<ActivityInfo> {
    /**
     * 分页查询活动列表
     * @param pageParam
     * @return
     */
    IPage<ActivityInfo> selectPage(Page<ActivityInfo> pageParam);

    /**
     * 获取活动规则列表方法
     * @param id
     * @return
     */
    Map<String, Object> findActivityRuleList(Long id);

    /**
     * 添加活动规则数据
     * @param activityRuleVo
     */
    void saveActivityRule(ActivityRuleVo activityRuleVo);

    /**
     * 根据关键字查询sku信息列表
     * @param keyword
     * @return
     */
    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    /**
     * 根据skuId获取促销规则信息
     * @param skuId
     * @return
     */
    List<ActivityRule> findActivityRule(Long skuId);

    /**
     * 根据skuId获取促销活动规则信息
     * @param skuIdList
     * @return
     */
    Map<Long, List<String>> findActivity(List<Long> skuIdList);

    Map<String, Object> findActivityAndCoupon(Long skuId, Long userId);

    /**
     * 获取购物车满足条件的促销与优惠券信息
     * @param cartInfoList
     * @param userId
     * @return
     */
    OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId);

    /**
     * 获取购物车对应规则数据
     * @param cartInfoList
     * @return
     */
    List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList);
}
