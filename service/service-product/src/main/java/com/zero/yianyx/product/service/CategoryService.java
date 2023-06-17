package com.zero.yianyx.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.product.Category;
import com.zero.yianyx.vo.product.CategoryVo;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
public interface CategoryService extends IService<Category> {
    /**
     * 商品分类分页列表，按商品名称查找
     * @param pageParam
     * @param categoryVo
     * @return
     */
    IPage selectPage(Page<Category> pageParam, CategoryVo categoryVo);

    /**
     * 获取所有商品分类
     * @return
     */
    List<Category> findAllList();

}
