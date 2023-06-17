package com.zero.yianyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.product.Category;
import com.zero.yianyx.product.mapper.CategoryMapper;
import com.zero.yianyx.product.service.CategoryService;
import com.zero.yianyx.vo.product.CategoryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Override
    public IPage selectPage(Page<Category> pageParam, CategoryVo categoryVo) {
        String name = categoryVo.getName();
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(name)){
            queryWrapper.like(Category::getName,name);
        }
        IPage<Category> categoryPage = baseMapper.selectPage(pageParam, queryWrapper);
        return categoryPage;
    }

    @Override
    public List<Category> findAllList() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        return this.list(queryWrapper);
    }

}
