package com.zero.yianyx.order.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.client.activity.ActivityFeignClient;
import com.zero.yianyx.client.cart.CartFeignClient;
import com.zero.yianyx.client.product.ProductFeignClient;
import com.zero.yianyx.client.user.UserFeignClient;
import com.zero.yianyx.common.auth.AuthContextHolder;
import com.zero.yianyx.common.constant.RedisConst;
import com.zero.yianyx.common.exception.YianyxException;
import com.zero.yianyx.common.result.ResultCodeEnum;
import com.zero.yianyx.enums.*;
import com.zero.yianyx.model.activity.ActivityRule;
import com.zero.yianyx.model.activity.CouponInfo;
import com.zero.yianyx.model.order.CartInfo;
import com.zero.yianyx.model.order.OrderInfo;
import com.zero.yianyx.model.order.OrderItem;
import com.zero.yianyx.mq.constant.MqConst;
import com.zero.yianyx.mq.service.RabbitService;
import com.zero.yianyx.order.mapper.OrderInfoMapper;
import com.zero.yianyx.order.service.OrderInfoService;
import com.zero.yianyx.order.service.OrderItemService;
import com.zero.yianyx.utils.DateUtil;
import com.zero.yianyx.vo.order.CartInfoVo;
import com.zero.yianyx.vo.order.OrderConfirmVo;
import com.zero.yianyx.vo.order.OrderSubmitVo;
import com.zero.yianyx.vo.order.OrderUserQueryVo;
import com.zero.yianyx.vo.product.SkuStockLockVo;
import com.zero.yianyx.vo.user.LeaderAddressVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/15
 */
