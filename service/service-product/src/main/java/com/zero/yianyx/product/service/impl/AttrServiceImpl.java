package com.zero.yianyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.product.Attr;
import com.zero.yianyx.product.mapper.AttrMapper;
import com.zero.yianyx.product.service.AttrService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Service
public class AttrServiceImpl extends ServiceImpl<AttrMapper, Attr> implements AttrService {
    @Override
    public List<Attr> findByAttrGroupId(Long attrGroupId) {
        LambdaQueryWrapper<Attr> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attr::getAttrGroupId,attrGroupId);
        return baseMapper.selectList(wrapper);
    }
}
