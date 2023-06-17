package com.zero.yianyx.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.product.SkuAttrValue;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
public interface SkuAttrValueService extends IService<SkuAttrValue> {
    List<SkuAttrValue> findBySkuId(Long skuId);
}
