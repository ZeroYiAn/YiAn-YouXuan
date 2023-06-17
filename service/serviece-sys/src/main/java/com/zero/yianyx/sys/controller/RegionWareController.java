package com.zero.yianyx.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.sys.RegionWare;
import com.zero.yianyx.sys.service.RegionWareService;
import com.zero.yianyx.vo.sys.RegionWareQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */
@Api(tags = "RegionWare管理")
@RestController
@RequestMapping("/admin/sys/regionWare")
public class RegionWareController {
    @Resource
    private RegionWareService regionWareService;

    @ApiOperation("开通区域列表")
    @GetMapping("{page}/{limit}")
    public Result index(
            @PathVariable Long page,
            @PathVariable Long limit,
            RegionWareQueryVo regionWareQueryVo){
        Page<RegionWare>pageParam = new Page<>(page,limit);
        IPage<RegionWare>pageModel = regionWareService.selectPage(pageParam,regionWareQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("添加开通区域")
    @PostMapping("save")
    public Result save(@RequestBody RegionWare regionWare){
        regionWareService.saveRegionWare(regionWare);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除开通区域")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        regionWareService.removeById(id);
        return Result.ok(null);
    }


    @ApiOperation(value = "取消开通区域")
    @PostMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id,@PathVariable Integer status) {
        regionWareService.updateStatus(id, status);
        return Result.ok(null);
    }

}
