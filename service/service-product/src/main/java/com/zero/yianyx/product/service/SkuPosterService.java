package com.zero.yianyx.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.product.SkuPoster;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
public interface SkuPosterService extends IService<SkuPoster> {
    List<SkuPoster> findBySkuId(Long skuId);
}
