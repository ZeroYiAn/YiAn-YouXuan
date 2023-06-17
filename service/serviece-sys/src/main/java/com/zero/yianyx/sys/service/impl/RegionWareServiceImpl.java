package com.zero.yianyx.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.common.exception.YianyxException;
import com.zero.yianyx.common.result.ResultCodeEnum;
import com.zero.yianyx.model.sys.RegionWare;
import com.zero.yianyx.sys.mapper.RegionWareMapper;
import com.zero.yianyx.sys.service.RegionWareService;
import com.zero.yianyx.vo.sys.RegionWareQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */
@Service
public class RegionWareServiceImpl extends ServiceImpl<RegionWareMapper, RegionWare> implements RegionWareService {
    @Override
    public IPage<RegionWare> selectPage(Page<RegionWare> pageParam, RegionWareQueryVo regionWareQueryVo) {
        String keyword = regionWareQueryVo.getKeyword();
        LambdaQueryWrapper<RegionWare> wrapper = new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)){
            //根据区域名称 或者 仓库名称 进行查询
            wrapper.like(RegionWare::getRegionName,keyword)
                    .or().like(RegionWare::getWareName,keyword);
        }
        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public void saveRegionWare(RegionWare regionWare) {
        LambdaQueryWrapper<RegionWare> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RegionWare::getRegionId,regionWare.getRegionId());
        Integer count = baseMapper.selectCount(queryWrapper);
        if(count>0){
            throw new YianyxException(ResultCodeEnum.REGION_OPEN);
        }
        baseMapper.insert(regionWare);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        RegionWare regionWare = baseMapper.selectById(id);
        regionWare.setStatus(status);
        baseMapper.updateById(regionWare);
    }
}
