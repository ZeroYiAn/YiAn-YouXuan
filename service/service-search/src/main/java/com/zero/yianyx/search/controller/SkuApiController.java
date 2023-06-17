package com.zero.yianyx.search.controller;

import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.search.SkuEs;
import com.zero.yianyx.search.service.SkuService;
import com.zero.yianyx.vo.search.SkuEsQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/10
 */
@RestController
@RequestMapping("api/search/sku")
public class SkuApiController {
    @Resource
    private SkuService skuService;

    @ApiOperation(value = "上架商品")
    @GetMapping("inner/upperSku/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId) {
        skuService.upperSku(skuId);
        return Result.ok();
    }

    @ApiOperation(value = "下架商品")
    @GetMapping("inner/lowerSku/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId) {
        skuService.lowerSku(skuId);
        return Result.ok();
    }

    @ApiOperation("获取爆款商品")
    @GetMapping("inner/findHotSkuList")
    public List<SkuEs> findHotSkuList(){
        return skuService.findHotSkuList();
    }

    @GetMapping("{page}/{limit}")
    public Result listSku(
            @PathVariable Integer page,
            @PathVariable Integer limit,
            SkuEsQueryVo skuEsQueryVo) {
        //创建
        Pageable pageable = PageRequest.of(page-1, limit);
        Page<SkuEs> pageModel = skuService.search(pageable,skuEsQueryVo);
        return Result.ok(pageModel);
    }

    @GetMapping("inner/incrHotScore/{skuId}")
    public Boolean incrHotScore(@PathVariable("skuId")Long skuId){
        skuService.incrHotScore(skuId);
        return true;
    }

}
