package com.zero.yianyx.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.sys.Region;
import com.zero.yianyx.sys.mapper.RegionMapper;
import com.zero.yianyx.sys.service.RegionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */
@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements RegionService {
    @Override
    public List<Region> findRegionByKeyword(String keyword) {
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Region::getName,keyword);
        return baseMapper.selectList(wrapper);
    }
}
