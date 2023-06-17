package com.zero.yianyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.product.SkuAttrValue;
import com.zero.yianyx.product.mapper.SkuAttrValueMapper;
import com.zero.yianyx.product.service.SkuAttrValueService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Service
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValue> implements SkuAttrValueService {
    @Override
    public List<SkuAttrValue> findBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuAttrValue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuAttrValue::getSkuId,skuId);
        return baseMapper.selectList(queryWrapper);
    }
}
