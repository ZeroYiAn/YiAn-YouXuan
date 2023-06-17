package com.zero.yianyx.home.controller;

import com.zero.yianyx.client.product.ProductFeignClient;
import com.zero.yianyx.common.result.Result;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/13
 */
@Api(tags = "商品分类")
@RestController
@RequestMapping("api/home")
public class CategoryApiController {
    @Resource
    private ProductFeignClient productFeignClient;

    @GetMapping("category")
    public Result index() {
        return Result.ok(productFeignClient.findAllCategoryList());
    }

}
