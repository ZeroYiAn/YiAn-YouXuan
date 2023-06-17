package com.zero.yianyx.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.product.AttrGroup;
import com.zero.yianyx.product.service.AttrGroupService;
import com.zero.yianyx.vo.product.AttrGroupQueryVo;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 平台属性分组处理器
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */

@RestController
@RequestMapping(value="/admin/product/attrGroup")
public class AttrGroupController {

    @Resource
    private AttrGroupService attrGroupService;


    @GetMapping("{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(name = "attrGroupQueryVo", value = "查询对象", required = false)
                    AttrGroupQueryVo attrGroupQueryVo) {
        Page<AttrGroup> pageParam = new Page<>(page, limit);
        IPage<AttrGroup> pageModel = attrGroupService.selectPage(pageParam, attrGroupQueryVo);
        return Result.ok(pageModel);
    }


    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        AttrGroup attrGroup = attrGroupService.getById(id);
        return Result.ok(attrGroup);
    }

    @PostMapping("save")
    public Result save(@RequestBody AttrGroup attrGroup) {
        attrGroupService.save(attrGroup);
        return Result.ok(null);
    }


    @PutMapping("update")
    public Result updateById(@RequestBody AttrGroup attrGroup) {
        attrGroupService.updateById(attrGroup);
        return Result.ok(null);
    }


    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        attrGroupService.removeById(id);
        return Result.ok(null);
    }

    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        attrGroupService.removeByIds(idList);
        return Result.ok(null);
    }


    @GetMapping("findAllList")
    public Result findAllList() {
        return Result.ok(attrGroupService.findAllList());
    }
}