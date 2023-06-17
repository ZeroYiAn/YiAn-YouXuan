package com.zero.yianyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.product.AttrGroup;
import com.zero.yianyx.product.mapper.AttrGroupMapper;
import com.zero.yianyx.product.service.AttrGroupService;
import com.zero.yianyx.vo.product.AttrGroupQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Service
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroup> implements AttrGroupService {

    @Override
    public IPage<AttrGroup> selectPage(Page<AttrGroup> pageParam, AttrGroupQueryVo attrGroupQueryVo) {
        String name = attrGroupQueryVo.getName();
        LambdaQueryWrapper<AttrGroup> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            wrapper.like(AttrGroup::getName,name);
        }
        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public List<AttrGroup> findAllList() {
        return this.list();
    }
}
