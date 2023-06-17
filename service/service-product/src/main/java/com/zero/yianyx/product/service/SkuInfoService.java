package com.zero.yianyx.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.product.SkuInfo;
import com.zero.yianyx.vo.product.SkuInfoQueryVo;
import com.zero.yianyx.vo.product.SkuInfoVo;
import com.zero.yianyx.vo.product.SkuStockLockVo;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
public interface SkuInfoService extends IService<SkuInfo> {
    /**
     * 获取sku分页列表
     * @param pageParam
     * @param skuInfoQueryVo
     * @return
     */
    IPage<SkuInfo> selectPage(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo);

    /**
     * 添加sku信息
     * @param skuInfoVo
     */
    void saveSkuInfo(SkuInfoVo skuInfoVo);

    /**
     * 获取Sku信息视图
     * @param id
     * @return
     */
    SkuInfoVo getSkuInfoVo(Long id);

    /**
     * 修改Sku信息
     * @param skuInfoVo
     */
    void updateSkuInfo(SkuInfoVo skuInfoVo);

    /**
     * 商品审核
     * @param skuId
     * @param status
     */
    void check(Long skuId, Integer status);

    /**
     * 商品上架
     * @param skuId
     * @param status
     */
    void publish(Long skuId, Integer status);

    /**
     * 新人专享
     * @param skuId
     * @param status
     */
    void isNewUser(Long skuId, Integer status);

    /**
     * 下架删除ES中的sku信息
     * @param id
     */
    void removeFromES(Long id);

    /**
     * 批量获取sku信息
     * @param skuIdList
     * @return
     */
    List<SkuInfo> findSkuInfoList(List<Long> skuIdList);

    /**
     * 根据关键词获取sku信息
     * @param keyword
     * @return
     */
    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    /**
     * 获取新人专享商品
     * @return
     */
    List<SkuInfo> findNewPersonList();

    /**
     * 验证和锁定库存
     * @param skuStockLockVoList
     * @param orderNo
     * @return
     */
    Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList, String orderNo);

    /**
     * 扣减库存
     * @param orderNo
     */
    void minusStock(String orderNo);
}
