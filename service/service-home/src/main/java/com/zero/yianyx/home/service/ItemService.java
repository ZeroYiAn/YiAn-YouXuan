package com.zero.yianyx.home.service;

import java.util.Map;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/13
 */
public interface ItemService {
    /**
     * 获取商品详情信息
     * @param id
     * @param userId
     * @return
     */
    Map<String,Object> item(Long id, Long userId);
}
