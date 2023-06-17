package com.zero.yianyx.home.controller;

import com.zero.yianyx.common.auth.AuthContextHolder;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.home.service.ItemService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/13
 */

@Api(tags = "商品详情")
@RestController
@RequestMapping("api/home")
public class ItemApiController {

    @Resource
    private ItemService itemService;

   // @ApiOperation(value = "获取商品详细信息")
    @GetMapping("item/{id}")
    public Result index(@PathVariable Long id) {
        Long userId = AuthContextHolder.getUserId();
        return Result.ok(itemService.item(id, userId));
    }
}