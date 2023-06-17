package com.zero.yianyx.search.service;

import com.zero.yianyx.model.search.SkuEs;
import com.zero.yianyx.vo.search.SkuEsQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/10
 */
public interface SkuService {
    /**
     * 上架商品
     * @param skuId
     */
    void upperSku(Long skuId);

    /**
     * 下架商品
     * @param skuId
     */
    void lowerSku(Long skuId);

    /**
     * 获取热销商品
     * @return
     */
    List<SkuEs> findHotSkuList();

    /**
     * 查询各类所拥有的商品
     * @param pageable
     * @param skuEsQueryVo
     * @return
     */
    Page<SkuEs> search(Pageable pageable, SkuEsQueryVo skuEsQueryVo);

    /**
     * 更新商品热度
     * @param skuId
     */
    void incrHotScore(Long skuId);
}
