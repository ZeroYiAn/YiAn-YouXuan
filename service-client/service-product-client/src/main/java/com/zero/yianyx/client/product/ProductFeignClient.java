package com.zero.yianyx.client.product;

import com.zero.yianyx.model.product.Category;
import com.zero.yianyx.model.product.SkuInfo;
import com.zero.yianyx.vo.product.SkuInfoVo;
import com.zero.yianyx.vo.product.SkuStockLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description: 商品信息远程调用服务接口定义
 * @author: ZeroYiAn
 * @time: 2023/6/10
 */
@FeignClient(value = "service-product")
public interface ProductFeignClient {

    /**
     * 验证和锁定库存
     * @param skuStockLockVoList
     * @param orderNo
     * @return
     */
    @PostMapping("/api/product/inner/checkAndLock/{orderNo}")
    public Boolean checkAndLock(@RequestBody List<SkuStockLockVo> skuStockLockVoList, @PathVariable String orderNo);

    @GetMapping("/api/product/inner/findNewPersonSkuInfoList")
    public List<SkuInfo> findNewPersonSkuInfoList();

    @PostMapping("/api/product/inner/findAllCategoryList")
    public List<Category> findAllCategoryList();

    @GetMapping("/api/product/inner/getCategory/{categoryId}")
    public Category getCategory(@PathVariable("categoryId") Long categoryId);

    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

    /**
     * 批量获取sku信息
     * @param skuIdList
     * @return
     */
    @PostMapping("/api/product/inner/findSkuInfoList")
    List<SkuInfo> findSkuInfoList(@RequestBody List<Long> skuIdList);

    /**
     * 根据关键字获取sku列表，活动使用
     * @param keyword
     * @return
     */
    @GetMapping("/api/product/inner/findSkuInfoByKeyword/{keyword}")
    List<SkuInfo> findSkuInfoByKeyword(@PathVariable("keyword") String keyword);

    /**
     * 批量获取分类信息
     * @param categoryIdList
     * @return
     */
    @PostMapping("/api/product/inner/findCategoryList")
    List<Category> findCategoryList(@RequestBody List<Long> categoryIdList);

    /**
     * 根据skuId获取skuInfoVo对象
     * @param skuId
     * @return
     */
    @GetMapping("/api/product/inner/getSkuInfoVo/{skuId}")
    public SkuInfoVo getSkuInfoVo(@PathVariable Long skuId);
}
