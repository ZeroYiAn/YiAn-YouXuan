package com.zero.yianyx.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.activity.mapper.ActivityInfoMapper;
import com.zero.yianyx.activity.mapper.ActivityRuleMapper;
import com.zero.yianyx.activity.mapper.ActivitySkuMapper;
import com.zero.yianyx.activity.service.ActivityInfoService;
import com.zero.yianyx.activity.service.CouponInfoService;
import com.zero.yianyx.client.product.ProductFeignClient;
import com.zero.yianyx.enums.ActivityType;
import com.zero.yianyx.model.activity.ActivityInfo;
import com.zero.yianyx.model.activity.ActivityRule;
import com.zero.yianyx.model.activity.ActivitySku;
import com.zero.yianyx.model.activity.CouponInfo;
import com.zero.yianyx.model.order.CartInfo;
import com.zero.yianyx.model.product.SkuInfo;
import com.zero.yianyx.vo.activity.ActivityRuleVo;
import com.zero.yianyx.vo.order.CartInfoVo;
import com.zero.yianyx.vo.order.OrderConfirmVo;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {
    @Resource
    private ActivityRuleMapper activityRuleMapper;

    @Resource
    private ActivitySkuMapper activitySkuMapper;

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private CouponInfoService couponInfoService;


    @Override
    public OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId) {
        //1.获取购物车，每个购物项参与活动，根据活动规则分组，一个规则对应多个商品
        List<CartInfoVo> cartInfoVoList = this.findCartActivityList(cartInfoList);
        //2.计算参与促销活动优惠的总金额，这里用的stream()流的方法进行相加，也可以直接进行遍历相加
        BigDecimal activityReduceAmount = cartInfoVoList.stream()
                //不能为空
                .filter(carInfoVo -> null != carInfoVo.getActivityRule())
                .map(carInfoVo -> carInfoVo.getActivityRule().getReduceAmount())
                //初始值是0，计算规则是加法
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        //3.计算优惠券可优惠的总金额，一次购物只能使用一张优惠券
        //购物车可使用的优惠券列表
        List<CouponInfo> couponInfoList = couponInfoService.findCartCouponInfo(cartInfoList, userId);
        BigDecimal couponReduceAmount = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(couponInfoList)) {
            couponReduceAmount = couponInfoList.stream()
                    //couponInfo.getIsOptimal().intValue() == 1 表示是最优优惠选项
                    .filter(couponInfo -> couponInfo.getIsOptimal().intValue() == 1)
                    .map(couponInfo -> couponInfo.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        //购物车原始总金额
        BigDecimal originalTotalAmount = cartInfoList.stream()
                //cartInfo.getIsChecked() == 1 表示商品被选中
                .filter(cartInfo -> cartInfo.getIsChecked() == 1)
                //单价乘以数量
                .map(cartInfo -> cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //最终总金额
        BigDecimal totalAmount = originalTotalAmount.subtract(activityReduceAmount).subtract(couponReduceAmount);

        //封装结果
        OrderConfirmVo orderTradeVo = new OrderConfirmVo();
        orderTradeVo.setCarInfoVoList(cartInfoVoList);
        orderTradeVo.setActivityReduceAmount(activityReduceAmount);
        orderTradeVo.setCouponInfoList(couponInfoList);
        orderTradeVo.setCouponReduceAmount(couponReduceAmount);
        orderTradeVo.setOriginalTotalAmount(originalTotalAmount);
        orderTradeVo.setTotalAmount(totalAmount);
        return orderTradeVo;
    }

    @Override
    public List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList) {
        List<CartInfoVo> carInfoVoList = new ArrayList<>();

        //第一步：把购物车里面相同活动的购物项汇总一起
        //获取skuId列表
        List<Long> skuIdList = cartInfoList.stream().map(CartInfo::getSkuId).collect(Collectors.toList());
        //获取skuId列表对应的全部活动促销规则
        //selectCartActivityList()查出来的是skuId对应的活动id(一个商品sku只能参与一个活动)
        if(CollectionUtils.isEmpty(skuIdList)){
            return carInfoVoList;
        }
        List<ActivitySku> activitySkuList = baseMapper.selectCartActivityList(skuIdList);
        //根据活动分组，获取每个活动对应的skuId列表(一个活动可以对应多个商品)，即把购物车里面相同活动的购物项汇总一起，凑单使用
        //根据活动id进行分组 一个活动id 对应一个set集合，集合中放其对应的skuId
        Map<Long, Set<Long>> activityIdToSkuIdListMap = activitySkuList.stream().
                collect(Collectors.groupingBy(ActivitySku::getActivityId,
                        Collectors.mapping(ActivitySku::getSkuId, Collectors.toSet())));

        //第二步：获取活动对应的促销规则
        //获取购物车对应的活动id
        Set<Long> activityIdSet = activitySkuList.stream().map(ActivitySku::getActivityId).collect(Collectors.toSet());
        // Map<Long, List<ActivityRule>>  key是活动id，value是活动对应的规则
        Map<Long, List<ActivityRule>> activityIdToActivityRuleListMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(activityIdSet)) {
            LambdaQueryWrapper<ActivityRule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(ActivityRule::getConditionAmount, ActivityRule::getConditionNum);
            queryWrapper.in(ActivityRule::getActivityId, activityIdSet);
            //这里是查出来了所有的活动规则
            List<ActivityRule> activityRuleList = activityRuleMapper.selectList(queryWrapper);
            //然后按活动Id分组，获取活动对应的规则
            activityIdToActivityRuleListMap = activityRuleList.stream().collect(Collectors.groupingBy(activityRule -> activityRule.getActivityId()));
        }

        //第三步：根据活动汇总购物项，相同活动的购物项为一组显示在页面，并且计算最优优惠金额
        //记录 有参与活动的购物项skuId
        Set<Long> activitySkuIdSet = new HashSet<>();
        if(!CollectionUtils.isEmpty(activityIdToSkuIdListMap)) {
            Iterator<Map.Entry<Long, Set<Long>>> iterator = activityIdToSkuIdListMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Set<Long>> entry = iterator.next();
                //key是活动id
                Long activityId = entry.getKey();
                //当前活动对应的购物项skuId列表
                Set<Long> currentActivitySkuIdSet = entry.getValue();
                //当前活动对应的购物项列表   ，过滤出参加这个活动的购物项拿出来进行封装
                List<CartInfo> currentActivityCartInfoList = cartInfoList.stream().filter(cartInfo -> currentActivitySkuIdSet.contains(cartInfo.getSkuId())).collect(Collectors.toList());

                //当前活动的总金额
                BigDecimal activityTotalAmount = this.computeTotalAmount(currentActivityCartInfoList);
                //当前活动的购物项总个数
                Integer activityTotalNum = this.computeCartNum(currentActivityCartInfoList);
                //计算当前活动对应的最优规则
                //活动当前活动对应的规则
                List<ActivityRule> currentActivityRuleList = activityIdToActivityRuleListMap.get(activityId);
                //获取活动类型    FULL_REDUCTION(1,"满减")     FULL_DISCOUNT(2,"满量打折" );
                ActivityType activityType = currentActivityRuleList.get(0).getActivityType();
                ActivityRule optimalActivityRule = null;
                if (activityType == ActivityType.FULL_REDUCTION) {
                    optimalActivityRule = this.computeFullReduction(activityTotalAmount, currentActivityRuleList);
                } else {
                    optimalActivityRule = this.computeFullDiscount(activityTotalNum, activityTotalAmount, currentActivityRuleList);
                }

                //同一活动对应的购物项列表与对应优化规则
                CartInfoVo carInfoVo = new CartInfoVo();
                carInfoVo.setCartInfoList(currentActivityCartInfoList);
                carInfoVo.setActivityRule(optimalActivityRule);
                carInfoVoList.add(carInfoVo);
                //记录哪些购物项参加了活动，方便后续使用
                activitySkuIdSet.addAll(currentActivitySkuIdSet);
            }
        }

        //第四步：记录没有参与活动的购物项，每一项一组
        skuIdList.removeAll(activitySkuIdSet);
        if(!CollectionUtils.isEmpty(skuIdList)) {
            //获取skuId对应的购物项
            Map<Long, CartInfo> skuIdToCartInfoMap = cartInfoList.stream().collect(Collectors.toMap(CartInfo::getSkuId, CartInfo->CartInfo));
            for(Long skuId : skuIdList) {
                CartInfoVo carInfoVo = new CartInfoVo();
                //没有使用的最优活动规则
                carInfoVo.setActivityRule(null);
                List<CartInfo> currentCartInfoList = new ArrayList<>();
                currentCartInfoList.add(skuIdToCartInfoMap.get(skuId));
                carInfoVo.setCartInfoList(currentCartInfoList);
                carInfoVoList.add(carInfoVo);
            }
        }
        return carInfoVoList;
    }
    /**
     * 计算满量打折最优规则
     * @param totalNum
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠折扣从大到小排序了
     */
    private ActivityRule computeFullDiscount(Integer totalNum, BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项购买个数大于等于满减件数，则优化打折
            if (totalNum.intValue() >= activityRule.getConditionNum()) {
                BigDecimal skuDiscountTotalAmount = totalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                BigDecimal reduceAmount = totalAmount.subtract(skuDiscountTotalAmount);
                activityRule.setReduceAmount(reduceAmount);
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，还差")
                    .append(totalNum-optimalActivityRule.getConditionNum())
                    .append("件");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    /**
     * 计算满减最优规则
     * @param totalAmount
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
     */
    private ActivityRule computeFullReduction(BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项金额大于等于满减金额，则优惠金额
            if (totalAmount.compareTo(activityRule.getConditionAmount()) > -1) {
                //优惠后减少金额
                activityRule.setReduceAmount(activityRule.getBenefitAmount());
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，还差")
                    .append(totalAmount.subtract(optimalActivityRule.getConditionAmount()))
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }


    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    private int computeCartNum(List<CartInfo> cartInfoList) {
        int total = 0;
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                total += cartInfo.getSkuNum();
            }
        }
        return total;
    }


    @Override
    public Map<String, Object> findActivityAndCoupon(Long skuId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        //1.根据skuId 获取sku营销活动，一个活动有多个规则
        //注意要先根据skuId获取活动id
       // QueryWrapper<ActivitySku> activitySkuQueryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<ActivitySku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySku::getSkuId,skuId);
        ActivitySku activitySku = activitySkuMapper.selectOne(queryWrapper);
        if(null!=activitySku){
            Long activityId = activitySku.getActivityId();
            //注意！传入的是activityId
            Map<String, Object> activityRuleList = this.findActivityRuleList(activityId);
            result.putAll(activityRuleList);
        }else {
            result.put("activityRuleList",new ArrayList<>());
        }

        //2.根据skuId 和userId 获取优惠券信息
        List<CouponInfo>couponInfoList = couponInfoService.findCouponInfoList(skuId,userId);
        if(!CollectionUtils.isEmpty(couponInfoList)){
            result.put("couponInfoList",couponInfoList);
        }else {
            result.put("couponInfoList",new ArrayList<>());
        }

        return result;
    }



    @Override
    public IPage<ActivityInfo> selectPage(Page<ActivityInfo> pageParam) {
        QueryWrapper<ActivityInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        //分页查询对象里面获取列表数据
        //遍历数据集合得到每个ActivityInfo对象，向ActivityInfo对象封装活动类型到activityTypeString属性里面
        IPage<ActivityInfo> page = baseMapper.selectPage(pageParam, queryWrapper);
        page.getRecords().stream().forEach(item -> {
            item.setActivityTypeString(item.getActivityType().getComment());
        });
        return page;
    }

    @Override
    public Map<String, Object> findActivityRuleList(Long activityId) {
        Map<String,Object>result = new HashMap<>();
        //根据活动id查询，查询使用规则列表activity_rule表
        QueryWrapper<ActivityRule> queryWrapper = new QueryWrapper<>();
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(queryWrapper.eq("activity_id",activityId));
        if(CollectionUtils.isEmpty(activityRuleList)){
            result.put("activityRuleList",new ArrayList<ActivityRule>());
        }else {
            result.put("activityRuleList", activityRuleList);
        }
        //根据活动id查询，查询使用活动商品列表activity_sku表
        QueryWrapper<ActivitySku> activitySkuQueryWrapper = new QueryWrapper<>();
        List<ActivitySku> activitySkuList = activitySkuMapper.selectList(activitySkuQueryWrapper.eq("activity_id",activityId));
        //获取所有skuId
        List<Long> skuIdList = activitySkuList.stream().map(ActivitySku::getSkuId).collect(Collectors.toList());
        //通过远程调用service-product模块，得到商品列表信息
        if(!CollectionUtils.isEmpty(skuIdList)){
            List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(skuIdList);
            if(CollectionUtils.isEmpty(skuInfoList)){
                result.put("skuInfoList", null);
            }else {
                result.put("skuInfoList", skuInfoList);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {
        //1.根据活动id删除之前的规则数据
        Long activityId = activityRuleVo.getActivityId();
        activityRuleMapper.delete(new QueryWrapper<ActivityRule>().eq("activity_id", activityId));
        activitySkuMapper.delete(new QueryWrapper<ActivitySku>().eq("activity_id", activityId));
        //2.获取规则列表数据
        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();
        List<Long> couponIdList = activityRuleVo.getCouponIdList();

        ActivityInfo activityInfo = baseMapper.selectById(activityId);
        for (ActivityRule activityRule : activityRuleList) {
            activityRule.setActivityId(activityId);
            activityRule.setActivityType(activityInfo.getActivityType());
            activityRuleMapper.insert(activityRule);
        }
        for (ActivitySku activitySku : activitySkuList) {
            activitySku.setActivityId(activityId);
            activitySkuMapper.insert(activitySku);
        }
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        //根据关键字查询sku匹配内容列表
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        //判断：如果根据关键字查询不到匹配内容，直接返回空集合
        if(CollectionUtils.isEmpty(skuInfoList)){
            return new ArrayList<>();
        }
        //所有匹配的skuId
        List<Long> skuIdList = skuInfoList.stream().map(SkuInfo::getId).collect(Collectors.toList());
        //判断添加商品之前是否参加过该活动，如果之前参加过，活动正在进行中，排除商品
        List<SkuInfo> notExistSkuInfoList = new ArrayList<>();
        //得到已经存在的skuId，一个sku只能参加一个促销活动，所以存在的得排除
        List<Long> existSkuIdList = baseMapper.selectExistSkuIdList(skuIdList);
        for(SkuInfo skuInfo : skuInfoList) {
            if(!existSkuIdList.contains(skuInfo.getId())) {
                log.info("符合活动条件的商品：{}",skuInfo.getId());
                notExistSkuInfoList.add(skuInfo);
            }
        }
        return notExistSkuInfoList;
    }

    @Override
    public List<ActivityRule> findActivityRule(Long skuId) {
        List<ActivityRule> activityRuleList = baseMapper.selectActivityRuleList(skuId);
        if(!CollectionUtils.isEmpty(activityRuleList)) {
            for(ActivityRule activityRule : activityRuleList) {
                activityRule.setRuleDesc(this.getRuleDesc(activityRule));
            }
        }
        return activityRuleList;
    }

    @Override
    public Map<Long, List<String>> findActivity(List<Long> skuIdList) {
        Map<Long, List<String>>result = new HashMap<>();
        skuIdList.forEach(skuId -> {
            //根据skuId进行查询，查询sku对应活动里面规则列表
            List<ActivityRule>activityRuleList=baseMapper.selectActivityRuleList(skuId);
            //数据封装：给每一个活动进行规则描述
            if(!CollectionUtils.isEmpty(activityRuleList)){
                List<String> ruleList = new ArrayList<>();
                //处理规则名称
                for (ActivityRule activityRule : activityRuleList) {
                    ruleList.add(this.getRuleDesc(activityRule));
                }
                log.info("ruleList:{}",ruleList);
                result.put(skuId,ruleList);
            }
        });
        return result;
    }




    /**
     * 根据活动规则 构造出 规则名称（describe）
     * @param activityRule
     * @return
     */
    private String getRuleDesc(ActivityRule activityRule) {
        ActivityType activityType = activityRule.getActivityType();
        StringBuffer ruleDesc = new StringBuffer();
        if (activityType == ActivityType.FULL_REDUCTION) {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionAmount())
                    .append("元减")
                    .append(activityRule.getBenefitAmount())
                    .append("元");
        } else {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionNum())
                    .append("元打")
                    .append(activityRule.getBenefitDiscount())
                    .append("折");
        }
        return ruleDesc.toString();
    }
}
