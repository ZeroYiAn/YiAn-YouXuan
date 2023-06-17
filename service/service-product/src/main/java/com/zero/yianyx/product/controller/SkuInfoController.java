package com.zero.yianyx.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.product.SkuInfo;
import com.zero.yianyx.product.service.SkuInfoService;
import com.zero.yianyx.vo.product.SkuInfoQueryVo;
import com.zero.yianyx.vo.product.SkuInfoVo;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 商品SKU处理器
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */

@RestController
@RequestMapping(value="/admin/product/skuInfo")
public class SkuInfoController {

    @Resource
    private SkuInfoService skuInfoService;

    @GetMapping("{page}/{limit}")
    public Result<IPage<SkuInfo>> index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,
            @ApiParam(name = "skuInfoQueryVo", value = "查询对象", required = false)
                    SkuInfoQueryVo skuInfoQueryVo) {
        Page<SkuInfo> pageParam = new Page<>(page, limit);
        IPage<SkuInfo> pageModel = skuInfoService.selectPage(pageParam, skuInfoQueryVo);
        return Result.ok(pageModel);
    }

    @PostMapping("save")
    public Result save(@RequestBody SkuInfoVo skuInfoVo) {
        skuInfoService.saveSkuInfo(skuInfoVo);
        return Result.ok();
    }


    @GetMapping("get/{id}")
    public Result<SkuInfoVo> get(@PathVariable Long id) {
        SkuInfoVo skuInfoVo = skuInfoService.getSkuInfoVo(id);
        return Result.ok(skuInfoVo);
    }


    @PutMapping("update")
    public Result updateById(@RequestBody SkuInfoVo skuInfoVo) {
        skuInfoService.updateSkuInfo(skuInfoVo);
        return Result.ok();
    }


    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {

        //把数据库中的sku删除的同时还有把ES中存储的sku信息下架(如果上架了的话)
        skuInfoService.removeFromES(id);
        return Result.ok();
    }

    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        skuInfoService.removeByIds(idList);
        return Result.ok();
    }

    @GetMapping("check/{skuId}/{status}")
    public Result check(@PathVariable("skuId") Long skuId, @PathVariable("status") Integer status) {
        skuInfoService.check(skuId, status);
        return Result.ok();
    }

    @GetMapping("publish/{skuId}/{status}")
    public Result publish(@PathVariable("skuId") Long skuId,
                          @PathVariable("status") Integer status) {
        skuInfoService.publish(skuId, status);
        return Result.ok();
    }

    @GetMapping("isNewPerson/{skuId}/{status}")
    public Result isNewPerson(@PathVariable("skuId") Long skuId,
                              @PathVariable("status") Integer status) {
        skuInfoService.isNewUser(skuId, status);
        return Result.ok();
    }




}
