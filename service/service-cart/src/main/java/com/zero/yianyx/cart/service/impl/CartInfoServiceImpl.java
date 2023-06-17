package com.zero.yianyx.cart.service.impl;

import com.zero.yianyx.cart.service.CartInfoService;
import com.zero.yianyx.client.product.ProductFeignClient;
import com.zero.yianyx.common.constant.RedisConst;
import com.zero.yianyx.common.exception.YianyxException;
import com.zero.yianyx.common.result.ResultCodeEnum;
import com.zero.yianyx.enums.SkuType;
import com.zero.yianyx.model.order.CartInfo;
import com.zero.yianyx.model.product.SkuInfo;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description: 利用Redis中的hash数据类型存储购物车数据信息
 * 存储数据为： 【Key】：userId   【Filed】SkuId    【Value】CartInfo对象
 *
 * @author: ZeroYiAn
 * @time: 2023/6/14
 */
@Service
public class CartInfoServiceImpl implements CartInfoService {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ProductFeignClient productFeignClient;

    private String getCartKey(Long userId) {
        // 定义Redis的hash集合中的key:   user:userId:cart
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    @Override
    public List<CartInfo> getCartList(Long userId) {
        // 判断userId
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (StringUtils.isEmpty(userId)){
            return cartInfoList;
        }
        // 定义key user:userId:cart
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        // 获取数据
        cartInfoList = hashOperations.values();
        if (!CollectionUtils.isEmpty(cartInfoList)) {
            // 购物车列表显示有顺序：按照商品的更新时间 降序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    // str1 = ab str2 = ac;
                    return o2.getCreateTime().compareTo(o1.getCreateTime());
                }
            });
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(Long userId, Integer isChecked, Long skuId) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = boundHashOps.get(skuId.toString());
        //根据filed(skuId)获取value(cartInfo)
        if(null != cartInfo) {
            cartInfo.setIsChecked(isChecked);
            boundHashOps.put(skuId.toString(), cartInfo);
            this.setCartKeyExpire(cartKey);
        }
    }

    @Override
    public void checkAllCart(Long userId, Integer isChecked) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(cartKey);
        boundHashOps.values().forEach(cartInfo -> {
            cartInfo.setIsChecked(isChecked);
            boundHashOps.put(cartInfo.getSkuId().toString(), cartInfo);
        });
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        //根据filed(skuId)获取value(cartInfo)
        skuIdList.forEach(skuId -> {
            CartInfo cartInfo = hashOperations.get(skuId.toString());
            cartInfo.setIsChecked(isChecked);
            hashOperations.put(cartInfo.getSkuId().toString(), cartInfo);
        });
    }

    @Override
    public void addToCart(Long skuId, Long userId, Integer skuNum) {
        // 定义key user:userId:cart
        String cartKey = getCartKey(userId);
        //获取缓存对象

        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = null;
        // 如果购物车中有该商品并且没有失效，则更新个数并选中 && cartInfo.getStatus().intValue() == 1
        if (Boolean.TRUE.equals(hashOperations.hasKey(skuId.toString()))) {
            cartInfo = hashOperations.get(skuId.toString());
            assert cartInfo != null;
            //当前商品数量
            int currentSkuNum = cartInfo.getSkuNum() + skuNum;
            if(currentSkuNum < 1) {
                return;
            }

            //获取用户当前已经购买的sku个数，sku限量，每天不能超买
            //  添加购物车数量
            cartInfo.setSkuNum(currentSkuNum);
            //当天购买数量
            cartInfo.setCurrentBuyNum(currentSkuNum);
            //大于限购个数，不能更新个数
            if(currentSkuNum >= cartInfo.getPerLimit()) {
                throw new YianyxException(ResultCodeEnum.SKU_LIMIT_ERROR);
            }
            cartInfo.setIsChecked(1);
            cartInfo.setUpdateTime(new Date());
        } else {//购物车中没有该商品
            //第一次添加只能添加一个
            skuNum = 1;
            // 当购物车中没有该商品的时候，则直接添加到购物车！insert
            cartInfo = new CartInfo();
            // 购物车数据是从商品详情得到 {skuInfo}
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            if(null == skuInfo) {
                throw new YianyxException(ResultCodeEnum.DATA_ERROR);
            }
            cartInfo.setSkuId(skuId);
            cartInfo.setCategoryId(skuInfo.getCategoryId());
            cartInfo.setSkuType(skuInfo.getSkuType());
            cartInfo.setIsNewPerson(skuInfo.getIsNewPerson());
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCurrentBuyNum(skuNum);
            cartInfo.setSkuType(SkuType.COMMON.getCode());
            cartInfo.setPerLimit(skuInfo.getPerLimit());
            cartInfo.setImgUrl(skuInfo.getImgUrl());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setWareId(skuInfo.getWareId());
            cartInfo.setIsChecked(1);
            cartInfo.setStatus(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }

        // 更新缓存
        hashOperations.put(skuId.toString(), cartInfo);
        // 设置过期时间
        this.setCartKeyExpire(cartKey);
    }


    private void setCartKeyExpire(String cartKey) {
        //  设置key 的过期时间: 7天
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public void deleteCart(Long skuId, Long userId) {
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(this.getCartKey(userId));
        //  判断购物车中是否有该商品！
        if (Boolean.TRUE.equals(boundHashOps.hasKey(skuId.toString()))){
            boundHashOps.delete(skuId.toString());
        }
    }

    @Override
    public void deleteAllCart(Long userId) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        hashOperations.values().forEach(cartInfo -> {
            hashOperations.delete(cartInfo.getSkuId().toString());
        });
    }

    @Override
    public void batchDeleteCart(List<Long> skuIdList, Long userId) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            hashOperations.delete(skuId.toString());
        });
    }

    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(this.getCartKey(userId));
        List<CartInfo> cartInfoCheckList = boundHashOps.values().stream().filter((cartInfo) -> {
            return cartInfo.getIsChecked() == 1;
        }).collect(Collectors.toList());
        return cartInfoCheckList;
    }

    @Override
    public void deleteCartChecked(Long userId) {
        //查询用户选中的购物项记录
        List<CartInfo> cartInfoList = this.getCartCheckedList(userId);
        //获得购物项id
        List<Long> skuIdList = cartInfoList.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
        //构建redis的key值  hash类型：key filed value
        String cartKey = getCartKey(userId);
        //根据获取缓存对象（filed，value）
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        //根据filed(skuId)删除redis中对应数据
        skuIdList.forEach(skuId -> {
            hashOperations.delete(skuId.toString());
        });
    }
}