@Service
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper,OrderInfo> implements OrderInfoService {

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ActivityFeignClient activityFeignClient;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private CartFeignClient cartFeignClient;

    @Resource
    private RabbitService rabbitService;


    @Override
    public IPage<OrderInfo> getOrderInfoByUserIdPage(Page<OrderInfo> pageParam,
                                                     OrderUserQueryVo orderUserQueryVo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId,orderUserQueryVo.getUserId());
        wrapper.eq(OrderInfo::getOrderStatus,orderUserQueryVo.getOrderStatus());
        IPage<OrderInfo> pageModel = baseMapper.selectPage(pageParam, wrapper);

        //获取每个订单，把每个订单里面订单项查询封装
        List<OrderInfo> orderInfoList = pageModel.getRecords();
        for(OrderInfo orderInfo : orderInfoList) {
            //根据订单id查询里面所有订单项列表
            List<OrderItem> orderItemList = orderItemService.list(
                    new LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getOrderId, orderInfo.getId())
            );
            //把订单项集合封装到每个订单里面
            orderInfo.setOrderItemList(orderItemList);
            //封装订单状态名称
            orderInfo.getParam().put("orderStatusName",orderInfo.getOrderStatus().getComment());
        }
        return pageModel;
    }

    @Override
    public void orderPay(String orderNo) {
        OrderInfo orderInfo = this.getOrderInfoByOrderNo(orderNo);
        if(null == orderInfo || orderInfo.getOrderStatus() != OrderStatus.UNPAID) {return;}
        //更改订单状态: 未支付状态----> 待发货状态
        this.updateOrderStatus(orderInfo.getId());
        //发送MQ消息：通知product模块异步扣减库存
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT, MqConst.ROUTING_MINUS_STOCK, orderNo);
    }

    private void updateOrderStatus(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        orderInfo.setOrderStatus(OrderStatus.WAITING_DELEVER);
        orderInfo.setProcessStatus(ProcessStatus.WAITING_DELEVER);
        baseMapper.updateById(orderInfo);
    }

    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        //查询订单基本信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        orderInfo.getParam().put("orderStatusName", orderInfo.getOrderStatus().getComment());
        //根据orderId查询订单所有订单细项
        List<OrderItem> orderItemList = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderInfo.getId()));
        orderInfo.setOrderItemList(orderItemList);
        return orderInfo;

    }

    @Override
    public OrderInfo getOrderInfoByOrderNo(String orderNo) {
        OrderInfo orderInfo = baseMapper.selectOne(
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getOrderNo, orderNo)
        );
        return orderInfo;
    }



    @Override
    public OrderConfirmVo confirmOrder() {
        // 获取到当前用户Id
        Long userId = AuthContextHolder.getUserId();
        //获取用户地址
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        // 先得到用户选中要购买的商品！
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        // 防重：生成一个唯一标识，保存到redis中一份  或者IdWorker.getTimeId()
        String orderNo = System.currentTimeMillis()+"";
        redisTemplate.opsForValue().set(RedisConst.ORDER_REPEAT + orderNo, orderNo, 24, TimeUnit.HOURS);
        //获取购物车满足条件的促销与优惠券信息
        OrderConfirmVo orderConfirmVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);
        orderConfirmVo.setLeaderAddressVo(leaderAddressVo);
        orderConfirmVo.setOrderNo(orderNo);
        return orderConfirmVo;
    }


    /**代理对象, 作为全局变量(成员变量)供submitOrder()函数使用*/
    private OrderInfoService proxy;
    @Override
    public Long submitOrder(OrderSubmitVo orderParamVo) {
        //1.设置给哪个用户生成订单 获取到当前用户Id
        Long userId = AuthContextHolder.getUserId();
        orderParamVo.setUserId(userId);
        //2. 订单不能重复提交，重复提交验证
        // 通过redis + lua脚本判断
        //2.1获取传递过来的订单orderNo
        String orderNo = orderParamVo.getOrderNo();
        if(StringUtils.isEmpty(orderNo)){
            throw new YianyxException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        //2.2拿着orderNo到redis进行查询  lua脚本：有相同orderNo返回 1 ，没有返回 0
        //2.3如果redis中有相同的orderNo，表示正常提交订单，把redis的orderNo删除
        //2.4如果redis中没有。表示表单重复提交
        String script = "if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
        Boolean flag = (Boolean)redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(RedisConst.ORDER_REPEAT + orderNo), orderNo);
        if(!flag){
            throw new YianyxException(ResultCodeEnum.REPEAT_SUBMIT);
        }

        //3.验证库存 并且 锁定库存
        //3.1普通商品
        //获取用户已经选中的购物项
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        //获取其中的普通商品
        List<CartInfo> commonSkuList = cartInfoList.stream().filter(cartInfo -> cartInfo.getSkuType().equals(SkuType.COMMON.getCode())).collect(Collectors.toList());
        //把 List<CartInfo> 封装转换成  List<SkuStockLockVo>
        if(!CollectionUtils.isEmpty(commonSkuList)) {
            List<SkuStockLockVo> commonStockLockVoList = commonSkuList.stream().map(item -> {
                SkuStockLockVo skuStockLockVo = new SkuStockLockVo();
                skuStockLockVo.setSkuId(item.getSkuId());
                skuStockLockVo.setSkuNum(item.getSkuNum());
                return skuStockLockVo;
            }).collect(Collectors.toList());
            //是否锁定
            Boolean isLockCommon = productFeignClient.checkAndLock(commonStockLockVoList, orderNo);
            if (!isLockCommon){
                //获取锁失败、
                log.info("不允许重复下单！");
                throw new YianyxException(ResultCodeEnum.ORDER_STOCK_FALL);
            }
        }

        //3.2秒杀商品
//        List<CartInfo> seckillSkuList = cartInfoList.stream().filter(cartInfo -> cartInfo.getSkuType().equals(SkuType.SECKILL.getCode())).collect(Collectors.toList());
//        if(!CollectionUtils.isEmpty(seckillSkuList)) {
//            List<SkuStockLockVo> seckillStockLockVoList = seckillSkuList.stream().map(item -> {
//                SkuStockLockVo skuStockLockVo = new SkuStockLockVo();
//                skuStockLockVo.setSkuId(item.getSkuId());
//                skuStockLockVo.setSkuNum(item.getSkuNum());
//                return skuStockLockVo;
//            }).collect(Collectors.toList());
//            //是否锁定
//            Boolean isLockSeckill = seckillFeignClient.checkAndMinusStock(seckillStockLockVoList, orderSubmitVo.getOrderNo());
//            if (!isLockSeckill){
//                throw new GmallException(ResultCodeEnum.ORDER_STOCK_FALL);
//            }
//        }


        //4.下单过程：向两张表order_info 和 order_item中添加数据
        Long orderId = null;
        try {
            /**
             * 注意这里！ 直接在sevice实现里面自己调用自己的方法会导致事务失效
             * 解决方法1：在本方法上也加上 @Transactional 注解 ，但是这样事务就太大了
             * 解决方法2：利用代理对象，获取接口的代理对象
             * proxy = (OrderInfoService) AopContext.currentProxy()
             */
            orderId = this.saveOrder(orderParamVo, cartInfoList);

            // 异步删除购物车中对应的记录。不应该影响下单的整体流程
            rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT, MqConst.ROUTING_DELETE_CART, orderParamVo.getUserId());

        } catch (Exception e) {
            e.printStackTrace();
            // 出现异常立马解锁库存 标记订单时无效订单
            //rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT, MqConst.ROUTING_ROLLBACK_STOCK, orderSubmitVo.getOrderNo());
            throw new YianyxException(ResultCodeEnum.CREATE_ORDER_FAIL);
        }

        //5.返回订单id
        return orderId;

    }

    /**
     *下单过程：向两张表order_info 和 order_item中添加数据
     * @param orderParamVo
     * @param cartInfoList
     * @return
     */
    @Transactional(rollbackFor = {Exception.class})
    public Long saveOrder(OrderSubmitVo orderParamVo, List<CartInfo> cartInfoList) {
        Long userId = AuthContextHolder.getUserId();
        if(CollectionUtils.isEmpty(cartInfoList)) {
            throw new YianyxException(ResultCodeEnum.DATA_ERROR);
        }
        //查询用户提货点 和 团长信息
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        if(null == leaderAddressVo) {
            throw new YianyxException(ResultCodeEnum.DATA_ERROR);
        }
        //计算购物项分摊的优惠减少金额，按比例分摊，退款时按实际支付金额退款
        Map<String, BigDecimal> activitySplitAmountMap = this.computeActivitySplitAmount(cartInfoList);
        Map<String, BigDecimal> couponInfoSplitAmountMap = this.computeCouponInfoSplitAmount(cartInfoList, orderParamVo.getCouponId());
        //sku对应的订单明细
        List<OrderItem> orderItemList = new ArrayList<>();
        /**
         * 向 order_item中封装添加数据
         */
        //   保存订单明细
        for (CartInfo cartInfo : cartInfoList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(null);
            orderItem.setCategoryId(cartInfo.getCategoryId());
            if(cartInfo.getSkuType().equals(SkuType.COMMON.getCode())) {
                orderItem.setSkuType(SkuType.COMMON);
            } else {
                orderItem.setSkuType(SkuType.SECKILL);
            }
            orderItem.setSkuId(cartInfo.getSkuId());
            orderItem.setSkuName(cartInfo.getSkuName());
            orderItem.setSkuPrice(cartInfo.getCartPrice());
            orderItem.setImgUrl(cartInfo.getImgUrl());
            orderItem.setSkuNum(cartInfo.getSkuNum());
            orderItem.setLeaderId(orderParamVo.getLeaderId());

            //促销活动分摊金额
            BigDecimal splitActivityAmount = activitySplitAmountMap.get("activity:"+orderItem.getSkuId());
            if(null == splitActivityAmount) {
                splitActivityAmount = new BigDecimal(0);
            }
            orderItem.setSplitActivityAmount(splitActivityAmount);

            //优惠券分摊金额
            BigDecimal splitCouponAmount = couponInfoSplitAmountMap.get("coupon:"+orderItem.getSkuId());
            if(null == splitCouponAmount) {
                splitCouponAmount = new BigDecimal(0);
            }
            orderItem.setSplitCouponAmount(splitCouponAmount);

            //优惠后的总金额  单价乘以数量
            BigDecimal skuTotalAmount = orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum()));
            BigDecimal splitTotalAmount = skuTotalAmount.subtract(splitActivityAmount).subtract(splitCouponAmount);
            orderItem.setSplitTotalAmount(splitTotalAmount);
            orderItemList.add(orderItem);
        }

        /**
         * 向 order_info中封装添加数据
         */
        //保存订单
        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
        order.setOrderNo(orderParamVo.getOrderNo());
        order.setOrderStatus(OrderStatus.UNPAID);
        order.setProcessStatus(ProcessStatus.UNPAID);
        order.setCouponId(orderParamVo.getCouponId());
        order.setLeaderId(orderParamVo.getLeaderId());
        order.setLeaderName(leaderAddressVo.getLeaderName());
        order.setLeaderPhone(leaderAddressVo.getLeaderPhone());
        order.setTakeName(leaderAddressVo.getTakeName());
        order.setReceiverName(orderParamVo.getReceiverName());
        order.setReceiverPhone(orderParamVo.getReceiverPhone());
        order.setReceiverProvince(leaderAddressVo.getProvince());
        order.setReceiverCity(leaderAddressVo.getCity());
        order.setReceiverDistrict(leaderAddressVo.getDistrict());
        order.setReceiverAddress(leaderAddressVo.getDetailAddress());
        order.setWareId(cartInfoList.get(0).getWareId());

        //计算订单金额
        BigDecimal originalTotalAmount = this.computeTotalAmount(cartInfoList);
        BigDecimal activityAmount = activitySplitAmountMap.get("activity:total");
        if(null == activityAmount) {activityAmount = new BigDecimal(0);}
        BigDecimal couponAmount = couponInfoSplitAmountMap.get("coupon:total");
        if(null == couponAmount){ couponAmount = new BigDecimal(0);}
        BigDecimal totalAmount = originalTotalAmount.subtract(activityAmount).subtract(couponAmount);
        //计算订单金额
        order.setOriginalTotalAmount(originalTotalAmount);
        order.setActivityAmount(activityAmount);
        order.setCouponAmount(couponAmount);
        order.setTotalAmount(totalAmount);

        //计算团长佣金
        //BigDecimal profitRate = orderSetService.getProfitRate();
        //该功能未开发，这里设定成订单总金额的百分之十
        BigDecimal profitRate = totalAmount.multiply(new BigDecimal(0.1));
        BigDecimal commissionAmount = order.getTotalAmount().multiply(profitRate);
        order.setCommissionAmount(commissionAmount);

        //添加数据到订单基本信息表order_info中
        baseMapper.insert(order);

        //保存订单项
        for(OrderItem orderItem : orderItemList) {
            //各个商品细项的OrderId都是总订单项的id
            orderItem.setOrderId(order.getId());
        }
        orderItemService.saveBatch(orderItemList);

        //更新优惠券使用状态
        if(null != order.getCouponId()) {
            activityFeignClient.updateCouponInfoUseStatus(order.getCouponId(), userId, order.getId());
        }

        //下单成功，利用redis记录用户商品购买个数  hash类型，key(userId)-- [filed(skuId)-value(skuNum)]
        String orderSkuKey = RedisConst.ORDER_SKU_MAP + orderParamVo.getUserId();
        BoundHashOperations<String, String, Integer> hashOperations = redisTemplate.boundHashOps(orderSkuKey);
        cartInfoList.forEach(cartInfo -> {
            if(hashOperations.hasKey(cartInfo.getSkuId().toString())) {
                Integer orderSkuNum = hashOperations.get(cartInfo.getSkuId().toString()) + cartInfo.getSkuNum();
                hashOperations.put(cartInfo.getSkuId().toString(), orderSkuNum);
            }
        });
        //设置过期时间，当前时间到当日24点则过期
        redisTemplate.expire(orderSkuKey, DateUtil.getCurrentExpireTimes(), TimeUnit.SECONDS);

        //发送消息
        return order.getId();
    }

    /**
     * 计算总金额
     * @param cartInfoList
     * @return
     */
    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal(0);
        for (CartInfo cartInfo : cartInfoList) {
            BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            total = total.add(itemTotal);
        }
        return total;
    }

    /**
     * 计算购物项分摊的优惠减少金额
     * 打折：按折扣分担
     * 现金：按比例分摊
     * @param cartInfoParamList
     * @return
     */
    private Map<String, BigDecimal> computeActivitySplitAmount(List<CartInfo> cartInfoParamList) {
        Map<String, BigDecimal> activitySplitAmountMap = new HashMap<>();

        //促销活动相关信息
        List<CartInfoVo> cartInfoVoList = activityFeignClient.findCartActivityList(cartInfoParamList);

        //活动总金额
        BigDecimal activityReduceAmount = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(cartInfoVoList)) {
            for(CartInfoVo cartInfoVo : cartInfoVoList) {
                ActivityRule activityRule = cartInfoVo.getActivityRule();
                List<CartInfo> cartInfoList = cartInfoVo.getCartInfoList();
                if(null != activityRule) {
                    //优惠金额， 按比例分摊
                    BigDecimal reduceAmount = activityRule.getReduceAmount();
                    activityReduceAmount = activityReduceAmount.add(reduceAmount);
                    if(cartInfoList.size() == 1) {
                        activitySplitAmountMap.put("activity:"+cartInfoList.get(0).getSkuId(), reduceAmount);
                    } else {
                        //总金额
                        BigDecimal originalTotalAmount = new BigDecimal(0);
                        for(CartInfo cartInfo : cartInfoList) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                        }
                        //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                        BigDecimal skuPartReduceAmount = new BigDecimal(0);
                        if (activityRule.getActivityType() == ActivityType.FULL_REDUCTION) {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                                    //sku分摊金额
                                    BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        } else {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));

                                    //sku分摊金额
                                    BigDecimal skuDiscountTotalAmount = skuTotalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                                    BigDecimal skuReduceAmount = skuTotalAmount.subtract(skuDiscountTotalAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        }
                    }
                }
            }
        }
        activitySplitAmountMap.put("activity:total", activityReduceAmount);
        return activitySplitAmountMap;
    }

    private Map<String, BigDecimal> computeCouponInfoSplitAmount(List<CartInfo> cartInfoList, Long couponId) {
        Map<String, BigDecimal> couponInfoSplitAmountMap = new HashMap<>();

        if(null == couponId) {return couponInfoSplitAmountMap;}
        CouponInfo couponInfo = activityFeignClient.findRangeSkuIdList(cartInfoList, couponId);

        if(null != couponInfo) {
            //sku对应的订单明细
            Map<Long, CartInfo> skuIdToCartInfoMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                skuIdToCartInfoMap.put(cartInfo.getSkuId(), cartInfo);
            }
            //优惠券对应的skuId列表
            List<Long> skuIdList = couponInfo.getSkuIdList();
            if(CollectionUtils.isEmpty(skuIdList)) {
                return couponInfoSplitAmountMap;
            }
            //优惠券优化总金额
            BigDecimal reduceAmount = couponInfo.getAmount();
            if(skuIdList.size() == 1) {
                //sku的优化金额
                couponInfoSplitAmountMap.put("coupon:"+skuIdToCartInfoMap.get(skuIdList.get(0)).getSkuId(), reduceAmount);
            } else {
                //总金额
                BigDecimal originalTotalAmount = new BigDecimal(0);
                for (Long skuId : skuIdList) {
                    CartInfo cartInfo = skuIdToCartInfoMap.get(skuId);
                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                    originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                }
                //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                BigDecimal skuPartReduceAmount = new BigDecimal(0);
                if (couponInfo.getCouponType() == CouponType.CASH || couponInfo.getCouponType() == CouponType.FULL_REDUCTION) {
                    for(int i=0, len=skuIdList.size(); i<len; i++) {
                        CartInfo cartInfo = skuIdToCartInfoMap.get(skuIdList.get(i));
                        if(i < len -1) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            //sku分摊金额
                            BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);

                            skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                        } else {
                            BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);
                        }
                    }
                }
            }
            couponInfoSplitAmountMap.put("coupon:total", couponInfo.getAmount());
        }
        return couponInfoSplitAmountMap;
    }


}
