package com.zero.yianyx.product.controller;

import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.product.Attr;
import com.zero.yianyx.product.service.AttrService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 平台属性处理器
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */

@RestController
@RequestMapping(value="/admin/product/attr")
public class AttrController {

    @Resource
    private AttrService attrService;

    @GetMapping("{attrGroupId}")
    public Result index(
            @ApiParam(name = "attrGroupId", value = "分组id", required = true)
            @PathVariable Long attrGroupId) {
        return Result.ok(attrService.findByAttrGroupId(attrGroupId));
    }

    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        Attr attr = attrService.getById(id);
        return Result.ok(attr);
    }


    @PostMapping("save")
    public Result save(@RequestBody Attr attr) {
        attrService.save(attr);
        return Result.ok(null);
    }

    @ApiOperation(value = "修改属性")
    @PutMapping("update")
    public Result updateById(@RequestBody Attr attr) {
        attrService.updateById(attr);
        return Result.ok(null);
    }


    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        attrService.removeById(id);
        return Result.ok(null);
    }

    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        attrService.removeByIds(idList);
        return Result.ok(null);
    }
}
