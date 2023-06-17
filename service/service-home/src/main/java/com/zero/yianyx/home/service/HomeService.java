package com.zero.yianyx.home.service;

import java.util.Map;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */
public interface HomeService {
    /**首页数据*/
    Map<String, Object> home(Long userId);
}
