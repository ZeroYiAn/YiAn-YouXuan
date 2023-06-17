package com.zero.yianyx.product.api;

import com.zero.yianyx.model.product.Category;
import com.zero.yianyx.model.product.SkuInfo;
import com.zero.yianyx.product.service.CategoryService;
import com.zero.yianyx.product.service.SkuInfoService;
import com.zero.yianyx.vo.product.SkuInfoVo;
import com.zero.yianyx.vo.product.SkuStockLockVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: rpc远程调用api
 * @author: ZeroYiAn
 * @time: 2023/6/10
 */
@RestController
@RequestMapping("/api/product")
public class ProductInnerController {
    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private CategoryService categoryService;


    @GetMapping("inner/getCategory/{categoryId}")
    public Category getCategory(@PathVariable Long categoryId) {
        return categoryService.getById(categoryId);
    }

    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId) {
        return skuInfoService.getById(skuId);
    }

    @PostMapping("inner/findSkuInfoList")
    public List<SkuInfo> findSkuInfoList(@RequestBody List<Long> skuIdList) {
        return skuInfoService.findSkuInfoList(skuIdList);
    }


    @GetMapping("inner/findSkuInfoByKeyword/{keyword}")
    public List<SkuInfo> findSkuInfoByKeyword(@PathVariable("keyword") String keyword) {
        return skuInfoService.findSkuInfoByKeyword(keyword);
    }

    @PostMapping("inner/findCategoryList")
    public List<Category> findCategoryList(@RequestBody List<Long> categoryIdList) {
        return categoryService.listByIds(categoryIdList);
    }


    @PostMapping("inner/findAllCategoryList")
    public List<Category> findAllCategoryList() {
        return categoryService.list();
    }


    @GetMapping("inner/findNewPersonSkuInfoList")
    public List<SkuInfo> findNewPersonSkuInfoList() {
        return skuInfoService.findNewPersonList();
    }


    @GetMapping("inner/getSkuInfoVo/{skuId}")
    public SkuInfoVo getSkuInfoVo(@PathVariable Long skuId){
        SkuInfoVo skuInfoVo = skuInfoService.getSkuInfoVo(skuId);
        return skuInfoVo;
    }


    /**
     * 验证和锁定库存
     * @param skuStockLockVoList
     * @param orderNo
     * @return
     */
    @PostMapping("inner/checkAndLock/{orderNo}")
    public Boolean checkAndLock(@RequestBody List<SkuStockLockVo> skuStockLockVoList, @PathVariable String orderNo) {
        return skuInfoService.checkAndLock(skuStockLockVoList, orderNo);
    }


}
