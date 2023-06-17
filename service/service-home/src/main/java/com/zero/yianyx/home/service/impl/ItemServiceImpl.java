package com.zero.yianyx.home.service.impl;

import com.zero.yianyx.client.activity.ActivityFeignClient;
import com.zero.yianyx.client.product.ProductFeignClient;
import com.zero.yianyx.client.search.SkuFeignClient;
import com.zero.yianyx.home.service.ItemService;
import com.zero.yianyx.vo.product.SkuInfoVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/13
 */
@Service
public class ItemServiceImpl  implements ItemService {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private ActivityFeignClient activityFeignClient;
    @Resource
    private SkuFeignClient skuFeignClient;

    @Override
    public Map<String, Object> item(Long skuId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        //线程1：supplyAsync()方法需要返回值
        CompletableFuture<SkuInfoVo> skuInfoVoCompletableFuture = CompletableFuture.supplyAsync(()->{
            //远程调用获取SkuInfoVo
            SkuInfoVo skuInfoVo = productFeignClient.getSkuInfoVo(skuId);
            result.put("skuInfoVo",skuInfoVo);
            return skuInfoVo;
        },threadPoolExecutor);

        //线程2：sku对应优惠券信息    runAsync()方法不需要返回值
        CompletableFuture<Void> activityCompletableFuture = CompletableFuture.runAsync(()->{
            //远程调用获取优惠券信息,
            /**
             * Map中包含活动信息(可能为空，即商品不参与任何活动) 和优惠券信息
             * 但是如果活动信息为空 ，商品详情页面显示就有问题
             * 所以让所有商品都在activity_sku表中有记录才行(即让每个商品都参与活动
             */
            Map<String,Object>activityMap = activityFeignClient.findActivityAndCoupon(skuId,userId);
            result.putAll(activityMap);
        },threadPoolExecutor);

        //线程3：更新商品热度
        CompletableFuture<Void> hotCompletableFuture = CompletableFuture.runAsync(()->{
            //远程调用更新热度
            skuFeignClient.incrHotScore(skuId);

        },threadPoolExecutor);

        //任务组合，allOf()等3个线程中的任务都完成后执行
        CompletableFuture.allOf(
                skuInfoVoCompletableFuture,activityCompletableFuture,hotCompletableFuture
        ).join();

        return result;
    }
}
