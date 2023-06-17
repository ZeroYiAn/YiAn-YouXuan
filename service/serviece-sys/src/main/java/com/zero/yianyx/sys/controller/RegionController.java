package com.zero.yianyx.sys.controller;

import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.sys.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */
@Api(tags = "区域管理")
@RestController
@RequestMapping("/admin/sys/region")
public class RegionController {
    @Resource
    private RegionService regionService;

    @ApiOperation("根据关键字获取区域列表")
    @GetMapping("findRegionByKeyWord/{keyword}")
    public Result getRegionByKeyword(@PathVariable("keyword")String keyword){
        return Result.ok(regionService.findRegionByKeyword(keyword));
    }
}
