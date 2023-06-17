package com.zero.yianyx.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.product.Category;
import com.zero.yianyx.product.service.CategoryService;
import com.zero.yianyx.vo.product.CategoryVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 商品分类处理器
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */

@RestController
@RequestMapping("/admin/product/category")
public class CategoryController {
    @Resource
    private CategoryService categoryService;


    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit,
                        CategoryVo categoryVo){
        Page<Category>pageParam = new Page<>(page,limit);
        IPage pageModel= categoryService.selectPage(pageParam,categoryVo);
        return Result.ok(pageModel);
    }


    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        return Result.ok(categoryService.getById(id));
    }



    @PostMapping("save")
    private Result save(@RequestBody Category category){
        categoryService.save(category);
        return Result.ok(null);
    }


    @PutMapping("update")
    public Result updateById(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.ok(null);
    }


    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.ok(null);
    }


    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        categoryService.removeByIds(idList);
        return Result.ok(null);
    }

    @GetMapping("findAllList")
    public Result findAllList() {
        return Result.ok(categoryService.findAllList());
    }


}
