package com.zero.yianyx.cart.service;

import com.zero.yianyx.model.order.CartInfo;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/14
 */
public interface CartInfoService {
    /**
     * 添加商品到购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void addToCart(Long skuId, Long userId, Integer skuNum);

    void deleteCart(Long skuId, Long userId);

    void deleteAllCart(Long userId);

    void batchDeleteCart(List<Long> skuIdList, Long userId);

    /**
     * 通过用户Id查询购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(Long userId);

    void checkCart(Long userId, Integer isChecked, Long skuId);

    void checkAllCart(Long userId, Integer isChecked);

    void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked);

    /**
     * 获取当前用户选中的购物项目
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(Long userId);

    /**
     * 根据用户id 去删除选中的购物车记录
     * @param userId
     */
    void deleteCartChecked(Long userId);
}
