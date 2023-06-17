package com.zero.yianyx.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.product.SkuImage;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
public interface SkuImageService extends IService<SkuImage> {
    /**
     * 根据id查询sku图片列表
     * @param skuId
     * @return
     */
    List<SkuImage> findBySkuId(Long skuId);
}
