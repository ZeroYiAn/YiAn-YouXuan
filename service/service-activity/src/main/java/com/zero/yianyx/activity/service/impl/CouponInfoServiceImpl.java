package com.zero.yianyx.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.activity.mapper.CouponInfoMapper;
import com.zero.yianyx.activity.mapper.CouponRangeMapper;
import com.zero.yianyx.activity.mapper.CouponUseMapper;
import com.zero.yianyx.activity.service.CouponInfoService;
import com.zero.yianyx.client.product.ProductFeignClient;
import com.zero.yianyx.enums.CouponRangeType;
import com.zero.yianyx.enums.CouponStatus;
import com.zero.yianyx.model.activity.CouponInfo;
import com.zero.yianyx.model.activity.CouponRange;
import com.zero.yianyx.model.activity.CouponUse;
import com.zero.yianyx.model.order.CartInfo;
import com.zero.yianyx.model.product.Category;
import com.zero.yianyx.model.product.SkuInfo;
import com.zero.yianyx.vo.activity.CouponRuleVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/11
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo>
        implements CouponInfoService {

    @Resource
    private CouponInfoMapper couponInfoMapper;

    @Resource
    private CouponRangeMapper couponRangeMapper;

    @Resource
    private CouponUseMapper couponUseMapper;

    @Resource
    private ProductFeignClient productFeignClient;

    @Override
    public List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId) {
        //1.根据userId获取全部用户优惠券
        List<CouponInfo> userAllCouponInfoList = baseMapper.selectCartCouponInfoList(userId);
        //对查询得到的结果要进行判空
        if(CollectionUtils.isEmpty(userAllCouponInfoList)){
            return new ArrayList<CouponInfo>();
        }
        //2.获取所有优惠券id列表
        List<Long> couponIdList = userAllCouponInfoList.stream().map(couponInfo -> couponInfo.getId()).collect(Collectors.toList());
        //3.查询优惠券对应的范围
        List<CouponRange> couponRangesList = couponRangeMapper.selectList(new LambdaQueryWrapper<CouponRange>().in(CouponRange::getCouponId, couponIdList));
        //4.获取优惠券id对应的满足使用范围的 购物项skuId列表
        Map<Long, List<Long>> couponIdToSkuIdMap = this.findCouponIdToSkuIdMap(cartInfoList, couponRangesList);
        //记录优惠后减少金额
        BigDecimal reduceAmount = new BigDecimal("0");
        //5.遍历全部优惠券，判断优惠券类型，记录最优优惠券
        CouponInfo optimalCouponInfo = null;
        for(CouponInfo couponInfo : userAllCouponInfoList) {
            if(CouponRangeType.ALL == couponInfo.getRangeType()) {
                //全场通用
                //判断是否满足优惠使用门槛
                //计算购物车商品的总价
                BigDecimal totalAmount = computeTotalAmount(cartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            } else {
                //优惠券id对应的满足使用范围的购物项skuId列表
                List<Long> skuIdList = couponIdToSkuIdMap.get(couponInfo.getId());
                //当前满足使用范围的购物项
                List<CartInfo> currentCartInfoList = cartInfoList.stream().filter(cartInfo -> skuIdList.contains(cartInfo.getSkuId())).collect(Collectors.toList());
                BigDecimal totalAmount = computeTotalAmount(currentCartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            }
            if (couponInfo.getIsSelect() == 1 && couponInfo.getAmount().subtract(reduceAmount).doubleValue() > 0) {
                reduceAmount = couponInfo.getAmount();
                optimalCouponInfo = couponInfo;
            }
        }
        if(null != optimalCouponInfo) {
            optimalCouponInfo.setIsOptimal(1);
        }
        return userAllCouponInfoList;
    }

    /**
     * 获取优惠券范围对应的购物车列表
     * @param cartInfoList
     * @param couponId
     * @return
     */
    @Override
    public CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId) {
        CouponInfo couponInfo = this.getById(couponId);
        if(null == couponInfo || couponInfo.getCouponStatus() == 2){ return null;}

        //查询优惠券对应的范围（有哪些商品）
        List<CouponRange> couponRangesList = couponRangeMapper.selectList(new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId, couponId));
        //获取优惠券id对应的满足使用范围的购物项skuId列表
        Map<Long, List<Long>> couponIdToSkuIdMap = this.findCouponIdToSkuIdMap(cartInfoList, couponRangesList);
        //遍历map，得到value集合
        List<Long> skuIdList = couponIdToSkuIdMap.entrySet().iterator().next().getValue();
        couponInfo.setSkuIdList(skuIdList);
        return couponInfo;
    }

    @Override
    public void updateCouponInfoUseStatus(Long couponId, Long userId, Long orderId) {
        CouponUse couponUse = couponUseMapper.selectOne(
                new LambdaQueryWrapper<CouponUse>().eq(CouponUse::getCouponId, couponId)
                        .eq(CouponUse::getUserId, userId).eq(CouponUse::getOrderId, orderId)
        );
        //设置状态：已使用
        if(null != couponUse){
            couponUse.setCouponStatus(CouponStatus.USED);
            couponUseMapper.updateById(couponUse);
        }


    }

    /**
     * 获取优惠券id对应的满足使用范围的购物项skuId列表
     * 说明：一个优惠券可能有多个购物项满足它的使用范围，那么多个购物项可以拼单使用这个优惠券
     * @param cartInfoList
     * @param couponRangesList
     * @return
     */
    private Map<Long, List<Long>> findCouponIdToSkuIdMap(List<CartInfo> cartInfoList, List<CouponRange> couponRangesList) {
        Map<Long, List<Long>> couponIdToSkuIdMap = new HashMap<>();
        //优惠券id对应的范围列表, 根据优惠券id进行分组
        Map<Long, List<CouponRange>> couponIdToCouponRangeListMap = couponRangesList.stream().collect(Collectors.groupingBy(CouponRange::getCouponId));
        Iterator<Map.Entry<Long, List<CouponRange>>> iterator = couponIdToCouponRangeListMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, List<CouponRange>> entry = iterator.next();
            Long couponId = entry.getKey();
            List<CouponRange> couponRangeList = entry.getValue();

            Set<Long> skuIdSet = new HashSet<>();
            for (CartInfo cartInfo : cartInfoList) {
                for(CouponRange couponRange : couponRangeList) {
                    //适用范围：指定sku
                    if(CouponRangeType.SKU == couponRange.getRangeType() && couponRange.getRangeId().longValue() == cartInfo.getSkuId().longValue()) {
                        skuIdSet.add(cartInfo.getSkuId());
                    }//适用范围：指定类别
                    else if(CouponRangeType.CATEGORY == couponRange.getRangeType() && couponRange.getRangeId().longValue() == cartInfo.getCategoryId().longValue()) {
                        skuIdSet.add(cartInfo.getSkuId());
                    }else {
                        //适用范围：全场通用
                    }
                }
            }
            couponIdToSkuIdMap.put(couponId, new ArrayList<>(skuIdSet));
        }
        return couponIdToSkuIdMap;
    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }




    @Override
    public IPage selectPage(Page<CouponInfo> pageParam) {
        //  构造排序条件
        QueryWrapper<CouponInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        IPage<CouponInfo> page = couponInfoMapper.selectPage(pageParam, queryWrapper);
        page.getRecords().stream().forEach(item -> {
            item.setCouponTypeString(item.getCouponType().getComment());
            if(null != item.getRangeType()) {
                item.setRangeTypeString(item.getRangeType().getComment());
            }
        });
        //  返回数据集合
        return page;
    }


    @Override
    public CouponInfo getCouponInfo(String id) {
        CouponInfo couponInfo = this.getById(id);
        //枚举类型属性进行封装
        couponInfo.setCouponTypeString(couponInfo.getCouponType().getComment());
        if(null != couponInfo.getRangeType()) {
            couponInfo.setRangeTypeString(couponInfo.getRangeType().getComment());
        }
        return couponInfo;
    }


    @Override
    public Map<String, Object> findCouponRuleList(Long couponId) {
        Map<String, Object> result = new HashMap<>();
        //1.根据优惠券id查询优惠券基本信息 coupon_info表
        CouponInfo couponInfo = this.getById(couponId);
        //2.根据优惠券id查询coupon_range  查询里面对应的range_id
        QueryWrapper<CouponRange> couponRangeQueryWrapper = new QueryWrapper<>();
        couponRangeQueryWrapper.eq("coupon_id",couponId);
        List<CouponRange> activitySkuList = couponRangeMapper.selectList(couponRangeQueryWrapper);

        List<Long> rangeIdList = activitySkuList.stream().map(CouponRange::getRangeId).collect(Collectors.toList());
        //如果规则类型是SKU   range_id就是skuId值
        //如果规则类型是CATEGORY range_id就是分类Id值

        if(!CollectionUtils.isEmpty(rangeIdList)) {
            if(couponInfo.getRangeType() == CouponRangeType.SKU) {
                List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(rangeIdList);
                result.put("skuInfoList", skuInfoList);

            }else if(couponInfo.getRangeType() == CouponRangeType.CATEGORY) {
                List<Category> categoryList = productFeignClient.findCategoryList(rangeIdList);
                result.put("categoryList", categoryList);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCouponRule(CouponRuleVo couponRuleVo) {
        /*
        优惠券couponInfo 与 couponRange 要一起操作：先删除couponRange ，更新couponInfo ，再新增couponRange ！
         */
        //根据优惠券id删除之前的优惠券规则数据
        QueryWrapper<CouponRange> couponRangeQueryWrapper = new QueryWrapper<>();
        couponRangeQueryWrapper.eq("coupon_id",couponRuleVo.getCouponId());
        couponRangeMapper.delete(couponRangeQueryWrapper);

        //  更新优惠券基本信息
        CouponInfo couponInfo = baseMapper.selectById(couponRuleVo.getCouponId());
        couponInfo.setRangeType(couponRuleVo.getRangeType());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setAmount(couponRuleVo.getAmount());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());


        couponInfoMapper.updateById(couponInfo);

        //  插入新的优惠券规则 couponRangeList
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            couponRange.setCouponId(couponRuleVo.getCouponId());
            //  插入数据
            couponRangeMapper.insert(couponRange);
        }
    }


    @Override
    public List<CouponInfo> findCouponByKeyword(String keyword) {
        //  模糊查询
        QueryWrapper<CouponInfo> couponInfoQueryWrapper = new QueryWrapper<>();
        couponInfoQueryWrapper.like("coupon_name",keyword);
        return couponInfoMapper.selectList(couponInfoQueryWrapper);
    }

    @Override
    public List<CouponInfo> findCouponInfoList(Long skuId, Long userId) {
        //远程调用：根据skuId获取skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        //根据条件查询：skuId + 分类id + userId
        List<CouponInfo> couponInfoList = baseMapper.selectCouponInfoList(skuInfo.getId(),
                skuInfo.getCategoryId(),userId);

        return couponInfoList;
    }


}