package com.zero.yianyx.acl.controller;

import com.zero.yianyx.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 登录处理器
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */
@Api(tags = "登录接口")
@RestController
@RequestMapping("/admin/acl/index")
public class IndexController {

    /**
     * 1、请求登陆的login
     */
    @ApiOperation("用户登录")
    @PostMapping("login")
    public Result login() {
        Map<String,Object> map = new HashMap<>();
        map.put("token","admin-token");
        return Result.ok(map);
    }


    /**
     * 2 获取用户信息
     */
    @ApiOperation("获取信息")
    @GetMapping("info")
    public Result info(){
        Map<String,Object> map = new HashMap<>();
        map.put("name","admin");
        map.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        return Result.ok(map);
    }

    /**
     * 3.退出登录
     */
    @ApiOperation("退出登录")
    @PostMapping("logout")
    public Result logout(){
        return Result.ok(null);
    }




}
