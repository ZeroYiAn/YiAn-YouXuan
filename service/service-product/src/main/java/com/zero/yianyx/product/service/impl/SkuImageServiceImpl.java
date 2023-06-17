package com.zero.yianyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.product.SkuImage;
import com.zero.yianyx.product.mapper.SkuImageMapper;
import com.zero.yianyx.product.service.SkuImageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Service
public class SkuImageServiceImpl extends ServiceImpl<SkuImageMapper, SkuImage> implements SkuImageService {
    @Override
    public List<SkuImage> findBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuImage::getSkuId,skuId);
        return baseMapper.selectList(queryWrapper);
    }
}
