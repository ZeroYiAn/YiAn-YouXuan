package com.zero.yianyx.home.controller;

import com.zero.yianyx.common.auth.AuthContextHolder;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.home.service.HomeService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */

@Api(tags = "首页接口")
@RestController
@RequestMapping("api/home")
public class HomeApiController {

    @Resource
    private HomeService homeService;

    //@ApiOperation(value = "首页数据显示接口")
    @GetMapping("index")
    public Result index(HttpServletRequest request) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        return Result.ok(homeService.home(userId));
    }
}