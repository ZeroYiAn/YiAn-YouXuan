package com.zero.yianyx.sys.controller;

import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.sys.service.WareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */
@Api(tags = "仓库管理")
@RestController
@RequestMapping("/admin/sys/ware")
public class WareController {
    @Resource
    private WareService wareService;

    @ApiOperation(value = "获取全部仓库")
    @GetMapping("findAllList")
    public Result findAllList() {
        return Result.ok(wareService.list());
    }
}
