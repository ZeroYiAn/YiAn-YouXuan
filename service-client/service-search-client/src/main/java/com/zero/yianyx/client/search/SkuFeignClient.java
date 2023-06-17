package com.zero.yianyx.client.search;

import com.zero.yianyx.model.search.SkuEs;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */
@FeignClient("service-search")
public interface SkuFeignClient {
    @GetMapping("/api/search/sku/inner/findHotSkuList")
    public List<SkuEs> findHotSkuList();

    /**
     * 更新商品热度
     * @param skuId
     * @return
     */
    @GetMapping("/api/search/sku/inner/incrHotScore/{skuId}")
    public Boolean incrHotScore(@PathVariable("skuId")Long skuId);
}
