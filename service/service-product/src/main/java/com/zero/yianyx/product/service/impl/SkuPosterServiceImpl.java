package com.zero.yianyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.product.SkuPoster;
import com.zero.yianyx.product.mapper.SkuPosterMapper;
import com.zero.yianyx.product.service.SkuPosterService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Service
public class SkuPosterServiceImpl extends ServiceImpl<SkuPosterMapper, SkuPoster> implements SkuPosterService {
    @Override
    public List<SkuPoster> findBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuPoster> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuPoster::getSkuId,skuId);
        return baseMapper.selectList(queryWrapper);
    }
}
